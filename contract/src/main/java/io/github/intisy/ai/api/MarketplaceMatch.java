package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/**
 * Which catalog entries a contributed category holds.
 *
 * @implNote A match, never a list of entries, which is what keeps a category dynamic: something
 * published tomorrow carrying the topic appears with no change to the plugin that declared the
 * category, and no plugin code runs when the catalog is read.
 */
@TsInterface(data = true)
public interface MarketplaceMatch {
    /** Repository topics an entry must carry. */
    @TsOptional
    List<String> topics();

    /** The catalog kind an entry must be, as the reading host names its kinds. */
    @TsOptional
    String kind();
}
