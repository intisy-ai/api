package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** How long a plugin is willing to wait for a service that is not registered yet. */
@TsInterface(data = true)
public interface WantOptions {
    /** Milliseconds to wait before giving up. Absent takes the host's own default. */
    @TsOptional
    Long timeoutMs();
}
