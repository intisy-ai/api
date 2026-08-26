import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";
import { manifestSchema } from "../../generated/engine.js";

// The manifest is described TWICE: once as the generated `PluginManifest` an author writes against,
// and once as the schema the validator enforces. Adding a field to one and not the other type-checks
// and validates green while a manifest using it is rejected at run time, which is exactly what
// happened when `commands` and `config` were added to the contract alone.
const DTS = readFileSync(fileURLToPath(new URL("../../generated/api.d.ts", import.meta.url)), "utf8");

function declaredFields(iface: string): string[] {
  const body = new RegExp(`export interface ${iface} \\{([\\s\\S]*?)\\n\\}`).exec(DTS)?.[1];
  if (!body) throw new Error(`${iface} is not in the generated contract`);
  return [...body.matchAll(/^\s{2}(\$?\w+)\??:/gm)].map((match) => match[1]).sort();
}

interface SchemaNode {
  type?: string;
  properties?: Record<string, SchemaNode>;
  items?: SchemaNode;
}

function schemaFields(node: SchemaNode | undefined): string[] {
  const shape = node?.type === "array" ? node.items : node;
  return Object.keys(shape?.properties ?? {}).sort();
}

const ROOT = manifestSchema() as SchemaNode;

// Every nested object in the manifest, and the interface that describes it. The pairing is spelled
// out rather than derived: the schema names a field, the contract names a type, and nothing in
// either connects the two, which is the whole reason this test exists.
const NESTED: Array<{ field: string; iface: string }> = [
  { field: "services", iface: "ManifestServices" },
  { field: "commands", iface: "ManifestCommand" },
  { field: "config", iface: "ManifestConfig" },
  { field: "data", iface: "ManifestData" },
  { field: "lifecycle", iface: "ManifestLifecycle" },
  { field: "publish", iface: "ManifestPublish" },
  { field: "repo", iface: "RepoMeta" },
];

it("describes the same manifest in the contract and in the validator", () => {
  expect(declaredFields("PluginManifest")).toEqual(schemaFields(ROOT));
});

// Top-level parity alone let `publish` gain three fields and `repo` two in the contract while the
// published schema still described one and four, which type-checked, validated and shipped.
it.each(NESTED)("describes the same $field block in the contract and in the validator", ({ field, iface }) => {
  expect(declaredFields(iface)).toEqual(schemaFields(ROOT.properties?.[field]));
});

// A nested object added to the schema with no entry above would otherwise be checked by nobody, so
// the list itself is held to the schema rather than trusted.
it("covers every nested object the schema declares", () => {
  const nestedInSchema = Object.entries(ROOT.properties ?? {})
    .filter(([, node]) => schemaFields(node).length > 0)
    .map(([field]) => field)
    .sort();

  expect(NESTED.map((entry) => entry.field).sort()).toEqual(nestedInSchema);
});
