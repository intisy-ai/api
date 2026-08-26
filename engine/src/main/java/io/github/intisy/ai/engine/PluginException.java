package io.github.intisy.ai.engine;

/**
 * The one host-visible plugin failure.
 *
 * @implNote Named Exception rather than Error because a Java Error names an unrecoverable JVM
 * condition and a manifest typo is not one. Every instance names the plugin, what went wrong and
 * what to do about it, because a load failure is read by an author who has never seen this code.
 * On the JVM this class is the failure and {@code instanceof} identifies it directly. A plugin
 * bundled independently by TeaVM shares no class with the host, so class identity does not survive
 * that boundary; the JS side re-marks the same failure by a stable {@code error.name} string
 * instead of relying on {@code instanceof}. A reader working only in this class would not see that,
 * because the translation lives on the JS side of the boundary.
 */
public class PluginException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The id of the plugin the failure is about. */
    private final String pluginId;
    /** What went wrong, in a sentence an author can read without this code. */
    private final String detail;
    /** What the author should do about it. */
    private final String fix;

    /**
     * @param pluginId the id of the plugin the failure is about
     * @param detail what went wrong, in a sentence an author can read without this code
     * @param fix what the author should do about it
     */
    public PluginException(String pluginId, String detail, String fix) {
        super("[" + pluginId + "] " + detail + "\n  fix: " + fix);
        this.pluginId = pluginId;
        this.detail = detail;
        this.fix = fix;
    }

    /** @return the id of the plugin the failure is about */
    public String getPluginId() {
        return pluginId;
    }

    /** @return what went wrong, in a sentence an author can read without this code */
    public String getDetail() {
        return detail;
    }

    /** @return what the author should do about it */
    public String getFix() {
        return fix;
    }
}
