package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit this enum as a named string-literal union, and refer to it by that name where it is used.
 *
 * @implNote Without this an enum is inlined at every use site, which repeats a seven-arm union across
 * the surface and leaves a consumer no name to import. A named vocabulary type is part of the
 * contract, so it is worth emitting even though the union alone would type-check.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsEnum {
}
