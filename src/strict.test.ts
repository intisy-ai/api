import { afterEach, expect, it, vi } from "vitest";
import { ignoreUnknown, isStrict, setDiagnosticSink, setStrict } from "./strict.js";

afterEach(() => {
  setStrict(null);
  setDiagnosticSink(null);
  vi.restoreAllMocks();
});

it("is off unless the environment or the host turns it on", () => {
  setStrict(null);
  expect(isStrict()).toBe(false);
  setStrict(true);
  expect(isStrict()).toBe(true);
});

it("routes every ignored unknown to the host sink when one is set", () => {
  const seen: string[] = [];
  setDiagnosticSink((message) => seen.push(message));
  ignoreUnknown("capability", "screns", "wakatime-sync");
  expect(seen).toEqual(['ignored unknown capability "screns" from wakatime-sync']);
});

it("is quiet by default and loud in strict mode", () => {
  const debug = vi.spyOn(console, "debug").mockImplementation(() => {});
  const warn = vi.spyOn(console, "warn").mockImplementation(() => {});

  setStrict(false);
  ignoreUnknown("service", "acounts", "cairn");
  expect(debug).toHaveBeenCalledTimes(1);
  expect(warn).not.toHaveBeenCalled();

  setStrict(true);
  ignoreUnknown("service", "acounts", "cairn");
  expect(warn).toHaveBeenCalledWith('[plugin-api] ignored unknown service "acounts" from cairn');
});
