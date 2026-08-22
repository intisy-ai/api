package io.github.intisy.ai.engine;

/** One plugin's implementation of a capability, with the plugin it came from. */
public final class CapabilityRecord {

    private final String pluginId;
    private final Object implementation;

    CapabilityRecord(String pluginId, Object implementation) {
        this.pluginId = pluginId;
        this.implementation = implementation;
    }

    public String getPluginId() {
        return pluginId;
    }

    public Object getImplementation() {
        return implementation;
    }
}
