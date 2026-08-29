import { expect, it } from "vitest";
import { guardNoSuppressions, suppressions } from "../testing/index.js";

it("flags a suppression directive and accepts an ordinary comment", () => {
  expect(suppressions(`// @ts-nocheck\nexport const a = 1;`)).toEqual(["// @ts-nocheck"]);
  expect(suppressions(`// @ts-ignore\nexport const a = 1;`)).toEqual(["// @ts-ignore"]);
  expect(suppressions(`/* @ts-expect-error */`)).toEqual(["/* @ts-expect-error */"]);
  expect(suppressions(`// the runtime is @ts-nocheck; these types are advisory.`)).toEqual([]);
  expect(suppressions(`export const a = 1;`)).toEqual([]);
});

it("flags a directive that carries a reason, whatever comment shape it opens", () => {
  expect(suppressions(`// @ts-ignore -- legacy call site`)).toEqual(["// @ts-ignore -- legacy call site"]);
  expect(suppressions(`// @ts-expect-error: the overload is wrong`)).toEqual(["// @ts-expect-error: the overload is wrong"]);
  expect(suppressions(`/* @ts-expect-error keep */`)).toEqual(["/* @ts-expect-error keep */"]);
  expect(suppressions(`/**\n * @ts-nocheck\n */`)).toEqual(["* @ts-nocheck"]);
  expect(suppressions(` * the file was @ts-nocheck before this landed`)).toEqual([]);
});

guardNoSuppressions({ dir: new URL("..", import.meta.url) });
