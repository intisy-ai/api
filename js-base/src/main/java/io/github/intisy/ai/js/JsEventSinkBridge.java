package io.github.intisy.ai.js;

import io.github.intisy.ai.api.seam.EventSink;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSString;

/**
 * An {@link EventSink} that pushes straight into JS-provided callbacks.
 *
 * <p>Unlike {@link JsEventSourceBridge} this needs no {@code @Async} bridge: emitting is a plain
 * synchronous call out to JS, which is what lets a Java loop write between two suspending pulls.
 */
public final class JsEventSinkBridge implements EventSink {

    /** JS-provided push: {@code (event: string) => void}.
     *
     *  <p>Declared as its own {@code @JSFunctor} interface, and it must be DECLARED as that type at
     *  the crossing point: typed as a bare {@code JSObject} instead, TeaVM hands JavaScript an
     *  object carrying a prototype method rather than a callable function, while the generated
     *  {@code .d.ts} still claims a function. */
    @JSFunctor
    public interface JsStreamEmit extends JSObject {
        void emit(JSString event);
    }

    /** JS-provided completion: {@code (error: string | null) => void}. */
    @JSFunctor
    public interface JsStreamClose extends JSObject {
        void close(JSString error);
    }

    private final JsStreamEmit jsEmit;
    private final JsStreamClose jsClose;

    public JsEventSinkBridge(JsStreamEmit jsEmit, JsStreamClose jsClose) {
        this.jsEmit = jsEmit;
        this.jsClose = jsClose;
    }

    @Override
    public void emit(String event) {
        jsEmit.emit(JSString.valueOf(event));
    }

    @Override
    public void close(String error) {
        jsClose.close(error == null ? null : JSString.valueOf(error));
    }
}
