package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** The storage directories of the home a plugin runs in. */
@TsInterface(data = true)
public interface PluginPathsShape {
    /** The app home directory. */
    String home();

    /** Where plugin checkouts live. */
    String repos();

    /** Where deployed bundles and their manifest sidecars live. */
    String plugin();

    /** Where cached downloads live. */
    String cache();

    /** Where configuration files live. */
    String config();
}
