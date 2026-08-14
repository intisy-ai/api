export { API_VERSION } from "./manifest.js";
export type { ManifestLifecycle, ManifestPublish, ManifestServices, PluginManifest, Presentation, RepoMeta } from "./manifest.js";
export { PluginError, isPluginError } from "./errors.js";
export { ignoreUnknown, isStrict, reportDiagnostic, setDiagnosticSink, setStrict, STRICT_ENV } from "./strict.js";
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
  CapabilityImplementation,
  CapabilityKey,
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
  ProviderDescriptor,
  ScreensCapability,
  SettingsCapability,
  SyncResult,
} from "./capabilities.js";
export { createServiceHub } from "./services.js";
export type {
  AccountsService,
  ActivityImpact,
  ActivityPage,
  ActivityQuery,
  ActivityRecord,
  ActivityService,
  ActivitySpec,
  ActivitySubject,
  RoutingService,
  ServiceContract,
  ServiceEvent,
  ServiceHub,
  ServiceKey,
  ServiceListener,
  ServiceMap,
  ServiceRecorder,
  ServiceRegistry,
  WantOptions,
} from "./services.js";
export { ECOSYSTEM_TOPICS } from "./events.js";
export type { EventBus, EventMap, EventPayload, EventTopic, NotificationLevel } from "./events.js";
export type { HostDescriptor, Logger, PluginConfig, PluginPaths } from "./runtime.js";
export type { PluginContext } from "./context.js";
export { definePlugin } from "./plugin.js";
export type { Plugin } from "./plugin.js";
export { createPluginLedger } from "./ledger.js";
export type { LedgerEntry, PluginLedger, PluginStatus } from "./ledger.js";
export { createPluginHost } from "./host.js";
export type { CapabilityRecord, PluginHost, PluginHostOptions, PluginRuntime } from "./host.js";
export { activationOrder } from "./graph.js";
export type { ActivationPlan } from "./graph.js";
export { analyzePlugins } from "./doctor.js";
export type { DoctorFinding, DoctorReport } from "./doctor.js";
