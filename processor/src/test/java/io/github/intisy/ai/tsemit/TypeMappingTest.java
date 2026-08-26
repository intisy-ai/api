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
    void mapsTheFunctionalInterfacesAnEmittedSeamNames() throws IOException {
        String emitted = EmitFixtureTest.emit("fixture.Seam", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import java.util.function.BiConsumer;",
                "import java.util.function.Consumer;",
                "import java.util.function.UnaryOperator;",
                "@TsInterface",
                "public interface Seam {",
                "  void update(String key, UnaryOperator<String> mutator);",
                "  void watch(Consumer<String> listener);",
                "  void pair(BiConsumer<String, Object> listener);",
                "  void run(Runnable task);",
                "}"));
        assertEquals(String.join("\n",
                "export interface Seam {",
                "  pair(listener: ((a: string, b: unknown) => void)): void;",
                "  run(task: () => void): void;",
                "  update(key: string, mutator: ((value: string) => string)): void;",
                "  watch(listener: ((value: string) => void)): void;",
                "}") + "\n", emitted.substring(emitted.indexOf("export interface")).trim() + "\n");
    }

    @Test
    void mapsTheValueReturningFunctionalInterfaces() throws IOException {
        String emitted = EmitFixtureTest.emit("fixture.Transport", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import java.util.concurrent.CompletionStage;",
                "import java.util.function.BiFunction;",
                "import java.util.function.Function;",
                "import java.util.function.Predicate;",
                "import java.util.function.Supplier;",
                "@TsInterface",
                "public interface Transport {",
                "  void send(Function<String, CompletionStage<String>> call);",
                "  void merge(BiFunction<String, String, Object> combine);",
                "  void open(Supplier<String> token);",
                "  void filter(Predicate<String> accept);",
                "}"));
        assertEquals(String.join("\n",
                "export interface Transport {",
                "  filter(accept: ((value: string) => boolean)): void;",
                "  merge(combine: ((a: string, b: string) => unknown)): void;",
                "  open(token: (() => string)): void;",
                "  send(call: ((value: string) => Promise<string>)): void;",
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
