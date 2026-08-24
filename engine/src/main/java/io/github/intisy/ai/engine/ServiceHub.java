package io.github.intisy.ai.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The live service registry a host shares between every plugin it loads.
 *
 * @implNote get is for optional use, want for an awaited arrival and watch for churn. That triple is
 * what lets a plugin be installed, enabled, disabled and updated at runtime while its consumers keep
 * working, at any ecosystem size.
 */
public final class ServiceHub {

    /** Where the host records who provides and who consumes what. */
    public interface Recorder {
        void provided(String pluginId, String serviceId);

        void consumed(String pluginId, String serviceId);
    }

    /** Called when a watched service arrives or goes away. */
    public interface Listener {
        void changed(Object service, boolean registered);
    }

    /** One plugin's view of the registry, which is what stamps and enforces its namespace. */
    public interface Registry {
        Object get(String id);

        Pending<Object> want(String id);

        Pending<Object> want(String id, long timeoutMillis);

        Scheduler.Cancellable watch(String id, Listener listener);

        Scheduler.Cancellable register(String id, Object service);

        List<String> ids();
    }

    private static final class Entry {
        private final String pluginId;
        private final Object service;

        Entry(String pluginId, Object service) {
            this.pluginId = pluginId;
            this.service = service;
        }
    }

    private static final class Waiter {
        private final String pluginId;
        private final Pending<Object> settled;
        private Scheduler.Cancellable timer;

        Waiter(String pluginId, Pending<Object> settled) {
            this.pluginId = pluginId;
            this.settled = settled;
        }
    }

    private final Scheduler scheduler;
    private final Recorder recorder;
    private final Map<String, Entry> entries = new LinkedHashMap<String, Entry>();
    private final Map<String, Set<Waiter>> waiters = new LinkedHashMap<String, Set<Waiter>>();
    private final Map<String, Set<Listener>> watchers = new LinkedHashMap<String, Set<Listener>>();
    private final Map<String, List<Scheduler.Cancellable>> watching = new LinkedHashMap<String, List<Scheduler.Cancellable>>();
    private List<String> wellKnown = new ArrayList<String>();

    // No plugin can own this, because a manifest id is never empty, which is what keeps a host's own
    // service out of every per-plugin sweep.
    private static final String HOST_OWNER = "";

    public ServiceHub(Scheduler scheduler, Recorder recorder) {
        this.scheduler = scheduler == null ? Scheduler.NEVER : scheduler;
        this.recorder = recorder;
    }

    /**
     * The bare ids any plugin may register, because they name a CONTRACT rather than one plugin's own
     * offering.
     *
     * @implNote Set by the host rather than held as a constant, because this package mints no
     * vocabulary. A hub told nothing treats every bare id as squatting.
     */
    public void wellKnown(List<String> ids) {
        this.wellKnown = ids == null ? new ArrayList<String>() : new ArrayList<String>(ids);
    }

    public Registry forPlugin(final String pluginId) {
        return new Registry() {
            @Override
            public Object get(String id) {
                consumed(pluginId, id);
                Entry entry = entries.get(id);
                return entry == null ? null : entry.service;
            }

            @Override
            public Pending<Object> want(String id) {
                return awaitService(pluginId, id, null);
            }

            @Override
            public Pending<Object> want(String id, long timeoutMillis) {
                return awaitService(pluginId, id, Long.valueOf(timeoutMillis));
            }

            @Override
            public Scheduler.Cancellable watch(String id, Listener listener) {
                return addWatcher(pluginId, id, listener);
            }

            @Override
            public Scheduler.Cancellable register(String id, Object service) {
                return addService(pluginId, id, service);
            }

            @Override
            public List<String> ids() {
                return ServiceHub.this.ids();
            }
        };
    }

