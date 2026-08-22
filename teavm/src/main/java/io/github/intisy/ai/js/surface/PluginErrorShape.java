package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;

/** The shape {@code JsErrors.mint} actually attaches to a marked JavaScript {@code Error}. */
@TsInterface
public interface PluginErrorShape {
    /** Always `PluginError`, which is how the boundary recognises one. */
    @TsProperty(readOnly = true)
    String name();

    /** The detail and the fix, composed for a reader. */
    @TsProperty(readOnly = true)
    String message();

    /** The plugin the failure is attributed to. */
    @TsProperty(readOnly = true)
    String pluginId();

    /** What went wrong. */
    @TsProperty(readOnly = true)
    String detail();

    /** How to put it right. */
    @TsProperty(readOnly = true)
    String fix();
}
