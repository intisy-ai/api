package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit this holder of {@code public static final String} constants as a named string-literal union of
 * their values, and refer to it by that name wherever a member declares {@link TsVocabulary}.
 *
 * @implNote A vocabulary whose constants ARE the wire strings cannot be an enum, which is what
 * {@link TsEnum} needs: the field carrying one is a {@code String} that a codec reads and writes
 * verbatim, so every constant has to be assignable to it. {@link TsOpen} applies here too, and adding
 * it is how a vocabulary says a value outside the listed set is legal.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsStringUnion {
}
