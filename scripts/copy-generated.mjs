#!/usr/bin/env node
// Post-tsc step: tsc does not copy plain .js files, so a runtime `import("./generated/...")` would
// resolve to nothing in dist/. Copies whatever teavm-build.mjs staged under a generated directory
// (and its sourcemaps) into the matching directory under dist/ verbatim.
//
// Generic the same way teavm-build.mjs is: --root names the package whose tsc output is being
// completed, so a build that compiles a package other than the current directory passes it. A
// package publishing several modules from one tree keeps a generated directory per module, so --src
// and --out name that pair explicitly; they default to the single root-level pair.
import { existsSync, mkdirSync, copyFileSync, readdirSync } from "node:fs";
import { join } from "node:path";

const args = process.argv.slice(2);
const val = (f, d) => { const i = args.indexOf(f); return i >= 0 && args[i + 1] ? args[i + 1] : d; };
const root = val("--root", ".");

const srcDir = join(root, val("--src", join("src", "generated")));
const outDir = join(root, val("--out", join("dist", "generated")));

mkdirSync(outDir, { recursive: true });

if (existsSync(srcDir)) {
  for (const file of readdirSync(srcDir)) {
    if (file.endsWith(".js") || file.endsWith(".js.map")) {
      copyFileSync(join(srcDir, file), join(outDir, file));
      console.log(`copy-generated: copied ${join(srcDir, file)} -> ${join(outDir, file)}`);
    }
  }
}
