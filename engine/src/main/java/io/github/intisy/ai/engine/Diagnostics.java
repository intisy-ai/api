package io.github.intisy.ai.engine;

/**
 * Where the diagnostics an open vocabulary produces go.
 *
 * @implNote Open vocabularies mean an unknown id is ignored rather than rejected, which is exactly
 * what hides a typo while developing. Strict mode keeps the ignoring and makes it loud.
 */
public final class Diagnostics {

    /** Environment variable that turns strict mode on when it reads exactly "1". */
    public static final String STRICT_ENV = "INTISY_PLUGIN_STRICT";

    /** A host-supplied destination for the diagnostics this package would otherwise print. */
    public interface Sink {
        void accept(String message);
    }

    private static Boolean strict = null;
    private static Sink sink = null;

    private Diagnostics() {
    }

    public static boolean isStrict() {
        if (strict == null) {
            strict = Boolean.valueOf("1".equals(System.getenv(STRICT_ENV)));
        }
        return strict.booleanValue();
    }

    /** Forces strict mode on or off. Null falls back to {@link #STRICT_ENV}, which is how a test undoes itself. */
    public static void setStrict(Boolean enabled) {
        strict = enabled;
    }

    /** Directs every diagnostic to a host's own logger. Null restores the default. */
    public static void setSink(Sink destination) {
        sink = destination;
    }

    public static void ignoreUnknown(String kind, String id, String source) {
        report("ignored unknown " + kind + " \"" + id + "\" from " + source);
    }

    /**
     * @implNote The quiet case stays silent rather than mirroring the TypeScript's debug log: a JVM
     * host has no debug channel to send it to, and writing it to stderr anyway would put output a
     * host never asked for in front of its users.
     */
    public static void report(String message) {
        Sink destination = sink;
        if (destination != null) {
            destination.accept(message);
            return;
        }
        if (isStrict()) {
            System.err.println("[plugin-api] " + message);
        }
    }
}
