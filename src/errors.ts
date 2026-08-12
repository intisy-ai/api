/**
 * The one error type every host-visible plugin failure carries.
 *
 * @remarks
 * Recognised across bundle boundaries by the `name` marker rather than `instanceof`: a plugin,
 * a host, and a dashboard each bundle their own copy of this package, and class identity does
 * not survive an independent bundle. Use {@link isPluginError}, never `instanceof`.
 *
 * Every instance names the plugin, what went wrong, and what to do about it, because a load
 * error is read by an author who has never seen this code.
 */
export class PluginError extends Error {
  /** Stable cross-bundle marker. Never rename. */
  override readonly name = "PluginError";

  /** The plugin the failure is attributed to. */
  readonly pluginId: string;

  /** What went wrong, in one sentence, naming the field, capability, or service id involved. */
  readonly detail: string;

  /** The action that resolves it, phrased for the plugin's author or the operator. */
  readonly fix: string;

  /**
   * @param pluginId - the plugin the failure is attributed to
   * @param detail - what went wrong
   * @param fix - what to do about it
   */
  constructor(pluginId: string, detail: string, fix: string) {
    super(`[${pluginId}] ${detail}\n  fix: ${fix}`);
    this.pluginId = pluginId;
    this.detail = detail;
    this.fix = fix;
  }
}

/**
 * Whether a caught value is a {@link PluginError}, including one thrown by a separately
 * bundled copy of this package.
 */
export function isPluginError(value: unknown): value is PluginError {
  return typeof value === "object" && value !== null && (value as { name?: unknown }).name === "PluginError";
}
