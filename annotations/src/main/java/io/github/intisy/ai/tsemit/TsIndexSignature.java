package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Emit a string index signature as this interface's last member. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsIndexSignature {
    String key();

    String value();
}
