package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/**
 * The contents of a repo's plugin.json.
 *
 * @implNote No index signature and no additionalProperties: an author gets excess-property checking
 * while writing one, and the runtime validator still ignores fields it does not know, so a manifest
 * written against a later version loads on today's host.
 */
@TsInterface(data = true)
public interface PluginManifest {
    /**
     * Pointer at the published manifest schema, for an editor's completion and validation.
     *
     * @implNote Declared although nothing reads it, because every manifest in the ecosystem carries
     * it and the published schema accepts it: without it an author writing a manifest literal in
     * TypeScript could not include a field their own plugin.json has.
     */
    @TsOptional
    String $schema();

    /** The plugin's permanent identity, matching its repository name. */
    String id();

    /** The lowest API major version this plugin needs. A floor, not a build tag. */
    int api();

    /** The built module a host imports. Required once `capabilities` is non-empty. */
    @TsOptional
    String entry();

    /** The name a surface shows instead of the id. */
    @TsOptional
    String displayName();

    /** Path to a square-viewBox SVG mark, relative to the repo root. */
    @TsOptional
    String icon();

    /** Host-facing abilities this plugin provides at activation, declared statically so a host can answer what it can do without executing it. */
    @TsOptional
    List<String> capabilities();

    /** The inter-plugin contract. */
    @TsOptional
    ManifestServices services();

    /** Declared permissions, surfaced at install and in dashboards. Not sandbox-enforced. */
    @TsOptional
    List<String> permissions();

    /** Which optional lifecycle hooks the entry exports. */
    @TsOptional
    ManifestLifecycle lifecycle();

    /** How the repo is published to npm. */
    @TsOptional
    ManifestPublish publish();

    /** Repository metadata. */
    @TsOptional
    RepoMeta repo();
}
