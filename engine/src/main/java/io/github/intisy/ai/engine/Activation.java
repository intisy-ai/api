package io.github.intisy.ai.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sorts plugins so a provider activates before its consumers.
 *
 * @implNote Edges come from services only. A capability is consumed by the HOST after every
 * activation finishes, while a service is the plugin-to-plugin channel during activation, so a
 * capability implies no order and adding one would build a mechanism nothing needs.
 */
public final class Activation {

    private Activation() {
    }

    /**
     * Computes an activation order for a set of plugin ids from what each provides and consumes.
     *
     * @param ids the plugin ids to order
     * @param provides service ids each plugin id provides, keyed by plugin id
     * @param consumes service ids each plugin id consumes, keyed by plugin id
     * @return the resulting order, plus the dependency cycles that were left out of it
     * @implNote A consumed service nobody provides draws no edge: the consumer activates anyway and
     * resolves later or times out, because at ecosystem scale providers appear and disappear and a
     * hard order fails the moment one is disabled.
     */
    public static ActivationPlan order(List<String> ids, Map<String, List<String>> provides, Map<String, List<String>> consumes) {
        Map<String, List<String>> providersOf = new HashMap<String, List<String>>();
        for (String id : ids) {
            for (String serviceId : listOf(provides, id)) {
                List<String> owners = providersOf.get(serviceId);
                if (owners == null) {
                    owners = new ArrayList<String>();
                    providersOf.put(serviceId, owners);
                }
                owners.add(id);
            }
        }

        Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>();
        for (String id : ids) {
            Set<String> needed = new LinkedHashSet<String>();
            for (String serviceId : listOf(consumes, id)) {
                for (String provider : listOf(providersOf, serviceId)) {
                    if (!provider.equals(id)) {
                        needed.add(provider);
                    }
                }
            }
            dependencies.put(id, needed);
        }

        List<List<String>> cycles = findCycles(ids, dependencies);
        Set<String> settled = new HashSet<String>();
        for (List<String> cycle : cycles) {
            settled.addAll(cycle);
        }

        List<String> order = new ArrayList<String>();
        boolean progressed = true;
        while (progressed) {
            progressed = false;
            for (String id : ids) {
                if (settled.contains(id)) {
                    continue;
                }
                boolean pending = false;
                for (String dependency : dependencies.get(id)) {
                    if (!settled.contains(dependency)) {
                        pending = true;
                        break;
                    }
                }
                if (pending) {
                    continue;
                }
                order.add(id);
                settled.add(id);
                progressed = true;
            }
        }

        return new ActivationPlan(order, cycles);
    }

    private static List<String> listOf(Map<String, List<String>> source, String key) {
        List<String> found = source == null ? null : source.get(key);
        return found == null ? new ArrayList<String>() : found;
    }

    /**
     * @implNote Tarjan's strongly-connected components. A component of one is not a cycle, and each
     * reported component is reversed so the message reads as a chain rather than backwards.
     */
    private static List<List<String>> findCycles(List<String> ids, Map<String, Set<String>> dependencies) {
        Tarjan state = new Tarjan(dependencies);
        for (String id : ids) {
            if (!state.index.containsKey(id)) {
                state.visit(id);
            }
        }
        return state.cycles;
    }

    private static final class Tarjan {
        private final Map<String, Set<String>> dependencies;
        private final Map<String, Integer> index = new HashMap<String, Integer>();
        private final Map<String, Integer> lowLink = new HashMap<String, Integer>();
        private final Set<String> onStack = new HashSet<String>();
        private final Deque<String> stack = new ArrayDeque<String>();
        private final List<List<String>> cycles = new ArrayList<List<String>>();
        private int counter = 0;

        Tarjan(Map<String, Set<String>> dependencies) {
            this.dependencies = dependencies;
        }

        void visit(String id) {
            index.put(id, Integer.valueOf(counter));
            lowLink.put(id, Integer.valueOf(counter));
            counter++;
            stack.push(id);
            onStack.add(id);

            Set<String> needed = dependencies.get(id);
            if (needed != null) {
                for (String dependency : needed) {
                    if (!index.containsKey(dependency)) {
                        visit(dependency);
                        lowLink.put(id, Integer.valueOf(Math.min(lowLink.get(id).intValue(), lowLink.get(dependency).intValue())));
                    } else if (onStack.contains(dependency)) {
                        lowLink.put(id, Integer.valueOf(Math.min(lowLink.get(id).intValue(), index.get(dependency).intValue())));
                    }
                }
            }

            if (!lowLink.get(id).equals(index.get(id))) {
                return;
            }
            List<String> component = new ArrayList<String>();
            String member;
            do {
                member = stack.pop();
                onStack.remove(member);
                component.add(member);
            } while (!member.equals(id));
            if (component.size() > 1) {
                java.util.Collections.reverse(component);
                cycles.add(component);
            }
        }
    }
}
