package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsModule;
import io.github.intisy.ai.tsemit.TsNullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * The engine's JavaScript module surface, typed for a TypeScript consumer.
 *
 * @implNote Declares the shape {@code EngineJs} actually exports; it is never implemented, only
 * emitted, and {@link TsModule} renders its members as free functions rather than an interface a
 * caller would otherwise have to cast a module namespace through. pluginError returns
 * {@link PluginErrorShape} rather than a raw {@code Error}, because that is the real shape
 * {@code JsErrors.mint} attaches and a caller can read it with no cast. setDiagnosticSink's sink is a
 * {@link Consumer}, which this processor maps to a function type, so no raw escape remains anywhere
 * in this surface.
 */
@TsModule
public interface EngineSurface {

    ActivationPlanShape activationOrder(List<Object> manifests);

    Object assertManifest(Object manifest);

    Object assertManifest(Object manifest, List<String> wellKnownServices);

    HostSurface createPluginHost(PluginHostOptionsShape options);

    PluginErrorShape pluginError(String pluginId, String detail, String fix);

    boolean isPluginError(Object value);

    void setStrict(@TsNullable Boolean enabled);

    void setDiagnosticSink(@TsNullable Consumer<String> sink);
}
