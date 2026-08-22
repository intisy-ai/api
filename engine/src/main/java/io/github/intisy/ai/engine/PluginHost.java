package io.github.intisy.ai.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The host side of the plugin system: it opens sessions, collects what plugins provide, and keeps
 * the ledger.
 *
 * @implNote Nothing here branches on a plugin id, and nothing can: a capability id and a service id
 * are the only dispatch keys a host has, which is what keeps a host ignorant of every specific
 * plugin no matter how many exist. The capability vocabulary is DECLARED by the caller rather than
 * minted here, so a host that renders no screens can warn about a plugin providing them and a host
 * that declares nothing reports nothing rather than calling every id unknown.
 */
public final class PluginHost {

    private final String app;
    private final int api;
    private final List<String> surfaces;
    private final PluginLedger ledger = new PluginLedger();
    private final ServiceHub hub;
    private final Map<String, List<CapabilityRecord>> capabilities = new LinkedHashMap<String, List<CapabilityRecord>>();
    private final Map<String, List<Scheduler.Cancellable>> disposers = new LinkedHashMap<String, List<Scheduler.Cancellable>>();
    private final Set<String> revoked = new HashSet<String>();
    private List<String> known = new ArrayList<String>();

    public PluginHost(String app, int api, List<String> surfaces) {
        this(app, api, surfaces, Scheduler.NEVER);
    }

    public PluginHost(String app, int api, List<String> surfaces, Scheduler scheduler) {
        this.app = app;
        this.api = api;
        this.surfaces = surfaces == null ? new ArrayList<String>() : new ArrayList<String>(surfaces);
        this.hub = new ServiceHub(scheduler, new ServiceHub.Recorder() {
            @Override
            public void provided(String pluginId, String serviceId) {
                ledger.recordServiceProvided(pluginId, serviceId);
            }

            @Override
            public void consumed(String pluginId, String serviceId) {
                ledger.recordServiceConsumed(pluginId, serviceId);
            }
        });
    }

    public String getApp() {
        return app;
    }

    public int getApi() {
        return api;
    }

    public List<String> getSurfaces() {
        return Collections.unmodifiableList(surfaces);
    }

    public PluginLedger getLedger() {
        return ledger;
    }

    /** The capability ids this host understands. Told none, it verifies none and reports none. */
    public void knownCapabilities(List<String> ids) {
        this.known = ids == null ? new ArrayList<String>() : new ArrayList<String>(ids);
    }

    /** The bare service ids any plugin may register, forwarded to the registry. */
    public void wellKnownServices(List<String> ids) {
        hub.wellKnown(ids);
    }

    /** Null when this host can load the plugin, or the failure explaining why it cannot. */
    public PluginException supports(String pluginId, int pluginApi) {
        if (pluginApi <= api) {
            return null;
        }
        return new PluginException(pluginId,
                "needs api " + pluginApi + ", this host has api " + api,
                "update the app to a version that implements api " + pluginApi + " or later");
    }

    /** Opens the session one plugin's activation runs against, lifting any fence it was under. */
    public PluginSession sessionFor(ManifestFacts facts, EventBus bus) {
        String pluginId = facts.getId();
        revoked.remove(pluginId);
        ledger.recordDeclared(pluginId, facts.getCapabilities(), facts.getPermissions());
        return new PluginSession(this, facts, fenced(pluginId, hub.forPlugin(pluginId)), recording(pluginId, bus));
    }

    /**
     * Checks a finished activation against what its manifest declared, marking the plugin active
     * when the two agree and returning the failure to quarantine it with when they do not.
     */
    public PluginException verifyActivation(ManifestFacts facts) {
        String pluginId = facts.getId();
        List<String> declared = facts.getCapabilities();
        LedgerEntry entry = ledger.entry(pluginId);
        List<String> provided = entry == null ? new ArrayList<String>() : entry.getCapabilitiesProvided();

        List<String> missing = missingFrom(declared, provided);
        if (!missing.isEmpty()) {
            return new PluginException(pluginId,
                    "capabilities declared but never provided: " + join(missing),
                    "call ctx.provide(\"" + missing.get(0) + "\", ...) in activate, or remove it from \"capabilities\" in plugin.json");
        }
        List<String> extra = missingFrom(provided, declared);
        if (!extra.isEmpty()) {
            return new PluginException(pluginId,
                    "capabilities provided but never declared: " + join(extra),
                    "add \"" + extra.get(0) + "\" to \"capabilities\" in plugin.json");
        }
        ledger.recordStatus(pluginId, PluginStatus.ACTIVE, null, null);
        return null;
    }

    /** Every implementation of a capability, in activation order. */
    public List<CapabilityRecord> capability(String id) {
        List<CapabilityRecord> records = capabilities.get(id);
        return records == null ? new ArrayList<CapabilityRecord>() : new ArrayList<CapabilityRecord>(records);
    }

    /** One service, or null when nothing provides it. */
    public Object service(String id) {
        return hub.get(id);
    }

    /**
     * Quarantines a plugin: its capabilities, services and subscriptions go, the host stays up.
     *
     * @implNote Its session is fenced too, so an activation that finishes after the host stopped
     * waiting cannot register itself back in. Opening a session again lifts the fence.
     */
    public void markBroken(String pluginId, PluginException error) {
        strip(pluginId);
        ledger.recordStatus(pluginId, PluginStatus.BROKEN, error.getDetail(), error.getFix());
    }

    /** Releases everything a plugin provided and every subscription it holds, and fences its session. */
    public void release(String pluginId) {
        strip(pluginId);
        ledger.recordStatus(pluginId, PluginStatus.STOPPED, null, null);
    }

