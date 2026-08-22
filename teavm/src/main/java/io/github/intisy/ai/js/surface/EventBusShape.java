package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.function.Consumer;

/** Publish and subscribe, as reached only through {@link ContextSurface}. */
@TsInterface
public interface EventBusShape {
    /** Publishes a payload on a topic. */
    void publish(String topic, Object payload);

    /** Subscribes to a topic. Call the result to stop listening. */
    Runnable subscribe(String topic, Consumer<Object> listener);
}
