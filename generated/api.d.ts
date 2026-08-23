// Generated from Java sources. Do not edit.

/**
 * A capability id paired, in the type system, with the implementation that id requires.
 *
 * @remarks
 * The library that defines a category owns its key. This package deliberately mints none,
 * which is what lets the api be reused by a project that has never heard of this ecosystem.
 */
export interface CapabilityType<T> {
  /** Never present at run time. It exists so two keys parameterised differently cannot be interchanged. */
  readonly __phantom?: T;
  /** The id itself, which is what crosses the boundary at run time. */
  readonly id: string;
}

/**
 * A plugin's settings as it ships them.
 *
 * @remarks
 * Values only. What a setting is CALLED and how a surface renders it is the settings
 * capability's business, which this contract may not know: a manifest that carried labels would be
 * minting vocabulary, and the api mints none.
 */
export interface ManifestConfig {
  /** Every setting this plugin has, and what it is worth until a home changes it. */
  defaults: Record<string, unknown>;
}

/**
 * Everything a plugin may touch, and the only way it touches any of it.
 *
 * @remarks
 * Taking everything through the context is what makes the introspection ledger and the
 * doctor free rather than separately built, and it is the seam a host would use to run a plugin out
 * of process without changing plugin code.
 */
export interface PluginContext {
  /**
   * The typed key for a capability id, so a plugin can provide one without linking the library
   * that mints it.
   *
   * @remarks
   * The id is data the manifest already states, and the payload type comes from a
   * type-only import, which erases at build time. That pair is what makes a plugin's only runtime
   * dependency this package.
   */
  capability<T>(id: string): CapabilityType<T>;
  /** This plugin's resolved configuration. */
  readonly config: PluginConfig;
  /** How this plugin says something happened, and hears that something did. */
  readonly events: Events;
  /**
   * Every app home the host knows about, whether or not each exists on disk.
   *
   * @remarks
   * Asked rather than held, because a home can appear, and can be installed, while a
   * plugin is running. `paths` is this plugin's own home; this is every home there is.
   */
  homes(): HomeDescriptor[];
  /** What this plugin may know about the host. */
  readonly host: HostDescriptor;
  /** This plugin's logger. */
  readonly log: Logger;
  /** This plugin's own manifest, as the host validated it. */
  readonly manifest: PluginManifest;
  /** The storage directories of the home this plugin runs in. */
  readonly paths: PluginPaths;
  /**
   * Supplies the implementation behind a capability the manifest declares.
   *
   * @remarks
   * One signature, never a plain-string overload beside it: the pair would let a wrong
   * payload compile through the string side.
   */
  provide<T>(type: CapabilityType<T>, implementation: T): void;
  /** How this plugin reaches another plugin's API, and offers its own. */
  readonly services: Services;
}

/**
 * How a plugin reaches another plugin's API, and offers its own.
 *
 * @remarks
 * A typed handle, never an import: the plugin answering is whichever one registered the
 * id, so no plugin ever names another. This is what makes a plugin terminal, and it is the reason
 * a host needs no import of anything it drives.
 */
export interface Services {
  /** What is registered under this id right now, or absent when nothing is. */
  get<T>(type: ServiceType<T>): T | undefined;
  /** Every service id registered right now. */
  ids(): string[];
  /** Offers an implementation under this id, until the returned function is called. */
  register<T>(type: ServiceType<T>, implementation: T): () => void;
  /** Waits for the id to be registered, for a host default the implementation chooses. */
  want<T>(type: ServiceType<T>): Promise<T>;
  /** Waits for the id to be registered, giving up after the stated time. */
  want<T>(type: ServiceType<T>, options: WantOptions): Promise<T>;
  /**
   * Reports every registration and removal of this id until the returned function is called. The
   * listener is handed the service and either "register" or "unregister"; on the latter the
   * service is absent.
   *
   * @remarks
   * Distinct from `want`, which answers once: a service can be replaced while a
   * consumer is still holding the old one, and only a watcher sees that.
   */
  watch<T>(type: ServiceType<T>, listener: ((a: T, b: string) => void)): () => void;
}

/**
 * How a plugin says something happened, and hears that something did.
 *
 * @remarks
 * A topic is paired with its payload in the type system, so a publisher and a subscriber
 * cannot disagree about the shape without the compiler saying so.
 */
export interface Events {
  /** Says a topic carried this payload, to whoever is listening and to nobody in particular. */
  publish<T>(topic: TopicType<T>, payload: T): void;
  /** Hears this topic until the returned function is called. */
  subscribe<T>(topic: TopicType<T>, listener: ((value: T) => void)): () => void;
}

