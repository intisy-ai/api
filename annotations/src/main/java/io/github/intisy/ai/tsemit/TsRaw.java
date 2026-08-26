package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit this TypeScript verbatim.
 *
 * @implNote Last resort. Every use is a place the Java is not really the source, so the processor
 * reports the count and review treats a rise in it as a defect.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
public @interface TsRaw {
    /** @return the TypeScript source text to emit verbatim */
    String value();
}
