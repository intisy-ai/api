package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** One plugin's row in the ledger a host keeps. */
@TsInterface(data = true)
public interface LedgerRowShape {
    /** The plugin this row describes. */
    String pluginId();

    /** Where the plugin stands: activating, active, broken or stopped. */
    String status();

    /** Capability ids its manifest declared. */
    List<String> capabilitiesDeclared();

    /** Capability ids it actually provided. */
    List<String> capabilitiesProvided();

    /** Service ids it registered. */
    List<String> servicesProvided();

    /** Service ids it asked for, answered or not. */
    List<String> servicesConsumed();

    /** Event topics it subscribed to. */
    List<String> topics();

    /** Permissions its manifest declares. */
    List<String> permissions();

    /** Why it is broken, when it is. */
    @TsOptional
    LedgerErrorShape error();
}
