package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsProperty;
import java.util.List;

/**
 * The object {@code createPluginHost} returns.
 *
 * @implNote {@code supports} and {@code verifyActivation} return {@link PluginErrorShape} marked
 * {@code @TsNullable(asNull = true)}, because {@code host.ts} types both as {@code PluginError |
 * null} and the adapter genuinely returns {@code null}, not an omitted value. {@code service} is
 * marked plain {@link TsNullable} ({@code | undefined}), because a missing service is an absent map
 * entry, matching {@code host.ts}'s {@code service(id): unknown}. {@code markBroken} takes the same
 * {@link PluginErrorShape} for its error argument, since a caller passes back exactly what
 * {@code pluginError} produced.
 */
@TsInterface
public interface HostSurface {
    @TsProperty(readOnly = true)
    HostDescriptorShape descriptor();

    @TsProperty(readOnly = true)
    LedgerFacadeShape ledger();

    @TsNullable(asNull = true)
    PluginErrorShape supports(Object manifest);

    ContextSurface contextFor(Object manifest, PluginRuntimeShape runtime);

    @TsNullable(asNull = true)
    PluginErrorShape verifyActivation(Object manifest);

    List<CapabilityRecordShape> capability(String id);

    @TsNullable
    Object service(String id);

    void markBroken(String pluginId, PluginErrorShape error);

    void release(String pluginId);
}
