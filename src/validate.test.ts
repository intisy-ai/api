import { expect, it } from "vitest";
import { isPluginError } from "./errors.js";
import { assertManifest, validateManifest } from "./validate.js";

const VALID = { id: "wakatime-sync", api: 1, entry: "dist/index.js", capabilities: ["settings"] };

it("accepts a minimal library manifest with no capabilities and no entry", () => {
  expect(validateManifest({ id: "core-ir", api: 1 })).toEqual([]);
});

it("accepts a capability-declaring manifest with an entry", () => {
  expect(validateManifest(VALID)).toEqual([]);
});

it("reports structural issues before semantic ones", () => {
  expect(validateManifest({ api: 1, capabilities: ["settings"] })).toEqual([
    { path: "id", message: 'required field "id" is missing', fix: 'use lowercase words joined by single hyphens, for example "config-ledger"' },
  ]);
});

it("requires an entry once a capability is declared", () => {
  expect(validateManifest({ id: "wakatime-sync", api: 1, capabilities: ["settings"] })).toEqual([
    {
      path: "entry",
      message: 'capabilities are declared but no entry names the module that provides them',
      fix: 'add "entry": "dist/index.js"',
    },
  ]);
});

it("rejects an entry that escapes the repo", () => {
  expect(validateManifest({ ...VALID, entry: "../other/dist/index.js" })[0]).toEqual({
    path: "entry",
    message: '"../other/dist/index.js" is not a path inside the repo',
    fix: "use a repo-relative path with no leading slash and no ..",
  });
  expect(validateManifest({ ...VALID, entry: "/abs/index.js" })[0].path).toBe("entry");
});

it("rejects a provided service id outside the plugin's namespace", () => {
  expect(validateManifest({ ...VALID, services: { provides: ["history"] } })[0]).toEqual({
    path: "services.provides[0]",
    message: '"history" is neither namespaced by this plugin nor a well-known service id',
    fix: 'rename it to "wakatime-sync:history", or use one of: accounts, routing',
  });
  expect(validateManifest({ ...VALID, services: { provides: ["config-ledger:history"] } })[0].path).toBe("services.provides[0]");
});

it("accepts a well-known bare id and an own-namespace id as provided services", () => {
  expect(validateManifest({ ...VALID, services: { provides: ["accounts", "wakatime-sync:stats"], consumes: ["routing", "config-ledger:history"] } })).toEqual([]);
});

it("rejects duplicate ids in any list", () => {
  const issues = validateManifest({ ...VALID, capabilities: ["settings", "settings"] });
  expect(issues).toEqual([
    { path: "capabilities[1]", message: '"settings" is listed twice', fix: "remove the duplicate entry" },
  ]);
});

it("throws a PluginError naming the plugin and the first fix", () => {
  try {
    assertManifest({ id: "wakatime-sync", api: 1, capabilities: ["settings"] });
    throw new Error("expected assertManifest to throw");
  } catch (error) {
    expect(isPluginError(error)).toBe(true);
    expect((error as { pluginId: string }).pluginId).toBe("wakatime-sync");
    expect((error as { detail: string }).detail).toBe('plugin.json entry: capabilities are declared but no entry names the module that provides them');
    expect((error as { fix: string }).fix).toBe('add "entry": "dist/index.js"');
  }
});

it("attributes an unidentifiable manifest to a placeholder rather than crashing", () => {
  try {
    assertManifest(null);
    throw new Error("expected assertManifest to throw");
  } catch (error) {
    expect((error as { pluginId: string }).pluginId).toBe("(unknown plugin)");
  }
});

it("returns the manifest itself when it is valid", () => {
  expect(assertManifest(VALID)).toEqual(VALID);
});
