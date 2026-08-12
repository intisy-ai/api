import { expect, it, vi } from "vitest";
import { isPluginError } from "./errors.js";
import { createServiceHub } from "./services.js";

it("round-trips a namespaced registration through get", () => {
  const hub = createServiceHub();
  const ledger = hub.forPlugin("config-ledger");
  const history = { list: () => [] };
  ledger.register("config-ledger:history", history);
  expect(hub.forPlugin("cairn").get("config-ledger:history")).toBe(history);
  expect(hub.ids()).toEqual(["config-ledger:history"]);
});

it("returns undefined for a service nobody registered", () => {
  expect(createServiceHub().forPlugin("cairn").get("config-ledger:history")).toBeUndefined();
});

it("rejects a registration outside the caller's namespace, naming the fix", () => {
  const registry = createServiceHub().forPlugin("wakatime-sync");
  try {
    registry.register("config-ledger:history", {});
    throw new Error("expected register to throw");
  } catch (error) {
    expect(isPluginError(error)).toBe(true);
    expect((error as { detail: string }).detail).toBe('cannot register service "config-ledger:history", which belongs to another plugin');
    expect((error as { fix: string }).fix).toBe('namespace it as "wakatime-sync:history", or register one of the well-known ids: accounts, routing');
  }
});

it("allows a well-known bare id from any plugin", () => {
  const hub = createServiceHub();
  const store = { list: () => [] };
  hub.forPlugin("core-auth").register("accounts", store);
  expect(hub.get("accounts")).toBe(store);
});

it("rejects a second registration of the same id, naming both plugins", () => {
  const hub = createServiceHub();
  hub.forPlugin("core-auth").register("accounts", {});
  try {
    hub.forPlugin("stub-auth").register("accounts", {});
    throw new Error("expected the second register to throw");
  } catch (error) {
    expect((error as { detail: string }).detail).toBe('service "accounts" is already registered by core-auth');
    expect((error as { pluginId: string }).pluginId).toBe("stub-auth");
  }
});

it("resolves want when the provider registers later", async () => {
  const hub = createServiceHub();
  const pending = hub.forPlugin("cairn").want("config-ledger:history");
  const history = { list: () => [] };
  hub.forPlugin("config-ledger").register("config-ledger:history", history);
  await expect(pending).resolves.toBe(history);
});

it("resolves want immediately when the service is already there", async () => {
  const hub = createServiceHub();
  const history = {};
  hub.forPlugin("config-ledger").register("config-ledger:history", history);
  await expect(hub.forPlugin("cairn").want("config-ledger:history")).resolves.toBe(history);
});

it("rejects want after its timeout with an error that teaches", async () => {
  const hub = createServiceHub();
  await expect(hub.forPlugin("cairn").want("config-ledger:history", { timeoutMs: 5 })).rejects.toMatchObject({
    pluginId: "cairn",
    detail: 'waited 5ms for service "config-ledger:history" and nothing registered it',
    fix: 'install a plugin that provides "config-ledger:history", or use get() and carry on without it',
  });
});

it("reports registration and unregistration to a watcher until it is disposed", () => {
  const hub = createServiceHub();
  const seen: [unknown, string][] = [];
  const stop = hub.forPlugin("cairn").watch("accounts", (service, event) => seen.push([service, event]));
  const store = {};
  const release = hub.forPlugin("core-auth").register("accounts", store);
  release();
  stop();
  hub.forPlugin("core-auth").register("accounts", {});
  expect(seen).toEqual([[store, "register"], [undefined, "unregister"]]);
});

it("drops every service a released plugin registered", () => {
  const hub = createServiceHub();
  const registry = hub.forPlugin("config-ledger");
  registry.register("config-ledger:history", {});
  registry.register("config-ledger:profiles", {});
  hub.releasePlugin("config-ledger");
  expect(hub.ids()).toEqual([]);
});

it("records what each plugin provided and consumed", () => {
  const provided = vi.fn();
  const consumed = vi.fn();
  const hub = createServiceHub({ provided, consumed });
  hub.forPlugin("config-ledger").register("config-ledger:history", {});
  hub.forPlugin("cairn").get("config-ledger:history");
  hub.forPlugin("cairn").watch("accounts", () => {});
  expect(provided).toHaveBeenCalledWith("config-ledger", "config-ledger:history");
  expect(consumed).toHaveBeenCalledWith("cairn", "config-ledger:history");
  expect(consumed).toHaveBeenCalledWith("cairn", "accounts");
});
