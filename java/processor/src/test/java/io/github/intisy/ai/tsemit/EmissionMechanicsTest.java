package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmissionMechanicsTest {

    private static final String SOURCE = String.join("\n",
            "package fixture;",
            "import io.github.intisy.ai.tsemit.TsConstant;",
            "import io.github.intisy.ai.tsemit.TsInterface;",
            "@TsInterface",
            "interface Screens {",
            "  void open(String id);",
            "}",
            "final class Keys {",
            "  @TsConstant(type = \"CapabilityType<Screens>\", id = \"screens\")",
            "  static final Object SCREENS = null;",
            "}");

    private static List<String> options(String... values) {
        List<String> options = new ArrayList<String>();
        for (String value : values) {
            options.add(value);
        }
        return options;
    }

    @Test
    void defaultsEmitAmbientDeclarationsImportingTheSiblingApiFile() {
        EmitHarness.Result emitted = EmitHarness.compile("fixture.Unit", SOURCE, options("-Atsemit.name=demo"));
        assertTrue(emitted.files.containsKey("demo.d.ts"), "expected demo.d.ts, got " + emitted.files.keySet());
        assertTrue(emitted.files.get("demo.keys.ts").contains("from \"./demo.js\""),
                "the local interface import points at the sibling surface file");
    }

    @Test
    void extOptionEmitsCompilableTypeScript() {
        EmitHarness.Result emitted =
                EmitHarness.compile("fixture.Unit", SOURCE, options("-Atsemit.name=contracts", "-Atsemit.ext=.ts"));
        assertTrue(emitted.files.containsKey("contracts.ts"), "expected contracts.ts, got " + emitted.files.keySet());
        assertTrue(emitted.files.containsKey("contracts.keys.ts"), "the keys file keeps its own suffix");
    }

    @Test
    void keysOptionRedirectsTheKeyTypeImportToAPackage() {
        EmitHarness.Result emitted = EmitHarness.compile("fixture.Unit", SOURCE,
                options("-Atsemit.name=contracts", "-Atsemit.keys=@intisy-ai/api"));
        String keys = emitted.files.get("contracts.keys.ts");
        assertTrue(keys.contains("import type { CapabilityType } from \"@intisy-ai/api\";"), keys);
    }

    @Test
    void aKeysFileImportsTheLocalInterfacesItsConstantsName() {
        EmitHarness.Result emitted = EmitHarness.compile("fixture.Unit", SOURCE,
                options("-Atsemit.name=contracts", "-Atsemit.keys=@intisy-ai/api"));
        String keys = emitted.files.get("contracts.keys.ts");
        assertTrue(keys.contains("import type { Screens } from \"./contracts.js\";"), keys);
    }

    @Test
    void aSurfaceNeverImportsATypeItDeclaresItself() {
        String source = String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface",
                "interface CapabilityType<T> {",
                "  String id();",
                "}",
                "@TsInterface",
                "interface Ctx {",
                "  void provide(CapabilityType<String> type);",
                "}");
        EmitHarness.Result emitted = EmitHarness.compile("fixture.Unit", source, options("-Atsemit.name=api"));
        String surface = emitted.files.get("api.d.ts");
        assertEquals(-1, surface.indexOf("import type"),
                "a surface that declares CapabilityType must not import it from itself: " + surface);
    }

    @Test
    void importsOptionEmitsOnlyTheNamesThatAppear() {
        String source = String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface",
                "interface Provider {",
                "  IrRequest last();",
                "}",
                "interface IrRequest {",
                "}");
        EmitHarness.Result emitted = EmitHarness.compile("fixture.Unit", source,
                options("-Atsemit.name=ir-contracts", "-Atsemit.imports=../index.js=IrRequest,IrResponse"));
        String surface = emitted.files.get("ir-contracts.d.ts");
        assertTrue(surface.contains("import type { IrRequest } from \"../index.js\";"), surface);
        assertEquals(-1, surface.indexOf("IrResponse"), "an unreferenced name must not be imported");
    }
}
