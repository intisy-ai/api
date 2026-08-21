package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/** What a host tells a plugin about itself. */
@TsInterface(data = true)
public interface HostDescriptorShape {
    /** The app id, for example `claude` or `opencode`. */
    String app();

    /** The API major version this host implements. */
    int api();

    /** Surface ids this host renders. */
    List<String> surfaces();
}
