package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** One plugin's implementation of a capability, with the plugin it came from. */
@TsInterface(data = true)
public interface CapabilityRecordShape {
    /** The plugin that provided this implementation. */
    String pluginId();

    /** What the plugin passed to `provide`. */
    Object implementation();
}
