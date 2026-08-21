package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Append a nullish arm to the emitted type.
 *
 * @implNote On a method this appends an undefined arm to the return type by default, matching an
 * absent result; set {@link #asNull} when the real absence is a JavaScript {@code null} rather than
 * an omitted value. On a parameter this always appends a null arm, matching a caller passing an
 * explicit sentinel rather than omitting the argument, and {@link #asNull} has no effect there.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface TsNullable {
    boolean asNull() default false;
}
