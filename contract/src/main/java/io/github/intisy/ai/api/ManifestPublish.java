package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** How the repo is published, to npm and as Java release assets. */
@TsInterface(data = true)
public interface ManifestPublish {
    /** Publish only as `@intisy-ai/<name>`, because the unscoped name is unavailable. */
    @TsOptional
    boolean scopedOnly();

    /**
     * The Gradle modules whose jars ship as release assets, each named by its own classifier.
     *
     * @implNote A list rather than one name because a consumer resolves each module separately: they
     * serve different Gradle configurations, and one shaded jar would put every module on every
     * consumer's runtime classpath.
     */
    @TsOptional
    List<String> jarModule();

    /** The README is rendered at build time, so the release promotes it rather than testing it. */
    @TsOptional
    boolean generatedReadme();

    /** Run the Gradle build before the tests, because a test needs its jar installed first. */
    @TsOptional
    boolean jarPretest();
}
