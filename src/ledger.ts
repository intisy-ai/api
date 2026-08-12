import type { PluginManifest } from "./manifest.js";

/** Where a plugin stands in its lifecycle, as the host last saw it. */
export type PluginStatus = "activating" | "active" | "broken" | "stopped";

/** Everything the host observed about one plugin, which is every relationship it has. */
export interface LedgerEntry {
  /** The plugin this entry describes. */
  pluginId: string;
  /** Where the plugin stands. */
  status: PluginStatus;
  /** Capability ids the manifest declares. */
  capabilitiesDeclared: string[];
  /** Capability ids `activate` actually supplied. */
  capabilitiesProvided: string[];
  /** Service ids this plugin registered. */
  servicesProvided: string[];
  /** Service ids this plugin asked for, whether or not anything answered. */
  servicesConsumed: string[];
  /** Event topics this plugin subscribed to. */
  topics: string[];
  /** Permissions the manifest declares. */
  permissions: string[];
  /** Why the plugin is broken, when it is. */
  error?: { detail: string; fix: string };
}

/**
 * The record of every relationship that passed through a plugin context.
 *
 * @remarks
 * Kept by the host rather than assembled on demand, because a relationship is only observable at
 * the moment it is made. A dashboard's developer section, `plugin doctor`, and the quarantine UI
 * are all renderings of this one ledger.
 */
export interface PluginLedger {
  /** Every entry, as copies a caller may keep. */
  entries(): LedgerEntry[];
  /** One plugin's entry, as a copy, or `undefined` when the host never saw it. */
  entry(pluginId: string): LedgerEntry | undefined;
  /**
   * Opens an entry from a manifest, at status `activating`.
   *
   * @remarks
   * Every relationship an entry holds belongs to ONE activation, so this resets them: a plugin
   * that stops and activates again is described by what it does this time, not by the union of
   * every cycle it has ever run.
   */
  recordDeclared(manifest: PluginManifest): void;
  /** Notes that a plugin supplied a capability. */
  recordCapabilityProvided(pluginId: string, capabilityId: string): void;
  /** Notes that a plugin registered a service. */
  recordServiceProvided(pluginId: string, serviceId: string): void;
  /** Notes that a plugin asked for a service. */
  recordServiceConsumed(pluginId: string, serviceId: string): void;
  /** Notes that a plugin subscribed to a topic. */
  recordTopic(pluginId: string, topic: string): void;
  /** Moves a plugin to a status, with the error that put it there when it is broken. */
  recordStatus(pluginId: string, status: PluginStatus, error?: { detail: string; fix: string }): void;
}

/** Builds an empty {@link PluginLedger}. */
export function createPluginLedger(): PluginLedger {
  const entries = new Map<string, LedgerEntry>();

  function ensure(pluginId: string): LedgerEntry {
    const existing = entries.get(pluginId);
    if (existing) return existing;
    const fresh: LedgerEntry = {
      pluginId,
      status: "activating",
      capabilitiesDeclared: [],
      capabilitiesProvided: [],
      servicesProvided: [],
      servicesConsumed: [],
      topics: [],
      permissions: [],
    };
    entries.set(pluginId, fresh);
    return fresh;
  }

  function add(list: string[], value: string): void {
    if (!list.includes(value)) list.push(value);
  }

  function copy(entry: LedgerEntry): LedgerEntry {
    return {
      ...entry,
      capabilitiesDeclared: [...entry.capabilitiesDeclared],
      capabilitiesProvided: [...entry.capabilitiesProvided],
      servicesProvided: [...entry.servicesProvided],
      servicesConsumed: [...entry.servicesConsumed],
      topics: [...entry.topics],
      permissions: [...entry.permissions],
    };
  }

  return {
    entries: () => [...entries.values()].map(copy),
    entry: (pluginId) => {
      const entry = entries.get(pluginId);
      return entry ? copy(entry) : undefined;
    },
    recordDeclared: (manifest) => {
      const entry = ensure(manifest.id);
      entry.status = "activating";
      entry.capabilitiesDeclared = [];
      entry.capabilitiesProvided = [];
      entry.servicesProvided = [];
      entry.servicesConsumed = [];
      entry.topics = [];
      entry.permissions = [];
      delete entry.error;
      for (const id of manifest.capabilities ?? []) add(entry.capabilitiesDeclared, id);
      for (const permission of manifest.permissions ?? []) add(entry.permissions, permission);
    },
    recordCapabilityProvided: (pluginId, capabilityId) => add(ensure(pluginId).capabilitiesProvided, capabilityId),
    recordServiceProvided: (pluginId, serviceId) => add(ensure(pluginId).servicesProvided, serviceId),
    recordServiceConsumed: (pluginId, serviceId) => add(ensure(pluginId).servicesConsumed, serviceId),
    recordTopic: (pluginId, topic) => add(ensure(pluginId).topics, topic),
    recordStatus: (pluginId, status, error) => {
      const entry = ensure(pluginId);
      entry.status = status;
      if (error) entry.error = error;
      else delete entry.error;
    },
  };
}
