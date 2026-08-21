import { readFileSync } from "node:fs";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const repo = fileURLToPath(new URL("../..", import.meta.url));
const NODE_BUILTINS = ["fs", "path", "os", "process", "child_process", "url", "crypto", "util", "events", "stream"];

/** Subpaths that are Node-only by design, so a builtin in one of them is correct rather than a leak. */
const NODE_ALLOWED = ["./host"];

interface Served {
  subpath: string;
  file: string;
}

/**
 * Every file the package's `exports` serves, taken from the map rather than from a directory scan.
 *
 * @remarks
 * Derived so a subpath added later cannot quietly escape the check. A scan of `src/` used to do
 * this job and would now pass by finding nothing: the surface is generated, and `src/` holds only
 * the Node-only CLI.
 */
function served(): Served[] {
  const pkg = JSON.parse(readFileSync(join(repo, "package.json"), "utf8")) as {
    exports: Record<string, Record<string, string>>;
  };
  return Object.entries(pkg.exports)
    .filter(([subpath]) => !NODE_ALLOWED.includes(subpath))
    .flatMap(([subpath, conditions]) => Object.values(conditions).map((file) => ({ subpath, file })));
}

function nodeImports(source: string): string[] {
  const specifiers = [...source.matchAll(/(?:from|import)\s+["']([^"']+)["']/g)].map((m) => m[1]);
  return specifiers.filter((s) => s.startsWith("node:") || NODE_BUILTINS.includes(s));
}

it("looks at every file the exports map serves", () => {
  expect(served().map((entry) => entry.file).sort()).toEqual([
    "./generated/api.d.ts",
    "./generated/api.keys.d.ts",
    "./generated/api.keys.js",
    "./generated/engine.d.ts",
    "./generated/engine.js",
  ]);
});

it("serves no entry point that pulls a node builtin", () => {
  const offenders = served()
    .map((entry) => ({ ...entry, imports: nodeImports(readFileSync(join(repo, entry.file), "utf8")) }))
    .filter((entry) => entry.imports.length > 0);
  expect(offenders).toEqual([]);
});

it("detects a node import when one is present", () => {
  expect(nodeImports(`import { readFileSync } from "node:fs";`)).toEqual(["node:fs"]);
  expect(nodeImports(`import { join } from "path";`)).toEqual(["path"]);
  expect(nodeImports(`export type * from "./api.js";`)).toEqual([]);
});
