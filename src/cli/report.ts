import type { PluginManifest } from "../../generated/api.js";
import type { ValidationIssueShape } from "../../generated/engine.js";
import type { LoadedManifest } from "./read.js";

/** What the CLI prints when it is asked for help or given something it does not understand. */
export const USAGE = [
  "usage:",
  "  intisy-plugin validate [--dir <path>]",
  "",
  "  --dir   a repo checkout carrying a plugin.json, the working directory by default",
].join("\n");

/** Renders one manifest's validation outcome, valid or not. */
export function formatValidation(loaded: LoadedManifest, issues: ValidationIssueShape[]): string[] {
  const manifest = loaded.value as Partial<PluginManifest> | null;
  const id = typeof manifest?.id === "string" && manifest.id ? manifest.id : "(unknown plugin)";
  if (!issues.length) return [`ok  ${id} (api ${manifest?.api})`];
  const lines = [`${id}: ${issues.length} problem${issues.length === 1 ? "" : "s"}`];
  for (const issue of issues) {
    lines.push(`  error  ${issue.path}: ${issue.message}`);
    lines.push(`    fix: ${issue.fix}`);
  }
  return lines;
}
