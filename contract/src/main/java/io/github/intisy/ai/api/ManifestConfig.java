package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.Map;

/**
 * A plugin's settings as it ships them.
 *
 * @implNote Values only. What a setting is CALLED and how a surface renders it is the settings
 * capability's business, which this contract may not know: a manifest that carried labels would be
 * minting vocabulary, and the api mints none.
 */
@TsInterface(data = true)
public interface ManifestConfig {
    /** Every setting this plugin has, and what it is worth until a home changes it. */
    Map<String, Object> defaults();
}
