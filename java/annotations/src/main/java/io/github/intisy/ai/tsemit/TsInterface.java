package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsInterface {
    /** A data interface emits every zero-argument method as a property rather than a method. */
    boolean data() default false;
}
