/**
 * The API major version this package implements.
 *
 * @remarks
 * A manifest's `api` field is a FLOOR, not a build tag: a host refuses to load only a plugin
 * whose declared floor exceeds the version the host implements, and loads everything else.
 * The number rises only when a host gains abilities a plugin can require; additions to this
 * package alone never change it.
 *
 * Kept equal to the generated `generated/api.keys.ts`, which the Java `Api.API_VERSION` emits, by
 * an assertion in this file's test. It is not imported from there: `generated/` sits outside the
 * emitting tsconfig's inferred rootDir, so importing it would move the whole `dist` layout.
 */
export const API_VERSION = 2;

/** What a plugin offers other plugins, and what it asks of them. */
export interface ManifestServices {
  /** Service ids this plugin registers, each namespaced by its own id or a well-known bare id. */
  provides?: string[];
  /** Service ids this plugin asks for, used for activation ordering and for `plugin doctor`. */
  consumes?: string[];
}

/** Which optional lifecycle hooks the entry module exports. */
export interface ManifestLifecycle {
  /** The entry exports `install(ctx)`, run once after the first deploy. */
  install?: boolean;
  /** The entry exports `repair(ctx)`, run on demand from a host. */
  repair?: boolean;
}

/** How the repo is published to npm. */
export interface ManifestPublish {
  /** Publish only as `@intisy-ai/<name>`, because the unscoped name is unavailable. */
  scopedOnly?: boolean;
}

/** Repository metadata, from which the GitHub description and topic set are derived. */
export interface RepoMeta {
  /** The role phrase, capitalized, without the fixed ecosystem suffix. */
  role: string;
  /** The single category topic, for example `core-library` or `ai-provider`. */
  category: string;
  /** Domain topics, for example `claude` or `gemini`. */
  domains?: string[];
  /** The primary tech topic, `typescript` or `java`. */
  tech: string;
}

/** How surfaces render this plugin and the things it represents. */
export interface Presentation {
  /** Icon path per provider id this plugin serves, relative to the repo root. */
  providers?: Record<string, string>;
}

/**
 * The contents of a repo's `plugin.json`: the single machine-readable description of what the
 * repo is, what it can do, and how it is published.
 *
 * @remarks
 * This type carries no index signature on purpose. An author gets TypeScript's excess-property
 * checking while writing a manifest, and the runtime validator still ignores fields it does not
 * know, so a manifest written against a later version of this package loads on today's host.
 */
export interface PluginManifest {
  /** Pointer at the published manifest schema, for an editor's completion and validation. */
  $schema?: string;
  /** The plugin's permanent identity, matching its repository name. */
  id: string;
  /** The lowest API major version this plugin needs. A floor, not a build tag. */
  api: number;
  /** The built module a host imports. Required once `capabilities` is non-empty. */
  entry?: string;
  /** The name a surface shows instead of the id. */
  displayName?: string;
  /** Path to a square-viewBox SVG mark, relative to the repo root. */
  icon?: string;
  /**
   * Host-facing abilities this plugin provides at activation, declared statically so a host, an
   * installer, or a marketplace can answer "what can this do" without executing code.
   */
  capabilities?: string[];
  /** The inter-plugin contract. */
  services?: ManifestServices;
  /** Declared permissions, surfaced at install and in dashboards. Not sandbox-enforced. */
  permissions?: string[];
  /** Which optional lifecycle hooks the entry exports. */
  lifecycle?: ManifestLifecycle;
  /** How the repo is published to npm. */
  publish?: ManifestPublish;
  /** Repository metadata. */
  repo?: RepoMeta;
  /** How surfaces render this plugin. */
  presentation?: Presentation;
}
