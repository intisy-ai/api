package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsMaybeAsync;
import io.github.intisy.ai.tsemit.TsOptional;

/**
 * What a plugin's entry module exports.
 *
 * @implNote Each hook is awaited individually by the host under its own timeout, so a plugin that
 * throws or hangs is quarantined on its own rather than taking a host or a sibling with it.
 */
@TsInterface
public interface Plugin {
    @TsMaybeAsync
    void activate(PluginContext context);

    @TsMaybeAsync
    void deactivate();

    @TsOptional
    @TsMaybeAsync
    void install(PluginContext context);

    @TsOptional
    @TsMaybeAsync
    void repair(PluginContext context);
}
