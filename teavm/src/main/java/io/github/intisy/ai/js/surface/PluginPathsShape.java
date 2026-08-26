package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** The storage directories of the home a plugin runs in. */
@TsInterface(data = true)
public interface PluginPathsShape {
    /**
     * The app home directory.
     *
     * @return the home directory path
     */
    String home();

    /**
     * Where plugin checkouts live.
     *
     * @return the repos directory path
     */
    String repos();

    /**
     * Where deployed bundles and their manifest sidecars live.
     *
     * @return the plugin directory path
     */
    String plugin();

    /**
     * Where cached downloads live.
     *
     * @return the cache directory path
     */
    String cache();

    /**
     * Where configuration files live.
     *
     * @return the config directory path
     */
    String config();
}
