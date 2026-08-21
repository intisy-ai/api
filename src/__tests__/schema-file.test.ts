import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";
import { manifestSchema } from "../../generated/engine.js";

it("keeps the published schema file identical to the schema the validator uses", () => {
  const file = fileURLToPath(new URL("../../schema/plugin.schema.json", import.meta.url));
  expect(readFileSync(file, "utf8")).toBe(`${JSON.stringify(manifestSchema(), null, 2)}\n`);
});
