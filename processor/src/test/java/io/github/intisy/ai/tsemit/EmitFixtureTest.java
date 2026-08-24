package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmitFixtureTest {

    static String emit(String className, String source) throws IOException {
        return emitFile("api.d.ts", className, source);
    }

    static String emitFile(String fileName, String className, String source) throws IOException {
        List<String> noOptions = Collections.emptyList();
        return EmitHarness.compile(className, source, noOptions).files.get(fileName);
    }

    @Test
    void emitsABareInterface() throws IOException {
        String emitted = emit("fixture.Greeter", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface",
                "public interface Greeter {",
                "  void greet(String name);",
                "}"));
        assertEquals(String.join("\n",
                "// Generated from Java sources. Do not edit.",
                "",
                "export interface Greeter {",
                "  greet(name: string): void;",
                "}",
                "") + "\n", emitted);
    }
}
