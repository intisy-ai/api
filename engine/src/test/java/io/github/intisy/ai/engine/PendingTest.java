package io.github.intisy.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PendingTest {

    @Test
    void deliversAValueToAHandlerThatWasAlreadyWaiting() {
        Pending<String> pending = new Pending<String>();
        Settled<String> settled = Settled.of(pending);
        assertFalse(pending.isSettled());
        pending.resolve("here");
        assertEquals("here", settled.value());
        assertTrue(pending.isSettled());
    }

    @Test
    void deliversToAHandlerThatArrivesAfterTheOutcome() {
        Pending<String> pending = Pending.of("already");
        assertEquals("already", Settled.of(pending).value());
    }

    @Test
    void deliversAFailureAsAFailureAndNeverAsAValue() {
        Pending<String> pending = new Pending<String>();
        Settled<String> settled = Settled.of(pending);
        pending.reject(new PluginException("late", "gave up", "try again"));
        assertNull(settled.value());
        assertEquals("gave up", settled.failure().getDetail());
    }

    @Test
    void settlesOnce() {
        Pending<String> pending = new Pending<String>();
        Settled<String> settled = Settled.of(pending);
        pending.resolve("first");
        pending.resolve("second");
        pending.reject(new PluginException("late", "gave up", "try again"));
        assertEquals("first", settled.value());
        assertEquals(1, settled.settlements());
    }

    @Test
    void deliversToEveryHandler() {
        Pending<String> pending = new Pending<String>();
        Settled<String> one = Settled.of(pending);
        Settled<String> two = Settled.of(pending);
        pending.resolve("shared");
        assertEquals("shared", one.value());
        assertEquals("shared", two.value());
    }
}
