import { readdirSync, readFileSync, statSync } from "node:fs";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const SRC = fileURLToPath(new URL("..", import.meta.url));
const NODE_BUILTINS = ["fs", "path", "os", "process", "child_process", "url", "crypto", "util", "events", "stream"];

function sourceFiles(dir: string, out: string[] = []): string[] {
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) {
      if (entry === "cli" || entry === "__tests__") continue;
      sourceFiles(full, out);
      continue;
    }
    if (entry.endsWith(".ts") && !entry.endsWith(".test.ts")) out.push(full);
  }
  return out;
}

function nodeImports(source: string): string[] {
  const specifiers = [...source.matchAll(/(?:from|import)\s+["']([^"']+)["']/g)].map((m) => m[1]);
  return specifiers.filter((s) => s.startsWith("node:") || NODE_BUILTINS.includes(s));
}

it("imports no node builtin outside src/cli", () => {
  const offenders = sourceFiles(SRC)
    .map((file) => ({ file, imports: nodeImports(readFileSync(file, "utf8")) }))
    .filter((entry) => entry.imports.length > 0);
  expect(offenders).toEqual([]);
});

it("detects a node import when one is present", () => {
  expect(nodeImports(`import { readFileSync } from "node:fs";`)).toEqual(["node:fs"]);
  expect(nodeImports(`import { join } from "path";`)).toEqual(["path"]);
  expect(nodeImports(`import { PluginError } from "./errors.js";`)).toEqual([]);
});
