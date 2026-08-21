package io.github.intisy.ai.js;

import io.github.intisy.ai.engine.Activation;
import io.github.intisy.ai.engine.ActivationPlan;
import io.github.intisy.ai.engine.Diagnostics;
import io.github.intisy.ai.engine.ManifestValidator;
import io.github.intisy.ai.engine.PluginException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSExport;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;

/** The engine's JavaScript module surface. Shape only: every decision below belongs to the engine. */
public final class EngineJs {

    /** A host's diagnostic destination, arriving as a JavaScript function. */
    @JSFunctor
    public interface Sink extends JSObject {
        void accept(String message);
    }

    private EngineJs() {
    }

    /**
     * @implNote Activation.order works on ids and two service maps rather than manifests, so the
     * extraction happens here. That is shape work, which is this class's whole job.
     */
    @JSExport
    public static JSObject activationOrder(JSArray<JSObject> manifests) {
        List<String> ids = new ArrayList<String>();
        Map<String, List<String>> provides = new LinkedHashMap<String, List<String>>();
        Map<String, List<String>> consumes = new LinkedHashMap<String, List<String>>();
        for (int index = 0; index < manifests.getLength(); index++) {
            Object tree = JsJson.toTree(manifests.get(index));
            String id = text(tree, "id");
            if (id == null) {
                continue;
            }
            ids.add(id);
            Object services = member(tree, "services");
            provides.put(id, strings(member(services, "provides")));
            consumes.put(id, strings(member(services, "consumes")));
        }
        ActivationPlan plan = Activation.order(ids, provides, consumes);
        return plan(JsJson.fromStrings(plan.getOrder()), JsJson.fromStringLists(plan.getCycles()));
    }

    /**
     * @implNote ManifestValidator.require already composes the message and the fix, so this catches
     * and re-raises rather than describing the failure again. A second sentence here would be the
     * engine's sentence written twice. wellKnownServices is optional rather than a second Java
     * overload, because two Java methods sharing a name cannot both become a JavaScript export; a
     * caller that omits it validates against an empty vocabulary, so a bare provide of a service id
     * such as "accounts" is rejected as squatting until the caller names it well-known.
     */
    @JSExport
    public static JSObject assertManifest(JSObject manifest, JSArray<JSObject> wellKnownServices) {
        List<String> known = new ArrayList<String>();
        if (wellKnownServices != null && !nullish(wellKnownServices)) {
            for (int index = 0; index < wellKnownServices.getLength(); index++) {
                known.add(String.valueOf(JsJson.toTree(wellKnownServices.get(index))));
            }
        }
        try {
            ManifestValidator.require(JsJson.toTree(manifest), known);
        } catch (PluginException failure) {
            JsErrors.raise(JsErrors.of(failure));
        }
        return manifest;
    }

    @JSExport
    public static JSObject pluginError(String pluginId, String detail, String fix) {
        return JsErrors.mint(pluginId, detail, fix);
    }

    @JSExport
    public static boolean isPluginError(JSObject value) {
        return JsErrors.marked(value);
    }

    /** Null falls back to the environment, which a TeaVM build has none of, so null means off here. */
    @JSExport
    public static void setStrict(JSObject enabled) {
        Diagnostics.setStrict(booleanOrNull(enabled));
    }

    @JSExport
    public static JsPluginHost createPluginHost(JSObject options) {
        return JsPluginHost.from(JsJson.toTree(options));
    }

    @JSExport
    public static void setDiagnosticSink(final Sink sink) {
        if (sink == null) {
            Diagnostics.setSink(null);
            return;
        }
        Diagnostics.setSink(new Diagnostics.Sink() {
            @Override
            public void accept(String message) {
                sink.accept(message);
            }
        });
    }

    private static Object member(Object tree, String name) {
        return tree instanceof Map ? ((Map<?, ?>) tree).get(name) : null;
    }

    private static String text(Object tree, String name) {
        Object found = member(tree, name);
        return found == null ? null : String.valueOf(found);
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<String>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                out.add(String.valueOf(item));
            }
        }
        return out;
    }

    private static Boolean booleanOrNull(JSObject value) {
        if (value == null || nullish(value)) {
            return null;
        }
        return Boolean.valueOf(truthy(value));
    }

    @JSBody(params = "value", script = "return value === null || value === undefined;")
    private static native boolean nullish(JSObject value);

    @JSBody(params = "value", script = "return !!value;")
    private static native boolean truthy(JSObject value);

    @JSBody(params = {"order", "cycles"}, script = "return { order: order, cycles: cycles };")
    private static native JSObject plan(JSObject order, JSObject cycles);
}
