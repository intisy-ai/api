package io.github.intisy.ai.seam;

import java.util.List;
import java.util.Map;

/**
 * Narrowing helpers over the {@code JsonCodec} parsed shape ({@code Map}/{@code List}/
 * {@code String}/{@code Number}/{@code Boolean}/{@code null}). No reflection, no gson: every store
 * in this ecosystem hand-rolls its {@code Map<String,Object>} to-POJO conversion through these
 * helpers so the code stays transpilable.
 *
 * @implNote In layer 1 rather than beside either caller, because both the account store and the
 * routing engine narrow the same parsed shape, and two layer-3 libraries needing one type means the
 * type belongs below them.
 */
public final class JsonUtil {
    private JsonUtil() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object o) {
        return o instanceof List ? (List<Object>) o : null;
    }

    public static String asString(Object o) {
        return o instanceof String ? (String) o : null;
    }

    public static Boolean asBoolean(Object o) {
        return o instanceof Boolean ? (Boolean) o : null;
    }

    public static Long asLong(Object o) {
        return o instanceof Number ? ((Number) o).longValue() : null;
    }

    public static Integer asInt(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : null;
    }
}