/**
 * Key/value store of JSON strings, keyed by file-like names. `update` must be atomic; that is
 * the implementation's concern.
 *
 * @remarks
 * The key examples this once carried named plugin categories, which the contract may not
 * know, and became visible to that gate the moment this interface started emitting TypeScript.
 */
export interface Store {
  /** Removes `key`, and does nothing when it holds nothing. */
  delete(key: string): void;
  /** Whether anything is stored under `key`. */
  exists(key: string): boolean;
  /** The value stored under `key`, or `null` when nothing is stored there. */
  get(key: string): string;
  /** Every key beginning with `prefix`, in the order the store holds them. */
  listKeys(prefix: string): string[];
  /** Stores `value` under `key`, replacing whatever was there. */
  put(key: string, value: string): void;
  /**
   * Replaces `key`'s value with what `mutator` returns, reading and writing as one
   * step, and hands the mutator `null` when the key holds nothing yet.
   */
  update(key: string, mutator: ((value: string) => string)): void;
}

/**
 * One app home the host knows about.
 *
 * @remarks
 * A plugin whose job spans more than its own home takes them from the host rather than
 * resolving a registry itself, which is what keeps it from linking the library that owns the
 * registry's shape.
 */
export interface HomeDescriptor {
  /** The app id, for example `claude` or `opencode`. */
  app: string;
  /** The name a surface shows instead of the id. */
  label: string;
  /** The id of the plugin this app is reached through, absent when it has none. */
  loader?: string;
  /** This home's storage directories. */
  paths: PluginPaths;
  /** Whether this home exists on disk. An absent home means that app is not installed. */
  present: boolean;
}

/**
 * One slash command a plugin contributes, declared rather than registered by running code.
 *
 * @remarks
 * Declared so a host can deploy a plugin's commands without importing it, which is what
 * lets the command files exist before the plugin has ever activated.
 */
export interface ManifestCommand {
  /** The argument shape a picker hints at, such as "list | get <key>". */
  argumentHint?: string;
  /** Markdown the model is shown, after any shell output. */
  body?: string;
  /** What a command picker shows beside the name. */
  description: string;
  /** The command's name, which is also the file it is written to. */
  name: string;
  /** A shell line run before the body, which may use $ARGUMENTS and {{BUNDLE}}. */
  shell?: string;
}

/**
 * The absolute storage directories of the app home a plugin is running in.
 *
 * @remarks
 * A plugin joins paths onto these rather than assembling a home path itself, so a renamed
 * directory takes effect everywhere at once.
 */
export interface PluginPaths {
  /** Where cached downloads live. */
  cache: string;
  /** Where configuration files live. */
  config: string;
  /** The app home directory. */
  home: string;
  /** Where deployed plugin bundles and their manifest sidecars live. */
  plugin: string;
  /** Where plugin checkouts live. */
  repos: string;
}

/**
 * The contents of a repo's plugin.json.
 *
 * @remarks
 * No index signature and no additionalProperties: an author gets excess-property checking
 * while writing one, and the runtime validator still ignores fields it does not know, so a manifest
 * written against a later version loads on today's host.
 */
export interface PluginManifest {
  /**
   * Pointer at the published manifest schema, for an editor's completion and validation.
   *
   * @remarks
   * Declared although nothing reads it, because every manifest in the ecosystem carries
   * it and the published schema accepts it: without it an author writing a manifest literal in
   * TypeScript could not include a field their own plugin.json has.
   */
  $schema?: string;
  /** The lowest API major version this plugin needs. A floor, not a build tag. */
  api: number;
  /** Host-facing abilities this plugin provides at activation, declared statically so a host can answer what it can do without executing it. */
  capabilities?: string[];
  /** Slash commands this plugin contributes, which a host deploys without importing it. */
  commands?: ManifestCommand[];
  /** This plugin's settings as it ships them. */
  config?: ManifestConfig;
  /** The name a surface shows instead of the id. */
  displayName?: string;
  /** The built module a host imports. Required once `capabilities` is non-empty. */
  entry?: string;
  /** Path to a square-viewBox SVG mark, relative to the repo root. */
  icon?: string;
  /** The plugin's permanent identity, matching its repository name. */
  id: string;
  /** Which optional lifecycle hooks the entry exports. */
  lifecycle?: ManifestLifecycle;
  /** Declared permissions, surfaced at install and in dashboards. Not sandbox-enforced. */
  permissions?: string[];
  /** How the repo is published to npm. */
  publish?: ManifestPublish;
  /** Repository metadata. */
  repo?: RepoMeta;
  /** The inter-plugin contract. */
  services?: ManifestServices;
}

