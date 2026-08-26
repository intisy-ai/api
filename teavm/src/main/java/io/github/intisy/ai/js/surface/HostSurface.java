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
    /**
     * What every plugin is told about this host.
     *
     * @return the host's descriptor
     */
    @TsProperty(readOnly = true)
    HostDescriptorShape descriptor();

    /**
     * The record of what each plugin declared and provided.
     *
     * @return the ledger
     */
    @TsProperty(readOnly = true)
    LedgerFacadeShape ledger();

    /**
     * Why this host cannot load the manifest, or null when it can.
     *
     * @param manifest the parsed plugin.json object to check
     * @return the problem found, or null when the host can load this manifest
     */
    @TsNullable(asNull = true)
    PluginErrorShape supports(Object manifest);

    /**
     * Opens one plugin's context, fenced to its own namespace.
     *
     * @param manifest the parsed plugin.json object of the plugin being activated
     * @param runtime the host-supplied runtime object for this plugin
     * @return the context the plugin's activate receives
     */
    ContextSurface contextFor(Object manifest, PluginRuntimeShape runtime);

    /**
     * Why what the plugin provided disagrees with what it declared, or null when they agree.
     *
     * @param manifest the parsed plugin.json object of the plugin being verified
     * @return the disagreement found, or null when the plugin honored its declaration
     */
    @TsNullable(asNull = true)
    PluginErrorShape verifyActivation(Object manifest);

    /**
     * Every implementation of one capability, in activation order.
     *
     * @param id the capability id to look up
     * @return the providing plugins' records, or empty when no plugin provides it
     */
    List<CapabilityRecordShape> capability(String id);

    /**
     * The service registered under an id, or undefined when nothing is.
     *
     * @param id the service id to look up
     * @return the registered service, or undefined when none is registered
     */
    @TsNullable
    Object service(String id);

    /**
     * Offers a service the host itself implements, for every plugin to reach.
     *
     * @implNote How a plugin gets behaviour belonging to a library it may not link: the host links
     * it once and hands over a typed handle, rather than every plugin carrying a private copy. Call
     * it before starting plugins, so a plugin asking during activate finds it.
     * @param id the service id to register under
     * @param service the value every plugin's {@code context.services.get(id)} then receives
     */
    void provideService(String id, Object service);

    /**
     * Quarantines a plugin, dropping its registrations and recording why.
     *
     * @param pluginId the id of the plugin to quarantine
     * @param error the problem that caused the quarantine
     */
    void markBroken(String pluginId, PluginErrorShape error);

    /**
     * Drops a stopped plugin's registrations without marking it broken.
     *
     * @param pluginId the id of the plugin to release
     */
    void release(String pluginId);
}
