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
    /** Where plugin checkouts live. */
    String repos();

    /** Where deployed plugin bundles and their manifest sidecars live. */
    String plugin();

    /** Where cached downloads live. */
    String cache();

    /** Where configuration files live. */
    String config();
}
