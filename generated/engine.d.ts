// Generated from Java sources. Do not edit.

export declare function activationOrder(manifests: unknown[]): ActivationPlanShape;
export declare function assertManifest(manifest: unknown): unknown;
export declare function assertManifest(manifest: unknown, wellKnownServices: string[]): unknown;
export declare function createPluginHost(options: unknown): HostSurface;
export declare function isPluginError(value: unknown): boolean;
export declare function pluginError(pluginId: string, detail: string, fix: string): PluginErrorShape;
export declare function setDiagnosticSink(sink: ((message: string) => void) | null): void;
export declare function setStrict(enabled: boolean | null): void;

export interface ActivationPlanShape {
  cycles: string[][];
  order: string[];
}

export interface CapabilityRecordShape {
  implementation: unknown;
  pluginId: string;
}

export interface ContextSurface {
  readonly config: unknown;
  readonly events: EventBusShape;
  readonly host: HostDescriptorShape;
  readonly log: unknown;
  readonly manifest: unknown;
  readonly paths: PluginPathsShape;
  provide(id: string, implementation: unknown): void;
  readonly services: ServiceRegistryShape;
}

export interface EventBusShape {
  publish(topic: string, payload: unknown): void;
  subscribe(topic: string, listener: unknown): () => void;
}

export interface HostDescriptorShape {
  api: number;
  app: string;
  surfaces: string[];
}

export interface HostSurface {
  capability(id: string): CapabilityRecordShape[];
  contextFor(manifest: unknown, runtime: unknown): ContextSurface;
  readonly descriptor: HostDescriptorShape;
  readonly ledger: LedgerFacadeShape;
  markBroken(pluginId: string, error: PluginErrorShape): void;
  release(pluginId: string): void;
  service(id: string): unknown;
  supports(manifest: unknown): PluginErrorShape | undefined;
  verifyActivation(manifest: unknown): PluginErrorShape | undefined;
}

export interface LedgerErrorShape {
  detail: string;
  fix: string;
}

export interface LedgerFacadeShape {
  entries(): LedgerRowShape[];
  entry(pluginId: string): LedgerRowShape | undefined;
  recordDeclared(manifest: unknown): void;
}

export interface LedgerRowShape {
  capabilitiesDeclared: string[];
  capabilitiesProvided: string[];
  error?: LedgerErrorShape;
  permissions: string[];
  pluginId: string;
  servicesConsumed: string[];
  servicesProvided: string[];
  status: string;
  topics: string[];
}

export interface PluginErrorShape {
  readonly detail: string;
  readonly fix: string;
  readonly message: string;
  readonly name: string;
  readonly pluginId: string;
}

export interface PluginPathsShape {
  cache: string;
  config: string;
  home: string;
  plugin: string;
  repos: string;
}

export interface ServiceRegistryShape {
  get(id: string): unknown;
  ids(): string[];
  register(id: string, service: unknown): () => void;
  want(id: string): Promise<unknown>;
  want(id: string, options: WantOptionsShape): Promise<unknown>;
  watch(id: string, listener: unknown): () => void;
}

export interface WantOptionsShape {
  timeoutMs?: number;
}

