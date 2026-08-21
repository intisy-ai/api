import { mkdirSync, writeFileSync } from "node:fs";
import { dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { manifestSchema } from "../generated/engine.js";

const target = fileURLToPath(new URL("../schema/plugin.schema.json", import.meta.url));
mkdirSync(dirname(target), { recursive: true });
writeFileSync(target, `${JSON.stringify(manifestSchema(), null, 2)}\n`, "utf8");
console.log(`wrote ${target}`);
