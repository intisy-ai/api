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
    /** The app id, for example {@code claude} or {@code opencode}. */
    String app();

    /** The name a surface shows instead of the id. */
    String label();

    /** Whether this home exists on disk. An absent home means that app is not installed. */
    boolean present();

    /** The id of the plugin this app is reached through, absent when it has none. */
    @TsOptional
    String loader();

    /** This home's storage directories. */
    PluginPaths paths();
}
