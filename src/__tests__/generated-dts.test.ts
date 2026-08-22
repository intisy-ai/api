import { execFileSync } from "node:child_process";
import { mkdtempSync, readFileSync, readdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const repo = fileURLToPath(new URL("../..", import.meta.url));

function emitDts(args: string[], scratch: string): void {
  execFileSync(process.execPath, [
    join(repo, "scripts", "emit-dts.mjs"),
    ...args,
    "--out", scratch,
  ], { cwd: repo, stdio: "inherit" });
}

it("keeps the committed contract declarations identical to what the java emits", () => {
  const scratch = mkdtempSync(join(tmpdir(), "api-dts-"));
  emitDts(["--java-dir", repo, "--module", ":contract"], scratch);

  const emitted = readdirSync(scratch).sort();
  expect(emitted).toEqual(["api.d.ts", "api.keys.ts"]);
  for (const name of emitted) {
    expect(readFileSync(join(scratch, name), "utf8")).toBe(readFileSync(join(repo, "generated", name), "utf8"));
  }
});

it("keeps the committed engine declarations identical to what the java emits", () => {
  const scratch = mkdtempSync(join(tmpdir(), "api-dts-"));
  emitDts(["--java-dir", repo, "--module", ":teavm"], scratch);

  const emitted = readdirSync(scratch).sort();
  expect(emitted).toEqual(["engine.d.ts"]);
  for (const name of emitted) {
    expect(readFileSync(join(scratch, name), "utf8")).toBe(readFileSync(join(repo, "generated", name), "utf8"));
  }
});

it("keeps the committed engine bundle byte-identical to a fresh teavm emission", () => {
  // obfuscated=false/OptimizationLevel.NONE (teavm/build.gradle) is measured byte-stable; if that changes, revert the optimization rather than weakening this comparison.
  const scratch = mkdtempSync(join(tmpdir(), "api-dts-"));
  emitDts([
    "--java-dir", repo,
    "--module", ":teavm",
    "--ext", ".js",
    "--gradle-task", ":teavm:generateJavaScript",
    "--source-dir", join("teavm", "build", "generated", "teavm", "js"),
  ], scratch);

  const emitted = readdirSync(scratch).sort();
  expect(emitted).toEqual(["engine.js"]);
  expect(readFileSync(join(scratch, "engine.js")).equals(readFileSync(join(repo, "generated", "engine.js")))).toBe(true);
});
