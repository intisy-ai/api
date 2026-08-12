import { PluginError } from "./errors.js";
import { mayRegister, WELL_KNOWN_SERVICES } from "./ids.js";

/**
 * The account store contract.
 *
 * @remarks
 * Declared here as an empty interface and filled in by the plugin that owns the account store,
 * through declaration merging, so this package names the contract without implementing it.
 */
export interface AccountsService {}

/** The routing contract, filled in by the routing engine through declaration merging. */
export interface RoutingService {}

/**
 * Every service id paired with the contract behind it.
 *
 * @remarks
 * A service is something ANOTHER PLUGIN consumes, so plugins extend this map by declaration
 * merging rather than editing it here:
 *
 * @example
 * ```ts
 * declare module "@intisy-ai/api" {
 *   interface ServiceMap { "config-ledger:history": ConfigHistoryCapability }
 * }
 * ```
 */
export interface ServiceMap {
  /** The account store. */
  accounts: AccountsService;
  /** The routing engine. */
  routing: RoutingService;
}

/** What happened to a watched service. */
export type ServiceEvent = "register" | "unregister";

/** Called when a watched service arrives or goes away, with `undefined` on the way out. */
export type ServiceListener<T> = (service: T | undefined, event: ServiceEvent) => void;

/** How long to wait for a service that has not arrived yet. */
export interface WantOptions {
  /** Milliseconds to wait before rejecting. Waits indefinitely when absent. */
  timeoutMs?: number;
}

/**
 * One plugin's view of the service registry.
 *
 * @remarks
 * `get` is for optional use, `want` for an awaited arrival, and `watch` for churn. That triple
 * is what lets a plugin be installed, enabled, disabled, and updated at runtime while its
 * consumers keep working, at any ecosystem size.
 */
export interface ServiceRegistry {
  /** The service now, or `undefined` when nothing provides it. */
  get<K extends keyof ServiceMap>(id: K): ServiceMap[K] | undefined;
  /** The service now, or `undefined` when nothing provides it. */
  get(id: string): unknown;
  /** Resolves when the service is registered, now or later. */
  want<K extends keyof ServiceMap>(id: K, options?: WantOptions): Promise<ServiceMap[K]>;
  /** Resolves when the service is registered, now or later. */
  want(id: string, options?: WantOptions): Promise<unknown>;
  /** Reports every registration and unregistration of the id. Returns a disposer. */
  watch<K extends keyof ServiceMap>(id: K, listener: ServiceListener<ServiceMap[K]>): () => void;
  /** Reports every registration and unregistration of the id. Returns a disposer. */
  watch(id: string, listener: ServiceListener<unknown>): () => void;
  /** Registers one of this plugin's own services. Returns a disposer that unregisters it. */
  register<K extends keyof ServiceMap>(id: K, service: ServiceMap[K]): () => void;
  /** Registers one of this plugin's own services. Returns a disposer that unregisters it. */
  register(id: string, service: unknown): () => void;
  /** Every service id registered right now. */
  ids(): string[];
}

/** Where the host records who provides and who consumes what. */
export interface ServiceRecorder {
  /** A plugin registered a service. */
  provided(pluginId: string, serviceId: string): void;
  /** A plugin asked for a service, whether or not it was there. */
  consumed(pluginId: string, serviceId: string): void;
}

/** The host's own handle on the registry every plugin shares. */
export interface ServiceHub {
  /** A registry scoped to one plugin, which is what stamps and enforces its namespace. */
  forPlugin(pluginId: string): ServiceRegistry;
  /** The service now, or `undefined`, without attributing the read to a plugin. */
  get(id: string): unknown;
  /** Every service id registered right now. */
  ids(): string[];
  /** Unregisters everything a plugin registered, on deactivation or quarantine. */
  releasePlugin(pluginId: string): void;
}

interface Entry {
  pluginId: string;
  service: unknown;
}

interface Waiter {
  resolve: (service: unknown) => void;
  reject: (error: unknown) => void;
  timer: ReturnType<typeof setTimeout> | null;
}

