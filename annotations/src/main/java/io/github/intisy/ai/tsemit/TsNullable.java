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
 * an omitted value. On a parameter or a field this always appends a null arm, matching a value that
 * is explicitly absent rather than an omitted argument, and {@link #asNull} has no effect there.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface TsNullable {
    /**
     * @return true to append a null arm instead of an undefined arm on a method's return type; has
     *     no effect on a parameter or field, which always append a null arm
     */
    boolean asNull() default false;
}
