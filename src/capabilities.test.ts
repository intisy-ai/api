import { expect, it } from "vitest";
import type { CapabilityMap } from "./capabilities.js";
import { CAPABILITY_IDS } from "./ids.js";
import type { ScreenSpec } from "./capability-types.js";

const TYPED: Record<keyof CapabilityMap, true> = {
  provider: true,
  "front-door": true,
  screens: true,
  settings: true,
  commands: true,
  "plugin-management": true,
  "cross-app-sync": true,
  "custom-endpoints": true,
  "config-history": true,
  "marketplace-source": true,
};

it("types every minted capability id and nothing else", () => {
  expect(Object.keys(TYPED).sort()).toEqual([...CAPABILITY_IDS].sort());
});

it("accepts a screens implementation shaped like the one config-ledger already answers with", () => {
  const screen: ScreenSpec = {
    id: "config-ledger",
    label: "Config",
    layout: { kind: "stack", children: [{ kind: "summary", source: "summary" }] },
  };
  const implementation: CapabilityMap["screens"] = {
    screens: () => [screen],
    read: async () => ({ sources: { summary: [], pending: [], history: [], profiles: [] } }),
    invoke: async () => ({ ok: true, refresh: true }),
  };
  expect(implementation.screens()).toEqual([screen]);
});

it("keeps an unknown node kind and unknown node props legal", () => {
  const node: ScreenSpec["layout"] = { kind: "kind-from-a-later-host", children: [], somethingNew: 42 };
  expect(node.somethingNew).toBe(42);
});
