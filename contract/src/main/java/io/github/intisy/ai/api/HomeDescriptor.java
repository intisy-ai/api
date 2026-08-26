package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/**
 * One app home the host knows about.
 *
 * @implNote A plugin whose job spans more than its own home takes them from the host rather than
 * resolving a registry itself, which is what keeps it from linking the library that owns the
 * registry's shape.
 */
@TsInterface(data = true)
public interface HomeDescriptor {
    /**
     * The app id, for example {@code claude} or {@code opencode}.
     *
     * @return the app id
     */
    String app();

    /**
     * The name a surface shows instead of the id.
     *
     * @return the display label
     */
    String label();

    /**
     * Whether this home exists on disk. An absent home means that app is not installed.
     *
     * @return true when this home's directory exists, false when it does not
     */
    boolean present();

    /**
     * The id of the plugin this app is reached through, absent when it has none.
     *
     * @return the loader plugin's id, or absent when this app is reached with no loader
     */
    @TsOptional
    String loader();

    /**
     * This home's storage directories.
     *
     * @return the storage paths
     */
    PluginPaths paths();
}
