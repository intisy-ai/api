import type { CapabilityImplementation, CapabilityKey } from "./capabilities.js";
import type { EventBus } from "./events.js";
import type { PluginManifest } from "./manifest.js";
import type { HostDescriptor, Logger, PluginConfig, PluginPaths } from "./runtime.js";
import type { ServiceRegistry } from "./services.js";

/**
 * Everything a plugin may touch, and the only way it touches any of it.
 *
 * @remarks
 * A plugin that takes everything through the context is a plugin whose relationships the host can
 * see, which is what makes the introspection ledger, the developer section, and `plugin doctor`
 * free rather than separately built. It is also the seam a future host would use to run a plugin
 * out of process without changing plugin code.
 */
export interface PluginContext {
  /** This plugin's own manifest, as the host validated it. */
  readonly manifest: PluginManifest;
  /** What this plugin may know about the host. */
  readonly host: HostDescriptor;
  /** This plugin's resolved configuration. */
  readonly config: PluginConfig;
  /** This plugin's logger. */
  readonly log: Logger;
  /** The storage directories of the home this plugin runs in. */
  readonly paths: PluginPaths;
  /** The live service registry, scoped so registrations stay inside this plugin's namespace. */
  readonly services: ServiceRegistry;
  /** Publish and subscribe. */
  readonly events: EventBus;
  /**
   * Supplies the implementation behind a capability the manifest declares.
   *
   * @remarks
   * The manifest declares capability ids statically and `activate` supplies the implementations;
   * the host checks the two against each other, which is what keeps the static manifest honest.
   * An id this API version does not mint is accepted and typed as `unknown`, because vocabularies
   * are open.
   */
  provide<K extends CapabilityKey>(id: K, implementation: CapabilityImplementation<K>): void;
}
