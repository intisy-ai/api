package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import java.util.List;

/** The ledger a host exposes on {@link HostSurface}. */
@TsInterface
public interface LedgerFacadeShape {
    /**
     * One row per plugin the host has seen.
     *
     * @return the ledger rows, or empty when the host has seen no plugin
     */
    List<LedgerRowShape> entries();

    /**
     * One plugin's row, or undefined when the host has not seen it.
     *
     * @param pluginId the id of the plugin to look up
     * @return the plugin's row, or undefined when the host has not seen it
     */
    @TsNullable
    LedgerRowShape entry(String pluginId);

    /**
     * Records what a manifest declares, before its activation runs.
     *
     * @param manifest the parsed plugin.json object being recorded
     */
    void recordDeclared(Object manifest);
}
