package io.github.intisy.ai.engine;

import java.util.Collections;
import java.util.List;

/** The order plugins activate in, and the dependency cycles that kept some of them out of it. */
public final class ActivationPlan {

    private final List<String> order;
    private final List<List<String>> cycles;

    ActivationPlan(List<String> order, List<List<String>> cycles) {
        this.order = Collections.unmodifiableList(order);
        this.cycles = Collections.unmodifiableList(cycles);
    }

    /**
     * Plugin ids in activation order, providers before consumers.
     *
     * @return the resolvable plugin ids in order, excluding any plugin left out by a cycle
     */
    public List<String> getOrder() {
        return order;
    }

    /**
     * Each set of plugins that depend on one another, which is a load error naming the cycle.
     *
     * @return the dependency cycles found, or empty when the plugins have no such cycle
     */
    public List<List<String>> getCycles() {
        return cycles;
    }
}
