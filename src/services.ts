import { PluginError } from "./errors.js";
import { mayRegister, WELL_KNOWN_SERVICES } from "./ids.js";
import { reportDiagnostic } from "./strict.js";

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

/** How much one recorded activity matters. */
export type ActivityImpact = "debug" | "info" | "notice" | "warning" | "error";

/** What one activity was about. */
export interface ActivitySubject {
  /** What kind of thing it is, for example `plugin` or `account`. */
  kind: string;
  /** The thing's id, when it has one. */
  id?: string;
  /** The thing's name, when it has one. */
  label?: string;
}

/** What a plugin hands the activity record to have one activity written down. */
export interface ActivitySpec {
  /** Dotted topic the activity belongs to, for example `config.changed`. */
  topic: string;
  /** What happened, as one verb. */
  action: string;
  /** How much it matters. The implementation picks a default per topic when this is absent. */
  impact?: ActivityImpact;
  /** What the activity was about. */
  subject?: ActivitySubject;
  /** Anything else worth keeping, which a surface renders as it likes. */
  details?: Record<string, unknown>;
}

/** One activity as it is read back. */
export interface ActivityRecord {
  /** Record id. */
  id: string;
  /** When it happened, in epoch milliseconds. */
  ts: number;
  /** Absolute path of the app home it was recorded in. */
  home: string;
  /** Topic the activity belongs to. */
  topic: string;
  /** What happened. */
  action: string;
  /** How much it matters. */
  impact: ActivityImpact;
  /** Who recorded it, normally a plugin id. */
  source: string;
  /** What the activity was about, when the emitter said. */
  subject?: ActivitySubject;
  /** Whatever else the emitter kept. */
  details: Record<string, unknown>;
  /** One line describing the activity, for a surface that renders text. */
  text: string;
}

/** Which slice of the activity record a caller wants. */
export interface ActivityQuery {
  /** Keep only these impacts. */
  impacts?: ActivityImpact[];
  /** Keep only activity these sources recorded. */
  sources?: string[];
  /** Keep only these topics. */
  topics?: string[];
  /** Keep only activity at or after this epoch millisecond. */
  since?: number;
  /** Keep only activity at or before this epoch millisecond. */
  until?: number;
  /** Greatest number of records to return. */
  limit?: number;
  /** Opaque cursor from a previous page. */
  cursor?: string;
}

/** One page of read-back activity. */
export interface ActivityPage {
  /** The records, newest first. */
  records: ActivityRecord[];
  /** Cursor {@link ActivityQuery.cursor} takes for the next page, absent on the last one. */
  nextCursor?: string;
}

/**
 * The activity record contract.
 *
 * @remarks
 * Bare rather than namespaced because it is a contract any plugin may implement, exactly like the
 * account store. The shapes here are the smallest a consumer needs: an implementation is free to
 * record and return more, and a consumer that wants the extra reaches for that implementation's
 * own package.
 */
export interface ActivityService {
  /** Records one activity. */
  emit(spec: ActivitySpec): void;
  /** Reads recorded activity, newest first. */
  read(query?: ActivityQuery): Promise<ActivityPage>;
}

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
  /** The activity record. */
  activity: ActivityService;
}

/** What happened to a watched service. */
export type ServiceEvent = "register" | "unregister";

/** Called when a watched service arrives or goes away, with `undefined` on the way out. */
export type ServiceListener<T> = (service: T | undefined, event: ServiceEvent) => void;

/** How long to wait for a service that has not arrived yet. */
export interface WantOptions {
  /**
   * Milliseconds to wait before rejecting. Waits indefinitely when absent, though the wait still
   * ends if the waiting plugin is released.
   */
  timeoutMs?: number;
}

/** A service id this package types, or any other id a plugin mints. */
export type ServiceKey = keyof ServiceMap | (string & {});

