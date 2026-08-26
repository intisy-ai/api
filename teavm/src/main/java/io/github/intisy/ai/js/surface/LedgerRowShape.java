package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** One plugin's row in the ledger a host keeps. */
@TsInterface(data = true)
public interface LedgerRowShape {
    /**
     * The plugin this row describes.
     *
     * @return the plugin's id
     */
    String pluginId();

    /**
     * Where the plugin stands: activating, active, broken or stopped.
     *
     * @return the status string
     */
    String status();

    /**
     * Capability ids its manifest declared.
     *
     * @return the declared capability ids, or empty when it declared none
     */
    List<String> capabilitiesDeclared();

    /**
     * Capability ids it actually provided.
     *
     * @return the provided capability ids, or empty when it has provided none
     */
    List<String> capabilitiesProvided();

    /**
     * Service ids it registered.
     *
     * @return the registered service ids, or empty when it has registered none
     */
    List<String> servicesProvided();

    /**
     * Service ids it asked for, answered or not.
     *
     * @return the requested service ids, or empty when it has asked for none
     */
    List<String> servicesConsumed();

    /**
     * Event topics it subscribed to.
     *
     * @return the subscribed topics, or empty when it has subscribed to none
     */
    List<String> topics();

    /**
     * Permissions its manifest declares.
     *
     * @return the declared permissions, or empty when it declares none
     */
    List<String> permissions();

    /**
     * Why it is broken, when it is.
     *
     * @return the failure detail and fix, or absent when the plugin is not broken
     */
    @TsOptional
    LedgerErrorShape error();
}
