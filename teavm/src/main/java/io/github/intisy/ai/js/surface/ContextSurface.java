package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;
import java.util.List;

/** The context a plugin's activate receives. */
@TsInterface
public interface ContextSurface {
    /**
     * The plugin's own manifest, by identity, as it was parsed.
     *
     * @return the parsed plugin.json tree
     */
    @TsProperty(readOnly = true)
    Object manifest();

    /**
     * What the plugin may know about the host.
     *
     * @return the host's app id, api version and declared surfaces
     */
    @TsProperty(readOnly = true)
    HostDescriptorShape host();

    /**
     * The plugin's resolved configuration, as the runtime supplied it.
     *
     * @return the configuration value, opaque to this surface
     */
    @TsProperty(readOnly = true)
    Object config();

    /**
     * The plugin's logger, as the runtime supplied it.
     *
     * @return the logger value, opaque to this surface
     */
    @TsProperty(readOnly = true)
    Object log();

    /**
     * The storage directories of the home the plugin runs in.
     *
     * @return the plugin's storage paths
     */
    @TsProperty(readOnly = true)
    PluginPathsShape paths();

    /**
     * Every app home the host knows about, asked fresh on each call.
     *
     * @return the known homes, or empty when the host declared none
     */
    List<HomeDescriptorShape> homes();

    /**
     * The typed key for a capability id.
     *
     * @implNote Untyped return rather than a key shape, mirroring {@code provide}: the engine works
     * in ids, and the contract is where the phantom type is attached.
     * @param id the capability id to mint a key for
     * @return the typed key
     */
    Object capability(String id);

    /**
     * The typed key for a service id.
     *
     * @implNote Same shape and same reason as {@link #capability(String)}: a key is an id at run
     * time, so the three key kinds differ only in the phantom type the contract attaches.
     * @param id the service id to mint a key for
     * @return the typed key
     */
    Object service(String id);

    /**
     * The typed key for an event topic id.
     *
     * @param id the topic id to mint a key for
     * @return the typed key
     */
    Object topic(String id);

    /**
     * The service registry, fenced to this plugin's namespace.
     *
     * @return the registry
     */
    @TsProperty(readOnly = true)
    ServiceRegistryShape services();

    /**
     * Publish and subscribe, attributed to this plugin.
     *
     * @return the event bus
     */
    @TsProperty(readOnly = true)
    EventBusShape events();

    /**
     * Registers the plugin's implementation of a capability.
     *
     * @implNote The key is untyped rather than a String because the contract's {@code provide} hands
     * over a typed key object and a host hands over a bare id, and this one function serves both. The
     * engine still works in ids: the id is read off the key here, at the boundary, which is where
     * every other shape difference is resolved.
     * @param key the typed capability key, or a bare id, to register against
     * @param implementation the value the plugin provides for that capability
     */
    void provide(Object key, Object implementation);
}
