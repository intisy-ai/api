import { existsSync, readFileSync } from "node:fs";
import { join } from "node:path";
import { pluginError } from "../../generated/engine.js";

/** A manifest as read from disk, with where it came from. */
export interface LoadedManifest {
  /** The file it was read from, for reporting. */
  source: string;
  /** The parsed contents, not yet validated. */
  value: unknown;
}

/** Reads and parses one manifest file, reporting a missing or malformed file as a fixable error. */
export function readManifestFile(file: string): LoadedManifest {
  if (!existsSync(file)) {
    throw pluginError("(unknown plugin)", `no manifest at ${file}`, "add a plugin.json at the repo root, or point --dir at the repo that has one");
  }
  let text: string;
  try {
    text = readFileSync(file, "utf8");
  } catch {
    throw pluginError("(unknown plugin)", `cannot read ${file}`, "check the file's permissions");
  }
  try {
    return { source: file, value: JSON.parse(text) };
  } catch {
    throw pluginError("(unknown plugin)", `${file} is not valid JSON`, "fix the JSON syntax, for example a trailing comma or an unquoted key");
  }
}

/** Reads the `plugin.json` at the root of a checkout. */
export function readCheckout(dir: string): LoadedManifest {
  return readManifestFile(join(dir, "plugin.json"));
}
