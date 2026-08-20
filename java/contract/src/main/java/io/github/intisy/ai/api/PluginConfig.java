package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@TsInterface
public interface PluginConfig {
    Map<String, Object> all();

    @TsNullable
    <T> T get(String key);

    /**
     * @implNote Asynchronous even where a host implements it synchronously, because the seam has to
     * survive a host that runs the plugin out of process.
     */
    CompletionStage<Void> set(String key, Object value);
}
