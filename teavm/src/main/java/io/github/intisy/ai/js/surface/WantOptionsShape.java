package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/** How long a plugin is willing to wait for a service that has not arrived yet. */
@TsInterface(data = true)
public interface WantOptionsShape {
    /**
     * How long to wait before giving up.
     *
     * @return the timeout in milliseconds, or absent to use the registry's default deadline
     */
    @TsOptional
    Integer timeoutMs();
}
