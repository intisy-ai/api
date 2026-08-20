package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;

/** The shape {@code JsErrors.mint} actually attaches to a marked JavaScript {@code Error}. */
@TsInterface
public interface PluginErrorShape {
    @TsProperty(readOnly = true)
    String name();

    @TsProperty(readOnly = true)
    String message();

    @TsProperty(readOnly = true)
    String pluginId();

    @TsProperty(readOnly = true)
    String detail();

    @TsProperty(readOnly = true)
    String fix();
}
