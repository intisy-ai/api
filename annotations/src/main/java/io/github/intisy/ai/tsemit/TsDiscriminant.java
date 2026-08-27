package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit the named field as the given string literal in this type's declaration, so a set of types
 * sharing one base narrows to a union a consumer can switch over.
 *
 * @implNote The literal lives in a constructor call in Java, which the emitter cannot read, and a
 * subtype cannot narrow an inherited field's type in Java at all. Stating it here is what turns a
 * flattened hierarchy into a discriminated union rather than a set of structurally identical shapes.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsDiscriminant {
    /** @return the name of the field whose emitted type this narrows */
    String field();

    /** @return the string literal that field's emitted type becomes */
    String value();
}