    /**
     * Registers a service the HOST supplies rather than a plugin.
     *
     * @implNote Owned by no plugin, so quarantining or deactivating one never takes it away, and it
     * is exempt from the namespacing rule a plugin registration is held to: the ids a host offers
     * are ones it defines rather than ones it is claiming from a shared space. Registering the same
     * id twice replaces it, because a host wiring itself up twice is a restart, not a conflict.
     */
    public void hostService(String id, Object service) {
        entries.put(id, new Entry(HOST_OWNER, service));
        Set<Waiter> pending = waiters.remove(id);
        if (pending != null) {
            for (Waiter waiter : pending) {
                if (waiter.timer != null) {
                    waiter.timer.cancel();
                }
                waiter.settled.resolve(service);
            }
        }
        notifyWatchers(id, service, true);
    }

    /** The service now, or null, without attributing the read to a plugin. */
    public Object get(String id) {
        Entry entry = entries.get(id);
        return entry == null ? null : entry.service;
    }

    public List<String> ids() {
        return new ArrayList<String>(entries.keySet());
    }

    /**
     * Unregisters everything a plugin registered, stops the watchers it installed, and rejects the
     * want calls it is still waiting on, so a deactivated or quarantined plugin is inert.
     */
    public void releasePlugin(String pluginId) {
        List<Scheduler.Cancellable> owned = watching.get(pluginId);
        if (owned != null) {
            for (Scheduler.Cancellable stop : new ArrayList<Scheduler.Cancellable>(owned)) {
                stop.cancel();
            }
        }
        watching.remove(pluginId);

        for (Map.Entry<String, Entry> registered : new ArrayList<Map.Entry<String, Entry>>(entries.entrySet())) {
            if (registered.getValue().pluginId.equals(pluginId)) {
                unregister(pluginId, registered.getKey());
            }
        }

        Iterator<Map.Entry<String, Set<Waiter>>> pending = waiters.entrySet().iterator();
        while (pending.hasNext()) {
            Map.Entry<String, Set<Waiter>> waiting = pending.next();
            for (Waiter waiter : new ArrayList<Waiter>(waiting.getValue())) {
                if (!waiter.pluginId.equals(pluginId)) {
                    continue;
                }
                waiting.getValue().remove(waiter);
                if (waiter.timer != null) {
                    waiter.timer.cancel();
                }
                waiter.settled.reject(stoppedWaiting(pluginId, waiting.getKey()));
            }
            if (waiting.getValue().isEmpty()) {
                pending.remove();
            }
        }
    }

    private Scheduler.Cancellable addService(final String pluginId, final String id, Object service) {
        if (!mayRegister(pluginId, id)) {
            String name = id.contains(":") ? id.substring(id.lastIndexOf(':') + 1) : id;
            throw new PluginException(pluginId,
                    "cannot register service \"" + id + "\", which belongs to another plugin",
                    "namespace it as \"" + pluginId + ":" + name + "\", or register one of the well-known ids: " + join(wellKnown));
        }
        Entry existing = entries.get(id);
        if (existing != null) {
            throw new PluginException(pluginId,
                    "service \"" + id + "\" is already registered by " + existing.pluginId,
                    "disable one of the two plugins, or have each register its own namespaced id so consumers can ask for the one they want");
        }
        entries.put(id, new Entry(pluginId, service));
        provided(pluginId, id);
        Set<Waiter> pending = waiters.remove(id);
        if (pending != null) {
            for (Waiter waiter : pending) {
                if (waiter.timer != null) {
                    waiter.timer.cancel();
                }
                waiter.settled.resolve(service);
            }
        }
        notifyWatchers(id, service, true);
        return new Scheduler.Cancellable() {
            @Override
            public void cancel() {
                unregister(pluginId, id);
            }
        };
    }

    private void unregister(String pluginId, String id) {
        Entry entry = entries.get(id);
        if (entry == null || !entry.pluginId.equals(pluginId)) {
            return;
        }
        entries.remove(id);
        notifyWatchers(id, null, false);
    }

