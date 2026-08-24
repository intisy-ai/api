package io.github.intisy.ai.js;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSString;

/**
 * A JavaScript value as the tree the engine reads, and back.
 *
 * @implNote The engine works on plain trees on purpose, so this conversion is the whole cost of that
 * choice and it is paid once, here, rather than by giving the engine a JSON type it would then have
 * to transpile. Numbers arrive as Double because JavaScript has one number type; the engine's schema
 * validator already accepts that.
 */
final class JsJson {

    private JsJson() {
    }

    static Object toTree(JSObject value) {
        if (value == null || isNullish(value)) {
            return null;
        }
        if (isArray(value)) {
            JSArray<JSObject> items = (JSArray<JSObject>) value;
            List<Object> out = new ArrayList<Object>();
            for (int index = 0; index < items.getLength(); index++) {
                out.add(toTree(items.get(index)));
            }
            return out;
        }
        String kind = typeOf(value);
        if ("string".equals(kind)) {
            return ((JSString) value).stringValue();
        }
        if ("number".equals(kind)) {
            return Double.valueOf(((JSNumber) value).doubleValue());
        }
        if ("boolean".equals(kind)) {
            return Boolean.valueOf(((JSBoolean) value).booleanValue());
        }
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        JSArray<JSString> names = keys(value);
        for (int index = 0; index < names.getLength(); index++) {
            String name = names.get(index).stringValue();
            out.put(name, toTree(read(value, name)));
        }
        return out;
    }

    /**
     * @implNote Integer and Double are both handled because the engine's schema carries whole
     * numbers as Integer while a tree that came from JavaScript carries them as Double, and this
     * direction is reached from both.
     */
    static JSObject fromTree(Object tree) {
        if (tree == null) {
            return null;
        }
        if (tree instanceof Map) {
            JSObject out = emptyObject();
            Map<?, ?> entries = (Map<?, ?>) tree;
            for (Map.Entry<?, ?> entry : entries.entrySet()) {
                write(out, String.valueOf(entry.getKey()), fromTree(entry.getValue()));
            }
            return out;
        }
        if (tree instanceof List) {
            List<?> items = (List<?>) tree;
            JSArray<JSObject> out = JSArray.create();
            for (int index = 0; index < items.size(); index++) {
                out.set(index, fromTree(items.get(index)));
            }
            return out;
        }
        if (tree instanceof Boolean) {
            return JSBoolean.valueOf(((Boolean) tree).booleanValue());
        }
        if (tree instanceof Integer) {
            return JSNumber.valueOf(((Integer) tree).intValue());
        }
        if (tree instanceof Double) {
            return JSNumber.valueOf(((Double) tree).doubleValue());
        }
        return JSString.valueOf(String.valueOf(tree));
    }

    static JSObject fromStrings(List<String> values) {
        JSArray<JSString> out = JSArray.create();
        for (int index = 0; index < values.size(); index++) {
            out.set(index, JSString.valueOf(values.get(index)));
        }
        return out;
    }

    static JSObject fromStringLists(List<List<String>> values) {
        JSArray<JSObject> out = JSArray.create();
        for (int index = 0; index < values.size(); index++) {
            out.set(index, fromStrings(values.get(index)));
        }
        return out;
    }

    @JSBody(params = "value", script = "return value === null || value === undefined;")
    private static native boolean isNullish(JSObject value);

    @JSBody(params = "value", script = "return Array.isArray(value);")
    private static native boolean isArray(JSObject value);

    @JSBody(params = "value", script = "return typeof value;")
    private static native String typeOf(JSObject value);

    @JSBody(params = "value", script = "return Object.keys(value);")
    private static native JSArray<JSString> keys(JSObject value);

    @JSBody(params = {"value", "name"}, script = "return value[name];")
    private static native JSObject read(JSObject value, String name);

    @JSBody(script = "return {};")
    private static native JSObject emptyObject();

    @JSBody(params = {"target", "name", "value"}, script = "target[name] = value;")
    private static native void write(JSObject target, String name, JSObject value);
}
