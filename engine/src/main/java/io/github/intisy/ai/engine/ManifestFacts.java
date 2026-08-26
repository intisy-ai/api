package io.github.intisy.ai.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The four things a host decides with, plus the manifest it decided from.
 *
 * @implNote The payload travels through untouched and is handed back by identity, which is what lets
 * this module stay free of a manifest TYPE: a JavaScript host passes the object it parsed and gets
 * the same object on the session, and a JVM host passes its own manifest class. Adding the typed
 * manifest here would give this module a dependency and give neither caller anything.
 */
public final class ManifestFacts {

    private final String id;
    private final int api;
    private final List<String> capabilities;
    private final List<String> permissions;
    private final Object payload;

    /**
     * @param id the plugin's id
     * @param api the api version the plugin declares it needs
     * @param capabilities the capability ids the plugin declares, copied so later mutation of the argument has no effect
     * @param permissions the permission ids the plugin declares, copied so later mutation of the argument has no effect
     * @param payload the manifest object these facts were extracted from, kept by identity for {@link #getPayload}
     */
    public ManifestFacts(String id, int api, List<String> capabilities, List<String> permissions, Object payload) {
        this.id = id;
        this.api = api;
        this.capabilities = copy(capabilities);
        this.permissions = copy(permissions);
        this.payload = payload;
    }

    /** @return the plugin's id */
    public String getId() {
        return id;
    }

    /** @return the api version the plugin declares it needs */
    public int getApi() {
        return api;
    }

    /** @return the capability ids the plugin declares, or empty when it declares none */
    public List<String> getCapabilities() {
        return Collections.unmodifiableList(capabilities);
    }

    /** @return the permission ids the plugin declares, or empty when it declares none */
    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    /** @return the manifest object this instance was built from, handed back by identity */
    public Object getPayload() {
        return payload;
    }

    private static List<String> copy(List<String> values) {
        return values == null ? new ArrayList<String>() : new ArrayList<String>(values);
    }
}
