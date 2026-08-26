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

    /** @return this plugin's manifest facts */
    public ManifestFacts getFacts() {
        return facts;
    }

    /**
     * Scoped so a registration outside this plugin's namespace is refused, and fenced once it stops.
     *
     * @return this plugin's service registry
     */
    public ServiceHub.Registry getServices() {
        return services;
    }

    /**
     * Recording, so every subscription reaches the ledger, and fenced once the plugin stops.
     *
     * @return this plugin's event bus
     */
    public EventBus getEvents() {
        return events;
    }

    /**
     * @param id the capability id to provide
     * @param implementation this plugin's implementation of it
     */
    public void provide(String id, Object implementation) {
        host.provide(facts.getId(), id, implementation);
    }
}
