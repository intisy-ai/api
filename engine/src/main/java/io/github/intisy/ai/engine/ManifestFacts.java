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

    public ManifestFacts(String id, int api, List<String> capabilities, List<String> permissions, Object payload) {
        this.id = id;
        this.api = api;
        this.capabilities = copy(capabilities);
        this.permissions = copy(permissions);
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public int getApi() {
        return api;
    }

    public List<String> getCapabilities() {
        return Collections.unmodifiableList(capabilities);
    }

    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public Object getPayload() {
        return payload;
    }

    private static List<String> copy(List<String> values) {
        return values == null ? new ArrayList<String>() : new ArrayList<String>(values);
    }
}
