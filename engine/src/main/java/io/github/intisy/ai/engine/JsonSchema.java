package io.github.intisy.ai.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The subset of JSON Schema draft-07 this package validates against.
 *
 * @implNote additionalProperties:false is deliberately unrepresentable: an object property with no
 * declared schema is ignored rather than rejected, which is how the open-vocabulary rule reaches the
 * manifest. Declaring additionalProperties as a schema instead is how a record type is expressed,
 * and every value in it is then checked against it.
 */
public final class JsonSchema {

    private final String type;
    private String schemaDraft;
    private String schemaId;
    private String title;
    private String description;
    private Map<String, JsonSchema> properties;
    private List<String> required;
    private JsonSchema items;
    private JsonSchema additionalProperties;
    private String pattern;
    private Integer minimum;
    private List<String> enumValues;
    private String fix;

    private JsonSchema(String type) {
        this.type = type;
    }

    public static JsonSchema ofType(String type) {
        return new JsonSchema(type);
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public Map<String, JsonSchema> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, JsonSchema> value) {
        this.properties = value;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> value) {
        this.required = value;
    }

    public JsonSchema getItems() {
        return items;
    }

    public void setItems(JsonSchema value) {
        this.items = value;
    }

    public JsonSchema getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(JsonSchema value) {
        this.additionalProperties = value;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String value) {
        this.pattern = value;
    }

    public Integer getMinimum() {
        return minimum;
    }

    public void setMinimum(Integer value) {
        this.minimum = value;
    }

    /** Named enumValues because enum is a Java keyword. */
    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> value) {
        this.enumValues = value;
    }

    /**
     * @implNote Not a JSON Schema keyword. Validators ignore unknown keywords, so carrying the fix
     * beside the rule keeps errors-that-teach a property of the data rather than of the reporter.
     */
    public String getFix() {
        return fix;
    }

    public void setFix(String value) {
        this.fix = value;
    }

    /** Draft this schema is written against, emitted as $schema and otherwise unused. */
    public String getSchemaDraft() {
        return schemaDraft;
    }

    public void setSchemaDraft(String value) {
        this.schemaDraft = value;
    }

    /** Canonical URL this schema is published at, emitted as $id and otherwise unused. */
    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String value) {
        this.schemaId = value;
    }

    /**
     * This schema as the plain tree a JSON writer or a JavaScript caller consumes.
     *
     * @implNote One key order for every object, taken from this class's field order, because the
     * published file is regenerated and compared as text. Per-object hand-chosen orders cannot
     * survive a serializer, so pinning one order here is what makes the comparison stable. Absent
     * values are omitted rather than written as null: a null keyword is not the same as an unstated
     * one to a JSON Schema validator.
     */
    public Map<String, Object> toTree() {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        put(out, "$schema", schemaDraft);
        put(out, "$id", schemaId);
        put(out, "title", title);
        put(out, "description", description);
        put(out, "type", type);
        if (properties != null) {
            Map<String, Object> nested = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, JsonSchema> property : properties.entrySet()) {
                nested.put(property.getKey(), property.getValue().toTree());
            }
            out.put("properties", nested);
        }
        put(out, "required", required);
        if (items != null) {
            out.put("items", items.toTree());
        }
        if (additionalProperties != null) {
            out.put("additionalProperties", additionalProperties.toTree());
        }
        put(out, "pattern", pattern);
        put(out, "minimum", minimum);
        put(out, "enum", enumValues);
        put(out, "fix", fix);
        return out;
    }

    private static void put(Map<String, Object> out, String key, Object value) {
        if (value != null) {
            out.put(key, value);
        }
    }
}
