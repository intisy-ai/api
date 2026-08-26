package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** One category a plugin adds to a host's catalog of installable things. */
@TsInterface(data = true)
public interface MarketplaceCategory {
    /**
     * The category's id, unique across every plugin declaring one.
     *
     * @return the category id
     */
    String id();

    /**
     * The name a surface shows. Absent means the id is shown.
     *
     * @return the display label, or absent when the id is shown instead
     */
    @TsOptional
    String label();

    /**
     * Which entries this category holds.
     *
     * @return the match rule for this category's entries
     */
    MarketplaceMatch match();
}
