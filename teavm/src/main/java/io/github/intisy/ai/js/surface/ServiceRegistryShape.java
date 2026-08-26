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
    /**
     * The service registered under an id right now, or undefined.
     *
     * @param id the service id to look up
     * @return the registered service, or undefined when none is registered
     */
    @TsNullable
    Object get(String id);

    /**
     * Waits for a service to arrive, under the registry's default deadline.
     *
     * @param id the service id to wait for
     * @return a promise that resolves with the service once registered, or rejects on timeout
     */
    CompletionStage<Object> want(String id);

    /**
     * Waits for a service to arrive, under the deadline given.
     *
     * @param id the service id to wait for
     * @param options the deadline to wait under
     * @return a promise that resolves with the service once registered, or rejects on timeout
     */
    CompletionStage<Object> want(String id, WantOptionsShape options);

    /**
     * Watches one id for registration and unregistration. Call the result to stop.
     *
     * @param id the service id to watch
     * @param listener called with the service and the event on each registration or unregistration
     * @return a disposer that cancels the watch when called
     */
    Runnable watch(String id, BiConsumer<Object, ServiceEvent> listener);

    /**
     * Registers a service. Call the result to withdraw it.
     *
     * @param id the service id to register under
     * @param service the value other plugins reach through {@code get}, {@code want} or {@code watch}
     * @return a disposer that withdraws the registration when called
     */
    Runnable register(String id, Object service);

    /**
     * Every id registered right now.
     *
     * @return the registered service ids, or empty when none is registered
     */
    List<String> ids();
}
