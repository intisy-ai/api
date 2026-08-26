package io.github.intisy.ai.js;

import io.github.intisy.ai.engine.Activation;
import io.github.intisy.ai.engine.ActivationPlan;
import io.github.intisy.ai.engine.Diagnostics;
import io.github.intisy.ai.engine.ManifestSchema;
import io.github.intisy.ai.engine.ManifestValidator;
import io.github.intisy.ai.engine.PluginException;
import io.github.intisy.ai.engine.SchemaIssue;
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
        /**
         * Writes one diagnostic line.
         *
         * @param message the diagnostic text to write
         */
        void accept(String message);
    }

    private EngineJs() {
    }

    /**
     * Orders manifests so a service provider activates before its consumer, naming any cycle.
     *
     * @implNote Activation.order works on ids and two service maps rather than manifests, so the
     * extraction happens here. That is shape work, which is this class's whole job.
     * @param manifests the parsed plugin.json objects to order
     * @return an object with an {@code order} array and a {@code cycles} array of arrays
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
     * Validates a manifest against the schema, throwing the first problem as a plugin error.
     *
     * @implNote ManifestValidator.require already composes the message and the fix, so this catches
     * and re-raises rather than describing the failure again. A second sentence here would be the
     * engine's sentence written twice. wellKnownServices is optional rather than a second Java
     * overload, because two Java methods sharing a name cannot both become a JavaScript export; a
     * caller that omits it validates against an empty vocabulary, so a bare provide of a service id
     * such as "accounts" is rejected as squatting until the caller names it well-known.
     * @param manifest the parsed plugin.json object to validate
     * @param wellKnownServices the bare service ids this host accepts without a namespace, or null
     * @return the same manifest, unchanged, once it has been validated
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

    /**
     * Every problem with a manifest, rather than the first.
     *
     * @implNote An omitted wellKnownServices becomes null here, not the empty list assertManifest
     * uses, and the difference is the whole point of both signatures. assertManifest's caller is a
     * host, which knows its own vocabulary and is held to it. This one's callers include a plugin
     * author's own test suite, which cannot know a vocabulary that lives outside api, so null tells
     * the engine to skip the question rather than answer it wrongly.
     * @param manifest the parsed plugin.json object to validate
     * @param wellKnownServices the bare service ids this host accepts without a namespace, or null
     * to skip the bare-service check entirely
     * @return every issue found, or an empty array when the manifest is valid
     */
    @JSExport
    public static JSArray<JSObject> validateManifest(JSObject manifest, JSArray<JSObject> wellKnownServices) {
        List<String> known = null;
        if (wellKnownServices != null && !nullish(wellKnownServices)) {
            known = new ArrayList<String>();
            for (int index = 0; index < wellKnownServices.getLength(); index++) {
                known.add(String.valueOf(JsJson.toTree(wellKnownServices.get(index))));
            }
        }
        List<SchemaIssue> issues = ManifestValidator.validate(JsJson.toTree(manifest), known);
        JSArray<JSObject> out = JSArray.create();
        for (int index = 0; index < issues.size(); index++) {
            SchemaIssue issue = issues.get(index);
            out.set(index, validationIssue(issue.getPath(), issue.getMessage(), issue.getFix()));
        }
        return out;
    }

    /**
     * The published JSON Schema of plugin.json, as a tree ready to stringify.
     *
     * @return the schema tree
     */
    @JSExport
    public static JSObject manifestSchema() {
        return JsJson.fromTree(ManifestSchema.get().toTree());
    }

    /**
     * Mints a plugin error a caller can throw, marked so any bundle recognises it.
     *
     * @param pluginId the id of the plugin the error belongs to
     * @param detail what went wrong
     * @param fix what the plugin author should do about it
     * @return the error, ready to throw
     */
    @JSExport
    public static JSObject pluginError(String pluginId, String detail, String fix) {
        return JsErrors.mint(pluginId, detail, fix);
    }

    /**
     * Whether a caught value is a plugin error, recognised by its marker rather than its class.
     *
     * @param value the caught value to inspect
     * @return true when the value carries the plugin error marker, false otherwise
     */
    @JSExport
    public static boolean isPluginError(JSObject value) {
        return JsErrors.marked(value);
    }

    /**
     * Turns quiet failures loud. Null falls back to the environment, which a TeaVM build has none
     * of, so null means off here.
     *
     * @param enabled true to raise strict diagnostics, false or null to leave them quiet
     */
    @JSExport
    public static void setStrict(JSObject enabled) {
        Diagnostics.setStrict(booleanOrNull(enabled));
    }

    /**
     * Opens a host: the capability registry, the service hub, the event bus and the ledger.
     *
     * @param options the app id, api version, surfaces and vocabulary the host declares
     * @return the opened host
     */
    @JSExport
    public static JsPluginHost createPluginHost(JSObject options) {
        return JsPluginHost.from(JsJson.toTree(options));
    }

    /**
     * Installs where diagnostics are written, or null to stop writing them.
     *
     * @param sink the destination for each diagnostic message, or null to stop writing them
     */
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

    @JSBody(params = {"path", "message", "fix"}, script = "return { path: path, message: message, fix: fix };")
    private static native JSObject validationIssue(String path, String message, String fix);
}
