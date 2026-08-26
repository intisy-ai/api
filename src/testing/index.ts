import { readFileSync } from "node:fs";
import { relative } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";
import { sourceFiles, suppressions, undocumented } from "./predicates.js";

export * from "./predicates.js";

/** Where a guard looks, and what it skips. */
export interface GuardOptions {
  /** The directory to scan, normally `new URL("..", import.meta.url)` from a `__tests__` file. */
  dir: string | URL;
  /** File names to skip, relative to no directory. Defaults to `[]`. */
  skipFiles?: string[];
}

function resolveDir(dir: string | URL): string {
  return typeof dir === "string" ? dir : fileURLToPath(dir);
}

/**
 * Registers the guard asserting every exported declaration under `dir` carries TSDoc.
 *
 * @param options where to look and what to skip
 */
export function guardDocumentation(options: GuardOptions): void {
  it("documents every exported declaration", () => {
    const root = resolveDir(options.dir);
    const offenders = sourceFiles(root, options.skipFiles)
      .map((file) => ({ file: relative(root, file), missing: undocumented(readFileSync(file, "utf8")) }))
      .filter((entry) => entry.missing.length > 0);
    expect(offenders).toEqual([]);
  });
}

/**
 * Registers the guard asserting no file under `dir` opts out of type checking.
 *
 * @param options where to look and what to skip
 */
export function guardNoSuppressions(options: GuardOptions): void {
  it("suppresses type checking in no file", () => {
    const root = resolveDir(options.dir);
    const offenders = sourceFiles(root, options.skipFiles)
      .map((file) => ({ file: relative(root, file), found: suppressions(readFileSync(file, "utf8")) }))
      .filter((entry) => entry.found.length > 0);
    expect(offenders).toEqual([]);
  });
}
