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
 * caller would otherwise have to cast a module namespace through. manifestSchema returns
 * {@code Object} because a JSON Schema is a recursive blob its callers stringify rather than read
 * field by field, and declaring that recursion would buy no caller anything. pluginError returns
 * {@link PluginErrorShape} rather than a raw {@code Error}, because that is the real shape
 * {@code JsErrors.mint} attaches and a caller can read it with no cast. setDiagnosticSink's sink is a
 * {@link Consumer}, which this processor maps to a function type, so no raw escape remains anywhere
 * in this surface.
 */
@TsModule
public interface EngineSurface {

    /** Orders manifests so a service provider activates before its consumer, naming any cycle. */
    ActivationPlanShape activationOrder(List<Object> manifests);

    /**
     * Validates a manifest against the schema, throwing the first problem as a plugin error.
     *
     * @implNote A caller that names no vocabulary is held to an empty one, so a bare well-known id
     * counts as squatting: its caller is a host, which knows its own vocabulary.
     */
    Object assertManifest(Object manifest);

    /** Validates a manifest, treating the given ids as the bare service ids any plugin may register. */
    Object assertManifest(Object manifest, List<String> wellKnownServices);

    /**
     * Every problem with a manifest, rather than the first.
     *
     * @implNote An absent vocabulary means unverifiable here, not empty, so the bare-service check
     * is skipped rather than answered wrongly. Its callers include a plugin author's own suite.
     */
    List<ValidationIssueShape> validateManifest(Object manifest);

    /** Every problem with a manifest, checked against the given well-known service ids. */
    List<ValidationIssueShape> validateManifest(Object manifest, List<String> wellKnownServices);

    /** The published JSON Schema of plugin.json, as a tree ready to stringify. */
    Object manifestSchema();

    /** Opens a host: the capability registry, the service hub, the event bus and the ledger. */
    HostSurface createPluginHost(PluginHostOptionsShape options);

    /** Mints a plugin error a caller can throw, marked so any bundle recognises it. */
    PluginErrorShape pluginError(String pluginId, String detail, String fix);

    /** Whether a caught value is a plugin error, recognised by its marker rather than its class. */
    boolean isPluginError(Object value);

    /** Turns quiet failures loud. Null means off, since a compiled bundle has no environment to read. */
    void setStrict(@TsNullable Boolean enabled);

    /** Installs where diagnostics are written, or null to stop writing them. */
    void setDiagnosticSink(@TsNullable Consumer<String> sink);
}
