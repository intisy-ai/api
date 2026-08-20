package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * One plugin's view of the live service registry, as reached through {@link ContextSurface}.
 *
 * @implNote {@code want} is two overloads rather than one method with an optional parameter,
 * mirroring how {@link EngineSurface#assertManifest} already renders an optional trailing argument
 * for this same processor.
 */
@TsInterface
public interface ServiceRegistryShape {
    Object get(String id);

    CompletionStage<Object> want(String id);

    CompletionStage<Object> want(String id, WantOptionsShape options);

    Runnable watch(String id, Object listener);

    Runnable register(String id, Object service);

    List<String> ids();
}
