import type { PluginContext } from "./context.js";

/**
 * What a plugin's entry module default-exports.
 *
 * @remarks
 * Lifecycle stays minimal because minimal survives. `activate` and `deactivate` are required;
 * `install` and `repair` exist only when the manifest's `lifecycle` block declares them. Each is
 * awaited individually by the host under a timeout, so a plugin that throws or hangs is
 * quarantined on its own rather than taking a host or a sibling with it.
 */
export interface Plugin {
  /** Registers this plugin's capabilities and services. Called once, when the plugin loads. */
  activate(context: PluginContext): void | Promise<void>;
  /** Flushes state and releases resources. Awaited on shutdown, on disable, and before an update. */
  deactivate(): void | Promise<void>;
  /** Runs once after the first deploy. Present only when the manifest declares `lifecycle.install`. */
  install?(context: PluginContext): void | Promise<void>;
  /** Runs on demand from a host. Present only when the manifest declares `lifecycle.repair`. */
  repair?(context: PluginContext): void | Promise<void>;
}

/**
 * Declares a plugin from an object literal, for authors who prefer one to a class.
 *
 * @remarks
 * Produces exactly the shape a class implementing {@link Plugin} produces; it exists for the
 * types and the autocompletion, not for any behaviour of its own.
 *
 * @example
 * ```ts
 * export default definePlugin({
 *   async activate(ctx) { ctx.provide("screens", screens); },
 *   async deactivate() {},
 * });
 * ```
 */
export function definePlugin(plugin: Plugin): Plugin {
  return plugin;
}
