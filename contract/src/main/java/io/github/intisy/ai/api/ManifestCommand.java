package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;

/**
 * One slash command a plugin contributes, declared rather than registered by running code.
 *
 * @implNote Declared so a host can deploy a plugin's commands without importing it, which is what
 * lets the command files exist before the plugin has ever activated.
 */
@TsInterface(data = true)
public interface ManifestCommand {
    /**
     * The command's name, which is also the file it is written to.
     *
     * @return the command name
     */
    String name();

    /**
     * What a command picker shows beside the name.
     *
     * @return the command description
     */
    String description();

    /**
     * The argument shape a picker hints at, such as {@code list | get <key>}.
     *
     * @return the argument hint, or absent when this command declares none
     */
    @TsOptional
    String argumentHint();

    /**
     * Markdown the model is shown, after any shell output.
     *
     * @return the command body, or absent when this command runs shell output only
     */
    @TsOptional
    String body();

    /**
     * A shell line run before the body, which may use $ARGUMENTS and {{BUNDLE}}.
     *
     * @return the shell line, or absent when this command runs no shell step
     */
    @TsOptional
    String shell();
}
