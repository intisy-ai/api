import { expect, it } from "vitest";
import { CAPABILITY_IDS, isKnownCapability, isWellKnownService, mayRegister, serviceOwner, WELL_KNOWN_SERVICES } from "./ids.js";

it("mints exactly the ten v1 capability ids", () => {
  expect([...CAPABILITY_IDS]).toEqual([
    "provider",
    "front-door",
    "screens",
    "settings",
    "commands",
    "plugin-management",
    "cross-app-sync",
    "custom-endpoints",
    "config-history",
    "marketplace-source",
  ]);
  expect(isKnownCapability("screens")).toBe(true);
  expect(isKnownCapability("screns")).toBe(false);
});

it("declares the well-known bare service ids", () => {
  expect([...WELL_KNOWN_SERVICES]).toEqual(["accounts", "routing", "activity"]);
  expect(isWellKnownService("accounts")).toBe(true);
  expect(isWellKnownService("activity")).toBe(true);
  expect(isWellKnownService("config-ledger:history")).toBe(false);
});

it("reads the owner out of a namespaced service id", () => {
  expect(serviceOwner("config-ledger:history")).toBe("config-ledger");
  expect(serviceOwner("accounts")).toBe(null);
  expect(serviceOwner("config-ledger:")).toBe(null);
});

it("lets a plugin register its own namespace and the well-known ids, and nothing else", () => {
  expect(mayRegister("config-ledger", "config-ledger:history")).toBe(true);
  expect(mayRegister("config-ledger", "accounts")).toBe(true);
  expect(mayRegister("config-ledger", "plugin-updater:catalog")).toBe(false);
  expect(mayRegister("config-ledger", "history")).toBe(false);
});
