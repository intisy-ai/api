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
    /** @return the emitted TypeScript type of the constant */
    String type();

    /** @return the key's id string, embedded in the emitted typed-key object */
    String id() default "";

    /**
     * Emitted verbatim as the value. Empty emits the typed-key object instead.
     *
     * @return the literal source text to emit, or empty to emit the typed-key object built from {@link #id}
     */
    String literal() default "";
}
