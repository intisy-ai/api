import { readdirSync, readFileSync, statSync } from "node:fs";
import { join, relative } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const DECLARATION = /^export\s+(?:default\s+)?(?:declare\s+)?(?:abstract\s+)?(?:async\s+)?(?:interface|type|class|function\*?|const|let|enum)\s/;
const MEMBER = /^ {2}(?:readonly\s+)?[A-Za-z_$][\w$]*\??[(:<]/;
const RE_EXPORT = /^export\s+(?:type\s+)?(?:\{|\*)/;

/**
 * Every TypeScript source file under `dir`, excluding tests and the names in `skipFiles`.
 *
 * @param dir the directory to walk
 * @param skipFiles file names to skip, defaulting to the barrel, which only re-exports
 * @returns absolute paths, in directory order
 */
export function sourceFiles(dir: string, skipFiles: string[] = ["index.ts"], out: string[] = []): string[] {
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) {
      if (entry === "__tests__") continue;
      sourceFiles(full, skipFiles, out);
      continue;
    }
    if (entry.endsWith(".ts") && !entry.endsWith(".test.ts") && !skipFiles.includes(entry)) out.push(full);
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

/** A line that IS a type-checking suppression directive, not one that merely names it in prose. */
const SUPPRESSION = /^\s*(?:\/\/|\/\*)\s*@ts-(?:nocheck|ignore|expect-error)\b[\s*/]*$/;

/**
 * Every type-checking suppression directive in a file.
 *
 * @remarks
 * Anchored to the whole line on purpose. Two comments in `libs/core` explain the directive in prose,
 * and a scanner matching the bare string reports those as violations, which is how a guard earns a
 * reputation for crying wolf.
 *
 * @param source the file's contents
 * @returns the offending lines, trimmed, in file order
 */
export function suppressions(source: string): string[] {
  return source
    .split(/\r?\n/)
    .filter((line) => SUPPRESSION.test(line))
    .map((line) => line.trim());
}

/** Where a guard looks, and what it skips. */
export interface GuardOptions {
  /** The directory to scan, normally `new URL("..", import.meta.url)` from a `__tests__` file. */
  dir: string | URL;
  /** File names to skip, relative to no directory. Defaults to `["index.ts"]`. */
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
