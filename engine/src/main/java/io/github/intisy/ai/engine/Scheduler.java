package io.github.intisy.ai.engine;

/**
 * The clock the registry borrows.
 *
 * @implNote Injected rather than owned, because TeaVM has no thread support and this module must
 * transpile: it cannot hold a ScheduledExecutorService or a java.util.Timer. A JVM host backs this
 * with an executor, a TeaVM build with setTimeout, and a test with a scheduler it fires by hand.
 */
public interface Scheduler {

    /** A scheduled task that has not run yet. */
    interface Cancellable {
        /** Prevents the scheduled task from running, if it has not run already. */
        void cancel();
    }

    /**
     * @param task the work to run once {@code delayMillis} elapses
     * @param delayMillis how long to wait before running {@code task}, in milliseconds
     * @return a handle that cancels the scheduled run
     */
    Cancellable schedule(Runnable task, long delayMillis);

    /** Accepts work and never runs it, for a host that wants no timeouts at all. */
    Scheduler NEVER = new Scheduler() {
        @Override
        public Cancellable schedule(Runnable task, long delayMillis) {
            return new Cancellable() {
                @Override
                public void cancel() {
                }
            };
        }
    };
}
