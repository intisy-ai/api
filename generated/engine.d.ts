// Generated from Java sources. Do not edit.

export interface ActivationPlanShape {
  cycles: string[][];
  order: string[];
}

export interface EngineSurface {
  activationOrder(manifests: unknown[]): ActivationPlanShape;
  assertManifest(manifest: unknown): unknown;
  assertManifest(manifest: unknown, wellKnownServices: string[]): unknown;
  isPluginError(value: unknown): boolean;
  pluginError(pluginId: string, detail: string, fix: string): Error;
  setDiagnosticSink: (sink: ((message: string) => void) | null) => void;
  setStrict: (enabled: boolean | null) => void;
}

