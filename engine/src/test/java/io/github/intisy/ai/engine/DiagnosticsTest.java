package io.github.intisy.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DiagnosticsTest {

    @AfterEach
    void reset() {
        Diagnostics.setSink(null);
        Diagnostics.setStrict(null);
    }

    @Test
    void sendsIgnoredUnknownsToTheHostSink() {
        final List<String> seen = new ArrayList<String>();
        Diagnostics.setSink(new Diagnostics.Sink() {
            @Override
            public void accept(String message) {
                seen.add(message);
            }
        });
        Diagnostics.ignoreUnknown("capability", "setings", "config-ledger");
        assertEquals(1, seen.size());
        assertEquals("ignored unknown capability \"setings\" from config-ledger", seen.get(0));
    }

    @Test
    void strictModeIsForcedOnAndOff() {
        Diagnostics.setStrict(Boolean.TRUE);
        assertTrue(Diagnostics.isStrict());
        Diagnostics.setStrict(Boolean.FALSE);
        assertFalse(Diagnostics.isStrict());
    }

    @Test
    void aSinkTakesPrecedenceOverStrictModeOutput() {
        final List<String> seen = new ArrayList<String>();
        Diagnostics.setStrict(Boolean.TRUE);
        Diagnostics.setSink(new Diagnostics.Sink() {
            @Override
            public void accept(String message) {
                seen.add(message);
            }
        });
        Diagnostics.report("something happened");
        assertEquals(1, seen.size());
    }
}
