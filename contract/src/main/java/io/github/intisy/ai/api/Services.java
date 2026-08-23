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
    /** What is registered under this id right now, or absent when nothing is. */
    @TsNullable
    <T> T get(ServiceType<T> type);

    /** Waits for the id to be registered, for a host default the implementation chooses. */
    <T> CompletionStage<T> want(ServiceType<T> type);

    /** Waits for the id to be registered, giving up after the stated time. */
    <T> CompletionStage<T> want(ServiceType<T> type, WantOptions options);

    /**
     * Reports every registration and removal of this id until the returned function is called. The
     * listener is handed the service and either "register" or "unregister"; on the latter the
     * service is absent.
     *
     * @implNote Distinct from {@code want}, which answers once: a service can be replaced while a
     * consumer is still holding the old one, and only a watcher sees that.
     */
    <T> Runnable watch(ServiceType<T> type, BiConsumer<T, String> listener);

    /** Offers an implementation under this id, until the returned function is called. */
    <T> Runnable register(ServiceType<T> type, T implementation);

    /** Every service id registered right now. */
    List<String> ids();
}
