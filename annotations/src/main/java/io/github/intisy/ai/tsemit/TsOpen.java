package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Append the string escape arm to this enum's emitted union, so any other value stays legal.
 *
 * @implNote The listed names then autocomplete while a surface may still resolve one nobody minted,
 * which is what keeps a vocabulary open without giving up the suggestions.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsOpen {
}
