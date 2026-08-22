package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsConstant;

/**
 * Constants the api itself publishes.
 *
 * @implNote The api field of a manifest is a floor, not a build tag: a host refuses only a plugin
 * whose declared floor exceeds what the host implements. It reads 2 because a typed key is an
 * object, and an api-1 host expecting a string would register a capability named for the object's
 * coercion.
 */
public final class Api {

    /** The API major version this package implements. */
    @TsConstant(type = "number", literal = "2")
    public static final int API_VERSION = 2;

    private Api() {
    }
}
