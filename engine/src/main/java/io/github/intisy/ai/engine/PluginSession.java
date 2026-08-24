package io.github.intisy.ai.engine;

/**
 * One plugin's live relationship with the host, which is everything a plugin context is built from.
 *
 * @implNote Deliberately not the context itself. A context also carries the config, the logger and
 * the paths, and all three pass from the host to the plugin untouched, so the engine has no business
 * holding them. Whoever adapts this module to a language builds the context from a session plus
 * those three.
 */
public final class PluginSession {

    private final ManifestFacts facts;
    private final ServiceHub.Registry services;
    private final EventBus events;
    private final PluginHost host;

    PluginSession(PluginHost host, ManifestFacts facts, ServiceHub.Registry services, EventBus events) {
        this.host = host;
        this.facts = facts;
        this.services = services;
        this.events = events;
    }

    public ManifestFacts getFacts() {
        return facts;
    }

    /** Scoped so a registration outside this plugin's namespace is refused, and fenced once it stops. */
    public ServiceHub.Registry getServices() {
        return services;
    }

    /** Recording, so every subscription reaches the ledger, and fenced once the plugin stops. */
    public EventBus getEvents() {
        return events;
    }

    public void provide(String id, Object implementation) {
        host.provide(facts.getId(), id, implementation);
    }
}
