package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;
import java.util.Map;

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

    /**
     * Further marks this repo ships, each keyed by the id of the thing it belongs to.
     *
     * @implNote One repo can contribute several named things, and a single repo-level {@code icon}
     * cannot serve them. Keyed by id rather than by kind, so this package carries the marks without
     * learning what any of the ids name.
     */
    @TsOptional
    Map<String, String> icons();

    /** Host-facing abilities this plugin provides at activation, declared statically so a host can answer what it can do without executing it. */
    @TsOptional
    List<String> capabilities();

    /** The inter-plugin contract. */
    @TsOptional
    ManifestServices services();

    /** Slash commands this plugin contributes, which a host deploys without importing it. */
    @TsOptional
    List<ManifestCommand> commands();

    /** This plugin's settings as it ships them. */
    @TsOptional
    ManifestConfig config();

    /** Where this plugin keeps state that is not named after it. */
    @TsOptional
    ManifestData data();

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

    /** What this plugin contributes to a host's catalog of installable things. */
    @TsOptional
    ManifestMarketplace marketplace();

    /**
     * The app this repo is the loader for, declared by the app's own project.
     *
     * @implNote Present only on a repo that IS an app's loader, which is what makes "whose loader is
     * this" answerable from the manifest alone, with no consumer naming a plugin.
     */
    @TsOptional
    AppDescriptor app();
}
