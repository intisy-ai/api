import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";
import { manifestSchema } from "../../generated/engine.js";

// The manifest is described TWICE: once as the generated `PluginManifest` an author writes against,
// and once as the schema the validator enforces. Adding a field to one and not the other type-checks
// and validates green while a manifest using it is rejected at run time, which is exactly what
// happened when `commands` and `config` were added to the contract alone.
function declaredFields(): string[] {
  const dts = readFileSync(fileURLToPath(new URL("../../generated/api.d.ts", import.meta.url)), "utf8");
  const body = /export interface PluginManifest \{([\s\S]*?)\n\}/.exec(dts)?.[1];
  if (!body) throw new Error("PluginManifest is not in the generated contract");
  return [...body.matchAll(/^\s{2}(\$?\w+)\??:/gm)].map((match) => match[1]);
}

it("describes the same manifest in the contract and in the validator", () => {
  const contract = declaredFields().sort();
  const schema = manifestSchema() as { properties?: Record<string, unknown> };
  const validated = Object.keys(schema.properties ?? {}).sort();
  expect(contract).toEqual(validated);
});
