/** How prominent a notification is. */
export type NotificationLevel = "info" | "success" | "warning" | "error";

/**
 * Every ecosystem-owned bare topic paired with its payload.
 *
 * @remarks
 * Bare topics are owned by this package, exactly as capability ids are. A plugin's own topics are
 * namespaced `<plugin-id>:<topic>` and typed by declaration merging:
 *
 * @example
 * ```ts
 * declare module "@intisy-ai/api" {
 *   interface EventMap { "config-ledger:snapshot": { hash: string } }
 * }
 * ```
 *
 * An unknown topic is legal at runtime and simply absent from this map.
 */
export interface EventMap {
  /** Something a surface should show the user. */
  notification: {
    /** Text to show. */
    message: string;
    /** How prominent to show it. */
    level: NotificationLevel;
  };
  /** The proxy came up or went down. */
  "proxy.status": {
    /** Whether the proxy is now reachable. */
    up: boolean;
    /** Port the proxy listens on. */
    port: number;
  };
  /** An account hit an upstream rate limit. */
  "account.rate_limited": {
    /** Provider whose upstream rate-limited the account. */
    provider: string;
    /** Account that was rate-limited, when known. */
    accountId?: string;
    /** Routing lane the account was serving, when known. */
    lane?: string;
    /** Epoch millis when the limit is expected to clear, when known. */
    resetAt?: number;
  };
  /** A plugin's configuration file changed. */
  "config.changed": {
    /** Config name that changed. */
    name: string;
  };
  /** A configuration snapshot was taken. */
  "config.snapshot": {
    /** Snapshot content hash. */
    hash: string;
    /** Why the snapshot was taken. */
    reason: string;
    /** Files the snapshot covers. */
    files: string[];
  };
  /** The active configuration profile changed. */
  "config.profile_changed": {
    /** Name of the profile now active. */
    profile: string;
    /** Files the profile switch affected. */
    files: string[];
  };
  /** A long-running plugin operation reported progress. */
  "plugin.progress": {
    /** Plugin name reporting progress. */
    name: string;
    /** Current phase of the operation. */
    phase: string;
    /** Completion percentage, when known. */
    pct?: number;
  };
  /** A plugin finished installing. */
  "plugin.installed": {
    /** Plugin name that finished installing. */
    name: string;
    /** Version that was installed. */
    version: string;
  };
  /** A cross-app reconciliation finished. */
  "sync.completed": {
    /** Files that were reconciled. */
    files: string[];
    /** Plugins whose entries were mirrored. */
    plugins: string[];
    /** App homes involved in the reconciliation. */
    homes: string[];
  };
}

/** The ecosystem-owned bare topics, for a host that needs the list at runtime. */
export const ECOSYSTEM_TOPICS = [
  "notification",
  "proxy.status",
  "account.rate_limited",
  "config.changed",
  "config.snapshot",
  "config.profile_changed",
  "plugin.progress",
  "plugin.installed",
  "sync.completed",
] as const;

/** A topic this package types, or any other topic a plugin mints. */
export type EventTopic = keyof EventMap | (string & {});

/** The payload a topic carries, `unknown` for a topic this package does not type. */
export type EventPayload<T> = T extends keyof EventMap ? EventMap[T] : unknown;

/**
 * Publish and subscribe, alongside the registry's request and response.
 *
 * @remarks
 * Reached only through {@link PluginContext}: a plugin never imports a host's bus directly, which
 * is what lets the host record every subscription and what would let a future out-of-process
 * host swap the transport underneath.
 */
export interface EventBus {
  /** Publishes a payload on a topic. */
  publish<T extends EventTopic>(topic: T, payload: EventPayload<T>): void;
  /** Subscribes to a topic. Returns a disposer. */
  subscribe<K extends keyof EventMap>(topic: K, listener: (payload: EventMap[K]) => void): () => void;
  /** Subscribes to a topic. Returns a disposer. */
  subscribe(topic: string, listener: (payload: unknown) => void): () => void;
}
