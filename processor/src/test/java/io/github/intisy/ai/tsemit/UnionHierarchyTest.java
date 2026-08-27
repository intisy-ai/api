package io.github.intisy.ai.tsemit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class UnionHierarchyTest {

    @Test
    void aSubtypeNarrowsTheInheritedDiscriminator() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsDiscriminant;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "abstract class Base {",
                "  public String kind;",
                "  public String shared;",
                "}",
                "@TsDiscriminant(field = \"kind\", value = \"text\")",
                "@TsInterface(data = true)",
                "class Leaf extends Base {",
                "  public String text;",
                "}"));
        assertTrue(emitted.contains(String.join("\n",
                "export interface Leaf {",
                "  kind: \"text\";",
                "  shared: string;",
                "  text: string;",
                "}")), emitted);
    }

    @Test
    void aDiscriminantNamingNoFieldIsRefused() {
        List<String> errors = EmitHarness.errors("fixture.Unit", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsDiscriminant;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "abstract class Base {",
                "  public String kind;",
                "}",
                "@TsDiscriminant(field = \"knid\", value = \"text\")",
                "@TsInterface(data = true)",
                "class Leaf extends Base {",
                "}"));
        assertTrue(errors.toString().contains("knid"), errors.toString());
    }
}
