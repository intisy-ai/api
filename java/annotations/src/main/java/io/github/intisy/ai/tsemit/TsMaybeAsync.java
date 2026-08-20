package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Emit the return type as void or a promise of void, which Java cannot express. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface TsMaybeAsync {
}
