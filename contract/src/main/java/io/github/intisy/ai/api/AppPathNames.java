package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;

/**
 * The names of the four storage subdirectories inside an app home.
 *
 * @implNote Names rather than paths: an app whose layout differs, or a user who wants its storage
 * elsewhere, changes these rather than any consumer. A consumer resolves them into absolute paths
 * rather than joining the literal names.
 */
@TsInterface(data = true)
public interface AppPathNames {
    /**
     * Where plugin checkouts live.
     *
     * @return the repos subdirectory name
     */
    String repos();

    /**
     * Where deployed plugin bundles and their manifest sidecars live.
     *
     * @return the plugin subdirectory name
     */
    String plugin();

    /**
     * Where cached downloads live.
     *
     * @return the cache subdirectory name
     */
    String cache();

    /**
     * Where configuration files live.
     *
     * @return the config subdirectory name
     */
    String config();
}
