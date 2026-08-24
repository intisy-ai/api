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
 * for this same processor. {@code watch}'s listener is a {@link BiConsumer} of the service and the
 * event, so one subscription covers registration and withdrawal. {@code get} is {@code | undefined}
 * because a missing service is an absent map entry rather than an answer.
 */
@TsInterface
public interface ServiceRegistryShape {
    /** The service registered under an id right now, or undefined. */
    @TsNullable
    Object get(String id);

    /** Waits for a service to arrive, under the registry's default deadline. */
    CompletionStage<Object> want(String id);

    /** Waits for a service to arrive, under the deadline given. */
    CompletionStage<Object> want(String id, WantOptionsShape options);

    /** Watches one id for registration and unregistration. Call the result to stop. */
    Runnable watch(String id, BiConsumer<Object, ServiceEvent> listener);

    /** Registers a service. Call the result to withdraw it. */
    Runnable register(String id, Object service);

    /** Every id registered right now. */
    List<String> ids();
}
