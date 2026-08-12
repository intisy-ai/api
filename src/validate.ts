import { PluginError } from "./errors.js";
import { mayRegister, WELL_KNOWN_SERVICES } from "./ids.js";
import { MANIFEST_SCHEMA } from "./manifest-schema.js";
import type { PluginManifest } from "./manifest.js";
import { validateAgainstSchema } from "./schema.js";
import type { SchemaIssue } from "./schema.js";

/** One thing wrong with a manifest, located, explained, and paired with its remedy. */
export type ValidationIssue = SchemaIssue;

/**
 * Checks a parsed `plugin.json` against the schema and against the rules the schema cannot
 * express, which are the ones that need one field to know about another.
 *
 * @remarks
 * Structural issues are reported alone: once a field has the wrong type there is nothing
 * trustworthy left to cross-check, and a cascade of derived complaints buries the real one.
 *
 * @returns every issue found, empty when the manifest is valid
 */
export function validateManifest(value: unknown): ValidationIssue[] {
  const structural = validateAgainstSchema(value, MANIFEST_SCHEMA);
  if (structural.length) return structural;

  const manifest = value as PluginManifest;
  const issues: ValidationIssue[] = [];
  issues.push(...entryIssues(manifest));
  issues.push(...providedServiceIssues(manifest));
  issues.push(...duplicateIssues("capabilities", manifest.capabilities));
  issues.push(...duplicateIssues("services.provides", manifest.services?.provides));
  issues.push(...duplicateIssues("services.consumes", manifest.services?.consumes));
  issues.push(...duplicateIssues("permissions", manifest.permissions));
  return issues;
}

/**
 * Returns the manifest when it is valid, and throws a {@link PluginError} naming the plugin, the
 * first problem, and its fix when it is not.
 *
 * @throws PluginError when the manifest fails {@link validateManifest}
 */
export function assertManifest(value: unknown): PluginManifest {
  const issues = validateManifest(value);
  if (!issues.length) return value as PluginManifest;
  const id = (value as { id?: unknown } | null)?.id;
  const pluginId = typeof id === "string" && id ? id : "(unknown plugin)";
  const first = issues[0];
  throw new PluginError(pluginId, `plugin.json ${first.path}: ${first.message}`, first.fix);
}

function entryIssues(manifest: PluginManifest): ValidationIssue[] {
  const declaresCapabilities = (manifest.capabilities ?? []).length > 0;
  if (declaresCapabilities && !manifest.entry) {
    return [{
      path: "entry",
      message: "capabilities are declared but no entry names the module that provides them",
      fix: 'add "entry": "dist/index.js"',
    }];
  }
  const entry = manifest.entry;
  if (entry && (entry.startsWith("/") || entry.startsWith("\\") || /^[A-Za-z]:/.test(entry) || entry.split(/[\\/]/).includes(".."))) {
    return [{
      path: "entry",
      message: `"${entry}" is not a path inside the repo`,
      fix: "use a repo-relative path with no leading slash and no ..",
    }];
  }
  return [];
}

function providedServiceIssues(manifest: PluginManifest): ValidationIssue[] {
  const issues: ValidationIssue[] = [];
  (manifest.services?.provides ?? []).forEach((serviceId, index) => {
    if (mayRegister(manifest.id, serviceId)) return;
    issues.push({
      path: `services.provides[${index}]`,
      message: `"${serviceId}" is neither namespaced by this plugin nor a well-known service id`,
      fix: `rename it to "${manifest.id}:${serviceId.split(":").pop() ?? serviceId}", or use one of: ${WELL_KNOWN_SERVICES.join(", ")}`,
    });
  });
  return issues;
}

function duplicateIssues(path: string, values: string[] | undefined): ValidationIssue[] {
  const issues: ValidationIssue[] = [];
  const seen = new Set<string>();
  (values ?? []).forEach((value, index) => {
    if (seen.has(value)) issues.push({ path: `${path}[${index}]`, message: `"${value}" is listed twice`, fix: "remove the duplicate entry" });
    seen.add(value);
  });
  return issues;
}
