package io.github.intisy.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class PluginLedgerTest {

    @Test
    void returnsNullForAPluginItNeverSaw() {
        assertNull(new PluginLedger().entry("nobody"));
    }

    @Test
    void opensAnEntryAtActivatingWithWhatTheManifestDeclared() {
        PluginLedger ledger = new PluginLedger();
        ledger.recordDeclared("config-ledger", Arrays.asList("screens", "settings"), Arrays.asList("fs"));
        LedgerEntry entry = ledger.entry("config-ledger");
        assertEquals(PluginStatus.ACTIVATING, entry.getStatus());
        assertEquals(Arrays.asList("screens", "settings"), entry.getCapabilitiesDeclared());
        assertEquals(Arrays.asList("fs"), entry.getPermissions());
        assertTrue(entry.getCapabilitiesProvided().isEmpty());
    }

    @Test
    void recordsEachRelationshipOnce() {
        PluginLedger ledger = new PluginLedger();
        ledger.recordDeclared("a", Collections.<String>emptyList(), Collections.<String>emptyList());
        ledger.recordCapabilityProvided("a", "screens");
        ledger.recordCapabilityProvided("a", "screens");
        ledger.recordServiceProvided("a", "a:thing");
        ledger.recordServiceConsumed("a", "b:thing");
        ledger.recordTopic("a", "notification");
        LedgerEntry entry = ledger.entry("a");
        assertEquals(Arrays.asList("screens"), entry.getCapabilitiesProvided());
        assertEquals(Arrays.asList("a:thing"), entry.getServicesProvided());
        assertEquals(Arrays.asList("b:thing"), entry.getServicesConsumed());
        assertEquals(Arrays.asList("notification"), entry.getTopics());
    }

    @Test
    void redeclaringResetsEveryRelationshipBecauseEachBelongsToOneActivation() {
        PluginLedger ledger = new PluginLedger();
        ledger.recordDeclared("a", Arrays.asList("screens"), Collections.<String>emptyList());
        ledger.recordCapabilityProvided("a", "screens");
        ledger.recordTopic("a", "notification");
        ledger.recordStatus("a", PluginStatus.BROKEN, "it threw", "fix it");
        ledger.recordDeclared("a", Arrays.asList("settings"), Collections.<String>emptyList());
        LedgerEntry entry = ledger.entry("a");
        assertEquals(PluginStatus.ACTIVATING, entry.getStatus());
        assertEquals(Arrays.asList("settings"), entry.getCapabilitiesDeclared());
        assertTrue(entry.getCapabilitiesProvided().isEmpty());
        assertTrue(entry.getTopics().isEmpty());
        assertNull(entry.getErrorDetail());
    }

    @Test
    void recordsAndThenClearsAnError() {
        PluginLedger ledger = new PluginLedger();
        ledger.recordStatus("a", PluginStatus.BROKEN, "it threw", "fix it");
        assertEquals("it threw", ledger.entry("a").getErrorDetail());
        assertEquals("fix it", ledger.entry("a").getErrorFix());
        ledger.recordStatus("a", PluginStatus.ACTIVE, null, null);
        assertEquals(PluginStatus.ACTIVE, ledger.entry("a").getStatus());
        assertNull(ledger.entry("a").getErrorDetail());
    }

    @Test
    void handsOutCopiesACallerMayKeep() {
        PluginLedger ledger = new PluginLedger();
        ledger.recordCapabilityProvided("a", "screens");
        LedgerEntry first = ledger.entry("a");
        ledger.recordCapabilityProvided("a", "settings");
        assertEquals(Arrays.asList("screens"), first.getCapabilitiesProvided());
        assertEquals(2, ledger.entry("a").getCapabilitiesProvided().size());
        assertEquals(1, ledger.entries().size());
    }
}
