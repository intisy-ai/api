package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit the return type as the declared type or a promise of it, which Java cannot express.
 *
 * @implNote The union is over the method's own return type, so a handler that may answer
 * synchronously and an activation hook that returns nothing are one annotation rather than two.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface TsMaybeAsync {
}
