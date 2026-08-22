package io.github.intisy.ai.engine;

/** Captures a Pending's one outcome, so a test reads it without a concurrency primitive. */
final class Settled<T> implements Pending.Settlement<T> {

    private T value;
    private PluginException failure;
    private int settlements;

    static <T> Settled<T> of(Pending<T> pending) {
        Settled<T> captured = new Settled<T>();
        pending.then(captured);
        return captured;
    }

    @Override
    public void value(T result) {
        this.value = result;
        this.settlements++;
    }

    @Override
    public void failure(PluginException reason) {
        this.failure = reason;
        this.settlements++;
    }

    T value() {
        return value;
    }

    PluginException failure() {
        return failure;
    }

    int settlements() {
        return settlements;
    }
}
