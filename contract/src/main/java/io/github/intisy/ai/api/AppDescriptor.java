package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/**
 * One app a host loads plugins into, as the app's own project declares it.
 *
 * @implNote Everything here is data a consumer reads, never behaviour: an app is added by declaring
 * one of these and nothing else learns its name. A declaration carries only what the app's project
 * knows, so a reader fills what it omits rather than requiring the declaration to be complete.
 */
@TsInterface(data = true)
public interface AppDescriptor {
    /** The app's permanent id, for example {@code claude} or {@code opencode}. */
    String id();

    /** The name a surface shows instead of the id. */
    String label();

    /** Self-contained SVG mark for the app, rendered by dashboards. Data, not code. */
    @TsOptional
    String icon();

    /** Where this app keeps its home directory. */
    AppHome home();

    /** How to tell whether this app is installed. */
    AppDetect detect();

    /** The plugin this app is reached through. Absent means the app has no loader. */
    @TsOptional
    AppLoader loader();

    /** The subdirectory inside the app home holding its slash commands. */
    String commandsSubdir();

    /**
     * The names of the storage subdirectories inside this app's home.
     *
     * @implNote Optional because a declaration rarely states them: a reader resolves each name from
     * the declaration, then an environment override, then the ecosystem default.
     */
    @TsOptional
    AppPathNames paths();

    /** The port this app's proxy listens on, or 0 when it needs none. */
    int proxyPort();

    /** How this app reaches the local API. */
    AppIntegration integration();

    /** The wire format this app speaks, for example {@code anthropic}. */
    String wireFormat();

    /** Session-storage formats this app writes, for usage readers. Absent means no usage data. */
    @TsOptional
    AppUsage usage();

    /**
     * Accent colour for this app's surfaces, as a {@code #rrggbb} hex string.
     *
     * @implNote Presentation data beside {@code icon}. Absent means a consumer uses its own neutral
     * default rather than inventing one per app.
     */
    @TsOptional
    String accent();

    /**
     * The command a user types to launch this app through its loader's wrapper.
     *
     * @implNote Absent means the app is launched by its own binary, so nothing writes a wrapper.
     */
    @TsOptional
    String wrapperCommand();

    /** This app's own npm-plugin mechanism. Absent means it has none. */
    @TsOptional
    AppNpmPlugins npmPlugins();

    /** How this app runs a plugin at startup when it has no npm-plugin list of its own. */
    @TsOptional
    AppStartupHook startupHook();

    /** Where a marketplace looks for this app's community plugins. */
    @TsOptional
    AppDiscovery discovery();

    /** Where this app records the projects a user has worked in. */
    @TsOptional
    AppProjects projects();

    /** The app config file a model catalog is merged into. */
    @TsOptional
    AppModelCatalog modelCatalog();
}
