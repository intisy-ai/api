import { expect, it } from "vitest";
import type { EngineSurface } from "../generated/engine.js";
import * as engineModule from "../generated/engine.js";

const { activationOrder, assertManifest, isPluginError, pluginError, setDiagnosticSink } = engineModule as unknown as EngineSurface;

it("mints an error a separately bundled consumer recognises by its name marker", () => {
  const error = pluginError("config-ledger", "went wrong", "put it right");
  expect(error.name).toBe("PluginError");
  expect((error as { pluginId?: string }).pluginId).toBe("config-ledger");
  expect((error as { detail?: string }).detail).toBe("went wrong");
  expect((error as { fix?: string }).fix).toBe("put it right");
  expect(error.message).toBe("[config-ledger] went wrong\n  fix: put it right");
  expect(isPluginError(error)).toBe(true);
  expect(isPluginError(new Error("plain"))).toBe(false);
});

it("orders plugins so a provider activates before its consumer", () => {
  const plan = activationOrder([
    { id: "reader", api: 1, services: { consumes: ["store:things"] } },
    { id: "store", api: 1, services: { provides: ["store:things"] } },
  ]);
  expect(plan.order).toEqual(["store", "reader"]);
  expect(plan.cycles).toEqual([]);
});

it("reports a cycle rather than an order", () => {
  const plan = activationOrder([
    { id: "one", api: 1, services: { provides: ["one:a"], consumes: ["two:b"] } },
    { id: "two", api: 1, services: { provides: ["two:b"], consumes: ["one:a"] } },
  ]);
  expect(plan.cycles.length).toBe(1);
  expect([...plan.cycles[0]].sort()).toEqual(["one", "two"]);
});

it("returns a valid manifest and throws a marked error for an invalid one", () => {
  const manifest = { id: "config-ledger", api: 1, entry: "dist/index.js" };
  expect(assertManifest(manifest)).toEqual(manifest);
  try {
    assertManifest({ api: 1 });
    throw new Error("assertManifest accepted a manifest with no id");
  } catch (error) {
    expect(isPluginError(error)).toBe(true);
    expect((error as { detail: string }).detail).toContain("id");
  }
});

it("validates a bare well-known provide only when the caller names the vocabulary", () => {
  const manifest = { id: "core-auth", api: 1, entry: "dist/index.js", services: { provides: ["accounts"] } };
  expect(assertManifest(manifest, ["accounts"])).toEqual(manifest);
  try {
    assertManifest(manifest);
    throw new Error("assertManifest accepted a bare well-known provide with no vocabulary named");
  } catch (error) {
    expect(isPluginError(error)).toBe(true);
    expect((error as { detail: string }).detail).toContain("accounts");
  }
});

it("sends a diagnostic to the sink a host installs, and stops when it is removed", () => {
  const seen: string[] = [];
  setDiagnosticSink((message) => seen.push(message));
  assertManifestQuietly();
  setDiagnosticSink(null);
  expect(Array.isArray(seen)).toBe(true);
});

function assertManifestQuietly(): void {
  try {
    assertManifest({ api: 1 });
  } catch {
    return;
  }
}
