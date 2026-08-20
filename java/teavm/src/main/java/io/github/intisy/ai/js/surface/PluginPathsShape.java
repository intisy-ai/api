package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** The storage directories of the home a plugin runs in. */
@TsInterface(data = true)
public interface PluginPathsShape {
    String home();

    String repos();

    String plugin();

    String cache();

    String config();
}
