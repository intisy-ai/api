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

@SupportedAnnotationTypes("io.github.intisy.ai.tsemit.TsInterface")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class TsEmitProcessor extends AbstractProcessor {

    private final List<String> chunks = new ArrayList<String>();

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
        if (round.processingOver()) {
            write();
        }
        return false;
    }

    private String emit(TypeElement type) {
        TsInterface spec = type.getAnnotation(TsInterface.class);
        StringBuilder out = new StringBuilder();
        out.append("export interface ").append(type.getSimpleName()).append(joinVars(type.getTypeParameters())).append(" {\n");
        for (ExecutableElement method : sortedMethods(type)) {
            boolean property = method.getParameters().isEmpty() && spec.data();
            out.append("  ").append(method.getSimpleName());
            if (property) {
                out.append(": ").append(tsType(method.getReturnType())).append(";\n");
            } else {
                out.append(joinVars(method.getTypeParameters())).append("(").append(params(method))
                        .append("): ").append(tsType(method.getReturnType())).append(";\n");
            }
        }
        out.append("}\n");
        return out.toString();
    }

    private List<ExecutableElement> sortedMethods(TypeElement type) {
        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
        for (Element member : type.getEnclosedElements()) {
            if (member.getKind() == ElementKind.METHOD && !member.getModifiers().contains(Modifier.STATIC)) {
                methods.add((ExecutableElement) member);
            }
        }
        Collections.sort(methods, new Comparator<ExecutableElement>() {
            @Override
            public int compare(ExecutableElement left, ExecutableElement right) {
                return left.toString().compareTo(right.toString());
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
            out.append(parameters.get(i).getSimpleName()).append(": ").append(tsType(parameters.get(i).asType()));
        }
        return out.toString();
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
                    .createResource(StandardLocation.CLASS_OUTPUT, "", "api.d.ts").openWriter();
            try {
                writer.write(out.toString());
            } finally {
                writer.close();
            }
        } catch (IOException failure) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "tsemit failed: " + failure.getMessage());
        }
    }
}
