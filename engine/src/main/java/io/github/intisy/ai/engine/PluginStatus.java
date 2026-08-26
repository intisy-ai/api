package io.github.intisy.ai.engine;

/** Where a plugin stands in its lifecycle, as the host last saw it. */
public enum PluginStatus {
    /** Activation has started but not finished yet. */
    ACTIVATING,
    /** Activation finished and every declared capability was provided. */
    ACTIVE,
    /** Quarantined after a load or activation failure. */
    BROKEN,
    /** Released; its capabilities, services and subscriptions are gone. */
    STOPPED
}
