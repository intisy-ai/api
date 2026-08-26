package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/**
 * How an app runs a plugin at startup when it has no npm-plugin list of its own.
 *
 * @implNote Data, not code, so an app declaring neither this nor an npm mechanism auto-loads
 * nothing rather than being special-cased by a host.
 */
@TsInterface(data = true)
public interface AppStartupHook {
    /**
     * The file to write, relative to the app home.
     *
     * @return the file path, relative to the app home
     */
    String file();

    /**
     * The key path to the array the entry joins.
     *
     * @return the key path segments, from the file's root object down to the target array
     */
    List<String> path();

    /**
     * A JSON template whose strings have the `{plugin}` placeholder replaced with the plugin's name.
     *
     * @return the JSON template to join into the array
     */
    Object entry();
}
