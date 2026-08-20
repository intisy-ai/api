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
    String id();

    int api();

    @TsOptional
    String entry();

    @TsOptional
    String displayName();

    @TsOptional
    String icon();

    @TsOptional
    List<String> capabilities();

    @TsOptional
    ManifestServices services();

    @TsOptional
    List<String> permissions();

    @TsOptional
    ManifestLifecycle lifecycle();

    @TsOptional
    ManifestPublish publish();

    @TsOptional
    RepoMeta repo();
}
