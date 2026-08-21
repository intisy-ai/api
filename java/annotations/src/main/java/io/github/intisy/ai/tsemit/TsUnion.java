package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit the return type as a union of the named arms.
 *
 * @implNote Java has no union type, and the alternative was verbatim text. The arms are named
 * separately so the processor still knows this is a type reference rather than arbitrary TypeScript,
 * and a {@code CompletionStage} return keeps its promise wrapper: the Java declares the primary arm,
 * so the signature stays meaningful to a Java caller.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface TsUnion {
    String[] value();
}
