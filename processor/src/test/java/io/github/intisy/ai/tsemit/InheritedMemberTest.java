package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InheritedMemberTest {

    @Test
    void aSubclassEmitsTheFieldsItInherits() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "abstract class Base {",
                "  public String kind;",
                "  public String shared;",
                "}",
                "@TsInterface(data = true)",
                "class Leaf extends Base {",
                "  public String own;",
                "}"));
        assertTrue(emitted.contains(String.join("\n",
                "export interface Leaf {",
                "  kind: string;",
                "  own: string;",
                "  shared: string;",
                "}")), emitted);
    }

    @Test
    void anInterfaceEmitsTheMethodsItInherits() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "interface Super {",
                "  /** The inherited one. */",
                "  String inherited();",
                "}",
                "@TsInterface",
                "interface Sub extends Super {",
                "  String own();",
                "}"));
        assertTrue(emitted.contains("  /** The inherited one. */\n  inherited(): string;"), emitted);
        assertTrue(emitted.contains("  own(): string;"), emitted);
    }

    @Test
    void anOwnMemberWinsOverTheOneItRedeclares() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsProperty;",
                "interface Super {",
                "  String id();",
                "}",
                "@TsInterface",
                "interface Sub extends Super {",
                "  @Override",
                "  @TsProperty(readOnly = true)",
                "  String id();",
                "}"));
        assertTrue(emitted.contains("  readonly id: string;"), emitted);
        assertEquals(1, emitted.split("\\bid\\b", -1).length - 1, emitted);
    }

    @Test
    void anInheritedOverloadSetKeepsEveryArm() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "interface Super {",
                "  void error(String message);",
                "  void error(String message, Object cause);",
                "}",
                "@TsInterface",
                "interface Sub extends Super {",
                "}"));
        assertTrue(emitted.contains("  error(message: string): void;"), emitted);
        assertTrue(emitted.contains("  error(message: string, cause: unknown): void;"), emitted);
    }

    @Test
    void objectMembersAreNotInherited() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "@TsInterface(data = true)",
                "class Leaf {",
                "  public String own;",
                "}"));
        assertTrue(emitted.contains(String.join("\n",
                "export interface Leaf {",
                "  own: string;",
                "}")), emitted);
    }
}
