import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";
import { validateManifest } from "../../generated/engine.js";

/**
 * The vocabulary the fixture's expectations assume, matching the Java `ParityTest` exactly.
 *
 * @remarks
 * Passed rather than omitted because an omitted list means unverifiable, not empty, so the
 * squatting check would be skipped and the fixture would silently agree with nothing.
 */
const WELL_KNOWN = ["accounts", "routing", "activity"];

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
    expect(validateManifest(example.manifest, WELL_KNOWN).map((issue) => issue.path)).toEqual(example.expectedPaths);
  });
}
