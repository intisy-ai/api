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

    /**
     * Orders manifests so a service provider activates before its consumer, naming any cycle.
     *
     * @param manifests the parsed plugin.json trees to order
     * @return the resolvable order plus any dependency cycles found
     */
    ActivationPlanShape activationOrder(List<Object> manifests);

    /**
     * Validates a manifest against the schema, throwing the first problem as a plugin error.
     *
     * @implNote A caller that names no vocabulary is held to an empty one, so a bare well-known id
     * counts as squatting: its caller is a host, which knows its own vocabulary.
     * @param manifest the parsed plugin.json tree to validate
     * @return the same manifest, unchanged, once it has been validated
     */
    Object assertManifest(Object manifest);

    /**
     * Validates a manifest, treating the given ids as the bare service ids any plugin may register.
     *
     * @param manifest the parsed plugin.json tree to validate
     * @param wellKnownServices the bare service ids this host accepts without a namespace
     * @return the same manifest, unchanged, once it has been validated
     */
    Object assertManifest(Object manifest, List<String> wellKnownServices);

    /**
     * Every problem with a manifest, rather than the first.
     *
     * @implNote An absent vocabulary means unverifiable here, not empty, so the bare-service check
     * is skipped rather than answered wrongly. Its callers include a plugin author's own suite.
     * @param manifest the parsed plugin.json tree to validate
     * @return every issue found, or empty when the manifest is valid
     */
    List<ValidationIssueShape> validateManifest(Object manifest);

    /**
     * Every problem with a manifest, checked against the given well-known service ids.
     *
     * @param manifest the parsed plugin.json tree to validate
     * @param wellKnownServices the bare service ids this host accepts without a namespace
     * @return every issue found, or empty when the manifest is valid
     */
    List<ValidationIssueShape> validateManifest(Object manifest, List<String> wellKnownServices);

    /**
     * The published JSON Schema of plugin.json, as a tree ready to stringify.
     *
     * @return the schema tree
     */
    Object manifestSchema();

    /**
     * Opens a host: the capability registry, the service hub, the event bus and the ledger.
     *
     * @param options the app id, api version, surfaces and vocabulary the host declares
     * @return the opened host
     */
    HostSurface createPluginHost(PluginHostOptionsShape options);

    /**
     * Mints a plugin error a caller can throw, marked so any bundle recognises it.
     *
     * @param pluginId the id of the plugin the error belongs to
     * @param detail what went wrong
     * @param fix what the plugin author should do about it
     * @return the error, ready to throw
     */
    PluginErrorShape pluginError(String pluginId, String detail, String fix);

    /**
     * Whether a caught value is a plugin error, recognised by its marker rather than its class.
     *
     * @param value the caught value to inspect
     * @return true when the value carries the plugin error marker, false otherwise
     */
    boolean isPluginError(Object value);

    /**
     * Turns quiet failures loud. Null means off, since a compiled bundle has no environment to read.
     *
     * @param enabled true to raise strict diagnostics, false or null to leave them quiet
     */
    void setStrict(@TsNullable Boolean enabled);

    /**
     * Installs where diagnostics are written, or null to stop writing them.
     *
     * @param sink the destination for each diagnostic message, or null to stop writing them
     */
    void setDiagnosticSink(@TsNullable Consumer<String> sink);
}
