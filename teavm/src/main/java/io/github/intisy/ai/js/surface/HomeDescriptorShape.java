package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** One app home the host knows about, as the host supplies it. */
@TsInterface(data = true)
public interface HomeDescriptorShape {
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
     * Whether this home exists on disk.
     *
     * @return true when the home's directory exists, false when it does not
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
    PluginPathsShape paths();
}
