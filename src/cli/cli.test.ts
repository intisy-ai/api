import { mkdtempSync, mkdirSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { expect, it } from "vitest";
import { run } from "./main.js";
import { readCheckout, readHome } from "./read.js";

function tempDir(): string {
  return mkdtempSync(join(tmpdir(), "intisy-api-cli-"));
}

function write(dir: string, name: string, value: unknown): void {
  writeFileSync(join(dir, name), JSON.stringify(value), "utf8");
}

function collect(): { io: { out(line: string): void; err(line: string): void }; out: string[]; err: string[] } {
  const out: string[] = [];
  const err: string[] = [];
  return { io: { out: (line) => out.push(line), err: (line) => err.push(line) }, out, err };
}

it("reads a checkout's manifest", () => {
  const dir = tempDir();
  write(dir, "plugin.json", { id: "wakatime-sync", api: 1 });
  expect(readCheckout(dir).value).toEqual({ id: "wakatime-sync", api: 1 });
});

it("explains a missing manifest instead of throwing a stack trace at the user", () => {
  const dir = tempDir();
  expect(() => readCheckout(dir)).toThrow(/no manifest at/);
  try {
    readCheckout(dir);
  } catch (error) {
    expect((error as { fix: string }).fix).toBe("add a plugin.json at the repo root, or point --dir at the repo that has one");
  }
});

it("explains unparsable JSON", () => {
  const dir = tempDir();
  writeFileSync(join(dir, "plugin.json"), "{ id: 'nope' }", "utf8");
  expect(() => readCheckout(dir)).toThrow(/is not valid JSON/);
});

it("reads every manifest sidecar in an app home, and none when the directory is absent", () => {
  const home = tempDir();
  mkdirSync(join(home, "plugin"));
  write(join(home, "plugin"), "b.json", { id: "b", api: 1 });
  write(join(home, "plugin"), "a.json", { id: "a", api: 1 });
  writeFileSync(join(home, "plugin", "a.js"), "// bundle", "utf8");
  expect(readHome(home).map((loaded) => (loaded.value as { id: string }).id)).toEqual(["a", "b"]);
  expect(readHome(tempDir())).toEqual([]);
});

it("validates a good manifest and exits 0", () => {
  const dir = tempDir();
  write(dir, "plugin.json", { id: "wakatime-sync", api: 1 });
  const { io, out } = collect();
  expect(run(["validate", "--dir", dir], io)).toBe(0);
  expect(out[0]).toBe("ok  wakatime-sync (api 1)");
});

it("reports each problem with its fix and exits 1", () => {
  const dir = tempDir();
  write(dir, "plugin.json", { id: "wakatime-sync", api: 1, capabilities: ["settings"] });
  const { io, err } = collect();
  expect(run(["validate", "--dir", dir], io)).toBe(1);
  expect(err).toEqual([
    "wakatime-sync: 1 problem",
    "  error  entry: capabilities are declared but no entry names the module that provides them",
    '    fix: add "entry": "dist/index.js"',
  ]);
});

it("doctors an app home and exits 0 on warnings alone", () => {
  const home = tempDir();
  mkdirSync(join(home, "plugin"));
  write(join(home, "plugin"), "cairn.json", { id: "cairn", api: 1, entry: "dist/index.js", services: { consumes: ["config-ledger:history"] } });
  const { io, out } = collect();
  expect(run(["doctor", "--home", home], io)).toBe(0);
  expect(out).toEqual([
    "1 plugin checked",
    '  warning  cairn: consumes "config-ledger:history"; nothing installed provides it',
    '    fix: install the plugin that provides "config-ledger:history", or let the consumer carry on without it',
  ]);
});

it("doctors against a host api floor and exits 1 on an error", () => {
  const dir = tempDir();
  write(dir, "plugin.json", { id: "future", api: 3 });
  const { io, err } = collect();
  expect(run(["doctor", "--dir", dir, "--api", "2"], io)).toBe(1);
  expect(err).toContain("  error  future: needs api 3, this host has api 2");
});

it("prints usage and exits 2 for an unknown command", () => {
  const { io, err } = collect();
  expect(run(["frobnicate"], io)).toBe(2);
  expect(err[0]).toBe("unknown command: frobnicate");
});

it("prints usage and exits 0 when asked for help", () => {
  const { io, out } = collect();
  expect(run(["--help"], io)).toBe(0);
  expect(out[0]).toBe("usage:");
});
