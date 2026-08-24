package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** One app home the host knows about, as the host supplies it. */
@TsInterface(data = true)
public interface HomeDescriptorShape {
    /** The app id, for example {@code claude} or {@code opencode}. */
    String app();

    /** The name a surface shows instead of the id. */
    String label();

    /** Whether this home exists on disk. */
    boolean present();

    /** The id of the plugin this app is reached through, absent when it has none. */
    @TsOptional
    String loader();

    /** This home's storage directories. */
    PluginPathsShape paths();
}
