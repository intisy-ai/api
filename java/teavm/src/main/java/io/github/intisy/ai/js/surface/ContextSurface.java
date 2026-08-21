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

    /**
     * @implNote The key is untyped rather than a String because the contract's {@code provide} hands
     * over a typed key object and a host hands over a bare id, and this one function serves both. The
     * engine still works in ids: the id is read off the key here, at the boundary, which is where
     * every other shape difference is resolved.
     */
    void provide(Object key, Object implementation);
}
