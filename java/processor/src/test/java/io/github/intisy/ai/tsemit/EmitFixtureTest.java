package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

class EmitFixtureTest {

    static String emit(String className, String source) throws IOException {
        return emitFile("api.d.ts", className, source);
    }

    static String emitFile(String fileName, String className, String source) throws IOException {
        File output = Files.createTempDirectory("tsemit").toFile();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager files = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        List<JavaFileObject> units = new ArrayList<JavaFileObject>();
        units.add(new StringSource(className, source));
        List<String> options = Arrays.asList("-d", output.getAbsolutePath(), "-classpath", System.getProperty("java.class.path"));
        JavaCompiler.CompilationTask task = compiler.getTask(null, files, null, options, null, units);
        task.setProcessors(Collections.singletonList(new TsEmitProcessor()));
        assertTrue(task.call().booleanValue(), "fixture source must compile");
        return new String(Files.readAllBytes(new File(output, fileName).toPath()), StandardCharsets.UTF_8);
    }

    static final class StringSource extends SimpleJavaFileObject {
        private final String code;

        StringSource(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + ".java"), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
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
