package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/**
 * Where a marketplace looks for an app's community plugins.
 *
 * @implNote Absent on the descriptor means a consumer offers only its own verified list.
 */
@TsInterface(data = true)
public interface AppDiscovery {
    /** The repository topic a community plugin carries. */
    @TsOptional
    String topic();

    /** A free-text search to run where the topic alone under-reports. */
    @TsOptional
    String searchQuery();

    /** A curated list to read, as a raw URL. */
    @TsOptional
    String awesomeList();
}
