package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Append a nullish arm to the emitted type.
 *
 * @implNote On a method this appends an undefined arm to the return type, matching an absent
 * result. On a parameter it appends a null arm instead, matching a caller passing an explicit
 * sentinel rather than omitting the argument; the two positions mean different things in practice
 * and the annotation matches the position it is written on.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface TsNullable {
}
