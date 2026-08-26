import { existsSync, mkdtempSync, readFileSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const repo = fileURLToPath(new URL("../..", import.meta.url));
const NODE_BUILTINS = ["fs", "path", "os", "process", "child_process", "url", "crypto", "util", "events", "stream"];

/** Subpaths that are Node-only by design, so a builtin in one of them is correct rather than a leak. */
const NODE_ALLOWED = ["./host", "./testing"];

interface Served {
  subpath: string;
  file: string;
}

/**
 * Every file the package's `exports` serves, taken from the map rather than from a directory scan.
 *
 * @remarks
 * Derived so a subpath added later cannot quietly escape the check. A scan of `src/` used to do
 * this job and would now pass by finding nothing: the surface is generated, and `src/` holds the
 * Node-only CLI and the Node-only host.
 */
function served(): Served[] {
  const pkg = JSON.parse(readFileSync(join(repo, "package.json"), "utf8")) as {
    exports: Record<string, Record<string, string>>;
  };
  return Object.entries(pkg.exports)
    .filter(([subpath]) => !NODE_ALLOWED.includes(subpath))
    .flatMap(([subpath, conditions]) => Object.values(conditions).map((file) => ({ subpath, file })));
}

function specifiers(source: string): string[] {
  return [...source.matchAll(/(?:from|import)\s+["']([^"']+)["']/g)].map((match) => match[1]);
}

function nodeImports(source: string): string[] {
  return specifiers(source).filter((s) => s.startsWith("node:") || NODE_BUILTINS.includes(s));
}

/** The file a relative specifier names, or null when it resolves to nothing this check reads. */
function resolveRelative(fromFile: string, specifier: string): string | null {
  const base = resolve(dirname(fromFile), specifier);
  const candidates = [base, base.replace(/\.js$/, ".d.ts"), `${base}.d.ts`, `${base}.ts`];
  return candidates.find((candidate) => existsSync(candidate)) ?? null;
}

/**
 * Every node builtin the module graph rooted at `entry` reaches, not just the ones it names itself.
 *
 * @remarks
 * A per-file check passes the moment the builtin moves one relative import away, which is the same
 * hole the plugin linkage gate closed by walking the graph. The generated surface is bundled and
 * reaches nothing today, so this asserts a property rather than describing the current shape.
 */
function nodeImportsFrom(entry: string): string[] {
  const found = new Set<string>();
  const seen = new Set<string>();
  const queue = [entry];
  while (queue.length > 0) {
    const file = queue.shift() as string;
    if (seen.has(file)) continue;
    seen.add(file);
    let source: string;
    try {
      source = readFileSync(file, "utf8");
    } catch {
      continue;
    }
    for (const specifier of specifiers(source)) {
      if (specifier.startsWith(".")) {
        const next = resolveRelative(file, specifier);
        if (next) queue.push(next);
        continue;
      }
      if (specifier.startsWith("node:") || NODE_BUILTINS.includes(specifier)) found.add(specifier);
    }
  }
  return [...found].sort();
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

it("serves no entry point whose graph reaches a node builtin", () => {
  const offenders = served()
    .map((entry) => ({ ...entry, imports: nodeImportsFrom(join(repo, entry.file)) }))
    .filter((entry) => entry.imports.length > 0);
  expect(offenders).toEqual([]);
});

it("is a graph walk, so a builtin one relative import away is still caught", () => {
  const dir = mkdtempSync(join(tmpdir(), "api-node-free-"));
  writeFileSync(join(dir, "entry.js"), `export * from "./deep.js";\n`);
  writeFileSync(join(dir, "deep.js"), `import { readFileSync } from "node:fs";\nexport const read = readFileSync;\n`);

  expect(nodeImports(readFileSync(join(dir, "entry.js"), "utf8"))).toEqual([]);
  expect(nodeImportsFrom(join(dir, "entry.js"))).toEqual(["node:fs"]);
});

it("detects a node import when one is present", () => {
  expect(nodeImports(`import { readFileSync } from "node:fs";`)).toEqual(["node:fs"]);
  expect(nodeImports(`import { join } from "path";`)).toEqual(["path"]);
  expect(nodeImports(`export type * from "./api.js";`)).toEqual([]);
});

it("the host subpath is Node-only by design, and its graph proves it", () => {
  expect(NODE_ALLOWED).toContain("./host");
  expect(nodeImportsFrom(join(repo, "dist/host/index.js")).length).toBeGreaterThan(0);
});
