package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/** The shape {@link EngineSurface#activationOrder} returns: providers before consumers, and the cycles that could not be ordered. */
@TsInterface(data = true)
public interface ActivationPlanShape {
    /**
     * Plugin ids in the order they may be activated.
     *
     * @return the resolvable plugin ids in order, excluding any plugin a cycle left out
     */
    List<String> order();

    /**
     * One entry per dependency cycle, naming its members.
     *
     * @return the dependency cycles found, or empty when the plugins have no such cycle
     */
    List<List<String>> cycles();
}
