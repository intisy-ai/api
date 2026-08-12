import { expect, it } from "vitest";
import { MANIFEST_SCHEMA, SCHEMA_ID } from "./manifest-schema.js";
import { validateAgainstSchema } from "./schema.js";
import type { JsonSchema } from "./schema.js";
import { setDiagnosticSink } from "./strict.js";

const PERSON: JsonSchema = {
  type: "object",
  required: ["name"],
  properties: {
    name: { type: "string", pattern: "^[a-z]+$", fix: "use lowercase letters" },
    age: { type: "integer", minimum: 0 },
    tags: { type: "array", items: { type: "string" } },
    labels: { type: "object", additionalProperties: { type: "string" } },
  },
};

it("accepts a valid value", () => {
  expect(validateAgainstSchema({ name: "ada", age: 36, tags: ["x"], labels: { a: "b" } }, PERSON)).toEqual([]);
});

it("reports a missing required field with the schema's own fix", () => {
  expect(validateAgainstSchema({}, PERSON)).toEqual([
    { path: "name", message: 'required field "name" is missing', fix: "use lowercase letters" },
  ]);
});

it("reports a type mismatch with the value it actually got", () => {
  expect(validateAgainstSchema({ name: "ada", age: "old" }, PERSON)).toEqual([
    { path: "age", message: "expected integer, got string", fix: "set age to a integer" },
  ]);
});

it("reports a pattern miss, a minimum miss, and an item type miss with indexed paths", () => {
  const issues = validateAgainstSchema({ name: "Ada", age: -1, tags: ["ok", 7] }, PERSON);
  expect(issues).toEqual([
    { path: "name", message: '"Ada" does not match ^[a-z]+$', fix: "use lowercase letters" },
    { path: "age", message: "expected a value >= 0, got -1", fix: "set age to a value >= 0" },
    { path: "tags[1]", message: "expected string, got number", fix: "set tags[1] to a string" },
  ]);
});

it("ignores an unknown property, an explicit undefined, and validates a record's values", () => {
  expect(validateAgainstSchema({ name: "ada", futureField: { anything: true } }, PERSON)).toEqual([]);
  expect(validateAgainstSchema({ name: "ada", age: undefined }, PERSON)).toEqual([]);
  expect(validateAgainstSchema({ name: "ada", labels: { a: 1 } }, PERSON)).toEqual([
    { path: "labels.a", message: "expected string, got number", fix: "set labels.a to a string" },
  ]);
});

it("reports an ignored unknown field when a source is named, and stays silent without one", () => {
  const seen: string[] = [];
  setDiagnosticSink((message) => seen.push(message));
  expect(validateAgainstSchema({ name: "ada", futureField: 1 }, PERSON, "(root)", "wakatime-sync")).toEqual([]);
  expect(validateAgainstSchema({ name: "ada", futureField: 1 }, PERSON)).toEqual([]);
  setDiagnosticSink(null);
  expect(seen).toEqual(['ignored unknown field "futureField" from wakatime-sync']);
});

it("names the root when the value itself is the wrong shape", () => {
  expect(validateAgainstSchema([], PERSON)).toEqual([
    { path: "(root)", message: "expected object, got array", fix: "set (root) to a object" },
  ]);
});

it("ships a manifest schema that accepts the spec's example manifest", () => {
  const example = {
    id: "antigravity-auth",
    api: 1,
    entry: "dist/index.js",
    displayName: "Antigravity",
    icon: "icon.svg",
    capabilities: ["provider", "screens"],
    services: { provides: ["antigravity-auth:accounts"], consumes: ["accounts"] },
    permissions: ["network", "accounts:read"],
    lifecycle: { install: true, repair: true },
    publish: { scopedOnly: true },
    repo: { role: "Antigravity account provider", category: "ai-provider", domains: ["gemini"], tech: "typescript" },
    presentation: { providers: { antigravity: "icon.svg", "gemini-cli": "icons/gemini-cli.svg" } },
  };
  expect(validateAgainstSchema(example, MANIFEST_SCHEMA)).toEqual([]);
  expect(MANIFEST_SCHEMA.$id).toBe(SCHEMA_ID);
});

it("requires id and api, and rejects an id that is not a lowercase slug", () => {
  expect(validateAgainstSchema({}, MANIFEST_SCHEMA).map((i) => i.path)).toEqual(["id", "api"]);
  expect(validateAgainstSchema({ id: "Config Ledger", api: 1 }, MANIFEST_SCHEMA)[0].message).toBe(
    '"Config Ledger" does not match ^[a-z0-9]+(-[a-z0-9]+)*$',
  );
});
