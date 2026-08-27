package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class StringUnionEmissionTest {

    @Test
    void aConstantHolderEmitsAsANamedUnionOfItsValues() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsStringUnion;",
                "/** The vocabulary. */",
                "@TsStringUnion",
                "final class Kind {",
                "  public static final String TEXT = \"text\";",
                "  public static final String TOOL_USE = \"tool_use\";",
                "  static final String HIDDEN = \"hidden\";",
                "}"));
        assertTrue(emitted.contains(String.join("\n",
                "/** The vocabulary. */",
                "export type Kind = \"text\" | \"tool_use\";")), emitted);
        assertFalse(emitted.contains("hidden"), emitted);
    }

    @Test
    void anOpenHolderCarriesTheOpenArm() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsOpen;",
                "import io.github.intisy.ai.tsemit.TsStringUnion;",
                "@TsOpen",
                "@TsStringUnion",
                "final class Reason {",
                "  public static final String END_TURN = \"end_turn\";",
                "}"));
        assertTrue(emitted.contains("export type Reason = \"end_turn\" | (string & {});"), emitted);
    }

    @Test
    void aMemberNamingAVocabularyEmitsAsThatUnion() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsNullable;",
                "import io.github.intisy.ai.tsemit.TsStringUnion;",
                "import io.github.intisy.ai.tsemit.TsVocabulary;",
                "@TsStringUnion",
                "final class Kind {",
                "  public static final String TEXT = \"text\";",
                "}",
                "@TsInterface(data = true)",
                "class Holder {",
                "  @TsVocabulary(Kind.class) public String kind;",
                "  @TsNullable @TsVocabulary(Kind.class) public String maybe;",
                "  public String plain;",
                "}"));
        assertTrue(emitted.contains("  kind: Kind;"), emitted);
        assertTrue(emitted.contains("  maybe: Kind | null;"), emitted);
        assertTrue(emitted.contains("  plain: string;"), emitted);
    }

    @Test
    void aMethodNamingAVocabularyEmitsAsThatUnion() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsStringUnion;",
                "import io.github.intisy.ai.tsemit.TsVocabulary;",
                "@TsStringUnion",
                "final class Kind {",
                "  public static final String TEXT = \"text\";",
                "}",
                "@TsInterface",
                "interface Reader {",
                "  @TsVocabulary(Kind.class)",
                "  String kind();",
                "}"));
        assertTrue(emitted.contains("  kind(): Kind;"), emitted);
    }

    @Test
    void aHolderWithNoStringConstantsIsRefused() {
        List<String> errors = EmitHarness.errors("fixture.Unit", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsStringUnion;",
                "@TsStringUnion",
                "final class Empty {",
                "  public static final int ONE = 1;",
                "}"));
        assertTrue(errors.toString().contains("public static final String"), errors.toString());
    }
}
