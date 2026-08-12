import { expect, it } from "vitest";
import { API_VERSION } from "./manifest.js";

it("declares api major version 1", () => {
  expect(API_VERSION).toBe(1);
});
