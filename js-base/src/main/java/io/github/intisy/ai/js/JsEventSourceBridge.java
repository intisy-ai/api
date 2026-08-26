package io.github.intisy.ai.js;

import io.github.intisy.ai.api.seam.EventSource;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSPromise;
import org.teavm.jso.core.JSString;

/**
 * An {@link EventSource} whose events come from a JS-provided async pull function, bridged with the
 * same {@code @Async} + {@link AsyncCallback} mechanism as {@link JsHttpClientBridge}.
 *
 * <p>{@link #next()} looks synchronous to every caller up the shared call graph but suspends on
 * {@link #awaitNext}, and TeaVM's whole-program CPS transform propagates that suspend to whichever
 * entrypoint triggered it. Crossing this boundary REPEATEDLY inside one Java call is supported and
 * measured: the caller may pull in a loop, and may write to a sink between pulls, so a stream flows
 * through Java without being buffered.
 */
public final class JsEventSourceBridge implements EventSource {

    /** JS-provided async pull: {@code () => Promise<string | null>}, resolving null once drained.
     *
     *  <p>Uses {@link JSString} rather than plain {@code String} for the reason
     *  {@link JsHttpClientBridge.JsHttpSend} documents at length: a value crossing a generic
     *  JS-facing functor is type-erased at the call site, so no wrap/unwrap fires and a raw native
     *  JS string leaks into Java code expecting a {@code jl_String}. */
    @JSFunctor
    public interface JsStreamNext extends JSObject {
        /**
         * Pulls the next event from the JS side. Called from Java by {@link #awaitNext}; the
         * implementation lives in JS.
         *
         * @return a promise resolving to the next event, or a JS null once the source is drained
         */
        JSPromise<JSString> next();
    }

    private final JsStreamNext jsNext;
    private boolean drained;

    /**
     * Wraps a JS-supplied async pull function behind the {@link EventSource} contract.
     *
     * @param jsNext JS callback invoked to pull the next event
     */
    public JsEventSourceBridge(JsStreamNext jsNext) {
        this.jsNext = jsNext;
    }

    @Override
    public String next() {
        if (drained) return null;
        String event = awaitNext(jsNext); // <-- suspends here; resumes once JS's Promise settles
        if (event == null) drained = true;
        return event;
    }

    // -- @Async bridge ------------------------------------------------------------

    /** Blocking-looking native entrypoint; TeaVM's async transform makes every caller of this
     *  method (transitively) suspend/resume instead of actually blocking a JS thread. */
    @Async
    private static native String awaitNext(JsStreamNext fn);

    // Companion method: same name, void return, trailing AsyncCallback<T>, the exact shape TeaVM's
    // async codegen looks for to pair with the @Async native declaration above.
    private static void awaitNext(JsStreamNext fn, AsyncCallback<String> callback) {
        fn.next().then(
                value -> {
                    // A JS null/undefined arrives as a null JSString rather than the string "null",
                    // which is how a drained source is distinguished from one yielding empty text.
                    callback.complete(value == null ? null : value.stringValue());
                    return null;
                },
                error -> {
                    callback.error(new RuntimeException("stream source rejected: " + error));
                    return null;
                });
    }
}
