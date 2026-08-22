package io.github.intisy.ai.tsemit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emit this interface's methods as top-level {@code export declare function} declarations rather
 * than wrapping them in an interface body.
 *
 * @implNote A JavaScript module exports free functions, not an object with methods. Wrapping them in
 * an interface only produces a type a caller can cast a module namespace object THROUGH, which
 * asserts the shape rather than having the compiler check it against the module's real exports.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsModule {
}
