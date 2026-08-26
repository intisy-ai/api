package io.github.intisy.ai.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Everything the host observed about one plugin, which is every relationship it has. */
public final class LedgerEntry {

    private final String pluginId;
    private PluginStatus status = PluginStatus.ACTIVATING;
    private List<String> capabilitiesDeclared = new ArrayList<String>();
    private List<String> capabilitiesProvided = new ArrayList<String>();
    private List<String> servicesProvided = new ArrayList<String>();
    private List<String> servicesConsumed = new ArrayList<String>();
    private List<String> topics = new ArrayList<String>();
    private List<String> permissions = new ArrayList<String>();
    private String errorDetail;
    private String errorFix;

    LedgerEntry(String pluginId) {
        this.pluginId = pluginId;
    }

    /** @return the id of the plugin this entry is about */
    public String getPluginId() {
        return pluginId;
    }

    /** @return the plugin's current status */
    public PluginStatus getStatus() {
        return status;
    }

    /** @return the capability ids the plugin's manifest declares, or empty when it declares none */
    public List<String> getCapabilitiesDeclared() {
        return Collections.unmodifiableList(capabilitiesDeclared);
    }

    /** @return the capability ids the plugin has actually provided so far, or empty when it has provided none */
    public List<String> getCapabilitiesProvided() {
        return Collections.unmodifiableList(capabilitiesProvided);
    }

    /** @return the service ids the plugin has registered, or empty when it has registered none */
    public List<String> getServicesProvided() {
        return Collections.unmodifiableList(servicesProvided);
    }

    /** @return the service ids the plugin has consumed, or empty when it has consumed none */
    public List<String> getServicesConsumed() {
        return Collections.unmodifiableList(servicesConsumed);
    }

    /** @return the event topics the plugin has subscribed to, or empty when it has subscribed to none */
    public List<String> getTopics() {
        return Collections.unmodifiableList(topics);
    }

    /** @return the permissions the plugin's manifest declares, or empty when it declares none */
    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    /**
     * What went wrong, when the plugin is broken.
     *
     * @return the failure detail, or null when the plugin is not broken
     */
    public String getErrorDetail() {
        return errorDetail;
    }

    /**
     * How to fix it, when the plugin is broken.
     *
     * @return the fix instruction, or null when the plugin is not broken
     */
    public String getErrorFix() {
        return errorFix;
    }

    void setStatus(PluginStatus value) {
        this.status = value;
    }

    void setError(String detail, String fix) {
        this.errorDetail = detail;
        this.errorFix = fix;
    }

    void reset() {
        capabilitiesDeclared = new ArrayList<String>();
        capabilitiesProvided = new ArrayList<String>();
        servicesProvided = new ArrayList<String>();
        servicesConsumed = new ArrayList<String>();
        topics = new ArrayList<String>();
        permissions = new ArrayList<String>();
        errorDetail = null;
        errorFix = null;
        status = PluginStatus.ACTIVATING;
    }

    void addCapabilityDeclared(String id) {
        add(capabilitiesDeclared, id);
    }

    void addCapabilityProvided(String id) {
        add(capabilitiesProvided, id);
    }

    void addServiceProvided(String id) {
        add(servicesProvided, id);
    }

    void addServiceConsumed(String id) {
        add(servicesConsumed, id);
    }

    void addTopic(String id) {
        add(topics, id);
    }

    void addPermission(String id) {
        add(permissions, id);
    }

    LedgerEntry copy() {
        LedgerEntry clone = new LedgerEntry(pluginId);
        clone.status = status;
        clone.capabilitiesDeclared = new ArrayList<String>(capabilitiesDeclared);
        clone.capabilitiesProvided = new ArrayList<String>(capabilitiesProvided);
        clone.servicesProvided = new ArrayList<String>(servicesProvided);
        clone.servicesConsumed = new ArrayList<String>(servicesConsumed);
        clone.topics = new ArrayList<String>(topics);
        clone.permissions = new ArrayList<String>(permissions);
        clone.errorDetail = errorDetail;
        clone.errorFix = errorFix;
        return clone;
    }

    private static void add(List<String> list, String value) {
        if (!list.contains(value)) {
            list.add(value);
        }
    }
}
