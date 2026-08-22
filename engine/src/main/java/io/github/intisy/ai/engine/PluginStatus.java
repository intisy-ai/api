package io.github.intisy.ai.engine;

/** Where a plugin stands in its lifecycle, as the host last saw it. */
public enum PluginStatus {
    ACTIVATING,
    ACTIVE,
    BROKEN,
    STOPPED
}
