package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsPhantom;
import io.github.intisy.ai.tsemit.TsProperty;

/** A service id paired, in the type system, with the contract that id promises. */
@TsInterface
@TsPhantom("T")
public interface ServiceType<T> {
    /** The id itself, which is what crosses the boundary at run time. */
    @TsProperty(readOnly = true)
    String id();
}
