import { readdirSync, statSync } from "node:fs";
import { join } from "node:path";

const DECLARATION = /^export\s+(?:default\s+)?(?:declare\s+)?(?:abstract\s+)?(?:async\s+)?(?:interface|type|class|function\*?|const|let|enum)\s/;
const MEMBER = /^ {2}(?:readonly\s+)?[A-Za-z_$][\w$]*\??[(:<]/;
const RE_EXPORT = /^export\s+(?:type\s+)?(?:\{|\*)/;

/**
 * Every TypeScript source file under `dir`, excluding tests and the names in `skipFiles`.
 *
 * @param dir the directory to walk
 * @param skipFiles file names to skip, empty by default
 * @returns absolute paths, in directory order
 */
export function sourceFiles(dir: string, skipFiles: string[] = [], out: string[] = []): string[] {
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
