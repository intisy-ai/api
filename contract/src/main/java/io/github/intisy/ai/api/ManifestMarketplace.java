package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/** What this plugin contributes to a host's catalog of installable things. */
@TsInterface(data = true)
public interface ManifestMarketplace {
    /**
     * Categories this plugin adds.
     *
     * @return the marketplace categories this plugin contributes
     */
    List<MarketplaceCategory> categories();
}
