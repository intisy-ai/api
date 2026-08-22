package io.github.intisy.ai.js;

import io.github.intisy.ai.engine.Scheduler;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * The engine's clock, backed by the host page's timer.
 *
 * @implNote The engine owns no timer because TeaVM has no threads, so it cannot hold a
 * ScheduledExecutorService or a java.util.Timer. This is the whole reason {@link Scheduler} is
 * injected rather than owned, and a build that forgets to supply one still compiles: every deadline
 * is simply accepted and never fired.
 */
final class JsScheduler implements Scheduler {

    @JSFunctor
    interface Task extends JSObject {
        void run();
    }

    @Override
    public Cancellable schedule(final Runnable task, long delayMillis) {
        final int handle = later(new Task() {
            @Override
            public void run() {
                task.run();
            }
        }, delayMillis);
        return new Cancellable() {
            @Override
            public void cancel() {
                stop(handle);
            }
        };
    }

    @JSBody(params = {"task", "delay"}, script = "return setTimeout(task, delay);")
    private static native int later(Task task, double delay);

    @JSBody(params = "handle", script = "clearTimeout(handle);")
    private static native void stop(int handle);
}
