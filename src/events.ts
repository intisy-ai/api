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
  notification: { message: string; level: NotificationLevel };
  /** The proxy came up or went down. */
  "proxy.status": { up: boolean; port: number };
  /** An account hit an upstream rate limit. */
  "account.rate_limited": { provider: string; accountId?: string; lane?: string; resetAt?: number };
  /** A plugin's configuration file changed. */
  "config.changed": { name: string };
  /** A configuration snapshot was taken. */
  "config.snapshot": { hash: string; reason: string; files: string[] };
  /** The active configuration profile changed. */
  "config.profile_changed": { profile: string; files: string[] };
  /** A long-running plugin operation reported progress. */
  "plugin.progress": { name: string; phase: string; pct?: number };
  /** A plugin finished installing. */
  "plugin.installed": { name: string; version: string };
  /** A cross-app reconciliation finished. */
  "sync.completed": { files: string[]; plugins: string[]; homes: string[] };
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
