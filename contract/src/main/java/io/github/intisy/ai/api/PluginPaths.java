package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;

/**
 * The absolute storage directories of the app home a plugin is running in.
 *
 * @implNote A plugin joins paths onto these rather than assembling a home path itself, so a renamed
 * directory takes effect everywhere at once.
 */
@TsInterface(data = true)
public interface PluginPaths {
    /** The app home directory. */
    String home();

    /** Where plugin checkouts live. */
    String repos();

    /** Where deployed plugin bundles and their manifest sidecars live. */
    String plugin();

    /** Where cached downloads live. */
    String cache();

    /** Where configuration files live. */
    String config();
}
