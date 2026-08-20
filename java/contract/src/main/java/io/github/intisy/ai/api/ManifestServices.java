package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** What a plugin offers other plugins, and what it asks of them. */
@TsInterface(data = true)
public interface ManifestServices {
    @TsOptional
    List<String> provides();

    @TsOptional
    List<String> consumes();
}
