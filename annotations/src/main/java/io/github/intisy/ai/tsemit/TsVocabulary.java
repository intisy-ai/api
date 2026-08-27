package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit this {@code String} member as the named union its {@link TsStringUnion} holder declares,
 * rather than as {@code string}.
 *
 * @implNote Java carries no type-level record that a {@code String} holds one of a vocabulary's
 * constants, so the reference has to be stated. It is taken as a class rather than a name so a rename
 * of the holder is a compile error here instead of a silently wrong emitted type.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TsVocabulary {
    /** @return the holder whose emitted union name this member's type takes */
    Class<?> value();
}
