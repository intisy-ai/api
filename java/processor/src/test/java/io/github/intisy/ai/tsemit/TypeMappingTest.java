package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class TypeMappingTest {

    @Test
    void mapsCollectionsFuturesAndBoxes() throws IOException {
        String emitted = EmitFixtureTest.emit("fixture.Store", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import java.util.List;",
                "import java.util.Map;",
                "import java.util.concurrent.CompletionStage;",
                "@TsInterface",
                "public interface Store {",
                "  Map<String, Object> all();",
                "  List<String> names();",
                "  CompletionStage<Void> save(String key, Object value);",
                "  Boolean enabled();",
                "  int count();",
                "}"));
        assertEquals(String.join("\n",
                "export interface Store {",
                "  all(): Record<string, unknown>;",
                "  count(): number;",
                "  enabled(): boolean;",
                "  names(): string[];",
                "  save(key: string, value: unknown): Promise<void>;",
                "}") + "\n", emitted.substring(emitted.indexOf("export interface")).trim() + "\n");
    }

    @Test
    void emitsJavaOverloadsAsTypescriptOverloads() throws IOException {
        String emitted = EmitFixtureTest.emit("fixture.Log", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface",
                "public interface Log {",
                "  void error(String message);",
                "  void error(String message, Object cause);",
                "}"));
        assertEquals(String.join("\n",
                "export interface Log {",
                "  error(message: string): void;",
                "  error(message: string, cause: unknown): void;",
                "}") + "\n", emitted.substring(emitted.indexOf("export interface")).trim() + "\n");
    }
}
