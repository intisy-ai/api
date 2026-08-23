package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;
import java.util.List;

/** The context a plugin's activate receives. */
@TsInterface
public interface ContextSurface {
    /** The plugin's own manifest, by identity, as it was parsed. */
    @TsProperty(readOnly = true)
    Object manifest();

    /** What the plugin may know about the host. */
    @TsProperty(readOnly = true)
    HostDescriptorShape host();

    /** The plugin's resolved configuration, as the runtime supplied it. */
    @TsProperty(readOnly = true)
    Object config();

    /** The plugin's logger, as the runtime supplied it. */
    @TsProperty(readOnly = true)
    Object log();

    /** The storage directories of the home the plugin runs in. */
    @TsProperty(readOnly = true)
    PluginPathsShape paths();

    /** Every app home the host knows about, asked fresh on each call. */
    List<HomeDescriptorShape> homes();

    /**
     * The typed key for a capability id.
     *
     * @implNote Untyped return rather than a key shape, mirroring {@code provide}: the engine works
     * in ids, and the contract is where the phantom type is attached.
     */
    Object capability(String id);

    /** The service registry, fenced to this plugin's namespace. */
    @TsProperty(readOnly = true)
    ServiceRegistryShape services();

    /** Publish and subscribe, attributed to this plugin. */
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
