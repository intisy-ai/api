package io.github.intisy.ai.seam;

import io.github.intisy.ai.api.seam.Store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * In-memory {@link Store}: a plain {@code Map<String,String>}, no I/O, all state lost when the
 * process exits.
 *
 * @implNote Insertion-ordered so {@code listKeys} is deterministic, which the callers that seed it
 * from a snapshot rely on, and every method synchronizes on the map so a JVM caller gets
 * {@code FileStore}'s atomic-update guarantee in-process. A caller wanting a store that round-trips
 * every call into JavaScript takes {@code JsStoreBridge} instead.
 */
public class InMemoryStore implements Store {
    private final Map<String, String> data = new LinkedHashMap<>();

    @Override
    public String get(String key) {
        synchronized (data) {
            return data.get(key);
        }
    }

    @Override
    public void put(String key, String value) {
        synchronized (data) {
            data.put(key, value);
        }
    }

    @Override
    public boolean exists(String key) {
        synchronized (data) {
            return data.containsKey(key);
        }
    }

    @Override
    public void delete(String key) {
        synchronized (data) {
            data.remove(key);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implNote A {@code null} mutator result writes an empty string rather than removing the key,
     * matching {@code FileStore}: an updated key exists afterwards either way.
     */
    @Override
    public void update(String key, UnaryOperator<String> mutator) {
        synchronized (data) {
            String next = mutator.apply(data.get(key));
            data.put(key, next != null ? next : "");
        }
    }

    @Override
    public List<String> listKeys(String prefix) {
        synchronized (data) {
            List<String> keys = new ArrayList<>();
            for (String k : data.keySet()) {
                if (k.startsWith(prefix)) keys.add(k);
            }
            return keys;
        }
    }
}
