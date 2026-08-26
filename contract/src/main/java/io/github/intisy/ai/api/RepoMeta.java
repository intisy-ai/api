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

    /**
     * The tech topics, for example `typescript`, `java` or `svelte`.
     *
     * @implNote A list rather than one primary topic, because a repo carrying a Java engine behind a
     * TypeScript package is both and describing it as either is wrong.
     */
    List<String> tech();

    /** Topics this repo needs that no other rule derives, for example `github-actions`. */
    @TsOptional
    List<String> topics();
}
