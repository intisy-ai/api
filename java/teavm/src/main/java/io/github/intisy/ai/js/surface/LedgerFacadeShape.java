package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import java.util.List;

/** The ledger a host exposes on {@link HostSurface}. */
@TsInterface
public interface LedgerFacadeShape {
    List<LedgerRowShape> entries();

    @TsNullable
    LedgerRowShape entry(String pluginId);

    void recordDeclared(Object manifest);
}
