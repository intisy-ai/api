import { readdirSync, readFileSync } from "node:fs";
import { join, relative } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";
import { sourceFiles, undocumented, undocumentedMembers } from "../testing/index.js";

const REPO = fileURLToPath(new URL("../..", import.meta.url));
const SRC = fileURLToPath(new URL("..", import.meta.url));
const GENERATED = join(REPO, "generated");

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
