package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Runs the processor over a source string and hands back everything it emitted.
 *
 * @implNote Every emitted file is returned rather than one named file, because the options under
 * test change which files exist and what they are called, and a harness that takes the wanted name
 * as an argument cannot catch a wrong one.
 */
final class EmitHarness {

    static final class Result {
        final Map<String, String> files;

        Result(Map<String, String> files) {
            this.files = files;
        }
    }

    static Result compile(String className, String source, List<String> processorOptions) {
        try {
            File output = Files.createTempDirectory("tsemit").toFile();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager files = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
            List<JavaFileObject> units = new ArrayList<JavaFileObject>();
            units.add(new StringSource(className, source));
            List<String> options = new ArrayList<String>();
            options.add("-d");
            options.add(output.getAbsolutePath());
            options.add("-classpath");
            options.add(System.getProperty("java.class.path"));
            options.addAll(processorOptions);
            JavaCompiler.CompilationTask task = compiler.getTask(null, files, null, options, null, units);
            task.setProcessors(Collections.singletonList(new TsEmitProcessor()));
            assertTrue(task.call().booleanValue(), "fixture source must compile");
            return new Result(readEmitted(output));
        } catch (IOException failure) {
            throw new IllegalStateException(failure);
        }
    }

    /** One fixture's surface file, emitted under the default extension. */
    static String surface(String source) {
        return compile("fixture.Unit", source, Collections.singletonList("-Atsemit.name=demo")).files.get("demo.d.ts");
    }

    private static Map<String, String> readEmitted(File output) throws IOException {
        Map<String, String> emitted = new LinkedHashMap<String, String>();
        File[] children = output.listFiles();
        if (children == null) {
            return emitted;
        }
        for (File child : children) {
            String name = child.getName();
            if (child.isFile() && name.endsWith(".ts")) {
                emitted.put(name, new String(Files.readAllBytes(child.toPath()), StandardCharsets.UTF_8));
            }
        }
        return emitted;
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

    private EmitHarness() {
    }
}
