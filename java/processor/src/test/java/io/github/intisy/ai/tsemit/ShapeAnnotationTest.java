package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ShapeAnnotationTest {

    @Test
    void emitsPropertiesOptionalsAndUnions() throws IOException {
        String emitted = EmitFixtureTest.emit("fixture.Ctx", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.*;",
                "@TsInterface",
                "public interface Ctx {",
                "  @TsMaybeAsync void activate();",
                "  @TsOptional @TsMaybeAsync void install();",
                "  @TsProperty(readOnly = true) String app();",
                "  @TsNullable <T> T get(String key);",
                "  @TsRaw(\"ReadableStream<unknown>\") Object stream();",
                "}"));
        assertEquals(String.join("\n",
                "export interface Ctx {",
                "  activate(): void | Promise<void>;",
                "  readonly app: string;",
                "  get<T>(key: string): T | undefined;",
                "  install?(): void | Promise<void>;",
                "  stream(): ReadableStream<unknown>;",
                "}") + "\n", emitted.substring(emitted.indexOf("export interface")).trim() + "\n");
    }

    @Test
    void emitsPhantomMarkerSoStructuralTypingStillConstrains() throws IOException {
        String emitted = EmitFixtureTest.emit("fixture.Key", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.*;",
                "@TsInterface",
                "@TsPhantom(\"T\")",
                "public interface Key<T> {",
                "  @TsProperty(readOnly = true) String id();",
                "}"));
        assertEquals(String.join("\n",
                "export interface Key<T> {",
                "  readonly __phantom?: T;",
                "  readonly id: string;",
                "}") + "\n", emitted.substring(emitted.indexOf("export interface")).trim() + "\n");
    }

    @Test
    void maybeAsyncWrapsTheDeclaredReturnRatherThanHardcodingVoid() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsMaybeAsync;",
                "import java.util.List;",
                "@TsInterface",
                "interface Screens {",
                "  @TsMaybeAsync List<String> screens();",
                "  @TsMaybeAsync void ping();",
                "}"));
        assertTrue(emitted.contains("screens(): string[] | Promise<string[]>;"), emitted);
        assertTrue(emitted.contains("ping(): void | Promise<void>;"), emitted);
    }

    @Test
    void unionEmitsTheNamedArmsInsideThePromiseTheJavaDeclares() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsUnion;",
                "import java.util.concurrent.CompletionStage;",
                "@TsInterface",
                "interface Provider {",
                "  @TsUnion({\"IrResponse\", \"IrEventStream\"}) CompletionStage<Object> handleIr();",
                "  @TsUnion({\"string\", \"number\"}) Object plain();",
                "}"));
        assertTrue(emitted.contains("handleIr(): Promise<IrResponse | IrEventStream>;"), emitted);
        assertTrue(emitted.contains("plain(): string | number;"), emitted);
    }

    @Test
    void literalOverridesAnEnumConstantNameThatJavaCannotSpell() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsLiteral;",
                "@TsInterface",
                "interface Field {",
                "  FieldType type();",
                "}",
                "enum FieldType {",
                "  @TsLiteral(\"boolean\") BOOLEAN,",
                "  number,",
                "  secret",
                "}"));
        assertTrue(emitted.contains("type(): \"boolean\" | \"number\" | \"secret\";"), emitted);
    }

    @Test
    void openAppendsTheEscapeArmToAnEnumUnion() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsOpen;",
                "@TsInterface",
                "interface Column {",
                "  Tone tone();",
                "}",
                "@TsOpen",
                "enum Tone {",
                "  normal,",
                "  muted",
                "}"));
        assertTrue(emitted.contains("tone(): \"normal\" | \"muted\" | (string & {});"), emitted);
    }

    @Test
    void indexSignatureIsEmittedAsTheLastMember() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsIndexSignature;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface(data = true)",
                "@TsIndexSignature(key = \"prop\", value = \"unknown\")",
                "interface ScreenNode {",
                "  String kind();",
                "}"));
        assertTrue(emitted.contains("  kind: string;\n  [prop: string]: unknown;\n}"), emitted);
    }

    @Test
    void enumEmitsANamedAliasAndIsReferencedByThatName() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsEnum;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface",
                "interface Field {",
                "  FieldType type();",
                "}",
                "@TsEnum",
                "enum FieldType {",
                "  secret,",
                "  select",
                "}"));
        assertTrue(emitted.contains("export type FieldType = \"secret\" | \"select\";"), emitted);
        assertTrue(emitted.contains("type(): FieldType;"), emitted);
    }

    @Test
    void anUnannotatedEnumIsStillInlinedAtItsUseSite() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface",
                "interface Watcher {",
                "  void on(ServiceEvent event);",
                "}",
                "enum ServiceEvent {",
                "  register,",
                "  unregister",
                "}"));
        assertTrue(emitted.contains("on(event: \"register\" | \"unregister\"): void;"), emitted);
        assertEquals(-1, emitted.indexOf("export type ServiceEvent"), emitted);
    }
}
