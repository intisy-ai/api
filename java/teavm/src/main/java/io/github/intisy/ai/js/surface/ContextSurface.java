package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;

/** The context a plugin's activate receives. */
@TsInterface
public interface ContextSurface {
    @TsProperty(readOnly = true)
    Object manifest();

    @TsProperty(readOnly = true)
    HostDescriptorShape host();

    @TsProperty(readOnly = true)
    Object config();

    @TsProperty(readOnly = true)
    Object log();

    @TsProperty(readOnly = true)
    PluginPathsShape paths();

    @TsProperty(readOnly = true)
    ServiceRegistryShape services();

    @TsProperty(readOnly = true)
    EventBusShape events();

    void provide(String id, Object implementation);
}
