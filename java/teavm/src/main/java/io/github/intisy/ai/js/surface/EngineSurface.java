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
 * caller would otherwise have to cast a module namespace through. setDiagnosticSink's parameter is
 * raw because the processor has no mapping from a JSFunctor interface to a TypeScript function type;
 * setStrict needs no such escape, since a boxed Boolean plus {@link TsNullable} on the parameter is
 * enough. pluginError returns {@link PluginErrorShape} rather than a raw {@code Error}, because that
 * is the real shape {@code JsErrors.mint} attaches and a caller can read it with no cast.
 */
@TsModule
public interface EngineSurface {

    ActivationPlanShape activationOrder(List<Object> manifests);

    Object assertManifest(Object manifest);

    Object assertManifest(Object manifest, List<String> wellKnownServices);

    HostSurface createPluginHost(Object options);

    PluginErrorShape pluginError(String pluginId, String detail, String fix);

    boolean isPluginError(Object value);

    void setStrict(@TsNullable Boolean enabled);

    void setDiagnosticSink(@TsRaw("((message: string) => void) | null") EngineJs.Sink sink);
}
