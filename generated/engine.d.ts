// Generated from Java sources. Do not edit.

export declare function activationOrder(manifests: unknown[]): ActivationPlanShape;
export declare function assertManifest(manifest: unknown): unknown;
export declare function assertManifest(manifest: unknown, wellKnownServices: string[]): unknown;
export declare function isPluginError(value: unknown): boolean;
export declare function pluginError(pluginId: string, detail: string, fix: string): Error;
export declare function setDiagnosticSink(sink: ((message: string) => void) | null): void;
export declare function setStrict(enabled: boolean | null): void;

export interface ActivationPlanShape {
  cycles: string[][];
  order: string[];
}

