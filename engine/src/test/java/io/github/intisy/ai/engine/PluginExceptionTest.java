package io.github.intisy.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PluginExceptionTest {

    @Test
    void carriesTheAttributionTheHostPrints() {
        PluginException failure = new PluginException("config-ledger", "plugin.json id: required field is missing", "add an id");
        assertEquals("config-ledger", failure.getPluginId());
        assertEquals("plugin.json id: required field is missing", failure.getDetail());
        assertEquals("add an id", failure.getFix());
        assertEquals("[config-ledger] plugin.json id: required field is missing\n  fix: add an id", failure.getMessage());
    }
}
