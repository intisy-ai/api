package io.github.intisy.ai.js;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/** What the host supplies per plugin, as the JavaScript object it already builds. */
public interface JsRuntime extends JSObject {

    /** A JavaScript disposer, which is what every subscribe and register hands back. */
    @JSFunctor
    interface Disposer extends JSObject {
        void dispose();
    }

    /** A JavaScript event listener. */
    @JSFunctor
    interface Listener extends JSObject {
        void received(JSObject payload);
    }

    /** The host's bus, called into rather than replaced, so the engine can record and fence it. */
    interface Bus extends JSObject {
        void publish(String topic, JSObject payload);

        Disposer subscribe(String topic, Listener listener);
    }

    /** The host's home registry, asked on each call so a home appearing later is seen. */
    interface Homes extends JSObject {
        JSObject all();
    }

    @JSProperty
    JSObject getConfig();

    @JSProperty
    JSObject getLog();

    @JSProperty
    JSObject getPaths();

    @JSProperty
    Bus getEvents();

    @JSProperty
    Homes getHomes();
}
