#!/usr/bin/env node
import { realpathSync } from "node:fs";
import process from "node:process";
import { fileURLToPath } from "node:url";
import { analyzePlugins } from "../doctor.js";
import type { DoctorFinding, DoctorReport } from "../doctor.js";
import { isPluginError } from "../errors.js";
import type { PluginError } from "../errors.js";
import { API_VERSION } from "../manifest.js";
import type { PluginManifest } from "../manifest.js";
import { validateManifest } from "../validate.js";
import { readCheckout, readHome } from "./read.js";
import type { LoadedManifest } from "./read.js";
import { formatReport, formatValidation, USAGE } from "./report.js";

/** Where the CLI writes, supplied by the caller so a test needs no process. */
export interface CliIo {
  /** Writes one line of ordinary output. */
  out(line: string): void;
  /** Writes one line of problem output. */
  err(line: string): void;
}

/**
 * Runs one CLI invocation.
 *
 * @param argv - the arguments after the program name
 * @param io - where to write
 * @returns the process exit code: 0 for success, 1 for a reported problem, 2 for a misuse
 */
export function run(argv: string[], io: CliIo): number {
  const [command, ...rest] = argv;
  if (!command) {
    for (const line of USAGE.split("\n")) io.err(line);
    return 2;
  }
  if (command === "--help" || command === "-h" || command === "help") {
    for (const line of USAGE.split("\n")) io.out(line);
    return 0;
  }
  try {
    if (command === "validate") return validate(rest, io);
    if (command === "doctor") return doctor(rest, io);
  } catch (error) {
    io.err(isPluginError(error) ? error.message : String(error));
    return 1;
  }
  io.err(`unknown command: ${command}`);
  for (const line of USAGE.split("\n")) io.err(line);
  return 2;
}

function validate(argv: string[], io: CliIo): number {
  const dir = option(argv, "--dir");
  if (missingValue(dir, "--dir", io)) return 2;
  const loaded = readCheckout(dir.value ?? ".");
  const issues = validateManifest(loaded.value);
  const lines = formatValidation(loaded, issues);
  const write = issues.length ? (line: string) => io.err(line) : (line: string) => io.out(line);
  for (const line of lines) write(line);
  return issues.length ? 1 : 0;
}

function doctor(argv: string[], io: CliIo): number {
  const home = option(argv, "--home");
  const dir = option(argv, "--dir");
  const api = option(argv, "--api");
  if (missingValue(home, "--home", io)) return 2;
  if (missingValue(dir, "--dir", io)) return 2;
  if (missingValue(api, "--api", io)) return 2;
  const hostApi = api.value === undefined ? API_VERSION : Number(api.value);
  if (!Number.isInteger(hostApi) || hostApi < 1) {
    io.err(`--api needs a whole number of 1 or more, got "${api.value}"`);
    return 2;
  }

  const loaded: LoadedManifest[] = [];
  const unreadable: DoctorFinding[] = [];
  if (home.value) {
    const fromHome = readHome(home.value);
    loaded.push(...fromHome.loaded);
    for (const failure of fromHome.failed) unreadable.push(toFinding(failure.error));
  }
  if (dir.value || !home.value) {
    try {
      loaded.push(readCheckout(dir.value ?? "."));
    } catch (error) {
      if (!isPluginError(error)) throw error;
      unreadable.push(toFinding(error));
    }
  }

  const analysed = analyzePlugins(loaded.map((entry) => entry.value as PluginManifest), hostApi);
  const findings = [...unreadable, ...analysed.findings];
  const report: DoctorReport = { findings, ok: !findings.some((finding) => finding.level === "error") };
  const lines = formatReport(report, loaded.length);
  const write = report.ok ? (line: string) => io.out(line) : (line: string) => io.err(line);
  for (const line of lines) write(line);
  return report.ok ? 0 : 1;
}

function toFinding(error: PluginError): DoctorFinding {
  return { level: "error", pluginId: error.pluginId, detail: error.detail, fix: error.fix };
}

/** One flag as it appeared on the command line, so a flag given no value is a misuse rather than an absence. */
interface Flag {
  /** Whether the flag appeared at all. */
  present: boolean;
  /** The value that followed it, absent when nothing but another flag did. */
  value?: string;
}

function option(argv: string[], name: string): Flag {
  const index = argv.indexOf(name);
  if (index < 0) return { present: false };
  const value = argv[index + 1];
  if (value === undefined || value.startsWith("--")) return { present: true };
  return { present: true, value };
}

function missingValue(flag: Flag, name: string, io: CliIo): boolean {
  if (!flag.present || flag.value !== undefined) return false;
  io.err(`${name} needs a value`);
  return true;
}

function invokedDirectly(): boolean {
  const invoked = process.argv[1];
  if (!invoked) return false;
  try {
    return realpathSync(invoked) === fileURLToPath(import.meta.url);
  } catch {
    return false;
  }
}

if (invokedDirectly()) {
  process.exitCode = run(process.argv.slice(2), {
    out: (line) => process.stdout.write(`${line}\n`),
    err: (line) => process.stderr.write(`${line}\n`),
  });
}
