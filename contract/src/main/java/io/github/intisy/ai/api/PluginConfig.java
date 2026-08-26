package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/** One plugin's configuration, already resolved by the host. */
@TsInterface
public interface PluginConfig {
    /**
     * Every setting, defaults merged with what is on disk.
     *
     * @return the setting keys mapped to their effective values
     */
    Map<String, Object> all();

    /**
     * One setting, absent when it is neither set nor defaulted.
     *
     * @param <T> the setting's expected value type
     * @param key the setting's name
     * @return the setting's effective value, or {@code null} when it is neither set nor defaulted
     */
    @TsNullable
    <T> T get(String key);

    /**
     * Writes one setting.
     *
     * @implNote Asynchronous even where a host implements it synchronously, because the seam has to
     * survive a host that runs the plugin out of process.
     * @param key the setting's name
     * @param value the value to write
     * @return a stage that completes once the setting is written
     */
    CompletionStage<Void> set(String key, Object value);
}
