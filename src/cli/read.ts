import { existsSync, readdirSync, readFileSync } from "node:fs";
import { join } from "node:path";
import { isPluginError, PluginError } from "../errors.js";

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
    throw new PluginError("(unknown plugin)", `no manifest at ${file}`, "add a plugin.json at the repo root, or point --dir at the repo that has one");
  }
  let text: string;
  try {
    text = readFileSync(file, "utf8");
  } catch {
    throw new PluginError("(unknown plugin)", `cannot read ${file}`, "check the file's permissions");
  }
  try {
    return { source: file, value: JSON.parse(text) };
  } catch {
    throw new PluginError("(unknown plugin)", `${file} is not valid JSON`, "fix the JSON syntax, for example a trailing comma or an unquoted key");
  }
}

/** Reads the `plugin.json` at the root of a checkout. */
export function readCheckout(dir: string): LoadedManifest {
  return readManifestFile(join(dir, "plugin.json"));
}

/** A manifest sidecar that could not be read, with the error explaining why. */
export interface FailedManifest {
  /** The file that could not be read. */
  source: string;
  /** Why it could not be read, naming the fix. */
  error: PluginError;
}

/** Every manifest sidecar in an app home, each one either loaded or failed. */
export interface HomeManifests {
  /** The sidecars that parsed. */
  loaded: LoadedManifest[];
  /** The sidecars that did not, one entry each. */
  failed: FailedManifest[];
}

/**
 * Reads every manifest sidecar deployed into an app home.
 *
 * @remarks
 * Deploy writes each plugin's manifest beside its bundle, so a host answers identity and
 * capability questions from disk without importing anything. Each sidecar is read on its own: one
 * unreadable file is reported as a failure rather than hiding every other plugin in the home,
 * which is the whole point of a command that lists what is wrong. An app home with nothing
 * deployed is empty rather than an error.
 */
export function readHome(home: string): HomeManifests {
  const dir = join(home, "plugin");
  if (!existsSync(dir)) return { loaded: [], failed: [] };
  const loaded: LoadedManifest[] = [];
  const failed: FailedManifest[] = [];
  for (const name of readdirSync(dir).filter((entry) => entry.endsWith(".json")).sort()) {
    const source = join(dir, name);
    try {
      loaded.push(readManifestFile(source));
    } catch (error) {
      if (!isPluginError(error)) throw error;
      failed.push({ source, error });
    }
  }
  return { loaded, failed };
}
