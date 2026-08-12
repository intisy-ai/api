import { afterEach, expect, it, vi } from "vitest";
import { createPluginHost } from "./host.js";
import type { PluginRuntime } from "./host.js";
import type { PluginManifest } from "./manifest.js";
import { setDiagnosticSink } from "./strict.js";
import type { EventBus } from "./events.js";

afterEach(() => setDiagnosticSink(null));

function runtime(): PluginRuntime {
  const listeners = new Map<string, (payload: unknown) => void>();
  const events = {
    publish: (topic: string, payload: unknown) => listeners.get(topic)?.(payload),
    subscribe: (topic: string, listener: (payload: unknown) => void) => {
      listeners.set(topic, listener);
      return () => listeners.delete(topic);
    },
  } as EventBus;
  return {
    config: { all: () => ({}), get: () => undefined, set: async () => {} },
    log: { info: () => {}, warn: () => {}, error: () => {}, debug: () => {} },
    paths: { home: "/home", repos: "/home/repos", plugin: "/home/plugin", cache: "/home/cache", config: "/home/config" },
    events,
  };
}

const SCREENS: PluginManifest = { id: "config-ledger", api: 1, entry: "dist/index.js", capabilities: ["screens"], permissions: ["config:write"] };

it("refuses only a plugin whose api floor is above the host, and says what to do", () => {
  const host = createPluginHost({ app: "claude", api: 2 });
  expect(host.supports({ id: "old", api: 1 })).toBeNull();
  expect(host.supports({ id: "old", api: 2 })).toBeNull();
  const error = host.supports({ id: "new", api: 3 });
  expect(error?.detail).toBe("needs api 3, this host has api 2");
  expect(error?.fix).toBe("update the app to a version that implements api 3 or later");
});

it("hands a plugin a context carrying its manifest, the host descriptor, and its runtime", () => {
  const host = createPluginHost({ app: "claude", api: 1, surfaces: ["tui"] });
  const context = host.contextFor(SCREENS, runtime());
  expect(context.manifest).toBe(SCREENS);
  expect(context.host).toEqual({ app: "claude", api: 1, surfaces: ["tui"] });
  expect(context.paths.home).toBe("/home");
  expect(() => context.services.register("plugin-updater:catalog", {})).toThrow(/belongs to another plugin/);
});

it("collects a provided capability under the plugin that provided it", () => {
  const host = createPluginHost({ app: "claude" });
  const screens = { screens: () => [], read: async () => ({ sources: {} }), invoke: async () => ({ ok: true }) };
  host.contextFor(SCREENS, runtime()).provide("screens", screens);
  expect(host.capability("screens")).toEqual([{ pluginId: "config-ledger", implementation: screens }]);
  expect(host.ledger.entry("config-ledger")?.capabilitiesProvided).toEqual(["screens"]);
});

it("keeps an unknown capability id but reports it as ignored, which is what catches a typo", () => {
  const seen: string[] = [];
  setDiagnosticSink((message) => seen.push(message));
  const host = createPluginHost({ app: "claude" });
  host.contextFor({ id: "wakatime-sync", api: 1, entry: "dist/index.js", capabilities: ["screns"] }, runtime()).provide("screns", {});
  expect(seen).toEqual(['ignored unknown capability "screns" from wakatime-sync']);
  expect(host.capability("screns")).toHaveLength(1);
});

it("rejects providing the same capability twice", () => {
  const host = createPluginHost({ app: "claude" });
  const context = host.contextFor(SCREENS, runtime());
  context.provide("screens", {} as never);
  expect(() => context.provide("screens", {} as never)).toThrow(/provided capability "screens" twice/);
});

it("fails activation when a declared capability was never provided", () => {
  const host = createPluginHost({ app: "claude" });
  host.contextFor(SCREENS, runtime());
  const error = host.verifyActivation(SCREENS);
  expect(error?.detail).toBe("capabilities declared but never provided: screens");
  expect(error?.fix).toBe('call ctx.provide("screens", ...) in activate, or remove it from "capabilities" in plugin.json');
});

it("fails activation when a capability was provided without being declared", () => {
  const host = createPluginHost({ app: "claude" });
  const manifest: PluginManifest = { id: "config-ledger", api: 1, entry: "dist/index.js", capabilities: [] };
  host.contextFor(manifest, runtime()).provide("screens", {} as never);
  const error = host.verifyActivation(manifest);
  expect(error?.detail).toBe("capabilities provided but never declared: screens");
  expect(error?.fix).toBe('add "screens" to "capabilities" in plugin.json');
});

it("marks a plugin active when the manifest and the activation agree", () => {
  const host = createPluginHost({ app: "claude" });
  host.contextFor(SCREENS, runtime()).provide("screens", {} as never);
  expect(host.verifyActivation(SCREENS)).toBeNull();
  expect(host.ledger.entry("config-ledger")?.status).toBe("active");
  expect(host.ledger.entry("config-ledger")?.permissions).toEqual(["config:write"]);
});

it("records the topics a plugin subscribes to", () => {
  const host = createPluginHost({ app: "claude" });
  const context = host.contextFor(SCREENS, runtime());
  context.events.subscribe("config.changed", () => {});
  expect(host.ledger.entry("config-ledger")?.topics).toEqual(["config.changed"]);
});

it("drops a released plugin's capabilities and services", () => {
  const host = createPluginHost({ app: "claude" });
  const context = host.contextFor(SCREENS, runtime());
  context.provide("screens", {} as never);
  context.services.register("config-ledger:history", {});
  host.release("config-ledger");
  expect(host.capability("screens")).toEqual([]);
  expect(host.service("config-ledger:history")).toBeUndefined();
  expect(host.ledger.entry("config-ledger")?.status).toBe("stopped");
});

it("quarantines a broken plugin with the error attributed to it", () => {
  const host = createPluginHost({ app: "claude" });
  const context = host.contextFor(SCREENS, runtime());
  context.provide("screens", {} as never);
  const error = host.verifyActivation({ ...SCREENS, capabilities: ["screens", "settings"] })!;
  host.markBroken("config-ledger", error);
  const entry = host.ledger.entry("config-ledger")!;
  expect(entry.status).toBe("broken");
  expect(entry.error).toEqual({ detail: error.detail, fix: error.fix });
  expect(host.capability("screens")).toEqual([]);
});

it("hands out ledger copies rather than its own state", () => {
  const host = createPluginHost({ app: "claude" });
  host.contextFor(SCREENS, runtime());
  const entry = host.ledger.entry("config-ledger")!;
  entry.capabilitiesDeclared.push("tampered");
  expect(host.ledger.entry("config-ledger")?.capabilitiesDeclared).toEqual(["screens"]);
});
