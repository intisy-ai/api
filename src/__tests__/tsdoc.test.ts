import { mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";
import { guardDocumentation, guardGeneratedSurface, undocumented, undocumentedMembers } from "../testing/index.js";
import { sourceFiles } from "../testing/predicates.js";

const REPO = fileURLToPath(new URL("../..", import.meta.url));
const GENERATED = join(REPO, "generated");

guardDocumentation({ dir: new URL("..", import.meta.url) });

/**
 * @remarks
 * The generated surface is what a plugin author reads, and its prose comes from the javadoc on the
 * Java it is emitted from. A Java member written without a doc comment silently produces an
 * undocumented declaration here, which fails `npm run docs` rather than any build, so this is what
 * catches it at the point the emission is committed.
 */
guardGeneratedSurface({
  files: [join(GENERATED, "api.d.ts"), join(GENERATED, "api.keys.ts"), join(GENERATED, "engine.d.ts")],
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

it("accepts a documented declaration separated by a decorator or a line comment", () => {
  expect(undocumented(`/** Injectable. */\n@Injectable()\nexport class Foo {}`)).toEqual([]);
  expect(
    undocumented(`/** Documented. */\n// eslint-disable-next-line some-rule\nexport const A = 1;`),
  ).toEqual([]);
  expect(undocumented(`@Injectable()\nexport class Foo {}`)).toEqual(["export class Foo {}"]);
});

it("requires only the first signature of an overload group to be documented", () => {
  const overloads = [
    "/** Converts a value. */",
    "export function f(a: string): void;",
    "export function f(a: number): void;",
    "export function f(a: string | number): void {}",
  ].join("\n");
  expect(undocumented(overloads)).toEqual([]);

  const undocumentedOverloads = [
    "export function f(a: string): void;",
    "export function f(a: number): void;",
  ].join("\n");
  expect(undocumented(undocumentedOverloads)).toEqual(["export function f(a: string): void;"]);
});

it("still reports two distinct, genuinely undocumented declarations sitting back to back", () => {
  const distinct = ["export const a = 1;", "export const b = 2;"].join("\n");
  expect(undocumented(distinct)).toEqual(["export const a = 1;", "export const b = 2;"]);
});

it("flags each of the five previously-missed export forms", () => {
  expect(undocumented(`export var legacy = 1;`)).toEqual(["export var legacy = 1;"]);
  expect(undocumented(`export namespace N {\n}`)).toEqual(["export namespace N {"]);
  expect(undocumented(`declare module "x" {\n}`)).toEqual([`declare module "x" {`]);
  expect(undocumented(`export default createThing();`)).toEqual(["export default createThing();"]);
  expect(undocumented(`export default {\n  a: 1,\n};`)).toEqual(["export default {"]);
});

it("excludes .spec.ts and .d.ts files from sourceFiles", () => {
  const dir = mkdtempSync(join(tmpdir(), "api-testing-"));
  try {
    writeFileSync(join(dir, "real.ts"), "export const a = 1;\n");
    writeFileSync(join(dir, "real.spec.ts"), "export const a = 1;\n");
    writeFileSync(join(dir, "real.test.ts"), "export const a = 1;\n");
    writeFileSync(join(dir, "real.d.ts"), "export const a: number;\n");
    expect(sourceFiles(dir).map((file) => file.split(/[\\/]/).pop())).toEqual(["real.ts"]);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});
