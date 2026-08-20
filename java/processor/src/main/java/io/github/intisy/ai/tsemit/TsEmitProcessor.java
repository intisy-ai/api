package io.github.intisy.ai.tsemit;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
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
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({"io.github.intisy.ai.tsemit.TsInterface", "io.github.intisy.ai.tsemit.TsModule", "io.github.intisy.ai.tsemit.TsConstant"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions("tsemit.name")
public class TsEmitProcessor extends AbstractProcessor {

    private final List<String> chunks = new ArrayList<String>();
    private final List<String> constants = new ArrayList<String>();
    private int rawEscapes = 0;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        for (Element element : round.getElementsAnnotatedWith(TsInterface.class)) {
            if (element.getKind() != ElementKind.INTERFACE) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@TsInterface applies only to interfaces", element);
                continue;
            }
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
        if (method.getAnnotation(TsMaybeAsync.class) != null) {
            return "void | Promise<void>";
        }
        String emitted = tsType(method.getReturnType());
        return method.getAnnotation(TsNullable.class) != null ? emitted + " | undefined" : emitted;
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
     * @implNote Newlines are written literally rather than through {@code println}, whose separator
     * is the build platform's. A generated file committed on Windows and regenerated on Linux would
     * otherwise differ by line ending alone, and the drift gate would fail on one of the two.
     */
    private void write() {
        StringBuilder out = new StringBuilder("// Generated from Java sources. Do not edit.\n\n");
        Collections.sort(chunks);
        for (String chunk : chunks) {
            out.append(chunk).append("\n");
        }
        try {
            Writer writer = processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT, "", basename() + ".d.ts").openWriter();
            try {
                writer.write(out.toString());
            } finally {
                writer.close();
            }
            writeConstants();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "tsemit: " + chunks.size() + " interfaces, " + constants.size() + " constants, "
                            + rawEscapes + " raw escape hatches");
        } catch (IOException failure) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "tsemit failed: " + failure.getMessage());
        }
    }

    private String basename() {
        String name = processingEnv.getOptions().get("tsemit.name");
        return name == null || name.isEmpty() ? "api" : name;
    }

    private void writeConstants() throws IOException {
        if (constants.isEmpty()) {
            return;
        }
        StringBuilder out = new StringBuilder("// Generated from Java sources. Do not edit.\n\n");
        Collections.sort(constants);
        // Import only the key types actually referenced, so a file of plain constants does not carry
        // an unused import that a consumer compiling with noUnusedLocals would reject.
        List<String> imports = new ArrayList<String>();
        for (String candidate : new String[] {"CapabilityType", "ServiceType", "TopicType"}) {
            for (String constant : constants) {
                if (constant.contains(candidate)) {
                    imports.add(candidate);
                    break;
                }
            }
        }
        if (!imports.isEmpty()) {
            out.append("import type { ");
            for (int i = 0; i < imports.size(); i++) {
                if (i > 0) {
                    out.append(", ");
                }
                out.append(imports.get(i));
            }
            out.append(" } from \"./api.js\";\n\n");
        }
        for (String constant : constants) {
            out.append(constant).append("\n");
        }
        Writer writer = processingEnv.getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, "", basename() + ".keys.ts").openWriter();
        try {
            writer.write(out.toString());
        } finally {
            writer.close();
        }
    }
}
