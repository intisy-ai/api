import { expect, it, vi } from "vitest";
import { definePlugin } from "./plugin.js";
import type { PluginContext } from "./context.js";

it("returns the plugin it was given, so a class and an object literal are the same shape", async () => {
  const activate = vi.fn();
  const plugin = definePlugin({ activate, deactivate: () => {} });
  await plugin.activate({} as PluginContext);
  expect(activate).toHaveBeenCalledTimes(1);
  expect(typeof plugin.deactivate).toBe("function");
});

it("carries the optional manifest-declared hooks when they are present", () => {
  const plugin = definePlugin({
    activate: () => {},
    deactivate: () => {},
    install: () => {},
    repair: () => {},
  });
  expect(typeof plugin.install).toBe("function");
  expect(typeof plugin.repair).toBe("function");
});
