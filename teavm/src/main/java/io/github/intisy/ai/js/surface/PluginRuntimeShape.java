package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** What a host supplies per plugin, the second argument to {@code contextFor}. */
@TsInterface(data = true)
public interface PluginRuntimeShape {
    /** The plugin's resolved configuration. */
    Object config();

    /** The plugin's logger. */
    Object log();

    /** The storage directories of the home the plugin runs in. */
    PluginPathsShape paths();

    /** The event bus, scoped to this plugin as its source. */
    EventBusShape events();

    /** Every app home the host knows about. Absent means this host knows of none but its own. */
    @TsOptional
    HomeRegistryShape homes();
}
