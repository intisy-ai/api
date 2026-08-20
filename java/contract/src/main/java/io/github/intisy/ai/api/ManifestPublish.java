package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

@TsInterface(data = true)
public interface ManifestPublish {
    @TsOptional
    boolean scopedOnly();
}
