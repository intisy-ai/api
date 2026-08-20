package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** One plugin's row in the ledger a host keeps. */
@TsInterface(data = true)
public interface LedgerRowShape {
    String pluginId();

    String status();

    List<String> capabilitiesDeclared();

    List<String> capabilitiesProvided();

    List<String> servicesProvided();

    List<String> servicesConsumed();

    List<String> topics();

    List<String> permissions();

    @TsOptional
    LedgerErrorShape error();
}
