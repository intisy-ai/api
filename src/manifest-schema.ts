import type { JsonSchema } from "./schema.js";

/** Canonical URL a manifest points `$schema` at, served by the docs site. */
export const SCHEMA_ID = "https://intisy-ai.github.io/api/schema/plugin.schema.json";

/**
 * The schema of `plugin.json`, the single machine-readable description of a repo.
 *
 * @remarks
 * The one source of truth for both the runtime validator and the published
 * `schema/plugin.schema.json` file, so the two can never drift. No object here declares
 * `additionalProperties: false`: an unknown field is ignored, which is what lets a manifest
 * written against a later version of this package load on today's host.
 */
export const MANIFEST_SCHEMA: JsonSchema = {
  $schema: "http://json-schema.org/draft-07/schema#",
  $id: SCHEMA_ID,
  title: "intisy-ai plugin manifest",
  description: "The single machine-readable description of a repo in the intisy-ai ecosystem.",
  type: "object",
  required: ["id", "api"],
  properties: {
    id: {
      type: "string",
      pattern: "^[a-z0-9]+(-[a-z0-9]+)*$",
      description: "The plugin's permanent identity, matching its repository name.",
      fix: 'use lowercase words joined by single hyphens, for example "config-ledger"',
    },
    api: {
      type: "integer",
      minimum: 1,
      description: "The lowest API major version this plugin needs. A floor, not a build tag.",
      fix: 'set "api" to the lowest API major version this plugin needs, for example 1',
    },
    entry: {
      type: "string",
      description: "The built module a host imports. Required once capabilities are declared.",
      fix: 'point "entry" at the built module a host imports, for example "dist/index.js"',
    },
    displayName: { type: "string", description: "The name a surface shows instead of the id." },
    icon: { type: "string", description: "Path to a square-viewBox SVG mark, relative to the repo root." },
    capabilities: {
      type: "array",
      items: { type: "string" },
      description: "Host-facing abilities this plugin provides at activation.",
      fix: "list capability ids as strings, for example [\"provider\", \"screens\"]",
    },
    services: {
      type: "object",
      description: "The inter-plugin contract: what this plugin offers other plugins, and what it asks of them.",
      properties: {
        provides: { type: "array", items: { type: "string" }, description: "Service ids this plugin registers, each namespaced by its own id or a well-known bare id." },
        consumes: { type: "array", items: { type: "string" }, description: "Service ids this plugin asks for." },
      },
    },
    permissions: {
      type: "array",
      items: { type: "string" },
      description: "Declared permissions, surfaced at install and in dashboards.",
    },
    lifecycle: {
      type: "object",
      description: "Which optional lifecycle hooks the entry module exports.",
      properties: {
        install: { type: "boolean", description: "The entry exports install(ctx), run once after first deploy." },
        repair: { type: "boolean", description: "The entry exports repair(ctx), run on demand from a host." },
      },
    },
    publish: {
      type: "object",
      description: "How the repo is published to npm.",
      properties: {
        scopedOnly: { type: "boolean", description: "Publish only as @intisy-ai/<name>, because the unscoped name is unavailable." },
      },
    },
    repo: {
      type: "object",
      description: "Repository metadata: the GitHub description and topic set are derived from it.",
      required: ["role", "category", "tech"],
      properties: {
        role: { type: "string", description: 'The role phrase, capitalized, without the fixed "for the intisy-ai AI-proxy ecosystem." suffix.' },
        category: { type: "string", description: "The single category topic, for example core-library or ai-provider." },
        domains: { type: "array", items: { type: "string" }, description: "Domain topics, for example claude or gemini." },
        tech: { type: "string", description: "The primary tech topic, typescript or java." },
      },
    },
    presentation: {
      type: "object",
      description: "How surfaces render this plugin and what it represents.",
      properties: {
        providers: {
          type: "object",
          additionalProperties: { type: "string" },
          description: "Icon path per provider id this plugin serves.",
        },
      },
    },
  },
};
