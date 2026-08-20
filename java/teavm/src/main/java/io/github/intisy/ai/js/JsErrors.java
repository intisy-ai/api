package io.github.intisy.ai.js;

import io.github.intisy.ai.engine.PluginException;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * The one place a Java failure becomes a JavaScript one.
 *
 * @implNote The marker is a name string rather than a class, because a plugin, a host and a
 * dashboard each bundle their own copy of this package and class identity does not survive an
 * independent bundle. On the JVM the same failure is a PluginException and instanceof is right;
 * neither answer works in both places, so the boundary translates.
 */
final class JsErrors {

    private JsErrors() {
    }

    static JSObject mint(String pluginId, String detail, String fix) {
        return build(pluginId, detail, fix, "[" + pluginId + "] " + detail + "\n  fix: " + fix);
    }

    static JSObject of(PluginException failure) {
        return mint(failure.getPluginId(), failure.getDetail(), failure.getFix());
    }

    @JSBody(params = {"pluginId", "detail", "fix", "message"}, script =
            "var error = new Error(message);"
            + "error.name = 'PluginError';"
            + "error.pluginId = pluginId;"
            + "error.detail = detail;"
            + "error.fix = fix;"
            + "return error;")
    private static native JSObject build(String pluginId, String detail, String fix, String message);

    @JSBody(params = "error", script = "throw error;")
    static native void raise(JSObject error);

    @JSBody(params = "value", script =
            "return value !== null && typeof value === 'object' && value.name === 'PluginError';")
    static native boolean marked(JSObject value);
}