/**
 * What a plugin may know about its host.
 *
 * @remarks
 * Deliberately not a plugin registry: a plugin adapts to which surfaces exist, never to
 * which app it happens to be in.
 */
export interface HostDescriptor {
  /** The API major version this host implements. */
  api: number;
  /** The app id, for example `claude` or `opencode`. */
  app: string;
  /** Surface ids this host renders, for example `tui` or `gui`. An unknown id is ignored. */
  surfaces: string[];
}

/**
 * What a plugin's entry module exports.
 *
 * @remarks
 * Each hook is awaited individually by the host under its own timeout, so a plugin that
 * throws or hangs is quarantined on its own rather than taking a host or a sibling with it.
 */
export interface Plugin {
  /** Supplies the implementation behind every capability the manifest declares. */
  activate(context: PluginContext): void | Promise<void>;
  /** Releases whatever `activate` took: timers, watchers, child processes. */
  deactivate(): void | Promise<void>;
  /** Runs once after the first deploy. */
  install?(context: PluginContext): void | Promise<void>;
  /** Runs on demand from a host, to put a broken installation right. */
  repair?(context: PluginContext): void | Promise<void>;
}

/**
 * Where a component writes what happened.
 *
 * @remarks
 * The host decides where the lines go, whether they mirror to a console, and how they are
 * attributed, so a component never opens a log file itself.
 */
export interface Logger {
  /** Detail only useful while debugging. */
  debug(message: string): void;
  /** Something that failed. */
  error(message: string): void;
  /** Something that failed, with the cause. */
  error(message: string, cause: unknown): void;
  /** Normal operation. */
  info(message: string): void;
  /** Something unexpected that did not stop the operation. */
  warn(message: string): void;
}

/** A service id paired, in the type system, with the contract that id promises. */
export interface ServiceType<T> {
  /** Never present at run time. It exists so two keys parameterised differently cannot be interchanged. */
  readonly __phantom?: T;
  /** The id itself, which is what crosses the boundary at run time. */
  readonly id: string;
}

/** An event topic paired, in the type system, with the payload it carries. */
export interface TopicType<T> {
  /** Never present at run time. It exists so two keys parameterised differently cannot be interchanged. */
  readonly __phantom?: T;
  /** The id itself, which is what crosses the boundary at run time. */
  readonly id: string;
}

/** How long a plugin is willing to wait for a service that is not registered yet. */
export interface WantOptions {
  /** Milliseconds to wait before giving up. Absent takes the host's own default. */
  timeoutMs?: number;
}

/** How the repo is published to npm. */
export interface ManifestPublish {
  /** Publish only as `@intisy-ai/<name>`, because the unscoped name is unavailable. */
  scopedOnly?: boolean;
}

/** One plugin's configuration, already resolved by the host. */
export interface PluginConfig {
  /** Every setting, defaults merged with what is on disk. */
  all(): Record<string, unknown>;
  /** One setting, absent when it is neither set nor defaulted. */
  get<T>(key: string): T | undefined;
  /**
   * Writes one setting.
   *
   * @remarks
   * Asynchronous even where a host implements it synchronously, because the seam has to
   * survive a host that runs the plugin out of process.
   */
  set(key: string, value: unknown): Promise<void>;
}

/** Repository metadata, from which the GitHub description and topic set are derived. */
export interface RepoMeta {
  /** The single category topic, for example `core-library` or `dashboard`. */
  category: string;
  /** Domain topics, for example `claude` or `gemini`. */
  domains?: string[];
  /** The role phrase, capitalized, without the fixed ecosystem suffix. */
  role: string;
  /** The primary tech topic, `typescript` or `java`. */
  tech: string;
}

/** What a plugin offers other plugins, and what it asks of them. */
export interface ManifestServices {
  /** Service ids this plugin asks for, used for activation ordering. */
  consumes?: string[];
  /** Service ids this plugin registers, each namespaced by its own id or a well-known bare id. */
  provides?: string[];
}

/** Which optional lifecycle hooks the entry module exports. */
export interface ManifestLifecycle {
  /** The entry exports `install(ctx)`, run once after the first deploy. */
  install?: boolean;
  /** The entry exports `repair(ctx)`, run on demand from a host. */
  repair?: boolean;
}

