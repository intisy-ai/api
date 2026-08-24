import { readdirSync, readFileSync, statSync } from "node:fs";
import { join, relative } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const REPO = fileURLToPath(new URL("../..", import.meta.url));
const SRC = fileURLToPath(new URL("..", import.meta.url));
const GENERATED = join(REPO, "generated");
const DECLARATION = /^export\s+(?:default\s+)?(?:declare\s+)?(?:abstract\s+)?(?:async\s+)?(?:interface|type|class|function\*?|const|let|enum)\s/;
const MEMBER = /^ {2}(?:readonly\s+)?[A-Za-z_$][\w$]*\??[(:<]/;
const RE_EXPORT = /^export\s+(?:type\s+)?(?:\{|\*)/;

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

function documented(lines: string[], at: number): boolean {
  let previous = at - 1;
  while (previous >= 0 && lines[previous].trim() === "") previous--;
  return previous >= 0 && lines[previous].trim().endsWith("*/");
}

/** Every exported declaration in a file that carries no doc comment. */
export function undocumented(source: string): string[] {
  const lines = source.split(/\r?\n/);
  const missing: string[] = [];
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (RE_EXPORT.test(line) || !DECLARATION.test(line)) continue;
    if (!documented(lines, i)) missing.push(line.trim());
  }
  return missing;
}

/**
 * Every interface MEMBER in a file that carries no doc comment.
 *
 * @remarks
 * Separate from {@link undocumented} because a member is what the generated surface is almost
 * entirely made of: the declarations are few and the properties are many, and typedoc fails the
 * docs build on either. Two-space indentation identifies a member, which is the only shape the
 * emitter produces.
 */
export function undocumentedMembers(source: string): string[] {
  const lines = source.split(/\r?\n/);
  const missing: string[] = [];
  for (let i = 0; i < lines.length; i++) {
    if (!MEMBER.test(lines[i])) continue;
    if (!documented(lines, i)) missing.push(lines[i].trim());
  }
  return missing;
}

it("documents every exported declaration under src", () => {
  const offenders = sourceFiles(SRC)
    .map((file) => ({ file: relative(SRC, file), missing: undocumented(readFileSync(file, "utf8")) }))
    .filter((entry) => entry.missing.length > 0);
  expect(offenders).toEqual([]);
});

/**
 * @remarks
 * The generated surface is what a plugin author reads, and its prose comes from the javadoc on the
 * Java it is emitted from. A Java member written without a doc comment silently produces an
 * undocumented declaration here, which fails `npm run docs` rather than any build, so this is what
 * catches it at the point the emission is committed.
 */
it("documents every declaration and member of the generated surface", () => {
  const offenders = readdirSync(GENERATED)
    .filter((name) => name === "api.d.ts" || name === "api.keys.ts" || name === "engine.d.ts")
    .map((name) => {
      const source = readFileSync(join(GENERATED, name), "utf8");
      return { file: name, missing: [...undocumented(source), ...undocumentedMembers(source)] };
    })
    .filter((entry) => entry.missing.length > 0);
  expect(offenders).toEqual([]);
});

it("flags an undocumented export and accepts a documented one", () => {
  expect(undocumented(`export const A = 1;`)).toEqual(["export const A = 1;"]);
  expect(undocumented(`/** Documented. */\nexport const A = 1;`)).toEqual([]);
  expect(undocumented(`export { A } from "./a.js";`)).toEqual([]);
  expect(undocumented(`export type * from "./a.js";`)).toEqual([]);
  expect(undocumented(`export async function a() {}`)).toEqual(["export async function a() {}"]);
  expect(undocumented(`export function* g() {}`)).toEqual(["export function* g() {}"]);
});

it("flags an undocumented member and accepts a documented one", () => {
  expect(undocumentedMembers(`export interface A {\n  id: string;\n}`)).toEqual(["id: string;"]);
  expect(undocumentedMembers(`export interface A {\n  /** The id. */\n  id: string;\n}`)).toEqual([]);
  expect(undocumentedMembers(`export interface A {\n  read(): void;\n}`)).toEqual(["read(): void;"]);
  expect(undocumentedMembers(`export interface A {\n  readonly id?: string;\n}`)).toEqual(["readonly id?: string;"]);
  expect(undocumentedMembers(`export type A = "x" | "y";`)).toEqual([]);
});
