package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

/**
 * How a plugin reaches another plugin's API, and offers its own.
 *
 * @implNote A typed handle, never an import: the plugin answering is whichever one registered the
 * id, so no plugin ever names another. This is what makes a plugin terminal, and it is the reason
 * a host needs no import of anything it drives.
 */
@TsInterface
public interface Services {
    /**
     * What is registered under this id right now, or absent when nothing is.
     *
     * @param <T> the service's API type
     * @param type the service key, from {@link PluginContext#service(String)}
     * @return the current registration, or {@code null} when none is registered
     */
    @TsNullable
    <T> T get(ServiceType<T> type);

    /**
     * Waits for the id to be registered, for a host default the implementation chooses.
     *
     * @param <T> the service's API type
     * @param type the service key, from {@link PluginContext#service(String)}
     * @return a stage that completes with the registration once one appears
     */
    <T> CompletionStage<T> want(ServiceType<T> type);

    /**
     * Waits for the id to be registered, giving up after the stated time.
     *
     * @param <T> the service's API type
     * @param type the service key, from {@link PluginContext#service(String)}
     * @param options how long to wait before giving up
     * @return a stage that completes with the registration, or fails once the wait times out
     */
    <T> CompletionStage<T> want(ServiceType<T> type, WantOptions options);

    /**
     * Reports every registration and removal of this id until the returned function is called. The
     * listener is handed the service and either "register" or "unregister"; on the latter the
     * service is absent.
     *
     * @implNote Distinct from {@code want}, which answers once: a service can be replaced while a
     * consumer is still holding the old one, and only a watcher sees that.
     * @param <T> the service's API type
     * @param type the service key, from {@link PluginContext#service(String)}
     * @param listener called with the current registration (or absent) and the event kind
     * @return a function that stops the watch when called
     */
    <T> Runnable watch(ServiceType<T> type, BiConsumer<T, String> listener);

    /**
     * Offers an implementation under this id, until the returned function is called.
     *
     * @param <T> the service's API type
     * @param type the service key, from {@link PluginContext#service(String)}
     * @param implementation this plugin's implementation of the service
     * @return a function that withdraws the registration when called
     */
    <T> Runnable register(ServiceType<T> type, T implementation);

    /**
     * Every service id registered right now.
     *
     * @return the registered service ids
     */
    List<String> ids();
}
