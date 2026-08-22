package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class KeysReexportTest {

    private static final String KEYS = String.join("\n",
            "package fixture;",
            "import io.github.intisy.ai.tsemit.TsConstant;",
            "public final class Keys {",
            "  @TsConstant(type = \"CapabilityType<ScreensCapability>\", id = \"screens\")",
            "  public static final String SCREENS = \"screens\";",
            "  private Keys() {}",
            "}");

    private static final String NO_CONSTANTS = String.join("\n",
            "package fixture;",
            "import io.github.intisy.ai.tsemit.TsInterface;",
            "@TsInterface",
            "public interface Greeter {",
            "  void greet(String name);",
            "}");

    @Test
    void reexportsTheSurfaceWhenTheKeysFileIsThePackageRoot() {
        List<String> options = Arrays.asList("-Atsemit.name=demo", "-Atsemit.keys=./demo.js", "-Atsemit.reexport=true");
        String emitted = EmitHarness.compile("fixture.Keys", KEYS, options).files.get("demo.keys.ts");
        assertEquals(String.join("\n",
                "// Generated from Java sources. Do not edit.",
                "",
                "import type { CapabilityType } from \"./demo.js\";",
                "",
                "export type * from \"./demo.js\";",
                "",
                "export const SCREENS: CapabilityType<ScreensCapability> = { id: \"screens\" };",
                ""), emitted);
    }

    @Test
    void leavesTheKeysFileAloneByDefault() {
        List<String> options = Collections.singletonList("-Atsemit.name=demo");
        String emitted = EmitHarness.compile("fixture.Keys", KEYS, options).files.get("demo.keys.ts");
        assertFalse(emitted.contains("export type *"), "the re-export is opt-in");
    }

    @Test
    void emitsAKeysFileOfNothingButTheReexportWhenThereAreNoConstants() {
        List<String> options = Arrays.asList("-Atsemit.name=demo", "-Atsemit.reexport=true");
        String emitted = EmitHarness.compile("fixture.Greeter", NO_CONSTANTS, options).files.get("demo.keys.ts");
        assertEquals(String.join("\n",
                "// Generated from Java sources. Do not edit.",
                "",
                "export type * from \"./demo.js\";",
                "",
                ""), emitted);
    }
}
