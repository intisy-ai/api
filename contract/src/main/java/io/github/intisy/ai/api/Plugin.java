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
    /** Supplies the implementation behind every capability the manifest declares. */
    @TsMaybeAsync
    void activate(PluginContext context);

    /** Releases whatever `activate` took: timers, watchers, child processes. */
    @TsMaybeAsync
    void deactivate();

    /** Runs once after the first deploy. */
    @TsOptional
    @TsMaybeAsync
    void install(PluginContext context);

    /** Runs on demand from a host, to put a broken installation right. */
    @TsOptional
    @TsMaybeAsync
    void repair(PluginContext context);
}
