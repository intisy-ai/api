package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** What a host supplies per plugin, the second argument to {@code contextFor}. */
@TsInterface(data = true)
public interface PluginRuntimeShape {
    Object config();

    Object log();

    PluginPathsShape paths();

    EventBusShape events();
}
