import { expect, it } from "vitest";
import { API_VERSION as GENERATED } from "../generated/api.keys.js";
import { API_VERSION } from "./manifest.js";

it("declares api major version 2", () => {
  expect(API_VERSION).toBe(2);
});

// The two constants were allowed to disagree once (1 here, 2 generated), and which floor a consumer
// got depended on whether it entered through the TypeScript root or the generated engine.
it("agrees with the version the java emits", () => {
  expect(API_VERSION).toBe(GENERATED);
});
