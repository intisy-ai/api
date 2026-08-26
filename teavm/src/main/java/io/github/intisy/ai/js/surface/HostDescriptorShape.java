package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/** What a host tells a plugin about itself. */
@TsInterface(data = true)
public interface HostDescriptorShape {
    /**
     * The app id, for example `claude` or `opencode`.
     *
     * @return the app id
     */
    String app();

    /**
     * The API major version this host implements.
     *
     * @return the api version number
     */
    int api();

    /**
     * Surface ids this host renders.
     *
     * @return the rendered surface ids, or empty when the host renders none
     */
    List<String> surfaces();
}
