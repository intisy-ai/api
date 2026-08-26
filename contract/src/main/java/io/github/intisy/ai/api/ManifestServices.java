package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** What a plugin offers other plugins, and what it asks of them. */
@TsInterface(data = true)
public interface ManifestServices {
    /**
     * Service ids this plugin registers, each namespaced by its own id or a well-known bare id.
     *
     * @return the service ids this plugin provides, or absent when it provides none
     */
    @TsOptional
    List<String> provides();

    /**
     * Service ids this plugin asks for, used for activation ordering.
     *
     * @return the service ids this plugin consumes, or absent when it consumes none
     */
    @TsOptional
    List<String> consumes();
}