    void provide(String pluginId, String id, Object implementation) {
        if (refuseLate(pluginId, "a late provision of capability", id)) {
            return;
        }
        if (!known.isEmpty() && !known.contains(id)) {
            Diagnostics.ignoreUnknown("capability", id, pluginId);
        }
        List<CapabilityRecord> records = capabilities.get(id);
        if (records == null) {
            records = new ArrayList<CapabilityRecord>();
            capabilities.put(id, records);
        }
        for (CapabilityRecord record : records) {
            if (record.getPluginId().equals(pluginId)) {
                throw new PluginException(pluginId,
                        "provided capability \"" + id + "\" twice",
                        "call ctx.provide once per capability in activate");
            }
        }
        records.add(new CapabilityRecord(pluginId, implementation));
        ledger.recordCapabilityProvided(pluginId, id);
    }

    private void strip(String pluginId) {
        revoked.add(pluginId);
        dropCapabilities(pluginId);
        detach(pluginId);
        hub.releasePlugin(pluginId);
    }

    /**
     * Whether a call from this plugin arrives after it was quarantined or released.
     *
     * @implNote Quarantine stops the host waiting for a plugin, it does not stop the plugin: an
     * activation abandoned at its deadline can still finish and register. A late call is reported
     * rather than thrown, because the caller is work nobody is waiting on.
     */
    private boolean refuseLate(String pluginId, String what, String id) {
        if (!revoked.contains(pluginId)) {
            return false;
        }
        Diagnostics.report("ignored " + what + " \"" + id + "\" from " + pluginId + ", which is no longer running");
        return true;
    }

    /**
     * @implNote get stays open: it installs nothing, and its honest answer to a revoked plugin is the
     * same null every other caller gets for a service nobody registered.
     */
    private ServiceHub.Registry fenced(final String pluginId, final ServiceHub.Registry registry) {
        return new ServiceHub.Registry() {
            @Override
            public Object get(String id) {
                return registry.get(id);
            }

            @Override
            public Pending<Object> want(String id) {
                return refuseLate(pluginId, "a late want of service", id) ? refuseWant(pluginId, id) : registry.want(id);
            }

            @Override
            public Pending<Object> want(String id, long timeoutMillis) {
                return refuseLate(pluginId, "a late want of service", id) ? refuseWant(pluginId, id) : registry.want(id, timeoutMillis);
            }

            @Override
            public Scheduler.Cancellable watch(String id, ServiceHub.Listener listener) {
                return refuseLate(pluginId, "a late watch of service", id) ? inert() : registry.watch(id, listener);
            }

            @Override
            public Scheduler.Cancellable register(String id, Object service) {
                return refuseLate(pluginId, "a late registration of service", id) ? inert() : registry.register(id, service);
            }

            @Override
            public List<String> ids() {
                return registry.ids();
            }
        };
    }

    private EventBus recording(final String pluginId, final EventBus bus) {
        return new EventBus() {
            @Override
            public void publish(String topic, Object payload) {
                bus.publish(topic, payload);
            }

            @Override
            public Scheduler.Cancellable subscribe(String topic, EventBus.Listener listener) {
                if (refuseLate(pluginId, "a late subscription to topic", topic)) {
                    return inert();
                }
                ledger.recordTopic(pluginId, topic);
                return tracked(pluginId, bus.subscribe(topic, listener));
            }
        };
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
                List<Scheduler.Cancellable> owned = disposers.get(pluginId);
                if (owned == null) {
                    return;
                }
                owned.remove(self[0]);
                if (owned.isEmpty()) {
                    disposers.remove(pluginId);
                }
            }
        };
        List<Scheduler.Cancellable> owned = disposers.get(pluginId);
        if (owned == null) {
            owned = new ArrayList<Scheduler.Cancellable>();
            disposers.put(pluginId, owned);
        }
        owned.add(self[0]);
        return self[0];
    }

    private void detach(String pluginId) {
        List<Scheduler.Cancellable> owned = disposers.get(pluginId);
        if (owned != null) {
            for (Scheduler.Cancellable dispose : new ArrayList<Scheduler.Cancellable>(owned)) {
                dispose.cancel();
            }
        }
        disposers.remove(pluginId);
    }

    private void dropCapabilities(String pluginId) {
        List<String> emptied = new ArrayList<String>();
        for (Map.Entry<String, List<CapabilityRecord>> held : capabilities.entrySet()) {
            List<CapabilityRecord> kept = new ArrayList<CapabilityRecord>();
            for (CapabilityRecord record : held.getValue()) {
                if (!record.getPluginId().equals(pluginId)) {
                    kept.add(record);
                }
            }
            if (kept.isEmpty()) {
                emptied.add(held.getKey());
            } else {
                held.setValue(kept);
            }
        }
        for (String id : emptied) {
            capabilities.remove(id);
        }
    }

    private static Pending<Object> refuseWant(String pluginId, String id) {
        Pending<Object> refused = new Pending<Object>();
        refused.reject(ServiceHub.stoppedWaiting(pluginId, id));
        return refused;
    }

    private static Scheduler.Cancellable inert() {
        return new Scheduler.Cancellable() {
            @Override
            public void cancel() {
            }
        };
    }

    private static List<String> missingFrom(List<String> wanted, List<String> present) {
        List<String> absent = new ArrayList<String>();
        for (String id : wanted) {
            if (!present.contains(id)) {
                absent.add(id);
            }
        }
        return absent;
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
