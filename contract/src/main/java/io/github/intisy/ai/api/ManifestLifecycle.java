package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** Which optional lifecycle hooks the entry module exports. */
@TsInterface(data = true)
public interface ManifestLifecycle {
    /** The entry exports `install(ctx)`, run once after the first deploy. */
    @TsOptional
    boolean install();

    /** The entry exports `repair(ctx)`, run on demand from a host. */
    @TsOptional
    boolean repair();
}
