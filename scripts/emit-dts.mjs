#!/usr/bin/env node
// Generic gradle-annotation-processor -> committed-source staging step, reusable by any module whose
// TypeScript surface is emitted from Java. Nothing here names api; every path is a flag.
//
// CONTRACT:
//   1. Runs `./gradlew <module>:classes` inside --java-dir.
//   2. Copies every emitted .ts file out of <java-dir>/<module-dir>/build/classes/java/main/ into --out.
//   3. Fails loudly rather than staging nothing, so a silent no-op cannot look like success.
//
// Usage:
//   node scripts/emit-dts.mjs --java-dir java --module :contract --out generated
//
// Optional flags:
//   --module-dir <dirName>  (default: --module with its leading ':' stripped)
//   --skip-build            (re-copy whatever was last generated, without re-running Gradle)

import { execFileSync } from "node:child_process";
import { copyFileSync, existsSync, mkdirSync, readdirSync, statSync } from "node:fs";
import { join, resolve } from "node:path";

const args = process.argv.slice(2);

function fail(message) {
  console.error(`emit-dts: ${message}`);
  process.exit(1);
}

function value(flag, fallback) {
  const at = args.indexOf(flag);
  return at >= 0 && args[at + 1] ? args[at + 1] : fallback;
}

const javaDirFlag = value("--java-dir", null);
if (!javaDirFlag) fail("--java-dir is required");
const gradleModule = value("--module", null);
if (!gradleModule) fail("--module is required, for example :contract");
const outFlag = value("--out", null);
if (!outFlag) fail("--out is required");

const javaDir = resolve(javaDirFlag);
const moduleDir = value("--module-dir", gradleModule.replace(/^:/, ""));
const outDir = resolve(outFlag);

if (!existsSync(javaDir)) fail(`--java-dir not found: ${javaDir}`);

if (!args.includes("--skip-build")) {
  const isWindows = process.platform === "win32";
  const wrapper = join(javaDir, isWindows ? "gradlew.bat" : "gradlew");
  if (!existsSync(wrapper)) fail(`gradle wrapper not found at ${wrapper}`);
  const gradleArgs = [`${gradleModule}:classes`, "--console=plain"];
  // gradlew.bat needs an interpreter, and cmd.exe is a real executable, so Node's normal Windows
  // argument quoting applies rather than the deprecated shell concatenation.
  if (isWindows) {
    execFileSync("cmd.exe", ["/d", "/s", "/c", wrapper, ...gradleArgs], { cwd: javaDir, stdio: "inherit" });
  } else {
    execFileSync(wrapper, gradleArgs, { cwd: javaDir, stdio: "inherit" });
  }
}

const emittedDir = join(javaDir, moduleDir, "build", "classes", "java", "main");
if (!existsSync(emittedDir)) fail(`no compiler output at ${emittedDir}, does --module-dir match the project directory?`);

const emitted = readdirSync(emittedDir).filter((name) => name.endsWith(".ts"));
if (emitted.length === 0) fail(`no .ts files emitted into ${emittedDir}`);

mkdirSync(outDir, { recursive: true });
for (const name of emitted) {
  const target = join(outDir, name);
  copyFileSync(join(emittedDir, name), target);
  if (statSync(target).size === 0) fail(`staged file ${target} is empty`);
  console.log(`emit-dts: staged ${name} -> ${target}`);
}
