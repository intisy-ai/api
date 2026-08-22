package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** How the repo is published to npm. */
@TsInterface(data = true)
public interface ManifestPublish {
    /** Publish only as `@intisy-ai/<name>`, because the unscoped name is unavailable. */
    @TsOptional
    boolean scopedOnly();
}
