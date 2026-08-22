package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DataClassEmissionTest {

    @Test
    void publicFieldsOfADataClassEmitAsProperties() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface(data = true)",
                "class HandlerCtx {",
                "  public String configDir;",
                "  public final String model = \"\";",
                "  private String hidden;",
                "  public static String shared;",
                "}"));
        assertTrue(emitted.contains(String.join("\n",
                "export interface HandlerCtx {",
                "  configDir: string;",
                "  readonly model: string;",
                "}")), emitted);
    }

    @Test
    void fieldsAndMethodsShareOneNameOrder() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface(data = true)",
                "class Mixed {",
                "  public String beta;",
                "  public String delta;",
                "  String alpha() { return \"\"; }",
                "  String charlie() { return \"\"; }",
                "}"));
        assertTrue(emitted.contains(String.join("\n",
                "  alpha: string;",
                "  beta: string;",
                "  charlie: string;",
                "  delta: string;")), emitted);
    }

    @Test
    void aFieldTakesTheSameShapeAnnotationsAParameterDoes() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsNullable;",
                "import io.github.intisy.ai.tsemit.TsOptional;",
                "import io.github.intisy.ai.tsemit.TsRaw;",
                "@TsInterface(data = true)",
                "class Ctx {",
                "  @TsNullable public String store;",
                "  @TsOptional public String model;",
                "  @TsRaw(\"ReadableStream<unknown>\") public Object body;",
                "}"));
        assertTrue(emitted.contains("body: ReadableStream<unknown>;"), emitted);
        assertTrue(emitted.contains("model?: string;"), emitted);
        assertTrue(emitted.contains("store: string | null;"), emitted);
    }

    @Test
    void aClassIsRefusedUnlessItDeclaresItselfDataCarrying() {
        List<String> errors = EmitHarness.errors("fixture.Ctx", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface",
                "class Ctx {",
                "  public String configDir;",
                "}"));
        assertEquals(1, errors.size(), errors.toString());
        assertTrue(errors.get(0).contains("data-carrying class marked data = true"), errors.toString());
    }
}
