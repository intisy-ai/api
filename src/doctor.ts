import { activationOrder } from "./graph.js";
import { isKnownCapability } from "./ids.js";
import { API_VERSION } from "./manifest.js";
import type { PluginManifest } from "./manifest.js";
import { validateManifest } from "./validate.js";

const UNKNOWN_PLUGIN = "(unknown plugin)";

function attributedTo(manifest: PluginManifest): string {
  return manifest.id ?? UNKNOWN_PLUGIN;
}

/** One thing worth telling a plugin author or an operator about a set of installed plugins. */
export interface DoctorFinding {
  /** `error` blocks a load, `warning` is legal but probably not what the author meant. */
  level: "error" | "warning";
  /** The plugin the finding is about. */
  pluginId: string;
  /** What was found. */
  detail: string;
  /** What to do about it. */
  fix: string;
}

/** What `plugin doctor` found. */
export interface DoctorReport {
  /** Every finding, errors first. */
  findings: DoctorFinding[];
  /** Whether the set is loadable, which is true when no finding is an error. */
  ok: boolean;
}

/**
 * Checks a set of manifests the way a host would, without loading anything.
 *
 * @remarks
 * An unresolved consume is a WARNING, not an error: providers arrive and leave at runtime, so a
 * checkout that cannot see one today may still be correct. A cycle, a duplicate id, an invalid
 * manifest, and an api floor above the host are errors, because none of them can resolve later.
 *
 * @param manifests - every manifest in the checkout or the app home
 * @param hostApi - the API major version to check floors against, this package's own by default
 */
export function analyzePlugins(manifests: PluginManifest[], hostApi: number = API_VERSION): DoctorReport {
  const findings: DoctorFinding[] = [];

  const seen = new Set<string>();
  for (const manifest of manifests) {
    const pluginId = attributedTo(manifest);

    for (const issue of validateManifest(manifest)) {
      findings.push({ level: "error", pluginId, detail: `plugin.json ${issue.path}: ${issue.message}`, fix: issue.fix });
    }

    if (manifest.api > hostApi) {
      findings.push({
        level: "error",
        pluginId,
        detail: `needs api ${manifest.api}, this host has api ${hostApi}`,
        fix: `update the app to a version that implements api ${manifest.api} or later`,
      });
    }

    if (seen.has(pluginId)) {
      findings.push({
        level: "error",
        pluginId,
        detail: `two manifests claim the id ${pluginId}`,
        fix: "rename one of the two plugins, or remove the copy that should not be installed",
      });
    }
    seen.add(pluginId);
  }

  for (const cycle of activationOrder(manifests).cycles) {
    for (const pluginId of cycle) {
      findings.push({
        level: "error",
        pluginId,
        detail: `service dependency cycle: ${cycle.join(", ")}`,
        fix: "break the cycle by having one of them use services.watch or services.get at call time instead of declaring the dependency",
      });
    }
  }

  const provided = new Set(manifests.flatMap((manifest) => manifest.services?.provides ?? []));
  const consumed = new Set(manifests.flatMap((manifest) => manifest.services?.consumes ?? []));

  for (const manifest of manifests) {
    const pluginId = attributedTo(manifest);
    for (const serviceId of manifest.services?.consumes ?? []) {
      if (provided.has(serviceId)) continue;
      findings.push({
        level: "warning",
        pluginId,
        detail: `consumes "${serviceId}"; nothing installed provides it`,
        fix: `install the plugin that provides "${serviceId}", or let the consumer carry on without it`,
      });
    }
    for (const serviceId of manifest.services?.provides ?? []) {
      if (consumed.has(serviceId)) continue;
      findings.push({
        level: "warning",
        pluginId,
        detail: `provides "${serviceId}"; nothing installed consumes it`,
        fix: "leave it if other installs use it, or drop it from services.provides",
      });
    }
    for (const capabilityId of manifest.capabilities ?? []) {
      if (isKnownCapability(capabilityId)) continue;
      findings.push({
        level: "warning",
        pluginId,
        detail: `declares capability "${capabilityId}", which this api version does not mint and every host ignores`,
        fix: "check the spelling against the capability ids this api version documents",
      });
    }
  }

  findings.sort((left, right) => rank(left) - rank(right));
  return { findings, ok: !findings.some((finding) => finding.level === "error") };
}

function rank(finding: DoctorFinding): number {
  return finding.level === "error" ? 0 : 1;
}
