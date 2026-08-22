package io.github.intisy.ai.engine;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The schema of plugin.json.
 *
 * @implNote No object declares additionalProperties as absent-meaning-closed: an unknown field is
 * ignored, which is what lets a manifest written against a later version of this package load on
 * today's host. The presentation block is deliberately absent, because its only field is an icon
 * path per provider id and that names a category this package must not know.
 */
public final class ManifestSchema {

    /** Canonical URL a manifest points $schema at, served by the docs site. */
    public static final String SCHEMA_ID = "https://intisy-ai.github.io/api/schema/plugin.schema.json";

    /** Draft the published schema declares, which no validator in this package reads. */
    public static final String DRAFT = "http://json-schema.org/draft-07/schema#";

    private ManifestSchema() {
    }

    public static JsonSchema get() {
        Map<String, JsonSchema> properties = new LinkedHashMap<String, JsonSchema>();
        properties.put("$schema", described(JsonSchema.ofType("string"),
                "Pointer at the published manifest schema, for an editor's completion and validation."));

        JsonSchema id = described(JsonSchema.ofType("string"), "The plugin's permanent identity, matching its repository name.");
        id.setPattern("^[a-z0-9]+(-[a-z0-9]+)*$");
        id.setFix("use lowercase words joined by single hyphens, for example \"config-ledger\"");
        properties.put("id", id);

        JsonSchema api = described(JsonSchema.ofType("integer"), "The lowest API major version this plugin needs. A floor, not a build tag.");
        api.setMinimum(Integer.valueOf(1));
        api.setFix("set \"api\" to the lowest API major version this plugin needs, for example 1");
        properties.put("api", api);

        JsonSchema entry = described(JsonSchema.ofType("string"), "The built module a host imports. Required once capabilities are declared.");
        entry.setFix("point \"entry\" at the built module a host imports, for example \"dist/index.js\"");
        properties.put("entry", entry);

        properties.put("displayName", described(JsonSchema.ofType("string"), "The name a surface shows instead of the id."));
        properties.put("icon", described(JsonSchema.ofType("string"), "Path to a square-viewBox SVG mark, relative to the repo root."));

        JsonSchema capabilities = described(JsonSchema.ofType("array"), "Host-facing abilities this plugin provides at activation.");
        capabilities.setItems(JsonSchema.ofType("string"));
        capabilities.setFix("list capability ids as strings, for example [\"provider\", \"screens\"]");
        properties.put("capabilities", capabilities);

        properties.put("services", services());

        JsonSchema permissions = described(JsonSchema.ofType("array"), "Declared permissions, surfaced at install and in dashboards.");
        permissions.setItems(JsonSchema.ofType("string"));
        properties.put("permissions", permissions);

        properties.put("lifecycle", lifecycle());
        properties.put("publish", publish());
        properties.put("repo", repo());

        JsonSchema root = described(JsonSchema.ofType("object"), "The single machine-readable description of a repo in the intisy-ai ecosystem.");
        root.setSchemaDraft(DRAFT);
        root.setSchemaId(SCHEMA_ID);
        root.setTitle("intisy-ai plugin manifest");
        root.setRequired(Arrays.asList("id", "api"));
        root.setProperties(properties);
        return root;
    }

    private static JsonSchema services() {
        Map<String, JsonSchema> properties = new LinkedHashMap<String, JsonSchema>();
        JsonSchema provides = described(JsonSchema.ofType("array"),
                "Service ids this plugin registers, each namespaced by its own id or a well-known bare id.");
        provides.setItems(JsonSchema.ofType("string"));
        properties.put("provides", provides);
        JsonSchema consumes = described(JsonSchema.ofType("array"), "Service ids this plugin asks for.");
        consumes.setItems(JsonSchema.ofType("string"));
        properties.put("consumes", consumes);
        JsonSchema services = described(JsonSchema.ofType("object"),
                "The inter-plugin contract: what this plugin offers other plugins, and what it asks of them.");
        services.setProperties(properties);
        return services;
    }

    private static JsonSchema lifecycle() {
        Map<String, JsonSchema> properties = new LinkedHashMap<String, JsonSchema>();
        properties.put("install", described(JsonSchema.ofType("boolean"), "The entry exports install(ctx), run once after first deploy."));
        properties.put("repair", described(JsonSchema.ofType("boolean"), "The entry exports repair(ctx), run on demand from a host."));
        JsonSchema lifecycle = described(JsonSchema.ofType("object"), "Which optional lifecycle hooks the entry module exports.");
        lifecycle.setProperties(properties);
        return lifecycle;
    }

    private static JsonSchema publish() {
        Map<String, JsonSchema> properties = new LinkedHashMap<String, JsonSchema>();
        properties.put("scopedOnly", described(JsonSchema.ofType("boolean"),
                "Publish only as @intisy-ai/<name>, because the unscoped name is unavailable."));
        JsonSchema publish = described(JsonSchema.ofType("object"), "How the repo is published to npm.");
        publish.setProperties(properties);
        return publish;
    }

    private static JsonSchema repo() {
        Map<String, JsonSchema> properties = new LinkedHashMap<String, JsonSchema>();
        properties.put("role", described(JsonSchema.ofType("string"),
                "The role phrase, capitalized, without the fixed \"for the intisy-ai AI-proxy ecosystem.\" suffix."));
        properties.put("category", described(JsonSchema.ofType("string"),
                "The single category topic, for example core-library or ai-provider."));
        JsonSchema domains = described(JsonSchema.ofType("array"), "Domain topics, for example claude or gemini.");
        domains.setItems(JsonSchema.ofType("string"));
        properties.put("domains", domains);
        properties.put("tech", described(JsonSchema.ofType("string"), "The primary tech topic, typescript or java."));
        JsonSchema repo = described(JsonSchema.ofType("object"),
                "Repository metadata: the GitHub description and topic set are derived from it.");
        repo.setRequired(Arrays.asList("role", "category", "tech"));
        repo.setProperties(properties);
        return repo;
    }

    private static JsonSchema described(JsonSchema schema, String description) {
        schema.setDescription(description);
        return schema;
    }
}
