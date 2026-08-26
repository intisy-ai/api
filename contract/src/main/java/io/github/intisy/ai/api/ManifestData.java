package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/**
 * Where a plugin keeps state that is not named after it.
 *
 * @implNote A surface finds most of what a plugin leaves behind by its id; this is the escape hatch
 * for a plugin that writes elsewhere. Declared rather than asked for, because the surface that needs
 * it most is an uninstall, where the plugin is on its way out and may not be running at all.
 */
@TsInterface(data = true)
public interface ManifestData {
    /**
     * Paths this plugin writes to, relative to the home it runs in.
     *
     * @return the plugin's data paths
     */
    List<String> paths();
}
