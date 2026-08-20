import { execFileSync } from "node:child_process";
import { mkdtempSync, readFileSync, readdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const repo = fileURLToPath(new URL("../..", import.meta.url));

it("keeps the committed declarations identical to what the java emits", () => {
  const scratch = mkdtempSync(join(tmpdir(), "api-dts-"));
  execFileSync(process.execPath, [
    join(repo, "scripts", "emit-dts.mjs"),
    "--java-dir", join(repo, "java"),
    "--module", ":contract",
    "--out", scratch,
  ], { cwd: repo, stdio: "inherit" });

  const emitted = readdirSync(scratch).sort();
  expect(emitted).toEqual(["api.d.ts", "api.keys.ts"]);
  for (const name of emitted) {
    expect(readFileSync(join(scratch, name), "utf8")).toBe(readFileSync(join(repo, "generated", name), "utf8"));
  }
});
