package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsProperty;
import java.util.List;

/**
 * The object {@code createPluginHost} returns.
 *
 * @implNote {@code supports} and {@code verifyActivation} return {@link PluginErrorShape}, marked
 * {@link TsNullable}, rather than a raw {@code Error}: {@code PluginErrorShape} is the real shape
 * {@code JsErrors.mint} attaches, so a caller reads {@code detail}/{@code fix} with no cast.
 * {@code markBroken} takes the same shape for its error argument, since a caller passes back exactly
 * what {@code pluginError} produced.
 */
@TsInterface
public interface HostSurface {
    @TsProperty(readOnly = true)
    HostDescriptorShape descriptor();

    @TsProperty(readOnly = true)
    LedgerFacadeShape ledger();

    @TsNullable
    PluginErrorShape supports(Object manifest);

    ContextSurface contextFor(Object manifest, Object runtime);

    @TsNullable
    PluginErrorShape verifyActivation(Object manifest);

    List<CapabilityRecordShape> capability(String id);

    Object service(String id);

    void markBroken(String pluginId, PluginErrorShape error);

    void release(String pluginId);
}
