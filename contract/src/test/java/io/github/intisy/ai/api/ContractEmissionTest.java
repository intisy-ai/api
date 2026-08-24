package io.github.intisy.ai.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class ContractEmissionTest {

    private static String emitted() throws IOException {
        File file = new File("build/classes/java/main/api.d.ts");
        assertTrue(file.isFile(), "the processor must have written " + file.getAbsolutePath());
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    @Test
    void declaresTheMetaVocabulary() throws IOException {
        String text = emitted();
        assertTrue(text.contains("export interface CapabilityType<T> {"), "CapabilityType must be emitted");
        assertTrue(text.contains("readonly __phantom?: T;"), "a phantom marker must be emitted");
        assertTrue(text.contains("provide<T>(type: CapabilityType<T>, implementation: T): void;"),
                "provide must take a typed key and its implementation");
        assertTrue(text.contains("activate(context: PluginContext): void | Promise<void>;"),
                "activate must be maybe-async");
        assertTrue(text.contains("install?(context: PluginContext): void | Promise<void>;"),
                "install must be optional and maybe-async");
    }

    /**
     * @implNote The ids are looked for as quoted LITERALS rather than as bare substrings. A bare
     * substring stopped meaning what this test means the day the manifest gained a {@code commands}
     * field and a {@code config} whose prose says "settings": naming a manifest field after the
     * thing it carries is not the same as minting a capability, and only a literal id or a type
     * named after one would be.
     */
    @Test
    void knowsNoCategories() throws IOException {
        String text = emitted();
        String[] ids = {"provider", "front-door", "screens", "settings", "commands",
            "plugin-management", "cross-app-sync", "custom-endpoints", "config-history",
            "marketplace-source", "routing", "accounts", "activity"};
        for (String id : ids) {
            assertFalse(text.contains("\"" + id + "\"") || text.contains("'" + id + "'"),
                    "the contract must not mint the id " + id);
        }
        String[] registries = {"CapabilityMap", "ServiceMap", "EventMap", "IrRequest"};
        for (String registry : registries) {
            assertFalse(text.contains(registry), "the contract must not declare " + registry);
        }
    }
}
