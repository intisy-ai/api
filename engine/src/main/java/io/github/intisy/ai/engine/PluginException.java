package io.github.intisy.ai.engine;

/**
 * The one host-visible plugin failure.
 *
 * @implNote Named Exception rather than Error because a Java Error names an unrecoverable JVM
 * condition and a manifest typo is not one. Every instance names the plugin, what went wrong and
 * what to do about it, because a load failure is read by an author who has never seen this code.
 */
public class PluginException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String pluginId;
    private final String detail;
    private final String fix;

    public PluginException(String pluginId, String detail, String fix) {
        super("[" + pluginId + "] " + detail + "\n  fix: " + fix);
        this.pluginId = pluginId;
        this.detail = detail;
        this.fix = fix;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getDetail() {
        return detail;
    }

    public String getFix() {
        return fix;
    }
}
