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

    @Test
    void aBaseEmitsAsTheUnionOfItsEmittedSubtypes() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsUnionType;",
                "/** Any leaf. */",
                "@TsUnionType",
                "abstract class Base {",
                "  public String kind;",
                "}",
                "@TsInterface(data = true)",
                "class Zulu extends Base {",
                "}",
                "@TsInterface(data = true)",
                "class Alpha extends Base {",
                "}"));
        assertTrue(emitted.contains(String.join("\n",
                "/** Any leaf. */",
                "export type Base = Alpha | Zulu;")), emitted);
    }

    @Test
    void anUnannotatedSubtypeIsNotAnArm() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsUnionType;",
                "@TsUnionType",
                "abstract class Base {",
                "}",
                "@TsInterface(data = true)",
                "class Kept extends Base {",
                "}",
                "class Skipped extends Base {",
                "}"));
        assertTrue(emitted.contains("export type Base = Kept;"), emitted);
    }

    @Test
    void aFlattenedHierarchyEmitsAsADiscriminatedUnion() {
        String emitted = EmitHarness.surface(String.join("\n",
                "package fixture;",
                "import java.util.Map;",
                "import io.github.intisy.ai.tsemit.TsDiscriminant;",
                "import io.github.intisy.ai.tsemit.TsInterface;",
                "import io.github.intisy.ai.tsemit.TsNullable;",
                "import io.github.intisy.ai.tsemit.TsStringUnion;",
                "import io.github.intisy.ai.tsemit.TsUnionType;",
                "import io.github.intisy.ai.tsemit.TsVocabulary;",
                "@TsStringUnion",
                "final class Kind {",
                "  public static final String TEXT = \"text\";",
                "  public static final String IMAGE = \"image\";",
                "}",
                "@TsUnionType",
                "abstract class Node {",
                "  public String kind;",
                "  @TsNullable public Map<String, Object> extensions;",
                "}",
                "@TsDiscriminant(field = \"kind\", value = \"text\")",
                "@TsInterface(data = true)",
                "class TextNode extends Node {",
                "  public String text;",
                "}",
                "@TsDiscriminant(field = \"kind\", value = \"image\")",
                "@TsInterface(data = true)",
                "class ImageNode extends Node {",
                "  public String url;",
                "}",
                "@TsInterface(data = true)",
                "class Envelope {",
                "  @TsVocabulary(Kind.class) public String kind;",
                "}"));
        assertTrue(emitted.contains("export type Kind = \"text\" | \"image\";"), emitted);
        assertTrue(emitted.contains("export type Node = ImageNode | TextNode;"), emitted);
        assertTrue(emitted.contains(String.join("\n",
                "export interface TextNode {",
                "  extensions: Record<string, unknown> | null;",
                "  kind: \"text\";",
                "  text: string;",
                "}")), emitted);
        assertTrue(emitted.contains(String.join("\n",
                "export interface ImageNode {",
                "  extensions: Record<string, unknown> | null;",
                "  kind: \"image\";",
                "  url: string;",
                "}")), emitted);
        assertTrue(emitted.contains(String.join("\n",
                "export interface Envelope {",
                "  kind: Kind;",
                "}")), emitted);
    }

    @Test
    void aBaseWithNoEmittedSubtypeIsRefused() {
        List<String> errors = EmitHarness.errors("fixture.Unit", String.join("\n",
                "package fixture;",
                "import io.github.intisy.ai.tsemit.TsUnionType;",
                "@TsUnionType",
                "abstract class Base {",
                "}"));
        assertTrue(errors.toString().contains("subtype"), errors.toString());
    }
}
