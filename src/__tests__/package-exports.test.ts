import { existsSync, readFileSync } from "node:fs";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { expect, it } from "vitest";

const repo = fileURLToPath(new URL("../..", import.meta.url));

interface Exports {
  [subpath: string]: { types?: string; default?: string };
}

function exportsMap(): Exports {
  const pkg = JSON.parse(readFileSync(join(repo, "package.json"), "utf8")) as { exports: Exports };
  return pkg.exports;
}

it("points every exported subpath at a file that exists", () => {
  for (const [subpath, conditions] of Object.entries(exportsMap())) {
    for (const [condition, target] of Object.entries(conditions)) {
      if (!target) continue;
      expect(existsSync(join(repo, target)), `${subpath} ${condition} -> ${target}`).toBe(true);
    }
  }
});

it("serves the generated contract declarations as a types-only subpath", () => {
  // Types-only on purpose: the generated contract is declarations plus typed keys emitted as
  // TypeScript source, so there is no JavaScript to import at run time. A `default` here would name
  // a file that does not exist, and a runtime import SHOULD fail rather than resolve to something.
  const contract = exportsMap()["./contract"];
  expect(contract).toEqual({ types: "./generated/api.d.ts" });
});
