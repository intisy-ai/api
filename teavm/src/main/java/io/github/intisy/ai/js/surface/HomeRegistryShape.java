package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/**
 * Every app home the host knows about, as reached only through {@link ContextSurface}.
 *
 * @implNote An object with a method rather than a plain array, for the same reason
 * {@link EventBusShape} is one: a runtime is handed over once per plugin, and a home can appear, or
 * be installed, long after that.
 */
@TsInterface
public interface HomeRegistryShape {
    /**
     * Every registered home, whether or not each exists on disk.
     *
     * @return the known homes, or empty when the host registered none
     */
    List<HomeDescriptorShape> all();
}
