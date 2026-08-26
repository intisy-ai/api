package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.function.Consumer;

/** Publish and subscribe, as reached only through {@link ContextSurface}. */
@TsInterface
public interface EventBusShape {
    /**
     * Publishes a payload on a topic.
     *
     * @param topic the topic to publish on
     * @param payload the value every current subscriber of the topic receives
     */
    void publish(String topic, Object payload);

    /**
     * Subscribes to a topic. Call the result to stop listening.
     *
     * @param topic the topic to listen on
     * @param listener called with each payload published on the topic
     * @return a disposer that cancels the subscription when called
     */
    Runnable subscribe(String topic, Consumer<Object> listener);
}
