package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit a named union of every {@link TsInterface} type that extends or implements the annotated one.
 *
 * @implNote Membership is derived rather than listed because a list is a second place to update and
 * the failure it invites is silent: a subtype added without an entry simply vanishes from the union.
 * Arms are alphabetical so the emitted file does not depend on the order javac visited them in.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsUnionType {
}
