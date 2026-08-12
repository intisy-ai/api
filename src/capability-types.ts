/** The input control a settings field asks a surface for. */
export type FieldType = "boolean" | "number" | "string" | "secret" | "select" | "multiline" | "list";

/** One configurable setting a plugin declares, which every settings surface renders its own way. */
export interface FieldSpec {
  /** Config key this field reads and writes. */
  key: string;
  /** Which input control to render. */
  type: FieldType;
  /** Label shown beside the control. Defaults to the key. */
  label?: string;
  /** One-line explanation shown under the control. */
  description?: string;
  /** Group heading this field sorts under. */
  group?: string;
  /** The choices for a `select` field. */
  options?: {
    /** Value stored when this choice is picked. */
    value: string;
    /** Label shown for this choice. */
    label: string;
  }[];
  /** Lowest accepted value for a `number` field. */
  min?: number;
  /** Highest accepted value for a `number` field. */
  max?: number;
  /** Step between accepted values for a `number` field. */
  step?: number;
  /** Element type for a `list` field. */
  itemType?: "string" | "number";
  /** Placeholder text for an empty control. */
  placeholder?: string;
}

/** A button a plugin offers on a settings surface or a screen row. */
export interface ActionSpec {
  /** Id passed back when the action runs. */
  id: string;
  /** Label on the button. */
  label: string;
  /** One-line explanation of what running it does. */
  description?: string;
  /** Text a surface must confirm with before running it. */
  confirm?: string;
  /** Marks the action as destructive, so a surface can style it as such. */
  danger?: boolean;
}

/**
 * A plugin asking a host's settings surface for a place of its own inside it.
 *
 * @remarks
 * `scope: "allHomes"` says the setting is not a per-home one, so a surface managing several
 * homes writes it to all of them rather than asking which. The default is per-home.
 */
export interface SectionSpec {
  /** Section id, unique within the plugin. */
  id: string;
  /** Heading shown on the section. */
  label: string;
  /** One-line explanation shown under the heading. */
  description?: string;
  /** Sort order among sections. Lower sorts first. */
  order?: number;
  /** Whether the section's settings are per-home or shared across every home. */
  scope?: "home" | "allHomes";
  /** Field keys placed in this section, in order. */
  fields?: string[];
  /** Action ids placed in this section, in order. */
  actions?: string[];
}

/**
 * Where a plugin keeps state inside a home, for a surface offering to delete it on uninstall.
 *
 * @remarks
 * Most plugins declare nothing: the config file, the log files, and the cache entries are all
 * named after the plugin and are found without asking. This is only for state written somewhere
 * the plugin's name does not appear. Paths are relative to the home directory.
 */
export interface DataSpec {
  /** Home-relative paths this plugin owns. */
  paths?: string[];
}

/** Everything the `settings` capability declares about itself. */
export interface CapabilitySchema {
  /** The settings this plugin exposes. */
  fields?: FieldSpec[];
  /** The actions this plugin offers. */
  actions?: ActionSpec[];
  /** The sections this plugin asks a settings surface for. */
  sections?: SectionSpec[];
  /** Where this plugin keeps state a host cannot derive. */
  data?: DataSpec;
}

/** What running an action produced, for a surface to report and act on. */
export interface ActionResult {
  /** Whether the action succeeded. */
  ok: boolean;
  /** One line for the surface to show, on success or on failure. */
  message?: string;
  /** Asks the surface to re-read the screen's data because the action changed it. */
  refresh?: boolean;
}

/**
 * Presentation hints every screen node may carry.
 *
 * @remarks
 * New sizing or spacing options belong here rather than on a kind, so adding one never touches
 * an existing kind's renderer.
 */
export interface NodeStyle {
  /** Width hint, in whatever unit the surface understands. */
  width?: string;
  /** Share of free space this node takes among its siblings. */
  grow?: number;
  /** Cross-axis alignment. */
  align?: "start" | "center" | "end";
  /** Padding hint. */
  pad?: "none" | "tight" | "normal";
  /** Named tone, resolved by the surface's own palette. */
  tone?: string;
}

/**
 * One node of a screen's layout tree.
 *
 * @remarks
 * `kind` is open on purpose: each surface dispatches it through a registry and skips what it
 * does not know, so a plugin built against a newer host degrades instead of blanking a screen.
 * The index signature carries each kind's own props.
 */
export interface ScreenNode {
  /** What to render. Unknown kinds are skipped by the surface. */
  kind: string;
  /** Presentation hints. */
  style?: NodeStyle;
  /** Child nodes, for container kinds. */
  children?: ScreenNode[];
  /** Kind-specific properties. */
  [prop: string]: unknown;
}

/** One column of a table node. */
export interface Column {
  /** Key read from each row. */
  key: string;
  /** Heading shown above the column. Defaults to the key. */
  label?: string;
  /** Named tone the surface renders the cell in. The listed names autocomplete, any other is legal and resolved by the surface's own palette. */
  tone?: "normal" | "muted" | "mono" | "old" | "new" | (string & {});
  /** Character budget beyond which the surface truncates. */
  truncate?: number;
}

/** Which keys of a row carry its title, subtitle, badge, and icon in a list node. */
export interface ItemShape {
  /** Row key holding the title. */
  title: string;
  /** Row key holding the subtitle. */
  subtitle?: string;
  /** Row key holding the badge text. */
  badge?: string;
  /** Row key holding the icon name. */
  icon?: string;
}

/**
 * A plugin asking a host for a navigation entry of its own whose contents it lays out.
 *
 * @remarks
 * The plugin supplies structure and data; the host supplies every component and all styling.
 * `refreshOn` names event topic prefixes whose arrival makes the host re-read the screen's data.
 */
export interface ScreenSpec {
  /** Screen id, unique within the plugin, and what a data or action request names. */
  id: string;
  /** Label on the navigation entry. */
  label: string;
  /** Glyph beside the navigation entry, resolved by the surface. */
  glyph?: string;
  /** Sort order among screens. Lower sorts first. */
  order?: number;
  /** Whether the screen shows one home or every home. */
  scope?: "home" | "allHomes";
  /** Event topic prefixes that make the host re-read this screen. */
  refreshOn?: string[];
  /** The layout tree. */
  layout: ScreenNode;
  /**
   * Per-surface layout overrides, keyed by surface id, for surfaces that need a different tree.
   * A surface uses `layout` when it finds no entry of its own, and an id no host renders is
   * ignored.
   */
  surfaces?: Record<string, ScreenNode>;
}

/** A host asking a plugin for the data behind one screen. */
export interface ScreenDataRequest {
  /** Which screen to read. */
  screenId: string;
  /** Absolute path of the app home to read, for a per-home screen. */
  home?: string;
  /** Marks a re-read triggered by an event or by a completed action. */
  refresh?: boolean;
}

/**
 * The data behind one screen, keyed by the source names its layout nodes reference.
 *
 * @remarks
 * Values are whatever the referencing node kind expects, which is why they are `unknown`: the
 * node kind registry, not this type, is what pairs a source with its renderer.
 */
export interface ScreenData {
  /** Data per source name. */
  sources: Record<string, unknown>;
}

/** A host asking a plugin to run one of a screen's actions. */
export interface ScreenActionRequest {
  /** Which screen the action belongs to. */
  screenId: string;
  /** Which action to run. */
  actionId: string;
  /** Absolute path of the app home to act on, for a per-home screen. */
  home?: string;
  /** Values the surface collected for the action. */
  input?: Record<string, unknown>;
}
