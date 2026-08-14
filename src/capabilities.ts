import type {
  ActionResult,
  ActionSpec,
  CapabilitySchema,
  ScreenActionRequest,
  ScreenData,
  ScreenDataRequest,
  ScreenSpec,
} from "./capability-types.js";
import type { IrEventStream, IrRequest, IrResponse, ProviderCallContext, WireRequest, WireResponse } from "./ir.js";

/**
 * One upstream lane a provider plugin serves, as a host lists it.
 *
 * @remarks
 * A plugin may back several lanes off one driver (a shared account pool with distinct upstream
 * quotas) or resolve them from the user's own configuration, so a lane is described rather than
 * inferred from the plugin's identity.
 */
export interface ProviderDescriptor {
  /** The provider id a routing chain names. */
  id: string;
  /** Name shown to the user. */
  label: string;
  /** Models this lane serves, keyed by model id. */
  models?: Record<string, unknown>;
  /** Whether accounts for this lane are obtained through an OAuth flow. */
  hasOAuth?: boolean;
  /** Account store key, when several lanes share one pool. Defaults to the lane's own id. */
  accountPool?: string;
  /** Wire format this lane speaks upstream, when it is not the plugin's default. */
  translator?: string;
}

/**
 * Talks to one upstream vendor, in canonical IR only.
 *
 * @remarks
 * A provider never sees an app's wire format: it translates IR into its own upstream vendor
 * format, calls upstream, and decodes the reply back into IR. On a non-2xx upstream outcome it
 * THROWS the typed handler error rather than returning it as data, so the front-door can rebuild
 * the response and rate-limit fallback keeps working.
 */
export interface ProviderCapability {
  /** The provider id a routing chain names. */
  readonly id: string;
  /** Handles one request, returning a response or a stream of IR events. */
  handleIr(request: IrRequest, context: ProviderCallContext): Promise<IrResponse | IrEventStream>;
  /**
   * Every lane this plugin serves, when it serves more than the one `id` names.
   *
   * @remarks
   * Optional because most providers are one lane. A host that does not call it sees exactly the
   * behaviour it saw before this method existed.
   */
  providers?(): ProviderDescriptor[] | Promise<ProviderDescriptor[]>;
}

/**
 * Owns one app's wire format: the seam between what an app sends and the canonical IR everything
 * downstream carries.
 */
export interface FrontDoorCapability {
  /** Decodes an app request into IR, or returns null when the request is not one to route. */
  decode(request: WireRequest): Promise<IrRequest | null>;
  /** Encodes an IR result back into the app's wire format. */
  encode(result: IrResponse | IrEventStream): Promise<WireResponse>;
  /** Rebuilds a wire response from a thrown handler error, or returns null when it cannot. */
  encodeError(error: unknown): WireResponse | null;
}

/** Contributes navigation entries of its own, whose contents the plugin lays out and fills. */
export interface ScreensCapability {
  /** The screens this plugin contributes. */
  screens(): ScreenSpec[] | Promise<ScreenSpec[]>;
  /** Reads the data behind one screen. */
  read(request: ScreenDataRequest): Promise<ScreenData>;
  /** Runs one of a screen's actions. */
  invoke(request: ScreenActionRequest): Promise<ActionResult>;
}

/** Declares configurable settings, actions, and the sections a settings surface renders them in. */
export interface SettingsCapability {
  /** What this plugin exposes on a settings surface. */
  schema(): CapabilitySchema | Promise<CapabilitySchema>;
  /** Runs one of the declared actions. */
  run(actionId: string, input?: Record<string, unknown>): Promise<ActionResult>;
}

/** One slash command a plugin deploys into an app. */
export interface CommandDef {
  /** Command name, which becomes the deployed file name. */
  name: string;
  /** One line shown in the command picker. */
  description: string;
  /** Hint describing the arguments, for example `list | get <key> | set <key> <value>`. */
  argumentHint?: string;
  /** Markdown the model sees when the command runs. */
  body?: string;
  /** Shell run before the body, whose output the body follows. */
  shell?: string;
}

/** Contributes slash commands to whichever app the plugin is deployed into. */
export interface CommandsCapability {
  /** The commands this plugin deploys. */
  commands(): CommandDef[] | Promise<CommandDef[]>;
}

/** One plugin as the plugin manager sees it. */
export interface ManagedPlugin {
  /** The plugin's id. */
  id: string;
  /** The version currently deployed, when one is known. */
  version?: string;
  /** Whether the plugin is enabled in this home. */
  enabled: boolean;
  /** Where the plugin is installed from. */
  url?: string;
}

