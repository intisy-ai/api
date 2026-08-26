import { expect, it } from "vitest";
import { guardNoSuppressions, suppressions } from "../testing/index.js";

it("flags a suppression directive and accepts an ordinary comment", () => {
  expect(suppressions(`// @ts-nocheck\nexport const a = 1;`)).toEqual(["// @ts-nocheck"]);
  expect(suppressions(`// @ts-ignore\nexport const a = 1;`)).toEqual(["// @ts-ignore"]);
  expect(suppressions(`/* @ts-expect-error */`)).toEqual(["/* @ts-expect-error */"]);
  expect(suppressions(`// the runtime is @ts-nocheck; these types are advisory.`)).toEqual([]);
  expect(suppressions(`export const a = 1;`)).toEqual([]);
});

guardNoSuppressions({ dir: new URL("..", import.meta.url) });
