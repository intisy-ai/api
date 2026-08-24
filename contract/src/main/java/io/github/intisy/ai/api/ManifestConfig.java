package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.Map;

/**
 * A plugin's settings as it ships them.
 *
 * @implNote Values only. What a setting is CALLED and how a surface renders it is the settings
 * capability's business, which this contract may not know: a manifest that carried labels would be
 * minting vocabulary, and the api mints none.
 */
@TsInterface(data = true)
public interface ManifestConfig {
    /**
     * The file these settings live in, {@code config/<name>.json}, when that is not the plugin's id.
     *
     * @implNote Absent means the id, which is the case for all but a plugin whose settings file
     * predates its repository name. Stated rather than assumed, because a surface that guesses
     * writes to a file the plugin never reads.
     */
    @TsOptional
    String name();

    /** Every setting this plugin has, and what it is worth until a home changes it. */
    Map<String, Object> defaults();
}
