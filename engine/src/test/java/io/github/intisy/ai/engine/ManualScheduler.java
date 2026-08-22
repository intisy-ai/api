package io.github.intisy.ai.engine;

import java.util.ArrayList;
import java.util.List;

/** A scheduler a test fires by hand, so a timeout test is deterministic rather than sleep-based. */
final class ManualScheduler implements Scheduler {

    private final List<Runnable> pending = new ArrayList<Runnable>();

    @Override
    public Cancellable schedule(final Runnable task, long delayMillis) {
        pending.add(task);
        return new Cancellable() {
            @Override
            public void cancel() {
                pending.remove(task);
            }
        };
    }

    void fireAll() {
        List<Runnable> due = new ArrayList<Runnable>(pending);
        pending.clear();
        for (Runnable task : due) {
            task.run();
        }
    }
}
