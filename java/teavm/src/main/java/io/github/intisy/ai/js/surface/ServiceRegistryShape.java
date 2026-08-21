package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

/**
 * One plugin's view of the live service registry, as reached through {@link ContextSurface}.
 *
 * @implNote {@code want} is two overloads rather than one method with an optional parameter,
 * mirroring how {@link EngineSurface#assertManifest} already renders an optional trailing argument
 * for this same processor. {@code watch}'s listener is a {@link BiConsumer} of the service and a
 * {@link ServiceEvent}, matching {@code ServiceListener<T>} in {@code host.ts}. {@code get} is
 * {@link TsNullable} because a missing service is an absent map entry, matching {@code host.ts}'s
 * {@code get(id): unknown}.
 */
@TsInterface
public interface ServiceRegistryShape {
    @TsNullable
    Object get(String id);

    CompletionStage<Object> want(String id);

    CompletionStage<Object> want(String id, WantOptionsShape options);

    Runnable watch(String id, BiConsumer<Object, ServiceEvent> listener);

    Runnable register(String id, Object service);

    List<String> ids();
}
