package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit a marker property carrying this type variable.
 *
 * @implNote Java generics are nominal, so a phantom parameter constrains assignment on its own.
 * TypeScript is structural, so without an emitted marker two differently parameterised keys become
 * mutually assignable and the typed key stops enforcing anything.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsPhantom {
    /** @return the emitted TypeScript type of the marker property */
    String value();
}
