package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;

/** How to tell whether an app is installed. */
@TsInterface(data = true)
public interface AppDetect {
    /** The executable a user launches, looked up on the path. */
    String binary();

    /** The npm package the app ships as, for a global-install check. */
    String pkg();
}
