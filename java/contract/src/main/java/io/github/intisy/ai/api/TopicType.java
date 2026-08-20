package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsPhantom;
import io.github.intisy.ai.tsemit.TsProperty;

/** An event topic paired, in the type system, with the payload it carries. */
@TsInterface
@TsPhantom("T")
public interface TopicType<T> {
    @TsProperty(readOnly = true)
    String id();
}
