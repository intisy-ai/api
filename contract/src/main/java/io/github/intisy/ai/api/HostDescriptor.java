package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/**
 * What a plugin may know about its host.
 *
 * @implNote Deliberately not a plugin registry: a plugin adapts to which surfaces exist, never to
 * which app it happens to be in.
 */
@TsInterface(data = true)
public interface HostDescriptor {
    /**
     * The app id, for example `claude` or `opencode`.
     *
     * @return the app id
     */
    String app();

    /**
     * The API major version this host implements.
     *
     * @return the API major version
     */
    int api();

    /**
     * Surface ids this host renders, for example `tui` or `gui`. An unknown id is ignored.
     *
     * @return the surface ids this host renders
     */
    List<String> surfaces();
}
