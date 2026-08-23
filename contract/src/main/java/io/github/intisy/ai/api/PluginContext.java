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
     */
    <T> void provide(CapabilityType<T> type, T implementation);

    /**
     * The typed key for a capability id, so a plugin can provide one without linking the library
     * that mints it.
     *
     * @implNote The id is data the manifest already states, and the payload type comes from a
     * type-only import, which erases at build time. That pair is what makes a plugin's only runtime
     * dependency this package.
     */
    <T> CapabilityType<T> capability(String id);

    /**
     * Every app home the host knows about, whether or not each exists on disk.
     *
     * @implNote Asked rather than held, because a home can appear, and can be installed, while a
     * plugin is running. {@code paths} is this plugin's own home; this is every home there is.
     */
    List<HomeDescriptor> homes();

    /** What this plugin may know about the host. */
    @TsProperty(readOnly = true)
    HostDescriptor host();

    /** This plugin's resolved configuration. */
    @TsProperty(readOnly = true)
    PluginConfig config();

    /** This plugin's logger. */
    @TsProperty(readOnly = true)
    Logger log();

    /** The storage directories of the home this plugin runs in. */
    @TsProperty(readOnly = true)
    PluginPaths paths();

    /** This plugin's own manifest, as the host validated it. */
    @TsProperty(readOnly = true)
    PluginManifest manifest();

    /** How this plugin reaches another plugin's API, and offers its own. */
    @TsProperty(readOnly = true)
    Services services();

    /** How this plugin says something happened, and hears that something did. */
    @TsProperty(readOnly = true)
    Events events();
}
