package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** Repository metadata, from which the GitHub description and topic set are derived. */
@TsInterface(data = true)
public interface RepoMeta {
    /** The role phrase, capitalized, without the fixed ecosystem suffix. */
    String role();

    /** The single category topic, for example `core-library` or `dashboard`. */
    String category();

    /** Domain topics, for example `claude` or `gemini`. */
    @TsOptional
    List<String> domains();

    /** The primary tech topic, `typescript` or `java`. */
    String tech();
}
