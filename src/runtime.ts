/**
 * Where a plugin writes what happened.
 *
 * @remarks
 * The host decides where the lines go, whether they mirror to a console, and how they are
 * attributed, so a plugin never opens a log file itself.
 */
export interface Logger {
  /** Normal operation. */
  info(message: string): void;
  /** Something unexpected that did not stop the operation. */
  warn(message: string): void;
  /** Something that failed, with the cause when there is one. */
  error(message: string, cause?: unknown): void;
  /** Detail only useful while debugging. */
  debug(message: string): void;
}

/**
 * One plugin's configuration, already resolved by the host.
 *
 * @remarks
 * Writing is asynchronous even where a host implements it synchronously, because the seam has to
 * survive a host that runs the plugin out of process.
 */
export interface PluginConfig {
  /** Every setting, defaults merged with what is on disk. */
  all(): Record<string, unknown>;
  /** One setting, or `undefined` when it is neither set nor defaulted. */
  get<T = unknown>(key: string): T | undefined;
  /** Writes one setting. */
  set(key: string, value: unknown): Promise<void>;
}

/**
 * The absolute storage directories of the app home a plugin is running in.
 *
 * @remarks
 * A plugin joins paths onto these rather than assembling a home path itself, so a renamed
 * directory takes effect everywhere at once.
 */
export interface PluginPaths {
  /** The app home directory. */
  home: string;
  /** Where plugin checkouts live. */
  repos: string;
  /** Where deployed plugin bundles and their manifest sidecars live. */
  plugin: string;
  /** Where cached downloads live. */
  cache: string;
  /** Where configuration files live. */
  config: string;
}

/**
 * What a plugin is allowed to know about the host it is running in.
 *
 * @remarks
 * Deliberately thin, and deliberately not a plugin registry: a plugin adapts to which SURFACES
 * exist, never to which app it happens to be in.
 */
export interface HostDescriptor {
  /** The app id, for example `claude` or `opencode`. */
  app: string;
  /** The API major version this host implements. */
  api: number;
  /** Surface ids this host renders, for example `tui` or `gui`. Unknown ids are ignored. */
  surfaces: string[];
}
