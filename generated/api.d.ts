// Generated from Java sources. Do not edit.

export interface CapabilityType<T> {
  readonly __phantom?: T;
  readonly id: string;
}

export interface HostDescriptor {
  api: number;
  app: string;
  surfaces: string[];
}

export interface Logger {
  debug(message: string): void;
  error(message: string): void;
  error(message: string, cause: unknown): void;
  info(message: string): void;
  warn(message: string): void;
}

export interface ManifestLifecycle {
  install?: boolean;
  repair?: boolean;
}

export interface ManifestPublish {
  scopedOnly?: boolean;
}

export interface ManifestServices {
  consumes?: string[];
  provides?: string[];
}

export interface Plugin {
  activate(context: PluginContext): void | Promise<void>;
  deactivate(): void | Promise<void>;
  install?(context: PluginContext): void | Promise<void>;
  repair?(context: PluginContext): void | Promise<void>;
}

export interface PluginConfig {
  all(): Record<string, unknown>;
  get<T>(key: string): T | undefined;
  set(key: string, value: unknown): Promise<void>;
}

export interface PluginContext {
  readonly config: PluginConfig;
  readonly host: HostDescriptor;
  readonly log: Logger;
  readonly manifest: PluginManifest;
  readonly paths: PluginPaths;
  provide<T>(type: CapabilityType<T>, implementation: T): void;
}

export interface PluginManifest {
  $schema?: string;
  api: number;
  capabilities?: string[];
  displayName?: string;
  entry?: string;
  icon?: string;
  id: string;
  lifecycle?: ManifestLifecycle;
  permissions?: string[];
  publish?: ManifestPublish;
  repo?: RepoMeta;
  services?: ManifestServices;
}

export interface PluginPaths {
  cache: string;
  config: string;
  home: string;
  plugin: string;
  repos: string;
}

export interface RepoMeta {
  category: string;
  domains?: string[];
  role: string;
  tech: string;
}

export interface ServiceType<T> {
  readonly __phantom?: T;
  readonly id: string;
}

export interface TopicType<T> {
  readonly __phantom?: T;
  readonly id: string;
}

