package io.github.intisy.ai.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class ManifestEmissionTest {

    private static String read(String name) throws IOException {
        File file = new File("build/classes/java/main/" + name);
        assertTrue(file.isFile(), "the processor must have written " + file.getAbsolutePath());
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    @Test
    void declaresTheManifestWithOptionalFields() throws IOException {
        String text = read("api.d.ts");
        assertTrue(text.contains("export interface PluginManifest {"), "PluginManifest must be emitted");
        assertTrue(text.contains("  id: string;"), "id is required");
        assertTrue(text.contains("  api: number;"), "api is required");
        assertTrue(text.contains("  entry?: string;"), "entry is optional");
        assertTrue(text.contains("  capabilities?: string[];"), "capabilities is an optional string list");
    }

    @Test
    void emitsTheApiFloorAsATwo() throws IOException {
        assertTrue(read("api.keys.ts").contains("export const API_VERSION: number = 2;"),
                "the floor must rise to 2, because an api-1 host cannot accept a typed key");
    }
}
