package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsProperty;
import java.util.List;

/**
 * The object {@code createPluginHost} returns.
 *
 * @implNote {@code supports} and {@code verifyActivation} are {@code | null} rather than
 * {@code | undefined}, because a check that ran and found nothing wrong is a stated answer and the
 * adapter genuinely returns null. {@code service} is {@code | undefined}, because a missing service
 * is an absent map entry rather than an answer. {@code markBroken} takes the same
 * {@link PluginErrorShape} for its error argument, since a caller passes back exactly what
 * {@code pluginError} produced.
 */
@TsInterface
public interface HostSurface {
    /** What every plugin is told about this host. */
    @TsProperty(readOnly = true)
    HostDescriptorShape descriptor();

    /** The record of what each plugin declared and provided. */
    @TsProperty(readOnly = true)
    LedgerFacadeShape ledger();

    /** Why this host cannot load the manifest, or null when it can. */
    @TsNullable(asNull = true)
    PluginErrorShape supports(Object manifest);

    /** Opens one plugin's context, fenced to its own namespace. */
    ContextSurface contextFor(Object manifest, PluginRuntimeShape runtime);

    /** Why what the plugin provided disagrees with what it declared, or null when they agree. */
    @TsNullable(asNull = true)
    PluginErrorShape verifyActivation(Object manifest);

    /** Every implementation of one capability, in activation order. */
    List<CapabilityRecordShape> capability(String id);

    /** The service registered under an id, or undefined when nothing is. */
    @TsNullable
    Object service(String id);

    /**
     * Offers a service the host itself implements, for every plugin to reach.
     *
     * @implNote How a plugin gets behaviour belonging to a library it may not link: the host links
     * it once and hands over a typed handle, rather than every plugin carrying a private copy. Call
     * it before starting plugins, so a plugin asking during activate finds it.
     */
    void provideService(String id, Object service);

    /** Quarantines a plugin, dropping its registrations and recording why. */
    void markBroken(String pluginId, PluginErrorShape error);

    /** Drops a stopped plugin's registrations without marking it broken. */
    void release(String pluginId);
}
