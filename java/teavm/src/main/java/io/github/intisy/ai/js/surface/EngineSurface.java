package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.js.EngineJs;
import io.github.intisy.ai.tsemit.TsModule;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsRaw;
import java.util.List;

/**
 * The engine's JavaScript module surface, typed for a TypeScript consumer.
 *
 * @implNote Declares the shape {@code EngineJs} actually exports; it is never implemented, only
 * emitted, and {@link TsModule} renders its members as free functions rather than an interface a
 * caller would otherwise have to cast a module namespace through. pluginError's return is raw
 * because Error is a TypeScript ambient global this processor's type vocabulary does not model.
 * setDiagnosticSink's parameter is raw because the processor has no mapping from a JSFunctor
 * interface to a TypeScript function type; setStrict needs no such escape, since a boxed Boolean
 * plus {@link TsNullable} on the parameter is enough.
 */
@TsModule
public interface EngineSurface {

    ActivationPlanShape activationOrder(List<Object> manifests);

    Object assertManifest(Object manifest);

    Object assertManifest(Object manifest, List<String> wellKnownServices);

    @TsRaw("Error")
    Object pluginError(String pluginId, String detail, String fix);

    boolean isPluginError(Object value);

    void setStrict(@TsNullable Boolean enabled);

    void setDiagnosticSink(@TsRaw("((message: string) => void) | null") EngineJs.Sink sink);
}
