import { expect, it } from "vitest";
import { activationOrder } from "./graph.js";
import type { PluginManifest } from "./manifest.js";

function plugin(id: string, provides: string[] = [], consumes: string[] = []): PluginManifest {
  return { id, api: 1, entry: "dist/index.js", services: { provides, consumes } };
}

it("activates a provider before its consumer", () => {
  const plan = activationOrder([
    plugin("cairn", [], ["config-ledger:history"]),
    plugin("config-ledger", ["config-ledger:history"]),
  ]);
  expect(plan).toEqual({ order: ["config-ledger", "cairn"], cycles: [] });
});

it("keeps input order among plugins that do not depend on each other", () => {
  const plan = activationOrder([plugin("b"), plugin("a"), plugin("c")]);
  expect(plan.order).toEqual(["b", "a", "c"]);
});

it("draws no edge for a consumed service nobody provides", () => {
  const plan = activationOrder([plugin("cairn", [], ["nothing:here"]), plugin("wakatime-sync")]);
  expect(plan).toEqual({ order: ["cairn", "wakatime-sync"], cycles: [] });
});

it("ignores a plugin consuming a service it provides itself", () => {
  const plan = activationOrder([plugin("core-auth", ["accounts"], ["accounts"])]);
  expect(plan).toEqual({ order: ["core-auth"], cycles: [] });
});

it("reports a cycle and leaves its members out of the order", () => {
  const plan = activationOrder([
    plugin("a", ["a:one"], ["b:two"]),
    plugin("b", ["b:two"], ["a:one"]),
    plugin("c"),
  ]);
  expect(plan.cycles).toEqual([["a", "b"]]);
  expect(plan.order).toEqual(["c"]);
});

it("activates a plugin that depends on a cycle member rather than stalling behind it", () => {
  const plan = activationOrder([
    plugin("a", ["a:one"], ["b:two"]),
    plugin("b", ["b:two"], ["a:one"]),
    plugin("d", [], ["a:one"]),
  ]);
  expect(plan.order).toEqual(["d"]);
  expect(plan.cycles).toEqual([["a", "b"]]);
});
