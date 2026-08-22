package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit this field as a TypeScript constant carrying a typed key.
 *
 * @implNote A key is a runtime value, so declarations alone would leave it undefined in JavaScript.
 * This is the one place the processor emits an implementation, and it is safe because a key holds no
 * logic: an id string behind a generated type.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface TsConstant {
    String type();

    String id() default "";

    /** Emitted verbatim as the value. Empty emits the typed-key object instead. */
    String literal() default "";
}
