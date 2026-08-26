package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** One category a plugin adds to a host's catalog of installable things. */
@TsInterface(data = true)
public interface MarketplaceCategory {
    /** The category's id, unique across every plugin declaring one. */
    String id();

    /** The name a surface shows. Absent means the id is shown. */
    @TsOptional
    String label();

    /** Which entries this category holds. */
    MarketplaceMatch match();
}
