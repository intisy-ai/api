import { mkdirSync, writeFileSync } from "node:fs";
import { dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { MANIFEST_SCHEMA } from "../dist/manifest-schema.js";

const target = fileURLToPath(new URL("../schema/plugin.schema.json", import.meta.url));
mkdirSync(dirname(target), { recursive: true });
writeFileSync(target, `${JSON.stringify(MANIFEST_SCHEMA, null, 2)}\n`, "utf8");
console.log(`wrote ${target}`);
