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

    /**
     * Every entry, as copies a caller may keep.
     *
     * @return every plugin's entry, or empty when the host has seen no plugin yet
     */
    public List<LedgerEntry> entries() {
        List<LedgerEntry> copies = new ArrayList<LedgerEntry>();
        for (LedgerEntry entry : entries.values()) {
            copies.add(entry.copy());
        }
        return copies;
    }

    /**
     * One plugin's entry as a copy, or null when the host never saw it.
     *
     * @param pluginId the plugin to look up
     * @return a copy of the plugin's entry, or null when the host never saw it
     */
    public LedgerEntry entry(String pluginId) {
        LedgerEntry found = entries.get(pluginId);
        return found == null ? null : found.copy();
    }

    /**
     * Opens an entry at status activating.
     *
     * @param pluginId the plugin whose entry to open
     * @param capabilities the capability ids the plugin's manifest declares, or null for none
     * @param permissions the permission ids the plugin's manifest declares, or null for none
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

    /**
     * @param pluginId the plugin that provided the capability
     * @param capabilityId the capability id it provided
     */
    public void recordCapabilityProvided(String pluginId, String capabilityId) {
        ensure(pluginId).addCapabilityProvided(capabilityId);
    }

    /**
     * @param pluginId the plugin that registered the service
     * @param serviceId the service id it registered
     */
    public void recordServiceProvided(String pluginId, String serviceId) {
        ensure(pluginId).addServiceProvided(serviceId);
    }

    /**
     * @param pluginId the plugin that consumed the service
     * @param serviceId the service id it consumed
     */
    public void recordServiceConsumed(String pluginId, String serviceId) {
        ensure(pluginId).addServiceConsumed(serviceId);
    }

    /**
     * @param pluginId the plugin that subscribed
     * @param topic the event topic it subscribed to
     */
    public void recordTopic(String pluginId, String topic) {
        ensure(pluginId).addTopic(topic);
    }

    /**
     * Moves a plugin to a status, with the error that put it there, or null to clear it.
     *
     * @param pluginId the plugin whose status changed
     * @param status the status to move it to
     * @param detail what went wrong, or null when the new status is not {@link PluginStatus#BROKEN}
     * @param fix what to do about it, or null when the new status is not {@link PluginStatus#BROKEN}
     */
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
