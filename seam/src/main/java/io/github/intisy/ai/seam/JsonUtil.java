package io.github.intisy.ai.seam;

import java.util.List;
import java.util.Map;

/**
 * Narrowing helpers over the {@code JsonCodec} parsed shape ({@code Map}/{@code List}/
 * {@code String}/{@code Number}/{@code Boolean}/{@code null}). No reflection, no gson: every store
 * in this ecosystem hand-rolls its conversion between {@code Map<String,Object>} and POJOs through these
 * helpers so the code stays transpilable.
 *
 * @implNote In layer 1 rather than beside either caller, because both the account store and the
 * routing engine narrow the same parsed shape, and two layer-3 libraries needing one type means the
 * type belongs below them.
 */
public final class JsonUtil {
    private JsonUtil() {
    }

    /**
     * Narrows a parsed JSON value to a map.
     *
     * @param o the parsed value to narrow
     * @return {@code o} cast to {@code Map<String, Object>}, or null when {@code o} is not a map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    /**
     * Narrows a parsed JSON value to a list.
     *
     * @param o the parsed value to narrow
     * @return {@code o} cast to {@code List<Object>}, or null when {@code o} is not a list
     */
    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object o) {
        return o instanceof List ? (List<Object>) o : null;
    }

    /**
     * Narrows a parsed JSON value to a string.
     *
     * @param o the parsed value to narrow
     * @return {@code o} cast to {@code String}, or null when {@code o} is not a string
     */
    public static String asString(Object o) {
        return o instanceof String ? (String) o : null;
    }

    /**
     * Narrows a parsed JSON value to a boolean.
     *
     * @param o the parsed value to narrow
     * @return {@code o} cast to {@code Boolean}, or null when {@code o} is not a boolean
     */
    public static Boolean asBoolean(Object o) {
        return o instanceof Boolean ? (Boolean) o : null;
    }

    /**
     * Narrows a parsed JSON number to a long.
     *
     * @param o the parsed value to narrow
     * @return {@code o} converted to a {@code long}, or null when {@code o} is not a number
     */
    public static Long asLong(Object o) {
        return o instanceof Number ? ((Number) o).longValue() : null;
    }

    /**
     * Narrows a parsed JSON number to an int.
     *
     * @param o the parsed value to narrow
     * @return {@code o} converted to an {@code int}, or null when {@code o} is not a number
     */
    public static Integer asInt(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : null;
    }
}
