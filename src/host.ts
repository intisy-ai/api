import type { CapabilityMap } from "./capabilities.js";
import type { PluginContext } from "./context.js";
import { PluginError } from "./errors.js";
import type { EventBus } from "./events.js";
import { isKnownCapability } from "./ids.js";
import { createPluginLedger } from "./ledger.js";
import type { PluginLedger } from "./ledger.js";
import { API_VERSION } from "./manifest.js";
import type { PluginManifest } from "./manifest.js";
import type { HostDescriptor, Logger, PluginConfig, PluginPaths } from "./runtime.js";
import { createServiceHub } from "./services.js";
import type { ServiceMap } from "./services.js";
import { ignoreUnknown } from "./strict.js";

/** One plugin's implementation of a capability, with the plugin it came from. */
export interface CapabilityRecord<T> {
  /** The plugin that provided it. */
  readonly pluginId: string;
  /** The implementation itself. */
  readonly implementation: T;
}

/** What the host supplies per plugin, which is everything a context carries that is not the host's own. */
export interface PluginRuntime {
  /** The plugin's resolved configuration. */
  readonly config: PluginConfig;
  /** The plugin's logger. */
  readonly log: Logger;
  /** The storage directories of the home the plugin runs in. */
  readonly paths: PluginPaths;
  /** The host's event bus. */
  readonly events: EventBus;
}

/** What a host says about itself when it builds its plugin host. */
export interface PluginHostOptions {
  /** The app id, for example `claude` or `opencode`. */
  readonly app: string;
  /** The API major version this host implements. Defaults to the one this package ships. */
  readonly api?: number;
  /** Surface ids this host renders, for example `tui` or `gui`. */
  readonly surfaces?: string[];
}

/**
 * The host side of the plugin system: it builds contexts, collects what plugins provide, and
 * keeps the ledger.
 *
 * @remarks
 * Nothing here branches on a plugin id, and nothing can: capability ids and service ids are the
 * only dispatch keys a host has, which is what keeps a host ignorant of every specific plugin no
 * matter how many exist.
 */
export interface PluginHost {
  /** What this host tells plugins about itself. */
  readonly descriptor: HostDescriptor;
  /** The record of every relationship that passed through a context. */
  readonly ledger: PluginLedger;
  /** `null` when this host can load the plugin, or the error explaining why it cannot. */
  supports(manifest: PluginManifest): PluginError | null;
  /** Builds the context one plugin's `activate` receives. */
  contextFor(manifest: PluginManifest, runtime: PluginRuntime): PluginContext;
  /**
   * Checks a finished activation against its manifest.
   *
   * @returns `null` when the declaration and the activation agree, marking the plugin active, or
   * the error to quarantine it with when they do not
   */
  verifyActivation(manifest: PluginManifest): PluginError | null;
  /** Every implementation of a capability, in activation order. */
  capability<K extends keyof CapabilityMap>(id: K): CapabilityRecord<CapabilityMap[K]>[];
  /** Every implementation of a capability, in activation order. */
  capability(id: string): CapabilityRecord<unknown>[];
  /** One service, or `undefined` when nothing provides it. */
  service<K extends keyof ServiceMap>(id: K): ServiceMap[K] | undefined;
  /** One service, or `undefined` when nothing provides it. */
  service(id: string): unknown;
  /** Quarantines a plugin: its capabilities, services and subscriptions go, the host stays up. */
  markBroken(pluginId: string, error: PluginError): void;
  /** Releases everything a plugin provided and every subscription it holds, on deactivation. */
  release(pluginId: string): void;
}

