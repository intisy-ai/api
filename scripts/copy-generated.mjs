#!/usr/bin/env node
// Post-tsc step: tsc does not copy plain .js files, so a runtime `import("./generated/...")` would
// resolve to nothing in dist/. Copies whatever teavm-build.mjs staged under <root>/src/generated/
// (and its sourcemaps) into <root>/dist/generated/ verbatim.
//
// Generic the same way teavm-build.mjs is: --root names the package whose tsc output is being
// completed, so a build that compiles a package other than the current directory passes it.
import { existsSync, mkdirSync, copyFileSync, readdirSync } from "node:fs";
import { join } from "node:path";

const args = process.argv.slice(2);
const val = (f, d) => { const i = args.indexOf(f); return i >= 0 && args[i + 1] ? args[i + 1] : d; };
const root = val("--root", ".");

const srcDir = join(root, "src", "generated");
const outDir = join(root, "dist", "generated");

mkdirSync(outDir, { recursive: true });

if (existsSync(srcDir)) {
  for (const file of readdirSync(srcDir)) {
    if (file.endsWith(".js") || file.endsWith(".js.map")) {
      copyFileSync(join(srcDir, file), join(outDir, file));
      console.log(`copy-generated: copied ${join(srcDir, file)} -> ${join(outDir, file)}`);
    }
  }
}
