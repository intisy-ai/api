package io.github.intisy.ai.engine;

/**
 * Publish and subscribe, supplied BY the host rather than owned by the engine.
 *
 * @implNote Injected for the same reason {@link Scheduler} is: the engine wraps a bus so it can
 * record every subscription and fence a stopped plugin's listeners, and it can do both without
 * knowing what the transport is. A plugin never reaches a host's bus except through its session.
 */
public interface EventBus {

    /** What a subscriber is handed when its topic carries a payload. */
    interface Listener {
        /** @param payload the value the publisher sent on the subscribed topic */
        void received(Object payload);
    }

    /**
     * @param topic the topic to publish on
     * @param payload the value delivered to every current subscriber of {@code topic}
     */
    void publish(String topic, Object payload);

    /**
     * @param topic the topic to subscribe to
     * @param listener called with each payload published on {@code topic} until cancelled
     * @return a handle that ends the subscription when cancelled
     */
    Scheduler.Cancellable subscribe(String topic, Listener listener);
}
