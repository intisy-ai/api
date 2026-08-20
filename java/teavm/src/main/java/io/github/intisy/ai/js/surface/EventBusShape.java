package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** Publish and subscribe, as reached only through {@link ContextSurface}. */
@TsInterface
public interface EventBusShape {
    void publish(String topic, Object payload);

    Runnable subscribe(String topic, Object listener);
}
