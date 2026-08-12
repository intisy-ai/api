/** A host-supplied destination for the diagnostics this package would otherwise print. */
export type DiagnosticSink = (message: string) => void;

/** Environment variable that turns strict mode on when set to `"1"`. */
export const STRICT_ENV = "INTISY_PLUGIN_STRICT";

let STRICT: boolean | null = null;
let SINK: DiagnosticSink | null = null;

/**
 * Whether strict mode is on.
 *
 * @remarks
 * Open vocabularies mean an unknown id is silently ignored in production, which is exactly what
 * hides a typo during development. Strict mode keeps the ignoring and makes it loud.
 */
export function isStrict(): boolean {
  if (STRICT === null) STRICT = readStrictEnv();
  return STRICT;
}

/**
 * Forces strict mode on or off. Pass `null` to fall back to the {@link STRICT_ENV} environment
 * variable, which is also what a test uses to undo itself.
 */
export function setStrict(enabled: boolean | null): void {
  STRICT = enabled;
}

/**
 * Directs every ignored-unknown diagnostic to a host's own logger. Pass `null` to restore the
 * console default.
 */
export function setDiagnosticSink(sink: DiagnosticSink | null): void {
  SINK = sink;
}

/**
 * Records that an unknown id was ignored.
 *
 * @param kind - what the id names, for example `"capability"`, `"service"`, or `"topic"`
 * @param id - the unknown id itself
 * @param source - who supplied it, normally a plugin id or a host name
 */
export function ignoreUnknown(kind: string, id: string, source: string): void {
  reportDiagnostic(`ignored unknown ${kind} "${id}" from ${source}`);
}

/**
 * Sends a diagnostic to the host's sink, or to the console when it installed none.
 *
 * @remarks
 * Quiet by default and loud in strict mode, because the open-vocabulary rule means most of these
 * are normal in production and only interesting while developing.
 */
export function reportDiagnostic(message: string): void {
  if (SINK) {
    SINK(message);
    return;
  }
  if (isStrict()) console.warn(`[plugin-api] ${message}`);
  else console.debug(`[plugin-api] ${message}`);
}

function readStrictEnv(): boolean {
  const env = (globalThis as { process?: { env?: Record<string, string | undefined> } }).process?.env;
  return env?.[STRICT_ENV] === "1";
}
