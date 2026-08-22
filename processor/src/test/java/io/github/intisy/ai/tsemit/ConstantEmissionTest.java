package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ConstantEmissionTest {

    @Test
    void emitsTypedKeyConstantsAsRuntimeValues() throws IOException {
        String emitted = EmitFixtureTest.emitFile("api.keys.ts", "fixture.Keys", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsConstant;",
                "public final class Keys {",
                "  @TsConstant(type = \"CapabilityType<ScreensCapability>\", id = \"screens\")",
                "  public static final String SCREENS = \"screens\";",
                "  private Keys() {}",
                "}"));
        assertEquals(String.join("\n",
                "// Generated from Java sources. Do not edit.",
                "",
                "import type { CapabilityType } from \"./api.js\";",
                "",
                "export const SCREENS: CapabilityType<ScreensCapability> = { id: \"screens\" };",
                ""), emitted);
    }
}
