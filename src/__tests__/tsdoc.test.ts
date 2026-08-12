import { readdirSync, readFileSync, statSync } from "node:fs";
import { join, relative } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const SRC = fileURLToPath(new URL("..", import.meta.url));
const DECLARATION = /^export\s+(?:default\s+)?(?:declare\s+)?(?:abstract\s+)?(?:async\s+)?(?:interface|type|class|function\*?|const|let|enum)\s/;
const RE_EXPORT = /^export\s+(?:type\s+)?\{|^export\s+\*/;

function sourceFiles(dir: string, out: string[] = []): string[] {
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) {
      if (entry === "__tests__") continue;
      sourceFiles(full, out);
      continue;
    }
    if (entry.endsWith(".ts") && !entry.endsWith(".test.ts") && entry !== "index.ts") out.push(full);
  }
  return out;
}

export function undocumented(source: string): string[] {
  const lines = source.split(/\r?\n/);
  const missing: string[] = [];
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (RE_EXPORT.test(line) || !DECLARATION.test(line)) continue;
    let previous = i - 1;
    while (previous >= 0 && lines[previous].trim() === "") previous--;
    if (previous < 0 || !lines[previous].trim().endsWith("*/")) missing.push(line.trim());
  }
  return missing;
}

it("documents every exported declaration", () => {
  const offenders = sourceFiles(SRC)
    .map((file) => ({ file: relative(SRC, file), missing: undocumented(readFileSync(file, "utf8")) }))
    .filter((entry) => entry.missing.length > 0);
  expect(offenders).toEqual([]);
});

it("flags an undocumented export and accepts a documented one", () => {
  expect(undocumented(`export const A = 1;`)).toEqual(["export const A = 1;"]);
  expect(undocumented(`/** Documented. */\nexport const A = 1;`)).toEqual([]);
  expect(undocumented(`export { A } from "./a.js";`)).toEqual([]);
  expect(undocumented(`export async function a() {}`)).toEqual(["export async function a() {}"]);
  expect(undocumented(`export function* g() {}`)).toEqual(["export function* g() {}"]);
});
