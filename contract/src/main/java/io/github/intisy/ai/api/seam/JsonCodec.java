package io.github.intisy.ai.api.seam;

/**
 * JSON boundary. The parsed shape is a plain {@code Object} tree built from
 * {@code java.util.Map}/{@code java.util.List}/{@code String}/{@code Number}/{@code Boolean}/{@code null},
 * matching what gson and JS {@code JSON.parse} both naturally produce.
 */
public interface JsonCodec {
    /**
     * Parses a JSON string into the plain-object tree described above.
     *
     * @param json the JSON text
     * @return the parsed tree, or {@code null} when {@code json} is null, empty, or blank
     */
    Object parse(String json);

    /**
     * Renders a plain-object tree back to JSON text.
     *
     * @param value a tree of {@code Map}/{@code List}/{@code String}/{@code Number}/{@code Boolean}/{@code null}
     * @return the JSON text
     */
    String stringify(Object value);
}
