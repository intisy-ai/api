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

    @Test
    void knowsNoCategories() throws IOException {
        String text = emitted();
        String[] categories = {"provider", "front-door", "screens", "settings", "commands",
            "plugin-management", "cross-app-sync", "custom-endpoints", "config-history",
            "marketplace-source", "CapabilityMap", "ServiceMap", "EventMap", "IrRequest", "routing",
            "accounts", "activity"};
        for (String category : categories) {
            assertFalse(text.contains(category), "the contract must not know the category " + category);
        }
    }
}
