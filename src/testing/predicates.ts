import { readdirSync, statSync } from "node:fs";
import { join } from "node:path";

const DECLARATION =
  /^export\s+(?:default\s+)?(?:declare\s+)?(?:abstract\s+)?(?:async\s+)?(?:interface|type|class|function\*?|const|let|var|enum|namespace|module)\s|^declare\s+module\s|^export\s+default\b/;
const MEMBER = /^ {2}(?:readonly\s+)?[A-Za-z_$][\w$]*\??[(:<]/;
const RE_EXPORT = /^export\s+(?:type\s+)?(?:\{|\*)/;
const FUNCTION_SIGNATURE = /^export\s+(?:default\s+)?(?:declare\s+)?(?:async\s+)?function\*?\s+([A-Za-z_$][\w$]*)/;
const SKIPPABLE = /^\s*(?:@[A-Za-z]|\/\/)/;

/**
 * Every TypeScript source file under `dir`, excluding tests, specs, declaration files and the
 * names in `skipFiles`.
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
    if (
      entry.endsWith(".ts") &&
      !entry.endsWith(".test.ts") &&
      !entry.endsWith(".spec.ts") &&
      !entry.endsWith(".d.ts") &&
      !skipFiles.includes(entry)
    ) {
      out.push(full);
    }
  }
  return out;
}

function documented(lines: string[], at: number): boolean {
  let previous = at - 1;
  while (previous >= 0 && (lines[previous].trim() === "" || SKIPPABLE.test(lines[previous]))) previous--;
  return previous >= 0 && lines[previous].trim().endsWith("*/");
}

/**
 * Whether the declaration at `at` is a later signature of a function overload group already
 * introduced above it, so it does not need a doc comment of its own.
 *
 * @remarks
 * Scoped to function signatures only, matched by name: an interface, class or const cannot be
 * overloaded this way, and requiring the same captured name keeps two distinct, genuinely
 * undocumented declarations that happen to sit back to back from being merged into one.
 */
function isOverloadContinuation(lines: string[], at: number): boolean {
  const match = FUNCTION_SIGNATURE.exec(lines[at]);
  if (!match) return false;
  let previous = at - 1;
  while (previous >= 0 && lines[previous].trim() === "") previous--;
  if (previous < 0) return false;
  const previousMatch = FUNCTION_SIGNATURE.exec(lines[previous]);
  return previousMatch !== null && previousMatch[1] === match[1];
}

/** Every exported declaration in a file that carries no doc comment. */
export function undocumented(source: string): string[] {
  const lines = source.split(/\r?\n/);
  const missing: string[] = [];
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (RE_EXPORT.test(line) || !DECLARATION.test(line)) continue;
    if (isOverloadContinuation(lines, i)) continue;
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
 *
 * This is safe only against EMITTED declaration files. At two-space indent in ordinary source,
 * `MEMBER` also matches an object literal property, a statement or a control-flow line, so this
 * must never be pointed at hand-written source, only at the generated surface (see
 * {@link guardGeneratedSurface} in `./index.js`).
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
const SUPPRESSION = /^\s*(?:\/|\*)+\s*@ts-(?:nocheck|ignore|expect-error)\b/;

/**
 * Every type-checking suppression directive in a file.
 *
 * @remarks
 * The directive must open the comment, which is the same rule the TypeScript compiler applies: it
 * accepts one behind any run of slashes or asterisks, so a block-comment continuation line counts,
 * and it accepts trailing text, so a reason does not disarm it. Anything the directive does not
 * begin is prose naming it, which a guard must let through or it earns a reputation for crying wolf.
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
