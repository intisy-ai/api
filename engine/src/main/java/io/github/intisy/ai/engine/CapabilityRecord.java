package io.github.intisy.ai.engine;

/** One plugin's implementation of a capability, with the plugin it came from. */
public final class CapabilityRecord {

    private final String pluginId;
    private final Object implementation;

    CapabilityRecord(String pluginId, Object implementation) {
        this.pluginId = pluginId;
        this.implementation = implementation;
    }

    /** @return the id of the plugin that supplied this implementation */
    public String getPluginId() {
        return pluginId;
    }

    /** @return the plugin's implementation object, which a caller casts to the capability's own interface */
    public Object getImplementation() {
        return implementation;
    }
}
