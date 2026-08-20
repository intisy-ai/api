package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsProperty;
import io.github.intisy.ai.tsemit.TsRaw;
import java.util.List;

/**
 * The engine's JavaScript module surface, typed for a TypeScript consumer.
 *
 * @implNote Declares the shape {@code EngineJs} actually exports; it is never implemented, only
 * emitted. setStrict and setDiagnosticSink are modelled as properties holding a raw function type
 * because the processor has no annotation for a single nullable parameter, only a nullable return
 * (see {@link io.github.intisy.ai.tsemit.TsNullable}); pluginError's return is raw because Error is a
 * TypeScript ambient global this processor's type vocabulary does not model.
 */
@TsInterface
public interface EngineSurface {

    ActivationPlanShape activationOrder(List<Object> manifests);

    Object assertManifest(Object manifest);

    Object assertManifest(Object manifest, List<String> wellKnownServices);

    @TsRaw("Error")
    Object pluginError(String pluginId, String detail, String fix);

    boolean isPluginError(Object value);

    @TsProperty
    @TsRaw("(enabled: boolean | null) => void")
    Object setStrict();

    @TsProperty
    @TsRaw("(sink: ((message: string) => void) | null) => void")
    Object setDiagnosticSink();
}
