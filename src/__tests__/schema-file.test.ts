import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";
import { MANIFEST_SCHEMA } from "../manifest-schema.js";

it("keeps the published schema file identical to the schema the validator uses", () => {
  const file = fileURLToPath(new URL("../../schema/plugin.schema.json", import.meta.url));
  expect(readFileSync(file, "utf8")).toBe(`${JSON.stringify(MANIFEST_SCHEMA, null, 2)}\n`);
});
