package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;

/** The shape {@code JsErrors.mint} actually attaches to a marked JavaScript {@code Error}. */
@TsInterface
public interface PluginErrorShape {
    /**
     * Always `PluginError`, which is how the boundary recognises one.
     *
     * @return the marker string
     */
    @TsProperty(readOnly = true)
    String name();

    /**
     * The detail and the fix, composed for a reader.
     *
     * @return the composed message
     */
    @TsProperty(readOnly = true)
    String message();

    /**
     * The plugin the failure is attributed to.
     *
     * @return the plugin's id
     */
    @TsProperty(readOnly = true)
    String pluginId();

    /**
     * What went wrong.
     *
     * @return the failure detail
     */
    @TsProperty(readOnly = true)
    String detail();

    /**
     * How to put it right.
     *
     * @return the fix instructions
     */
    @TsProperty(readOnly = true)
    String fix();
}
