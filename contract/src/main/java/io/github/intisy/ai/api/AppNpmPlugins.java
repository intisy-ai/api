package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/**
 * An app's own npm-plugin mechanism.
 *
 * @implNote Absent on the descriptor means the app has none, so a consumer offers no npm rows, no
 * npm section and no npm install method rather than offering ones that cannot work.
 */
@TsInterface(data = true)
public interface AppNpmPlugins {
    /**
     * Config files to look in, in order, for the plugin list.
     *
     * @return the candidate config file paths, tried in order
     */
    List<String> configFiles();

    /**
     * The key inside those files holding the plugin list.
     *
     * @return the config key holding the plugin list
     */
    String pluginsKey();

    /**
     * Where the app caches the packages it installed.
     *
     * @return the package cache path, or absent when this app has none
     */
    @TsOptional
    String packageCache();

    /**
     * The app's config schema, for an editor's completion.
     *
     * @return the schema URL, or absent when this app publishes none
     */
    @TsOptional
    String schemaUrl();
}
