package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** Where an app keeps its home directory, in the order a resolver tries. */
@TsInterface(data = true)
public interface AppHome {
    /**
     * Environment variable that overrides every candidate, set by a host driving this app.
     *
     * @return the environment variable name, or absent when no host override applies
     */
    @TsOptional
    String envOverride();

    /**
     * The app's OWN environment variable for its config directory, which it reads itself.
     *
     * @return the environment variable name, or absent when this app has none
     */
    @TsOptional
    String nativeEnv();

    /**
     * Subdirectory under the XDG config directory, when the app follows that layout.
     *
     * @return the XDG subdirectory name, or absent when this app does not follow that layout
     */
    @TsOptional
    String xdgSubdir();

    /**
     * Paths to try in order, each with a leading {@code ~} for the user home.
     *
     * @return the candidate home paths, tried in order
     */
    List<String> candidates();
}
