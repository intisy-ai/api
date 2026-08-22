package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit this enum constant as the given string literal rather than as its own name.
 *
 * @implNote An enum used as a type-level vocabulary emits the literal union of its constant names,
 * which cannot express a literal that is a Java reserved word: no constant may be called
 * {@code boolean} or {@code new}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface TsLiteral {
    String value();
}