/** The contract behind a service id, `unknown` for an id this package does not type. */
export type ServiceContract<T> = T extends keyof ServiceMap ? ServiceMap[T] : unknown;

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
  /**
   * Resolves when the service is registered, now or later, and rejects on the timeout when one
   * was given or when this plugin is released first.
   */
  want<K extends keyof ServiceMap>(id: K, options?: WantOptions): Promise<ServiceMap[K]>;
  /**
   * Resolves when the service is registered, now or later, and rejects on the timeout when one
   * was given or when this plugin is released first.
   */
  want(id: string, options?: WantOptions): Promise<unknown>;
  /** Reports every registration and unregistration of the id. Returns a disposer. */
  watch<K extends keyof ServiceMap>(id: K, listener: ServiceListener<ServiceMap[K]>): () => void;
  /** Reports every registration and unregistration of the id. Returns a disposer. */
  watch(id: string, listener: ServiceListener<unknown>): () => void;
  /** Registers one of this plugin's own services. Returns a disposer that unregisters it. */
  register<K extends ServiceKey>(id: K, service: ServiceContract<K>): () => void;
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
  /**
   * Unregisters everything a plugin registered, stops the watchers it installed, and rejects the
   * `want` calls it is still waiting on, so a deactivated or quarantined plugin is inert.
   */
  releasePlugin(pluginId: string): void;
}

interface Entry {
  pluginId: string;
  service: unknown;
}

interface Waiter {
  pluginId: string;
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
  const watching = new Map<string, Array<() => void>>();

  function notify(id: string, service: unknown, event: ServiceEvent): void {
    const listeners = watchers.get(id);
    if (!listeners) return;
    for (const listener of [...listeners]) {
      if (!listeners.has(listener)) continue;
      try {
        listener(service, event);
      } catch (error) {
        reportDiagnostic(`a watcher of "${id}" threw while handling ${event}: ${String(error)}`);
      }
    }
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

  /**
   * @remarks
   * The returned promise carries a no-op catch of its own, because a plugin may legitimately
   * `want(...).then(...)` and never handle a rejection: releasing that plugin would otherwise
   * raise an unhandled rejection and take the whole host down, which is the opposite of what
   * quarantine is for. Anyone who does await or catch the promise still sees the rejection.
   */
  function want(pluginId: string, id: string, options: WantOptions = {}): Promise<unknown> {
    recorder.consumed?.(pluginId, id);
    const entry = entries.get(id);
    if (entry) return Promise.resolve(entry.service);
    const settled = new Promise<unknown>((resolve, reject) => {
      const waiter: Waiter = { pluginId, resolve, reject, timer: null };
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
    settled.catch(() => {});
    return settled;
  }

  function tracked(pluginId: string, dispose: () => void): () => void {
    let done = false;
    const stop = () => {
      if (done) return;
      done = true;
      dispose();
      const owned = watching.get(pluginId);
      if (!owned) return;
      const index = owned.indexOf(stop);
      if (index >= 0) owned.splice(index, 1);
      if (!owned.length) watching.delete(pluginId);
    };
    const owned = watching.get(pluginId) ?? [];
    owned.push(stop);
    watching.set(pluginId, owned);
    return stop;
  }

  function watch(pluginId: string, id: string, listener: ServiceListener<unknown>): () => void {
    recorder.consumed?.(pluginId, id);
    const set = watchers.get(id) ?? new Set<ServiceListener<unknown>>();
    set.add(listener);
    watchers.set(id, set);
    return tracked(pluginId, () => set.delete(listener));
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
      for (const stop of [...(watching.get(pluginId) ?? [])]) stop();
      watching.delete(pluginId);
      for (const [id, entry] of [...entries]) if (entry.pluginId === pluginId) unregister(pluginId, id);
      for (const [id, waiting] of waiters) {
        for (const waiter of [...waiting]) {
          if (waiter.pluginId !== pluginId) continue;
          waiting.delete(waiter);
          if (waiter.timer) clearTimeout(waiter.timer);
          waiter.reject(new PluginError(
            pluginId,
            `stopped while waiting for service "${id}"`,
            `provide "${id}" before this plugin is stopped, or use get() and carry on without it`,
          ));
        }
        if (!waiting.size) waiters.delete(id);
      }
    },
  };
}
