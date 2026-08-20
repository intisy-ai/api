package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** One plugin's implementation of a capability, with the plugin it came from. */
@TsInterface(data = true)
public interface CapabilityRecordShape {
    String pluginId();

    Object implementation();
}