    /**
     * @implNote A Pending nobody watched is inert, so a want whose caller dropped it needs no guard.
     * The JavaScript boundary is where that stops being true, and it attaches the catch there.
     */
    private Pending<Object> awaitService(final String pluginId, final String id, Long timeoutMillis) {
        consumed(pluginId, id);
        Entry entry = entries.get(id);
        if (entry != null) {
            return Pending.of(entry.service);
        }
        Pending<Object> settled = new Pending<Object>();
        final Waiter waiter = new Waiter(pluginId, settled);
        if (timeoutMillis != null) {
            final long millis = timeoutMillis.longValue();
            waiter.timer = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    Set<Waiter> pending = waiters.get(id);
                    if (pending != null) {
                        pending.remove(waiter);
                    }
                    waiter.settled.reject(new PluginException(pluginId,
                            "waited " + millis + "ms for service \"" + id + "\" and nothing registered it",
                            "install a plugin that provides \"" + id + "\", or use get() and carry on without it"));
                }
            }, millis);
        }
        Set<Waiter> pending = waiters.get(id);
        if (pending == null) {
            pending = new LinkedHashSet<Waiter>();
            waiters.put(id, pending);
        }
        pending.add(waiter);
        return settled;
    }

    private Scheduler.Cancellable addWatcher(String pluginId, String id, final Listener listener) {
        consumed(pluginId, id);
        Set<Listener> listeners = watchers.get(id);
        if (listeners == null) {
            listeners = new LinkedHashSet<Listener>();
            watchers.put(id, listeners);
        }
        final Set<Listener> installed = listeners;
        installed.add(listener);
        return tracked(pluginId, new Scheduler.Cancellable() {
            @Override
            public void cancel() {
                installed.remove(listener);
            }
        });
    }

    private Scheduler.Cancellable tracked(final String pluginId, final Scheduler.Cancellable dispose) {
        final boolean[] done = new boolean[1];
        final Scheduler.Cancellable[] self = new Scheduler.Cancellable[1];
        self[0] = new Scheduler.Cancellable() {
            @Override
            public void cancel() {
                if (done[0]) {
                    return;
                }
                done[0] = true;
                dispose.cancel();
                List<Scheduler.Cancellable> owned = watching.get(pluginId);
                if (owned == null) {
                    return;
                }
                owned.remove(self[0]);
                if (owned.isEmpty()) {
                    watching.remove(pluginId);
                }
            }
        };
        List<Scheduler.Cancellable> owned = watching.get(pluginId);
        if (owned == null) {
            owned = new ArrayList<Scheduler.Cancellable>();
            watching.put(pluginId, owned);
        }
        owned.add(self[0]);
        return self[0];
    }

    /**
     * @implNote A watcher that throws is reported and iteration continues, because one bad consumer
     * must not stop the others from hearing about a registration. A watcher removed during the
     * iteration is skipped rather than called.
     */
    private void notifyWatchers(String id, Object service, boolean registered) {
        Set<Listener> listeners = watchers.get(id);
        if (listeners == null) {
            return;
        }
        for (Listener listener : new ArrayList<Listener>(listeners)) {
            if (!listeners.contains(listener)) {
                continue;
            }
            try {
                listener.changed(service, registered);
            } catch (RuntimeException failure) {
                Diagnostics.report("a watcher of \"" + id + "\" threw while handling "
                        + (registered ? "register" : "unregister") + ": " + failure);
            }
        }
    }

    private boolean mayRegister(String pluginId, String serviceId) {
        if (wellKnown.contains(serviceId)) {
            return true;
        }
        int separator = serviceId.indexOf(':');
        if (separator <= 0 || separator == serviceId.length() - 1) {
            return false;
        }
        return serviceId.substring(0, separator).equals(pluginId);
    }

    static PluginException stoppedWaiting(String pluginId, String id) {
        return new PluginException(pluginId,
                "stopped while waiting for service \"" + id + "\"",
                "provide \"" + id + "\" before this plugin is stopped, or use get() and carry on without it");
    }

    private void provided(String pluginId, String serviceId) {
        if (recorder != null) {
            recorder.provided(pluginId, serviceId);
        }
    }

    private void consumed(String pluginId, String serviceId) {
        if (recorder != null) {
            recorder.consumed(pluginId, serviceId);
        }
    }

    private static String join(List<String> values) {
        StringBuilder out = new StringBuilder();
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                out.append(", ");
            }
            out.append(values.get(index));
        }
        return out.toString();
    }
}
