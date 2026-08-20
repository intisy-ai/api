package io.github.intisy.ai.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * A value that arrives later, or the failure that means it never will.
 *
 * @implNote The engine's own rather than a CompletionStage, because TeaVM 0.15.0's class library has
 * neither CompletableFuture nor CompletionStage: its java.util.concurrent carries the executor
 * interfaces, the concurrent collections and nothing more. Same seam as {@link Scheduler}, so the
 * engine owns no concurrency primitive at all: a JVM caller adapts this to a CompletableFuture and a
 * TeaVM build to a JSPromise.
 *
 * <p>Nothing guards an unobserved failure, because there is nothing to guard: an unhandled JavaScript
 * rejection would take a host down, but a Pending nobody watched is inert.
 */
public final class Pending<T> {

    /** Both outcomes on one interface, which is the shape a promise's then and a future's whenComplete both take. */
    public interface Settlement<T> {
        void value(T value);

        void failure(PluginException reason);
    }

    private final List<Settlement<T>> handlers = new ArrayList<Settlement<T>>();
    private boolean settled;
    private T value;
    private PluginException failure;

    /** One already carrying its value, for the answer a caller could have had synchronously. */
    public static <T> Pending<T> of(T value) {
        Pending<T> ready = new Pending<T>();
        ready.resolve(value);
        return ready;
    }

    /**
     * @implNote Runs immediately when the outcome already happened, so a handler attached late never
     * waits for something that will not come again.
     */
    public void then(Settlement<T> handler) {
        if (settled) {
            deliver(handler);
            return;
        }
        handlers.add(handler);
    }

    public boolean isSettled() {
        return settled;
    }

    void resolve(T result) {
        settle(result, null);
    }

    void reject(PluginException reason) {
        settle(null, reason);
    }

    private void settle(T result, PluginException reason) {
        if (settled) {
            return;
        }
        settled = true;
        value = result;
        failure = reason;
        List<Settlement<T>> waiting = new ArrayList<Settlement<T>>(handlers);
        handlers.clear();
        for (Settlement<T> handler : waiting) {
            deliver(handler);
        }
    }

    private void deliver(Settlement<T> handler) {
        if (failure != null) {
            handler.failure(failure);
        } else {
            handler.value(value);
        }
    }
}
