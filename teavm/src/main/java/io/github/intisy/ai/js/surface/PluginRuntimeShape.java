package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** What a host supplies per plugin, the second argument to {@code contextFor}. */
@TsInterface(data = true)
public interface PluginRuntimeShape {
    /**
     * The plugin's resolved configuration.
     *
     * @return the configuration value, opaque to this surface
     */
    Object config();

    /**
     * The plugin's logger.
     *
     * @return the logger value, opaque to this surface
     */
    Object log();

    /**
     * The storage directories of the home the plugin runs in.
     *
     * @return the storage paths
     */
    PluginPathsShape paths();

    /**
     * The event bus, scoped to this plugin as its source.
     *
     * @return the event bus
     */
    EventBusShape events();

    /**
     * Every app home the host knows about. Absent means this host knows of none but its own.
     *
     * @return the home registry, or absent when this host knows of no home but its own
     */
    @TsOptional
    HomeRegistryShape homes();
}
