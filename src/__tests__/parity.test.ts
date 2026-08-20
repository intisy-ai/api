import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";
import { validateManifest } from "../validate.js";

interface Case {
  name: string;
  manifest: unknown;
  expectedPaths: string[];
}

const cases = JSON.parse(
  readFileSync(fileURLToPath(new URL("../../test/parity/manifests.json", import.meta.url)), "utf8"),
) as Case[];

for (const example of cases) {
  it(`agrees with the shared fixture: ${example.name}`, () => {
    expect(validateManifest(example.manifest).map((issue) => issue.path)).toEqual(example.expectedPaths);
  });
}
