export { API_VERSION } from "./manifest.js";
export type { ManifestLifecycle, ManifestPublish, ManifestServices, PluginManifest, Presentation, RepoMeta } from "./manifest.js";
export { PluginError, isPluginError } from "./errors.js";
export { ignoreUnknown, isStrict, setDiagnosticSink, setStrict, STRICT_ENV } from "./strict.js";
export type { DiagnosticSink } from "./strict.js";
export { validateAgainstSchema } from "./schema.js";
export type { JsonSchema, SchemaIssue } from "./schema.js";
export { MANIFEST_SCHEMA, SCHEMA_ID } from "./manifest-schema.js";
export { CAPABILITY_IDS, WELL_KNOWN_SERVICES, isKnownCapability, isWellKnownService, mayRegister, serviceOwner } from "./ids.js";
export type { CapabilityId, WellKnownServiceId } from "./ids.js";
export { assertManifest, validateManifest } from "./validate.js";
export type { ValidationIssue } from "./validate.js";
export type {
  ActionResult,
  ActionSpec,
  CapabilitySchema,
  Column,
  DataSpec,
  FieldSpec,
  FieldType,
  ItemShape,
  NodeStyle,
  ScreenActionRequest,
  ScreenData,
  ScreenDataRequest,
  ScreenNode,
  ScreenSpec,
  SectionSpec,
} from "./capability-types.js";
export type { IrEventStream, IrRequest, IrResponse, IrStreamEvent, ProviderCallContext, WireRequest, WireResponse } from "./ir.js";
export type {
  CapabilityMap,
  CommandDef,
  CommandsCapability,
  ConfigHistoryCapability,
  CrossAppSyncCapability,
  CustomEndpoint,
  CustomEndpointsCapability,
  FrontDoorCapability,
  HistoryEntry,
  HistoryQuery,
  ManagedPlugin,
  MarketplaceEntry,
  MarketplaceSourceCapability,
  PluginManagementCapability,
  ProviderCapability,
  ScreensCapability,
  SettingsCapability,
  SyncResult,
} from "./capabilities.js";
