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
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
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
        Run run = run(className, source, processorOptions);
        assertTrue(run.compiled, "fixture source must compile");
        return new Result(run.files);
    }

    /** The errors reported over a fixture the processor is expected to refuse. */
    static List<String> errors(String className, String source) {
        List<String> noOptions = Collections.emptyList();
        return run(className, source, noOptions).errors;
    }

    private static Run run(String className, String source, List<String> processorOptions) {
        try {
            File output = Files.createTempDirectory("tsemit").toFile();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> reported = new DiagnosticCollector<JavaFileObject>();
            StandardJavaFileManager files = compiler.getStandardFileManager(reported, null, StandardCharsets.UTF_8);
            List<JavaFileObject> units = new ArrayList<JavaFileObject>();
            units.add(new StringSource(className, source));
            List<String> options = new ArrayList<String>();
            options.add("-d");
            options.add(output.getAbsolutePath());
            options.add("-classpath");
            options.add(System.getProperty("java.class.path"));
            options.addAll(processorOptions);
            JavaCompiler.CompilationTask task = compiler.getTask(null, files, reported, options, null, units);
            task.setProcessors(Collections.singletonList(new TsEmitProcessor()));
            boolean compiled = task.call().booleanValue();
            List<String> errors = new ArrayList<String>();
            for (Diagnostic<? extends JavaFileObject> diagnostic : reported.getDiagnostics()) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    errors.add(diagnostic.getMessage(null));
                }
            }
            return new Run(compiled, readEmitted(output), errors);
        } catch (IOException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static final class Run {
        private final boolean compiled;
        private final Map<String, String> files;
        private final List<String> errors;

        Run(boolean compiled, Map<String, String> files, List<String> errors) {
            this.compiled = compiled;
            this.files = files;
            this.errors = errors;
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
