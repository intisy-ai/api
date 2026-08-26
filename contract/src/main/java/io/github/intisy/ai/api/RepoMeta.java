package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** Repository metadata, from which the GitHub description and topic set are derived. */
@TsInterface(data = true)
public interface RepoMeta {
    /**
     * The role phrase, capitalized, without the fixed ecosystem suffix.
     *
     * @return the role phrase
     */
    String role();

    /**
     * The single category topic, for example `core-library` or `dashboard`.
     *
     * @return the category topic
     */
    String category();

    /**
     * Domain topics, for example `claude` or `gemini`.
     *
     * @return the domain topics, or absent when this repo has none
     */
    @TsOptional
    List<String> domains();

    /**
     * The tech topics, for example `typescript`, `java` or `svelte`.
     *
     * @implNote A list rather than one primary topic, because a repo carrying a Java engine behind a
     * TypeScript package is both and describing it as either is wrong.
     * @return the tech topics
     */
    List<String> tech();

    /**
     * Topics this repo needs that no other rule derives, for example `github-actions`.
     *
     * @return the extra topics, or absent when this repo needs none
     */
    @TsOptional
    List<String> topics();
}