/** Builds the host side of the plugin system. */
export function createPluginHost(options: PluginHostOptions): PluginHost {
  const descriptor: HostDescriptor = {
    app: options.app,
    api: options.api ?? API_VERSION,
    surfaces: options.surfaces ?? [],
  };
  const ledger = createPluginLedger();
  const hub = createServiceHub({
    provided: (pluginId, serviceId) => ledger.recordServiceProvided(pluginId, serviceId),
    consumed: (pluginId, serviceId) => ledger.recordServiceConsumed(pluginId, serviceId),
  });
  const capabilities = new Map<string, CapabilityRecord<unknown>[]>();
  const disposers = new Map<string, Array<() => void>>();

  function tracked(pluginId: string, dispose: () => void): () => void {
    let done = false;
    const once = () => {
      if (done) return;
      done = true;
      dispose();
      const owned = disposers.get(pluginId);
      if (!owned) return;
      const index = owned.indexOf(once);
      if (index >= 0) owned.splice(index, 1);
      if (!owned.length) disposers.delete(pluginId);
    };
    const owned = disposers.get(pluginId) ?? [];
    owned.push(once);
    disposers.set(pluginId, owned);
    return once;
  }

  function detach(pluginId: string): void {
    for (const dispose of [...(disposers.get(pluginId) ?? [])]) dispose();
    disposers.delete(pluginId);
  }

  function provide(pluginId: string, id: string, implementation: unknown): void {
    if (!isKnownCapability(id)) ignoreUnknown("capability", id, pluginId);
    const records = capabilities.get(id) ?? [];
    if (records.some((record) => record.pluginId === pluginId)) {
      throw new PluginError(
        pluginId,
        `provided capability "${id}" twice`,
        "call ctx.provide once per capability in activate",
      );
    }
    records.push({ pluginId, implementation });
    capabilities.set(id, records);
    ledger.recordCapabilityProvided(pluginId, id);
  }

  function recordingEvents(pluginId: string, events: EventBus): EventBus {
    return {
      publish: ((topic: string, payload: unknown) => events.publish(topic, payload)) as EventBus["publish"],
      subscribe: ((topic: string, listener: (payload: unknown) => void) => {
        ledger.recordTopic(pluginId, topic);
        return tracked(pluginId, events.subscribe(topic, listener));
      }) as EventBus["subscribe"],
    };
  }

  function dropCapabilities(pluginId: string): void {
    for (const [id, records] of capabilities) {
      const kept = records.filter((record) => record.pluginId !== pluginId);
      if (kept.length) capabilities.set(id, kept);
      else capabilities.delete(id);
    }
  }

  return {
    descriptor,
    ledger,
    supports: (manifest) => {
      if (manifest.api <= descriptor.api) return null;
      return new PluginError(
        manifest.id,
        `needs api ${manifest.api}, this host has api ${descriptor.api}`,
        `update the app to a version that implements api ${manifest.api} or later`,
      );
    },
    contextFor: (manifest, runtime) => {
      ledger.recordDeclared(manifest);
      return {
        manifest,
        host: descriptor,
        config: runtime.config,
        log: runtime.log,
        paths: runtime.paths,
        services: hub.forPlugin(manifest.id),
        events: recordingEvents(manifest.id, runtime.events),
        provide: ((id: string, implementation: unknown) => provide(manifest.id, id, implementation)) as PluginContext["provide"],
      };
    },
    verifyActivation: (manifest) => {
      const declared = manifest.capabilities ?? [];
      const provided = ledger.entry(manifest.id)?.capabilitiesProvided ?? [];
      const missing = declared.filter((id) => !provided.includes(id));
      if (missing.length) {
        return new PluginError(
          manifest.id,
          `capabilities declared but never provided: ${missing.join(", ")}`,
          `call ctx.provide("${missing[0]}", ...) in activate, or remove it from "capabilities" in plugin.json`,
        );
      }
      const extra = provided.filter((id) => !declared.includes(id));
      if (extra.length) {
        return new PluginError(
          manifest.id,
          `capabilities provided but never declared: ${extra.join(", ")}`,
          `add "${extra[0]}" to "capabilities" in plugin.json`,
        );
      }
      ledger.recordStatus(manifest.id, "active");
      return null;
    },
    capability: ((id: string) => [...(capabilities.get(id) ?? [])]) as PluginHost["capability"],
    service: ((id: string) => hub.get(id)) as PluginHost["service"],
    markBroken: (pluginId, error) => {
      dropCapabilities(pluginId);
      detach(pluginId);
      hub.releasePlugin(pluginId);
      ledger.recordStatus(pluginId, "broken", { detail: error.detail, fix: error.fix });
    },
    release: (pluginId) => {
      dropCapabilities(pluginId);
      detach(pluginId);
      hub.releasePlugin(pluginId);
      ledger.recordStatus(pluginId, "stopped");
    },
  };
}
