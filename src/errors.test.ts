import { expect, it } from "vitest";
import { isPluginError, PluginError } from "./errors.js";

it("names the plugin, the problem and the fix in the message", () => {
  const error = new PluginError("wakatime-sync", 'declares capability "screens" but never provided it', 'call ctx.provide("screens", ...) in activate');
  expect(error.message).toBe('[wakatime-sync] declares capability "screens" but never provided it\n  fix: call ctx.provide("screens", ...) in activate');
  expect(error.pluginId).toBe("wakatime-sync");
  expect(error.detail).toBe('declares capability "screens" but never provided it');
  expect(error.fix).toBe('call ctx.provide("screens", ...) in activate');
});

it("is recognised across bundle boundaries by its name marker, not instanceof", () => {
  const foreign = { name: "PluginError", message: "x", pluginId: "p", detail: "d", fix: "f" };
  expect(isPluginError(foreign)).toBe(true);
  expect(isPluginError(new PluginError("p", "d", "f"))).toBe(true);
  expect(isPluginError(new Error("plain"))).toBe(false);
  expect(isPluginError(null)).toBe(false);
});
