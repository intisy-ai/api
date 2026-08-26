package io.github.intisy.ai.js;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/** What the host supplies per plugin, as the JavaScript object it already builds. */
public interface JsRuntime extends JSObject {

    /** A JavaScript disposer, which is what every subscribe and register hands back. */
    @JSFunctor
    interface Disposer extends JSObject {
        /** Cancels the subscription or registration this disposer came from. */
        void dispose();
    }

    /** A JavaScript event listener. */
    @JSFunctor
    interface Listener extends JSObject {
        /**
         * Called by the host's bus on every publish to the subscribed topic.
         *
         * @param payload the value the publisher passed, opaque to the engine
         */
        void received(JSObject payload);
    }

    /** The host's bus, called into rather than replaced, so the engine can record and fence it. */
    interface Bus extends JSObject {
        /**
         * Called by the engine to publish a payload on a topic.
         *
         * @param topic the topic to publish on
         * @param payload the value every current subscriber receives, opaque to the engine
         */
        void publish(String topic, JSObject payload);

        /**
         * Called by the engine to subscribe to a topic.
         *
         * @param topic the topic to listen on
         * @param listener called by the host with each payload published on the topic
         * @return the disposer that cancels this subscription
         */
        Disposer subscribe(String topic, Listener listener);
    }

    /** The host's home registry, asked on each call so a home appearing later is seen. */
    interface Homes extends JSObject {
        /**
         * Called by the engine to read every app home the host currently knows about.
         *
         * @return the known homes as a JavaScript array, opaque to the engine until it is converted
         */
        JSObject all();
    }

    /**
     * The plugin's resolved configuration, as the runtime supplied it.
     *
     * @return the configuration value, opaque to the engine
     */
    @JSProperty
    JSObject getConfig();

    /**
     * The plugin's logger, as the runtime supplied it.
     *
     * @return the logger value, opaque to the engine
     */
    @JSProperty
    JSObject getLog();

    /**
     * The storage paths of the home the plugin runs in.
     *
     * @return the paths value, opaque to the engine
     */
    @JSProperty
    JSObject getPaths();

    /**
     * The host's event bus for this plugin.
     *
     * @return the bus
     */
    @JSProperty
    Bus getEvents();

    /**
     * The host's registry of app homes.
     *
     * @return the registry, or absent when the host declared no home registry
     */
    @JSProperty
    Homes getHomes();
}
