package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;
import java.util.List;

/**
 * The object {@code createPluginHost} returns.
 *
 * @implNote {@code supports} and {@code verifyActivation} return {@code unknown} rather than a raw
 * {@code Error | null}: a caller narrows the result itself (as the contract test does, casting it to
 * a small shape before reading {@code detail}), and typing it as the ambient {@code Error} would make
 * that narrowing cast fail TypeScript's insufficient-overlap check, since {@code Error} carries no
 * {@code detail} property of its own.
 */
@TsInterface
public interface HostSurface {
    @TsProperty(readOnly = true)
    HostDescriptorShape descriptor();

    @TsProperty(readOnly = true)
    LedgerFacadeShape ledger();

    Object supports(Object manifest);

    ContextSurface contextFor(Object manifest, Object runtime);

    Object verifyActivation(Object manifest);

    List<CapabilityRecordShape> capability(String id);

    Object service(String id);

    void markBroken(String pluginId, Object error);

    void release(String pluginId);
}
