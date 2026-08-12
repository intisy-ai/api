import type { DoctorReport } from "../doctor.js";
import type { PluginManifest } from "../manifest.js";
import type { ValidationIssue } from "../validate.js";
import type { LoadedManifest } from "./read.js";

/** What the CLI prints when it is asked for help or given something it does not understand. */
export const USAGE = [
  "usage:",
  "  intisy-plugin validate [--dir <path>]",
  "  intisy-plugin doctor [--dir <path>] [--home <path>] [--api <n>]",
  "",
  "  --dir   a repo checkout carrying a plugin.json, the working directory by default",
  "  --home  an app home whose deployed manifest sidecars to check",
  "  --api   the host api major version to check floors against",
].join("\n");

/** Renders one manifest's validation outcome, valid or not. */
export function formatValidation(loaded: LoadedManifest, issues: ValidationIssue[]): string[] {
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

/** Renders a doctor report, one finding per pair of lines. */
export function formatReport(report: DoctorReport, checked: number): string[] {
  const lines = [`${checked} plugin${checked === 1 ? "" : "s"} checked`];
  for (const finding of report.findings) {
    lines.push(`  ${finding.level}  ${finding.pluginId}: ${finding.detail}`);
    lines.push(`    fix: ${finding.fix}`);
  }
  return lines;
}
