package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Append an undefined arm to the emitted return type. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface TsNullable {
}