/** Installs, updates, and removes other plugins. */
export interface PluginManagementCapability {
  /** Every plugin this manager knows about in the current home. */
  list(): Promise<ManagedPlugin[]>;
  /** Installs a plugin from its repository URL. */
  install(url: string): Promise<ActionResult>;
  /** Updates one installed plugin. */
  update(id: string): Promise<ActionResult>;
  /** Removes one installed plugin. */
  remove(id: string): Promise<ActionResult>;
  /** Rebuilds and redeploys one installed plugin without fetching. */
  repair(id: string): Promise<ActionResult>;
}

/** What one reconciliation across app homes moved. */
export interface SyncResult {
  /** Files reconciled. */
  files: string[];
  /** Plugin ids mirrored. */
  plugins: string[];
  /** Home paths touched. */
  homes: string[];
}

/** Reconciles state across the app homes on this machine. */
export interface CrossAppSyncCapability {
  /** Reconciles now and reports what moved. */
  sync(): Promise<SyncResult>;
}

/** One user-defined upstream endpoint. */
export interface CustomEndpoint {
  /** Endpoint id, unique within the plugin. */
  id: string;
  /** Name shown to the user. */
  label: string;
  /** Base URL requests are sent to. */
  baseUrl: string;
}

/** Serves endpoints the user defined rather than ones the ecosystem ships. */
export interface CustomEndpointsCapability {
  /** The endpoints currently defined. */
  endpoints(): Promise<CustomEndpoint[]>;
}

/** Which slice of the configuration history a caller wants. */
export interface HistoryQuery {
  /** Absolute path of the app home to read. */
  home?: string;
  /** Greatest number of entries to return. */
  limit?: number;
  /** Opaque cursor from a previous page. */
  cursor?: string;
}

/** One recorded configuration snapshot. */
export interface HistoryEntry {
  /** Snapshot id, which `restore` names. */
  id: string;
  /** When it was taken, in epoch milliseconds. */
  ts: number;
  /** One line describing what changed. */
  summary: string;
  /** Files the snapshot covers. */
  files: string[];
}

/** Keeps a history of configuration changes and can put an earlier state back. */
export interface ConfigHistoryCapability {
  /** Reads recorded snapshots, newest first. */
  history(query?: HistoryQuery): Promise<HistoryEntry[]>;
  /** Restores one snapshot. */
  restore(entryId: string): Promise<ActionResult>;
}

/** One installable thing a marketplace source offers. */
export interface MarketplaceEntry {
  /** Entry id, normally the repository name. */
  id: string;
  /** Repository URL the entry is installed from. */
  url: string;
  /** Name shown instead of the id. */
  displayName?: string;
  /** One line describing what it is. */
  description?: string;
  /** Topics a host filters and groups by. */
  topics?: string[];
}

/** Contributes installable entries to a host's marketplace listing. */
export interface MarketplaceSourceCapability {
  /** The entries this source offers. */
  entries(): Promise<MarketplaceEntry[]>;
}

/**
 * Every capability id this API version mints, paired with the interface a plugin must provide
 * for it.
 *
 * @remarks
 * A capability is something a HOST consumes, which is why the ids are bare names owned by this
 * package. Because a plugin supplies its implementations at activation and declares the ids in
 * its manifest, a host can answer "what can this do" without executing anything, and the two are
 * checked against each other when the plugin loads.
 */
export interface CapabilityMap {
  /** Talks to one upstream vendor in canonical IR. */
  provider: ProviderCapability;
  /** Owns one app's wire format. */
  "front-door": FrontDoorCapability;
  /** Contributes navigation entries of its own. */
  screens: ScreensCapability;
  /** Declares configurable settings and actions. */
  settings: SettingsCapability;
  /** Contributes slash commands. */
  commands: CommandsCapability;
  /** Installs, updates, and removes other plugins. */
  "plugin-management": PluginManagementCapability;
  /** Reconciles state across app homes. */
  "cross-app-sync": CrossAppSyncCapability;
  /** Serves user-defined upstream endpoints. */
  "custom-endpoints": CustomEndpointsCapability;
  /** Keeps a history of configuration changes. */
  "config-history": ConfigHistoryCapability;
  /** Contributes installable entries to a marketplace listing. */
  "marketplace-source": MarketplaceSourceCapability;
}

/** A capability id this API version mints, or any other id a later one might. */
export type CapabilityKey = keyof CapabilityMap | (string & {});

/** The implementation a capability id requires, `unknown` for an id this API version does not mint. */
export type CapabilityImplementation<T> = T extends keyof CapabilityMap ? CapabilityMap[T] : unknown;