/**
 * Builds the live service registry a host shares between every plugin it loads.
 *
 * @param recorder - where to report provisions and consumptions, for the introspection ledger
 */
export function createServiceHub(recorder: Partial<ServiceRecorder> = {}): ServiceHub {
  const entries = new Map<string, Entry>();
  const waiters = new Map<string, Set<Waiter>>();
  const watchers = new Map<string, Set<ServiceListener<unknown>>>();

  function notify(id: string, service: unknown, event: ServiceEvent): void {
    for (const listener of watchers.get(id) ?? []) listener(service, event);
  }

  function unregister(pluginId: string, id: string): void {
    const entry = entries.get(id);
    if (!entry || entry.pluginId !== pluginId) return;
    entries.delete(id);
    notify(id, undefined, "unregister");
  }

  function register(pluginId: string, id: string, service: unknown): () => void {
    if (!mayRegister(pluginId, id)) {
      const name = id.split(":").pop() ?? id;
      throw new PluginError(
        pluginId,
        `cannot register service "${id}", which belongs to another plugin`,
        `namespace it as "${pluginId}:${name}", or register one of the well-known ids: ${WELL_KNOWN_SERVICES.join(", ")}`,
      );
    }
    const existing = entries.get(id);
    if (existing) {
      throw new PluginError(
        pluginId,
        `service "${id}" is already registered by ${existing.pluginId}`,
        "disable one of the two plugins, or have each register its own namespaced id so consumers can ask for the one they want",
      );
    }
    entries.set(id, { pluginId, service });
    recorder.provided?.(pluginId, id);
    for (const waiter of waiters.get(id) ?? []) {
      if (waiter.timer) clearTimeout(waiter.timer);
      waiter.resolve(service);
    }
    waiters.delete(id);
    notify(id, service, "register");
    return () => unregister(pluginId, id);
  }

  function want(pluginId: string, id: string, options: WantOptions = {}): Promise<unknown> {
    recorder.consumed?.(pluginId, id);
    const entry = entries.get(id);
    if (entry) return Promise.resolve(entry.service);
    return new Promise((resolve, reject) => {
      const waiter: Waiter = { resolve, reject, timer: null };
      if (options.timeoutMs !== undefined) {
        waiter.timer = setTimeout(() => {
          waiters.get(id)?.delete(waiter);
          reject(new PluginError(
            pluginId,
            `waited ${options.timeoutMs}ms for service "${id}" and nothing registered it`,
            `install a plugin that provides "${id}", or use get() and carry on without it`,
          ));
        }, options.timeoutMs);
      }
      const set = waiters.get(id) ?? new Set<Waiter>();
      set.add(waiter);
      waiters.set(id, set);
    });
  }

  function watch(pluginId: string, id: string, listener: ServiceListener<unknown>): () => void {
    recorder.consumed?.(pluginId, id);
    const set = watchers.get(id) ?? new Set<ServiceListener<unknown>>();
    set.add(listener);
    watchers.set(id, set);
    return () => {
      set.delete(listener);
    };
  }

  function forPlugin(pluginId: string): ServiceRegistry {
    const registry: ServiceRegistry = {
      get: ((id: string) => {
        recorder.consumed?.(pluginId, id);
        return entries.get(id)?.service;
      }) as ServiceRegistry["get"],
      want: ((id: string, options?: WantOptions) => want(pluginId, id, options)) as ServiceRegistry["want"],
      watch: ((id: string, listener: ServiceListener<unknown>) => watch(pluginId, id, listener)) as ServiceRegistry["watch"],
      register: ((id: string, service: unknown) => register(pluginId, id, service)) as ServiceRegistry["register"],
      ids: () => [...entries.keys()],
    };
    return registry;
  }

  return {
    forPlugin,
    get: (id) => entries.get(id)?.service,
    ids: () => [...entries.keys()],
    releasePlugin: (pluginId) => {
      for (const [id, entry] of [...entries] ) if (entry.pluginId === pluginId) unregister(pluginId, id);
    },
  };
}
