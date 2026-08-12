import { expect, it } from "vitest";
import { analyzePlugins } from "./doctor.js";
import type { PluginManifest } from "./manifest.js";

function plugin(id: string, extra: Partial<PluginManifest> = {}): PluginManifest {
  return { id, api: 1, entry: "dist/index.js", ...extra };
}

it("passes a healthy set with nothing to say", () => {
  const report = analyzePlugins([
    plugin("config-ledger", { capabilities: ["config-history"], services: { provides: ["config-ledger:history"] } }),
    plugin("cairn", { services: { consumes: ["config-ledger:history"] } }),
  ]);
  expect(report).toEqual({ findings: [], ok: true });
});

it("reports an invalid manifest as an error carrying the validator's own fix", () => {
  const report = analyzePlugins([plugin("wakatime-sync", { capabilities: ["settings"], entry: undefined })]);
  expect(report.ok).toBe(false);
  expect(report.findings[0]).toEqual({
    level: "error",
    pluginId: "wakatime-sync",
    detail: "plugin.json entry: capabilities are declared but no entry names the module that provides them",
    fix: 'add "entry": "dist/index.js"',
  });
});

it("reports an api floor above the host", () => {
  const report = analyzePlugins([plugin("future", { api: 3 })], 2);
  expect(report.findings[0]).toEqual({
    level: "error",
    pluginId: "future",
    detail: "needs api 3, this host has api 2",
    fix: "update the app to a version that implements api 3 or later",
  });
});

it("reports two plugins claiming the same id", () => {
  const report = analyzePlugins([plugin("twin"), plugin("twin")]);
  expect(report.findings).toEqual([
    { level: "error", pluginId: "twin", detail: "two manifests claim the id twin", fix: "rename one of the two plugins, or remove the copy that should not be installed" },
  ]);
});

it("warns about a consumed service nothing provides, without failing", () => {
  const report = analyzePlugins([plugin("cairn", { services: { consumes: ["config-ledger:history"] } })]);
  expect(report.ok).toBe(true);
  expect(report.findings).toEqual([
    {
      level: "warning",
      pluginId: "cairn",
      detail: 'consumes "config-ledger:history"; nothing installed provides it',
      fix: 'install the plugin that provides "config-ledger:history", or let the consumer carry on without it',
    },
  ]);
});

it("warns about a provided service nothing consumes", () => {
  const report = analyzePlugins([plugin("config-ledger", { services: { provides: ["config-ledger:history"] } })]);
  expect(report.findings).toEqual([
    {
      level: "warning",
      pluginId: "config-ledger",
      detail: 'provides "config-ledger:history"; nothing installed consumes it',
      fix: "leave it if other installs use it, or drop it from services.provides",
    },
  ]);
});

it("warns about a capability id this api version does not mint", () => {
  const report = analyzePlugins([plugin("wakatime-sync", { capabilities: ["screns"] })]);
  expect(report.findings).toEqual([
    {
      level: "warning",
      pluginId: "wakatime-sync",
      detail: 'declares capability "screns", which this api version does not mint and every host ignores',
      fix: "check the spelling against the capability ids this api version documents",
    },
  ]);
});

it("reports a dependency cycle as an error naming its members", () => {
  const report = analyzePlugins([
    plugin("a", { services: { provides: ["a:one"], consumes: ["b:two"] } }),
    plugin("b", { services: { provides: ["b:two"], consumes: ["a:one"] } }),
  ]);
  expect(report.ok).toBe(false);
  expect(report.findings).toContainEqual({
    level: "error",
    pluginId: "a",
    detail: "service dependency cycle: a, b",
    fix: "break the cycle by having one of them use services.watch or services.get at call time instead of declaring the dependency",
  });
});

it("attributes a finding to a placeholder when the manifest carries no id", () => {
  const report = analyzePlugins([{ api: 1, entry: "dist/index.js", services: { consumes: ["config-ledger:history"] } } as PluginManifest]);
  expect(report.ok).toBe(false);
  expect(report.findings.map((finding) => finding.pluginId)).toEqual(["(unknown plugin)"]);
});

it("does not cross-check a manifest whose structure is already wrong", () => {
  const report = analyzePlugins([{ id: "x", api: 1, capabilities: "screens" } as unknown as PluginManifest]);
  expect(report.findings).toHaveLength(1);
  expect(report.findings[0].detail).toBe("plugin.json capabilities: expected array, got string");
});

it("reports a service two plugins both provide, naming each of them", () => {
  const report = analyzePlugins([
    plugin("core-auth", { services: { provides: ["accounts"] } }),
    plugin("stub-auth", { services: { provides: ["accounts"] } }),
  ]);
  expect(report.ok).toBe(false);
  expect(report.findings.filter((finding) => finding.level === "error")).toEqual([
    {
      level: "error",
      pluginId: "core-auth",
      detail: 'service "accounts" is provided by more than one plugin: core-auth, stub-auth',
      fix: "disable one of them, or have each provide its own namespaced id so consumers can ask for the one they want",
    },
    {
      level: "error",
      pluginId: "stub-auth",
      detail: 'service "accounts" is provided by more than one plugin: core-auth, stub-auth',
      fix: "disable one of them, or have each provide its own namespaced id so consumers can ask for the one they want",
    },
  ]);
});
