package io.github.intisy.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ActivationTest {

    private static ActivationPlan plan(String[] ids, Map<String, List<String>> provides, Map<String, List<String>> consumes) {
        return Activation.order(Arrays.asList(ids), provides, consumes);
    }

    private static Map<String, List<String>> edges(String id, String... services) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put(id, Arrays.asList(services));
        return map;
    }

    @Test
    void putsAProviderBeforeItsConsumer() {
        Map<String, List<String>> provides = edges("store", "store:accounts");
        Map<String, List<String>> consumes = edges("reader", "store:accounts");
        ActivationPlan result = plan(new String[] {"reader", "store"}, provides, consumes);
        assertEquals(Arrays.asList("store", "reader"), result.getOrder());
        assertTrue(result.getCycles().isEmpty());
    }

    @Test
    void activatesAConsumerWhoseServiceNobodyProvides() {
        ActivationPlan result = plan(new String[] {"lonely"}, new HashMap<String, List<String>>(), edges("lonely", "nobody:here"));
        assertEquals(Arrays.asList("lonely"), result.getOrder());
        assertTrue(result.getCycles().isEmpty());
    }

    @Test
    void reportsACycleAndLeavesItsMembersOutOfTheOrder() {
        Map<String, List<String>> provides = new HashMap<String, List<String>>();
        provides.put("left", Arrays.asList("left:thing"));
        provides.put("right", Arrays.asList("right:thing"));
        Map<String, List<String>> consumes = new HashMap<String, List<String>>();
        consumes.put("left", Arrays.asList("right:thing"));
        consumes.put("right", Arrays.asList("left:thing"));
        ActivationPlan result = plan(new String[] {"left", "right"}, provides, consumes);
        assertTrue(result.getOrder().isEmpty());
        assertEquals(1, result.getCycles().size());
        List<String> cycle = new ArrayList<String>(result.getCycles().get(0));
        assertEquals(2, cycle.size());
        assertTrue(cycle.contains("left"));
        assertTrue(cycle.contains("right"));
    }

    @Test
    void aPluginProvidingAndConsumingOneServiceDoesNotDependOnItself() {
        Map<String, List<String>> both = edges("solo", "solo:thing");
        ActivationPlan result = plan(new String[] {"solo"}, both, both);
        assertEquals(Arrays.asList("solo"), result.getOrder());
        assertTrue(result.getCycles().isEmpty());
    }

    @Test
    void returnsAnEmptyPlanForNoPlugins() {
        ActivationPlan result = plan(new String[] {}, new HashMap<String, List<String>>(), new HashMap<String, List<String>>());
        assertTrue(result.getOrder().isEmpty());
        assertTrue(result.getCycles().isEmpty());
    }
}
