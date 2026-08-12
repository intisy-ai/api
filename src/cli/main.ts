#!/usr/bin/env node
import { realpathSync } from "node:fs";
import process from "node:process";
import { fileURLToPath } from "node:url";
import { analyzePlugins } from "../doctor.js";
import { isPluginError } from "../errors.js";
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
  const loaded = readCheckout(option(argv, "--dir") ?? ".");
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
  const loaded: LoadedManifest[] = [];
  if (home) loaded.push(...readHome(home));
  if (dir || !home) loaded.push(readCheckout(dir ?? "."));

  const report = analyzePlugins(loaded.map((entry) => entry.value as PluginManifest), api ? Number(api) : API_VERSION);
  const lines = formatReport(report, loaded.length);
  const write = report.ok ? (line: string) => io.out(line) : (line: string) => io.err(line);
  for (const line of lines) write(line);
  return report.ok ? 0 : 1;
}

function option(argv: string[], name: string): string | undefined {
  const index = argv.indexOf(name);
  if (index < 0 || index === argv.length - 1) return undefined;
  return argv[index + 1];
}

const invokedDirectly = process.argv[1] ? realpathSync(process.argv[1]) === fileURLToPath(import.meta.url) : false;
if (invokedDirectly) {
  process.exitCode = run(process.argv.slice(2), {
    out: (line) => process.stdout.write(`${line}\n`),
    err: (line) => process.stderr.write(`${line}\n`),
  });
}
