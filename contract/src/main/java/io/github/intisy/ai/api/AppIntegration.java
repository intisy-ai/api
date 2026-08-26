package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsLiteral;

/** How an app reaches the local API. */
public enum AppIntegration {
    /** The app is pointed at the proxy by an environment variable carrying its base URL. */
    @TsLiteral("env-baseurl")
    ENV_BASEURL,

    /** The app loads the plugin itself, so nothing is proxied. */
    @TsLiteral("native")
    NATIVE
}
