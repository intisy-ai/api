import { expect, it } from "vitest";
import { ECOSYSTEM_TOPICS } from "./events.js";
import type { EventMap } from "./events.js";

const TYPED: Record<keyof EventMap, true> = {
  notification: true,
  "proxy.status": true,
  "account.rate_limited": true,
  "config.changed": true,
  "config.snapshot": true,
  "config.profile_changed": true,
  "plugin.progress": true,
  "plugin.installed": true,
  "sync.completed": true,
};

it("lists exactly the ecosystem-owned bare topics it types", () => {
  expect([...ECOSYSTEM_TOPICS].sort()).toEqual(Object.keys(TYPED).sort());
});
