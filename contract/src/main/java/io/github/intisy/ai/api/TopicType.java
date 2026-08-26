package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsPhantom;
import io.github.intisy.ai.tsemit.TsProperty;

/** An event topic paired, in the type system, with the payload it carries. */
@TsInterface
@TsPhantom("T")
public interface TopicType<T> {
    /**
     * The id itself, which is what crosses the boundary at run time.
     *
     * @return the topic id
     */
    @TsProperty(readOnly = true)
    String id();
}
