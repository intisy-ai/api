package io.github.intisy.ai.js;

import io.github.intisy.ai.engine.EventBus;
import io.github.intisy.ai.engine.Pending;
import io.github.intisy.ai.engine.PluginException;
import io.github.intisy.ai.engine.PluginSession;
import io.github.intisy.ai.engine.Scheduler;
import io.github.intisy.ai.engine.ServiceHub;
import java.util.Map;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * @implNote Shape only, exactly like {@link JsPluginHost}: it converts a Java list to a JSArray (via
 * {@code services.ids()}), a Pending to a Promise, and a PluginException to a marked JavaScript Error
 * for {@code provide} and for {@code register}, reusing {@link JsPluginHost#serviceEvent} for the
 * watch listener's event string and {@link JsPluginHost#orUndefined} for a missing {@code get}. Every
 * other branch here is the engine's, reached through the session it built.
 *
 * <p>{@code manifest}, {@code config}, {@code log} and {@code paths} cross untouched and by identity,
 * because {@code context.manifest} must be the very object a plugin's manifest was parsed from, not a
 * rebuilt copy that merely looks the same.
 */
final class JsPluginContext implements JSObject {

    private JsPluginContext() {
    }

    @JSFunctor
    interface IdFn extends JSObject {
        JSObject call(String id);
    }

    @JSFunctor
    interface IdsFn extends JSObject {
        JSObject call();
    }

    @JSFunctor
    interface WantFn extends JSObject {
        JSObject call(String id, JSObject options);
    }

    @JSFunctor
    interface ServiceListenerFn extends JSObject {
        void call(JSObject service, String event);
    }

    @JSFunctor
    interface WatchFn extends JSObject {
        JsRuntime.Disposer call(String id, ServiceListenerFn listener);
    }

    @JSFunctor
    interface RegisterFn extends JSObject {
        JsRuntime.Disposer call(String id, JSObject service);
    }

    @JSFunctor
    interface PayloadListenerFn extends JSObject {
        void call(JSObject payload);
    }

    @JSFunctor
    interface PublishFn extends JSObject {
        void call(String topic, JSObject payload);
    }

    @JSFunctor
    interface SubscribeFn extends JSObject {
        JsRuntime.Disposer call(String topic, PayloadListenerFn listener);
    }

    @JSFunctor
    interface ProvideFn extends JSObject {
        void call(JSObject key, JSObject implementation);
    }

    /** Builds the context one plugin's activate receives, from the session {@link JsPluginHost} opened. */
    static JsPluginContext build(final PluginSession session, JsRuntime runtime, JSObject hostDescriptor) {
        final ServiceHub.Registry services = session.getServices();
        final EventBus events = session.getEvents();
        JSObject manifest = (JSObject) session.getFacts().getPayload();

        IdFn get = new IdFn() {
            @Override
            public JSObject call(String id) {
                return JsPluginHost.orUndefined((JSObject) services.get(id));
            }
        };
        WantFn want = new WantFn() {
            @Override
            public JSObject call(String id, JSObject options) {
                Long timeoutMs = timeoutOf(options);
                Pending<Object> pending = timeoutMs == null ? services.want(id) : services.want(id, timeoutMs.longValue());
                return JsPluginHost.promise(pending);
            }
        };
        WatchFn watch = new WatchFn() {
            @Override
            public JsRuntime.Disposer call(String id, final ServiceListenerFn listener) {
                Scheduler.Cancellable cancellable = services.watch(id, new ServiceHub.Listener() {
                    @Override
                    public void changed(Object service, boolean registered) {
                        listener.call(JsPluginHost.orUndefined((JSObject) service), JsPluginHost.serviceEvent(registered));
                    }
                });
                return disposerOf(cancellable);
            }
        };
        RegisterFn register = new RegisterFn() {
            @Override
            public JsRuntime.Disposer call(String id, JSObject service) {
                try {
                    Scheduler.Cancellable cancellable = services.register(id, service);
                    return disposerOf(cancellable);
                } catch (PluginException failure) {
                    JsErrors.raise(JsErrors.of(failure));
                    return null;
                }
            }
        };
        IdsFn ids = new IdsFn() {
            @Override
            public JSObject call() {
                return JsJson.fromStrings(services.ids());
            }
        };
        JSObject servicesObj = servicesObject(get, want, watch, register, ids);

        PublishFn publish = new PublishFn() {
            @Override
            public void call(String topic, JSObject payload) {
                events.publish(topic, payload);
            }
        };
        SubscribeFn subscribe = new SubscribeFn() {
            @Override
            public JsRuntime.Disposer call(String topic, final PayloadListenerFn listener) {
                Scheduler.Cancellable cancellable = events.subscribe(topic, new EventBus.Listener() {
                    @Override
                    public void received(Object payload) {
                        listener.call((JSObject) payload);
                    }
                });
                return disposerOf(cancellable);
            }
        };
        JSObject eventsObj = eventsObject(publish, subscribe);

        ProvideFn provide = new ProvideFn() {
            @Override
            public void call(JSObject key, JSObject implementation) {
                try {
                    session.provide(capabilityId(key), implementation);
                } catch (PluginException failure) {
                    JsErrors.raise(JsErrors.of(failure));
                }
            }
        };

        return assemble(manifest, hostDescriptor, runtime.getConfig(), runtime.getLog(), runtime.getPaths(),
                servicesObj, eventsObj, provide);
    }

    /**
     * The capability id behind whatever {@code provide} was handed: a typed key's {@code id}, or the
     * value itself when a host passed a bare id.
     */
    @JSBody(params = "key", script =
            "return key !== null && typeof key === 'object' ? String(key.id) : String(key);")
    private static native String capabilityId(JSObject key);

    private static JsRuntime.Disposer disposerOf(final Scheduler.Cancellable cancellable) {
        return new JsRuntime.Disposer() {
            @Override
            public void dispose() {
                cancellable.cancel();
            }
        };
    }

    /** Null when the plugin called {@code want(id)} with no options, or the {@code timeoutMs} it gave. */
    private static Long timeoutOf(JSObject options) {
        if (options == null) {
            return null;
        }
        Object tree = JsJson.toTree(options);
        Object value = tree instanceof Map ? ((Map<?, ?>) tree).get("timeoutMs") : null;
        return value instanceof Number ? Long.valueOf(((Number) value).longValue()) : null;
    }

    @JSBody(params = {"get", "want", "watch", "register", "ids"}, script =
            "return { get: get, want: want, watch: watch, register: register, ids: ids };")
    private static native JSObject servicesObject(IdFn get, WantFn want, WatchFn watch, RegisterFn register, IdsFn ids);

    @JSBody(params = {"publish", "subscribe"}, script = "return { publish: publish, subscribe: subscribe };")
    private static native JSObject eventsObject(PublishFn publish, SubscribeFn subscribe);

    @JSBody(params = {"manifest", "host", "config", "log", "paths", "services", "events", "provide"}, script =
            "return { manifest: manifest, host: host, config: config, log: log, paths: paths, services: services, "
            + "events: events, provide: provide };")
    private static native JsPluginContext assemble(JSObject manifest, JSObject host, JSObject config, JSObject log,
            JSObject paths, JSObject services, JSObject events, ProvideFn provide);
}
