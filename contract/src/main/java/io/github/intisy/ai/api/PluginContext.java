package io.github.intisy.ai.api;

import io.github.intisy.ai.api.seam.Logger;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;
import java.util.List;

/**
 * Everything a plugin may touch, and the only way it touches any of it.
 *
 * @implNote Taking everything through the context is what makes the introspection ledger and the
 * doctor free rather than separately built, and it is the seam a host would use to run a plugin out
 * of process without changing plugin code.
 */
@TsInterface
public interface PluginContext {
    /**
     * Supplies the implementation behind a capability the manifest declares.
     *
     * @implNote One signature, never a plain-string overload beside it: the pair would let a wrong
     * payload compile through the string side.
     * @param <T> the capability's implementation type
     * @param type the capability key, from {@link #capability(String)}
     * @param implementation this plugin's implementation of that capability
     */
    <T> void provide(CapabilityType<T> type, T implementation);

    /**
     * The typed key for a capability id, so a plugin can provide one without linking the library
     * that mints it.
     *
     * @implNote The id is data the manifest already states, and the payload type comes from a
     * type-only import, which erases at build time. That pair is what makes a plugin's only runtime
     * dependency this package.
     * @param <T> the capability's implementation type
     * @param id the capability id, as the manifest declares it
     * @return the typed key for that id
     */
    <T> CapabilityType<T> capability(String id);

    /**
     * The typed key for a service id, so a plugin can reach another's API, or offer its own, without
     * linking the library that mints the id.
     *
     * @implNote The counterpart of {@link #capability(String)} for the ids a manifest states under
     * {@code services}. Without it a plugin would have to write the key literal itself, which is the
     * plugin minting vocabulary rather than naming an id it already declares.
     * @param <T> the service's API type
     * @param id the service id, as the manifest declares it
     * @return the typed key for that id
     */
    <T> ServiceType<T> service(String id);

    /**
     * The typed key for an event topic, so a plugin can publish or subscribe without linking the
     * library that names the topic.
     *
     * @implNote Same reason as {@link #service(String)}: a topic is an id, and a plugin that had to
     * build the key by hand would be minting the vocabulary instead of naming it.
     * @param <T> the topic's payload type
     * @param id the topic id, as the manifest declares it
     * @return the typed key for that id
     */
    <T> TopicType<T> topic(String id);

    /**
     * Every app home the host knows about, whether or not each exists on disk.
     *
     * @implNote Asked rather than held, because a home can appear, and can be installed, while a
     * plugin is running. {@code paths} is this plugin's own home; this is every home there is.
     * @return every app home the host knows about
     */
    List<HomeDescriptor> homes();

    /**
     * What this plugin may know about the host.
     *
     * @return this plugin's host descriptor
     */
    @TsProperty(readOnly = true)
    HostDescriptor host();

    /**
     * This plugin's resolved configuration.
     *
     * @return this plugin's config
     */
    @TsProperty(readOnly = true)
    PluginConfig config();

    /**
     * This plugin's logger.
     *
     * @return this plugin's logger
     */
    @TsProperty(readOnly = true)
    Logger log();

    /**
     * The storage directories of the home this plugin runs in.
     *
     * @return this plugin's storage paths
     */
    @TsProperty(readOnly = true)
    PluginPaths paths();

    /**
     * This plugin's own manifest, as the host validated it.
     *
     * @return this plugin's manifest
     */
    @TsProperty(readOnly = true)
    PluginManifest manifest();

    /**
     * How this plugin reaches another plugin's API, and offers its own.
     *
     * @return this plugin's services handle
     */
    @TsProperty(readOnly = true)
    Services services();

    /**
     * How this plugin says something happened, and hears that something did.
     *
     * @return this plugin's events handle
     */
    @TsProperty(readOnly = true)
    Events events();
}
