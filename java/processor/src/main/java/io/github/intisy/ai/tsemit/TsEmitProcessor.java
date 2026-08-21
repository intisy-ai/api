package io.github.intisy.ai.tsemit;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({"io.github.intisy.ai.tsemit.TsInterface", "io.github.intisy.ai.tsemit.TsModule", "io.github.intisy.ai.tsemit.TsConstant"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions({"tsemit.name", "tsemit.ext", "tsemit.keys", "tsemit.imports"})
public class TsEmitProcessor extends AbstractProcessor {

    private static final List<String> KEY_TYPES =
            Collections.unmodifiableList(Arrays.asList("CapabilityType", "ServiceType", "TopicType"));

    private final List<String> chunks = new ArrayList<String>();
    private final List<String> constants = new ArrayList<String>();
    private final List<String> emittedTypes = new ArrayList<String>();
    private int rawEscapes = 0;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        for (Element element : round.getElementsAnnotatedWith(TsInterface.class)) {
            if (element.getKind() != ElementKind.INTERFACE) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@TsInterface applies only to interfaces", element);
                continue;
            }
            emittedTypes.add(element.getSimpleName().toString());
            chunks.add(emit((TypeElement) element));
        }
        for (Element element : round.getElementsAnnotatedWith(TsModule.class)) {
            if (element.getKind() != ElementKind.INTERFACE) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@TsModule applies only to interfaces", element);
                continue;
            }
            chunks.add(emitModule((TypeElement) element));
        }
        for (Element element : round.getElementsAnnotatedWith(TsConstant.class)) {
            TsConstant constant = element.getAnnotation(TsConstant.class);
            String value = constant.literal().isEmpty()
                    ? "{ id: \"" + constant.id() + "\" }"
                    : constant.literal();
            constants.add("export const " + element.getSimpleName() + ": " + constant.type() + " = " + value + ";");
        }
        if (round.processingOver()) {
            write();
        }
        return false;
    }

    private String emit(TypeElement type) {
        TsInterface spec = type.getAnnotation(TsInterface.class);
        StringBuilder out = new StringBuilder();
        out.append("export interface ").append(type.getSimpleName()).append(joinVars(type.getTypeParameters())).append(" {\n");
        TsPhantom phantom = type.getAnnotation(TsPhantom.class);
        if (phantom != null) {
            out.append("  readonly __phantom?: ").append(phantom.value()).append(";\n");
        }
        for (ExecutableElement method : sortedMethods(type)) {
            TsProperty asProperty = method.getAnnotation(TsProperty.class);
            boolean property = method.getParameters().isEmpty() && (asProperty != null || spec.data());
            out.append("  ");
            if (property && asProperty != null && asProperty.readOnly()) {
                out.append("readonly ");
            }
            out.append(method.getSimpleName());
            if (method.getAnnotation(TsOptional.class) != null) {
                out.append("?");
            }
            if (property) {
                out.append(": ").append(returnType(method)).append(";\n");
            } else {
                out.append(joinVars(method.getTypeParameters())).append("(").append(params(method))
                        .append("): ").append(returnType(method)).append(";\n");
            }
        }
        TsIndexSignature index = type.getAnnotation(TsIndexSignature.class);
        if (index != null) {
            out.append("  [").append(index.key()).append(": string]: ").append(index.value()).append(";\n");
        }
        out.append("}\n");
        return out.toString();
    }

    /**
     * @implNote No property branch, no interface wrapper: every member is a free function, because
     * that is what a JavaScript module actually exports and what lets a plain named import type-check
     * against this declaration instead of merely being castable to it.
     */
    private String emitModule(TypeElement type) {
        StringBuilder out = new StringBuilder();
        for (ExecutableElement method : sortedMethods(type)) {
            out.append("export declare function ").append(method.getSimpleName())
                    .append(joinVars(method.getTypeParameters())).append("(").append(params(method))
                    .append("): ").append(returnType(method)).append(";\n");
        }
        return out.toString();
    }

    private String returnType(ExecutableElement method) {
        TsRaw raw = method.getAnnotation(TsRaw.class);
        if (raw != null) {
            rawEscapes++;
            return raw.value();
        }
        TsUnion union = method.getAnnotation(TsUnion.class);
        if (union != null) {
            StringBuilder arms = new StringBuilder();
            for (int i = 0; i < union.value().length; i++) {
                if (i > 0) {
                    arms.append(" | ");
                }
                arms.append(union.value()[i]);
            }
            return isStage(method.getReturnType()) ? "Promise<" + arms + ">" : arms.toString();
        }
        if (method.getAnnotation(TsMaybeAsync.class) != null) {
            String emitted = tsType(method.getReturnType());
            return emitted + " | Promise<" + emitted + ">";
        }
        String emitted = tsType(method.getReturnType());
        TsNullable nullable = method.getAnnotation(TsNullable.class);
        if (nullable == null) {
            return emitted;
        }
        return emitted + (nullable.asNull() ? " | null" : " | undefined");
    }

    private boolean isStage(TypeMirror mirror) {
        if (mirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        String qualified = ((TypeElement) ((DeclaredType) mirror).asElement()).getQualifiedName().toString();
        return "java.util.concurrent.CompletionStage".equals(qualified)
                || "java.util.concurrent.CompletableFuture".equals(qualified);
    }

    private List<ExecutableElement> sortedMethods(TypeElement type) {
        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
        for (Element member : type.getEnclosedElements()) {
            if (member.getKind() == ElementKind.METHOD && !member.getModifiers().contains(Modifier.STATIC)) {
                methods.add((ExecutableElement) member);
            }
        }
        // Name first, signature only as a tiebreaker: toString() renders a generic method as
        // "<T>get(...)", which would otherwise sort every generic member ahead of the rest.
        Collections.sort(methods, new Comparator<ExecutableElement>() {
            @Override
            public int compare(ExecutableElement left, ExecutableElement right) {
                int byName = left.getSimpleName().toString().compareTo(right.getSimpleName().toString());
                return byName != 0 ? byName : left.toString().compareTo(right.toString());
            }
        });
        return methods;
    }

    private String joinVars(List<? extends TypeParameterElement> vars) {
        if (vars.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder("<");
        for (int i = 0; i < vars.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(vars.get(i).getSimpleName());
        }
        return out.append(">").toString();
    }

    private String params(ExecutableElement method) {
        StringBuilder out = new StringBuilder();
        List<? extends VariableElement> parameters = method.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(parameters.get(i).getSimpleName()).append(": ").append(paramType(parameters.get(i)));
        }
        return out.toString();
    }

    private String paramType(VariableElement parameter) {
        TsRaw raw = parameter.getAnnotation(TsRaw.class);
        if (raw != null) {
            rawEscapes++;
            return raw.value();
        }
        String emitted = tsType(parameter.asType());
        return parameter.getAnnotation(TsNullable.class) != null ? emitted + " | null" : emitted;
    }

    private String tsType(TypeMirror mirror) {
        switch (mirror.getKind()) {
            case VOID:
                return "void";
            case BOOLEAN:
                return "boolean";
            case INT:
            case LONG:
            case DOUBLE:
            case FLOAT:
            case SHORT:
            case BYTE:
                return "number";
            case TYPEVAR:
                return mirror.toString();
            case ARRAY:
                return tsType(((ArrayType) mirror).getComponentType()) + "[]";
            case DECLARED:
                return declared((DeclaredType) mirror);
            default:
                return "unknown";
        }
    }

    private String declared(DeclaredType type) {
        TypeElement element = (TypeElement) type.asElement();
        if (element.getKind() == ElementKind.ENUM) {
            return enumLiteral(element);
        }
        String qualified = element.getQualifiedName().toString();
        if ("java.lang.String".equals(qualified)) {
            return "string";
        }
        if ("java.lang.Boolean".equals(qualified)) {
            return "boolean";
        }
        if ("java.lang.Integer".equals(qualified) || "java.lang.Long".equals(qualified) || "java.lang.Double".equals(qualified)) {
            return "number";
        }
        if ("java.lang.Object".equals(qualified)) {
            return "unknown";
        }
        if ("java.lang.Void".equals(qualified)) {
            return "void";
        }
        if ("java.lang.Runnable".equals(qualified)) {
            return "() => void";
        }
        List<? extends TypeMirror> mapped = type.getTypeArguments();
        if ("java.util.Map".equals(qualified)) {
            return "Record<" + tsType(mapped.get(0)) + ", " + tsType(mapped.get(1)) + ">";
        }
        if ("java.util.List".equals(qualified)) {
            return tsType(mapped.get(0)) + "[]";
        }
        if ("java.util.concurrent.CompletionStage".equals(qualified)
                || "java.util.concurrent.CompletableFuture".equals(qualified)) {
            return "Promise<" + (mapped.isEmpty() ? "void" : tsType(mapped.get(0))) + ">";
        }
        if ("java.util.function.Consumer".equals(qualified)) {
            return "((value: " + tsType(mapped.get(0)) + ") => void)";
        }
        if ("java.util.function.BiConsumer".equals(qualified)) {
            return "((a: " + tsType(mapped.get(0)) + ", b: " + tsType(mapped.get(1)) + ") => void)";
        }
        StringBuilder out = new StringBuilder(element.getSimpleName().toString());
        List<? extends TypeMirror> args = type.getTypeArguments();
        if (!args.isEmpty()) {
            out.append("<");
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    out.append(", ");
                }
                out.append(tsType(args.get(i)));
            }
            out.append(">");
        }
        return out.toString();
    }

    /**
     * @implNote An enum used purely as a type-level vocabulary (never itself annotated) emits as the
     * literal union of its constant names, so a two-value enum such as a service event becomes
     * {@code "register" | "unregister"} rather than a class name TypeScript cannot resolve.
     */
    private String enumLiteral(TypeElement element) {
        StringBuilder union = new StringBuilder();
        for (Element member : element.getEnclosedElements()) {
            if (member.getKind() != ElementKind.ENUM_CONSTANT) {
                continue;
            }
            if (union.length() > 0) {
                union.append(" | ");
            }
            TsLiteral literal = member.getAnnotation(TsLiteral.class);
            String name = literal != null ? literal.value() : member.getSimpleName().toString();
            union.append("\"").append(name).append("\"");
        }
        if (element.getAnnotation(TsOpen.class) != null) {
            union.append(" | (string & {})");
        }
        return union.toString();
    }

    /**
     * @implNote Newlines are written literally rather than through {@code println}, whose separator
     * is the build platform's. A generated file committed on Windows and regenerated on Linux would
     * otherwise differ by line ending alone, and the drift gate would fail on one of the two.
     */
    private void write() {
        StringBuilder body = new StringBuilder();
        Collections.sort(chunks);
        for (String chunk : chunks) {
            body.append(chunk).append("\n");
        }
        try {
            writeResource(basename() + surfaceExtension(), banner(body.toString(), null));
            writeConstants();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "tsemit: " + chunks.size() + " interfaces, " + constants.size() + " constants, "
                            + rawEscapes + " raw escape hatches");
        } catch (IOException failure) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "tsemit failed: " + failure.getMessage());
        }
    }

    private void writeConstants() throws IOException {
        if (constants.isEmpty()) {
            return;
        }
        StringBuilder body = new StringBuilder();
        Collections.sort(constants);
        for (String constant : constants) {
            body.append(constant).append("\n");
        }
        writeResource(basename() + ".keys.ts", banner(body.toString(), "./" + basename() + ".js"));
    }

    private String banner(String body, String localSpecifier) {
        StringBuilder out = new StringBuilder("// Generated from Java sources. Do not edit.\n\n");
        String imports = importLines(body, localSpecifier);
        if (!imports.isEmpty()) {
            out.append(imports).append("\n");
        }
        return out.append(body).toString();
    }

    private void writeResource(String name, String content) throws IOException {
        Writer writer = processingEnv.getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, "", name).openWriter();
        try {
            writer.write(content);
        } finally {
            writer.close();
        }
    }

    private String basename() {
        return option("tsemit.name", "api");
    }

    /**
     * @implNote The surface's suffix is a choice, not a constant: an ambient {@code .d.ts} is right
     * for a repo whose generated file sits outside its TypeScript rootDir, and a compilable
     * {@code .ts} is right for one whose whole package IS the generated surface, because tsc copies
     * a declaration file to no output directory.
     */
    private String surfaceExtension() {
        return option("tsemit.ext", ".d.ts");
    }

    private String option(String key, String fallback) {
        String value = processingEnv.getOptions().get(key);
        return value == null || value.isEmpty() ? fallback : value;
    }

    /**
     * @implNote A file never imports a name it declares, which is why the surface passes its own
     * emitted types as already-declared: api's surface both declares {@code CapabilityType} and
     * references it, and importing it from itself is a cycle the emitted text cannot resolve. The
     * keys file is a separate file and declares none of them, so it imports every name it uses.
     */
    private String importLines(String body, String localSpecifier) {
        List<String> declared = localSpecifier == null ? emittedTypes : Collections.<String>emptyList();
        StringBuilder out = new StringBuilder();
        out.append(importLine(body, option("tsemit.keys", "./api.js"), KEY_TYPES, declared));
        if (localSpecifier != null) {
            out.append(importLine(body, localSpecifier, emittedTypes, declared));
        }
        for (String entry : option("tsemit.imports", "").split(";")) {
            int split = entry.indexOf('=');
            if (split <= 0) {
                continue;
            }
            List<String> names = new ArrayList<String>();
            for (String name : entry.substring(split + 1).split(",")) {
                if (!name.trim().isEmpty()) {
                    names.add(name.trim());
                }
            }
            out.append(importLine(body, entry.substring(0, split), names, declared));
        }
        return out.toString();
    }

    /**
     * @implNote Only names the emitted text actually mentions are imported, because a consumer
     * compiling with noUnusedLocals rejects a generated file carrying an import it does not use.
     */
    private String importLine(String body, String specifier, List<String> candidates, List<String> declared) {
        List<String> used = new ArrayList<String>();
        for (String candidate : candidates) {
            if (!used.contains(candidate) && !declared.contains(candidate) && mentions(body, candidate)) {
                used.add(candidate);
            }
        }
        if (used.isEmpty()) {
            return "";
        }
        Collections.sort(used);
        StringBuilder line = new StringBuilder("import type { ");
        for (int i = 0; i < used.size(); i++) {
            if (i > 0) {
                line.append(", ");
            }
            line.append(used.get(i));
        }
        return line.append(" } from \"").append(specifier).append("\";\n").toString();
    }

    /**
     * @implNote Whole-word rather than substring: {@code ScreenData} contains {@code Screen}, and a
     * substring match would import a name the file never references.
     */
    private boolean mentions(String body, String name) {
        int at = body.indexOf(name);
        while (at >= 0) {
            boolean leftClean = at == 0 || !Character.isJavaIdentifierPart(body.charAt(at - 1));
            int after = at + name.length();
            boolean rightClean = after >= body.length() || !Character.isJavaIdentifierPart(body.charAt(after));
            if (leftClean && rightClean) {
                return true;
            }
            at = body.indexOf(name, at + 1);
        }
        return false;
    }
}
