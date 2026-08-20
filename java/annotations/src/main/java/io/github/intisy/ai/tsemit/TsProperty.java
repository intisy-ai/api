package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit this zero-argument method as a property rather than a method.
 *
 * @implNote Per member rather than per type: an interface may hold both, as a plugin context does
 * with its properties and its one provide method.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface TsProperty {
    boolean readOnly() default false;
}
