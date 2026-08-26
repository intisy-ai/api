package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/**
 * The app config file a model catalog is merged into.
 *
 * @implNote Absent on the descriptor means nothing is merged and a consumer reads its own model
 * cache directly.
 */
@TsInterface(data = true)
public interface AppModelCatalog {
    /** Files to try in order, relative to the app home. */
    List<String> files();

    /** Environment variable naming the config file outright. */
    @TsOptional
    String envOverride();

    /** The app's config schema, for an editor's completion. */
    @TsOptional
    String schemaUrl();

    /**
     * The key inside that file holding the catalog.
     *
     * @implNote Named after the app's OWN config key, which is data this package quotes rather than
     * a category it serves: it never reads what the key contains.
     */
    String providerKey();
}
