package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;
import io.github.intisy.ai.tsemit.TsRaw;

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
     * @implNote One signature, never a plain-string overload beside it: the pair would let a wrong
     * payload compile through the string side.
     */
    <T> void provide(CapabilityType<T> type, T implementation);

    @TsProperty(readOnly = true)
    HostDescriptor host();

    @TsProperty(readOnly = true)
    PluginConfig config();

    @TsProperty(readOnly = true)
    Logger log();

    @TsProperty(readOnly = true)
    PluginPaths paths();

    /**
     * @implNote Raw because the manifest type arrives with the manifest module in the next task, and
     * a forward reference would emit an undeclared name.
     */
    @TsProperty(readOnly = true)
    @TsRaw("PluginManifest")
    Object manifest();
}
