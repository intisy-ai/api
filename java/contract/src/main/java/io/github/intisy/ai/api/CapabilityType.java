package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsPhantom;
import io.github.intisy.ai.tsemit.TsProperty;

/**
 * A capability id paired, in the type system, with the implementation that id requires.
 *
 * @implNote The library that defines a category owns its key. This package deliberately mints none,
 * which is what lets the api be reused by a project that has never heard of this ecosystem.
 */
@TsInterface
@TsPhantom("T")
public interface CapabilityType<T> {
    @TsProperty(readOnly = true)
    String id();
}
