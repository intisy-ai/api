package io.github.intisy.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Runs the shared fixture through the Java validator and asserts the issue paths it names.
 *
 * @implNote Not the parity test the standards forbid, which bans asserting the Java matches a
 * DELETED implementation. This is a frozen fixture, and its other reader is the TeaVM bundle's own
 * suite: the pair is what catches a compile that reads correct and behaves differently. The reader
 * below is deliberately hand-written and test-only: this module has no dependencies, and adding one
 * for a fixture of known shape would trade that away for nothing.
 */
class ParityTest {

    private static final List<String> WELL_KNOWN = Arrays.asList("accounts", "routing", "activity");

    @TestFactory
    List<DynamicTest> agreesWithTheSharedFixture() throws IOException {
        File fixture = new File("../../test/parity/manifests.json");
        String text = new String(Files.readAllBytes(fixture.toPath()), StandardCharsets.UTF_8);
        List<Object> cases = asList(new Json(text).read());
        List<DynamicTest> tests = new ArrayList<DynamicTest>();
        for (Object entry : cases) {
            final Map<String, Object> example = asMap(entry);
            String name = String.valueOf(example.get("name"));
            tests.add(DynamicTest.dynamicTest(name, new org.junit.jupiter.api.function.Executable() {
                @Override
                public void execute() {
                    List<Object> expected = asList(example.get("expectedPaths"));
                    List<String> expectedPaths = new ArrayList<String>();
                    for (Object path : expected) {
                        expectedPaths.add(String.valueOf(path));
                    }
                    List<SchemaIssue> issues = ManifestValidator.validate(example.get("manifest"), WELL_KNOWN);
                    List<String> actualPaths = new ArrayList<String>();
                    for (SchemaIssue issue : issues) {
                        actualPaths.add(issue.getPath());
                    }
                    assertEquals(expectedPaths, actualPaths, issues.toString());
                }
            }));
        }
        return tests;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object value) {
        return (List<Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return (Map<String, Object>) value;
    }

    /** A minimal JSON reader for the fixture shape: objects, arrays, strings, numbers, booleans, null. */
    private static final class Json {
        private final String text;
        private int at = 0;

        Json(String text) {
            this.text = text;
        }

        Object read() {
            skipSpace();
            Object value = readValue();
            skipSpace();
            return value;
        }

        private Object readValue() {
            char current = text.charAt(at);
            if (current == '{') {
                return readObject();
            }
            if (current == '[') {
                return readArray();
            }
            if (current == '"') {
                return readString();
            }
            if (text.startsWith("true", at)) {
                at += 4;
                return Boolean.TRUE;
            }
            if (text.startsWith("false", at)) {
                at += 5;
                return Boolean.FALSE;
            }
            if (text.startsWith("null", at)) {
                at += 4;
                return null;
            }
            return readNumber();
        }

        private Map<String, Object> readObject() {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            at++;
            skipSpace();
            if (text.charAt(at) == '}') {
                at++;
                return result;
            }
            while (true) {
                skipSpace();
                String key = readString();
                skipSpace();
                at++;
                skipSpace();
                result.put(key, readValue());
                skipSpace();
                char separator = text.charAt(at++);
                if (separator == '}') {
                    return result;
                }
            }
        }

        private List<Object> readArray() {
            List<Object> result = new ArrayList<Object>();
            at++;
            skipSpace();
            if (text.charAt(at) == ']') {
                at++;
                return result;
            }
            while (true) {
                skipSpace();
                result.add(readValue());
                skipSpace();
                char separator = text.charAt(at++);
                if (separator == ']') {
                    return result;
                }
            }
        }

        private String readString() {
            StringBuilder out = new StringBuilder();
            at++;
            while (true) {
                char current = text.charAt(at++);
                if (current == '"') {
                    return out.toString();
                }
                if (current == '\\') {
                    char escaped = text.charAt(at++);
                    if (escaped == 'n') {
                        out.append('\n');
                    } else if (escaped == 't') {
                        out.append('\t');
                    } else {
                        out.append(escaped);
                    }
                    continue;
                }
                out.append(current);
            }
        }

        private Object readNumber() {
            int start = at;
            while (at < text.length() && "-+.eE0123456789".indexOf(text.charAt(at)) >= 0) {
                at++;
            }
            String literal = text.substring(start, at);
            if (literal.indexOf('.') < 0 && literal.indexOf('e') < 0 && literal.indexOf('E') < 0) {
                return Integer.valueOf(Integer.parseInt(literal));
            }
            return Double.valueOf(Double.parseDouble(literal));
        }

        private void skipSpace() {
            while (at < text.length() && Character.isWhitespace(text.charAt(at))) {
                at++;
            }
        }
    }
}
