package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;

@TsInterface(data = true)
public interface PluginPaths {
    String home();

    String repos();

    String plugin();

    String cache();

    String config();
}
