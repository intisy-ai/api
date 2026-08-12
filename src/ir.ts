/**
 * A canonical-IR chat request.
 *
 * @remarks
 * Declared here as an empty interface and filled in by `core-ir` through declaration merging, so
 * this package names the seam without depending on the IR library. A consumer that loads
 * `core-ir` types sees the real fields; one that does not still compiles against the seam.
 *
 * @example
 * ```ts
 * declare module "@intisy-ai/api" {
 *   interface IrRequest { model: string; messages: IrMessage[] }
 * }
 * ```
 */
export interface IrRequest {}

/** A canonical-IR chat response. Filled in by `core-ir` through declaration merging. */
export interface IrResponse {}

/** One event of a canonical-IR stream. Filled in by `core-ir` through declaration merging. */
export interface IrStreamEvent {}

/** A canonical-IR event stream, which is what a streaming provider returns. */
export type IrEventStream = ReadableStream<IrStreamEvent>;

/**
 * What a provider is told about the call it is handling, beyond the request itself.
 *
 * @remarks
 * Empty here and filled in by the routing engine through declaration merging, because what a
 * provider needs to know about a call belongs to the engine that routes it, not to this package.
 */
export interface ProviderCallContext {}

/**
 * An app's own wire request, before a front-door decodes it into IR. Filled in by the app-proxy
 * that owns the wire format, through declaration merging.
 */
export interface WireRequest {}

/** An app's own wire response, after a front-door encodes IR back into it. */
export interface WireResponse {}
