package io.github.intisy.ai.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A bus a test publishes on by hand, so a fenced subscription is observable rather than inferred. */
final class RecordingBus implements EventBus {

    private final Map<String, List<Listener>> listeners = new LinkedHashMap<String, List<Listener>>();

    @Override
    public void publish(String topic, Object payload) {
        List<Listener> waiting = listeners.get(topic);
        if (waiting == null) {
            return;
        }
        for (Listener listener : new ArrayList<Listener>(waiting)) {
            listener.received(payload);
        }
    }

    @Override
    public Scheduler.Cancellable subscribe(String topic, final Listener listener) {
        List<Listener> waiting = listeners.get(topic);
        if (waiting == null) {
            waiting = new ArrayList<Listener>();
            listeners.put(topic, waiting);
        }
        final List<Listener> installed = waiting;
        installed.add(listener);
        return new Scheduler.Cancellable() {
            @Override
            public void cancel() {
                installed.remove(listener);
            }
        };
    }
}
