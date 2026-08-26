package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** Why a broken plugin's ledger row says it is broken. */
@TsInterface(data = true)
public interface LedgerErrorShape {
    /**
     * What went wrong.
     *
     * @return the failure detail
     */
    String detail();

    /**
     * How to put it right.
     *
     * @return the fix instructions
     */
    String fix();
}
