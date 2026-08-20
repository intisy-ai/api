package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** Why a broken plugin's ledger row says it is broken. */
@TsInterface(data = true)
public interface LedgerErrorShape {
    String detail();

    String fix();
}
