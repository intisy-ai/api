package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** What a host says about itself when it builds its plugin host, the {@code createPluginHost} argument. */
@TsInterface(data = true)
public interface PluginHostOptionsShape {
    /** The app id plugins see on the host descriptor. */
    String app();

    /** The API major version to claim. Defaults to this package's own. */
    @TsOptional
    Integer api();

    /** Surface ids this host renders. */
    @TsOptional
    List<String> surfaces();

    /** Capability ids this host understands. Absent means unverifiable, not empty. */
    @TsOptional
    List<String> vocabulary();

    /** Bare service ids any plugin may register. Absent means none exist. */
    @TsOptional
    List<String> wellKnownServices();
}
