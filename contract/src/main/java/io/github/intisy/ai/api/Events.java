package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.function.Consumer;

/**
 * How a plugin says something happened, and hears that something did.
 *
 * @implNote A topic is paired with its payload in the type system, so a publisher and a subscriber
 * cannot disagree about the shape without the compiler saying so.
 */
@TsInterface
public interface Events {
    /**
     * Says a topic carried this payload, to whoever is listening and to nobody in particular.
     *
     * @param <T> the topic's payload type
     * @param topic the topic to publish on
     * @param payload the event payload
     */
    <T> void publish(TopicType<T> topic, T payload);

    /**
     * Hears this topic until the returned function is called.
     *
     * @param <T> the topic's payload type
     * @param topic the topic to listen on
     * @param listener called with each payload published on {@code topic}
     * @return a function that stops the subscription when called
     */
    <T> Runnable subscribe(TopicType<T> topic, Consumer<T> listener);
}
