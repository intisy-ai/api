package io.github.intisy.ai.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The record of every relationship that passed through a plugin context.
 *
 * @implNote Kept by the host rather than assembled on demand, because a relationship is only
 * observable at the moment it is made. A developer section, a doctor and a quarantine view are all
 * renderings of this one ledger.
 */
public final class PluginLedger {

    private final Map<String, LedgerEntry> entries = new LinkedHashMap<String, LedgerEntry>();

    /** Every entry, as copies a caller may keep. */
    public List<LedgerEntry> entries() {
        List<LedgerEntry> copies = new ArrayList<LedgerEntry>();
        for (LedgerEntry entry : entries.values()) {
            copies.add(entry.copy());
        }
        return copies;
    }

    /** One plugin's entry as a copy, or null when the host never saw it. */
    public LedgerEntry entry(String pluginId) {
        LedgerEntry found = entries.get(pluginId);
        return found == null ? null : found.copy();
    }

    /**
     * Opens an entry at status activating.
     *
     * @implNote Every relationship an entry holds belongs to ONE activation, so this resets them: a
     * plugin that stops and activates again is described by what it does this time, not by the union
     * of every cycle it has ever run.
     */
    public void recordDeclared(String pluginId, List<String> capabilities, List<String> permissions) {
        LedgerEntry entry = ensure(pluginId);
        entry.reset();
        if (capabilities != null) {
            for (String id : capabilities) {
                entry.addCapabilityDeclared(id);
            }
        }
        if (permissions != null) {
            for (String permission : permissions) {
                entry.addPermission(permission);
            }
        }
    }

    public void recordCapabilityProvided(String pluginId, String capabilityId) {
        ensure(pluginId).addCapabilityProvided(capabilityId);
    }

    public void recordServiceProvided(String pluginId, String serviceId) {
        ensure(pluginId).addServiceProvided(serviceId);
    }

    public void recordServiceConsumed(String pluginId, String serviceId) {
        ensure(pluginId).addServiceConsumed(serviceId);
    }

    public void recordTopic(String pluginId, String topic) {
        ensure(pluginId).addTopic(topic);
    }

    /** Moves a plugin to a status, with the error that put it there, or null to clear it. */
    public void recordStatus(String pluginId, PluginStatus status, String detail, String fix) {
        LedgerEntry entry = ensure(pluginId);
        entry.setStatus(status);
        entry.setError(detail, fix);
    }

    private LedgerEntry ensure(String pluginId) {
        LedgerEntry existing = entries.get(pluginId);
        if (existing != null) {
            return existing;
        }
        LedgerEntry fresh = new LedgerEntry(pluginId);
        entries.put(pluginId, fresh);
        return fresh;
    }
}
