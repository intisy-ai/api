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
    /** The command's name, which is also the file it is written to. */
    String name();

    /** What a command picker shows beside the name. */
    String description();

    /** The argument shape a picker hints at, such as {@code list | get <key>}. */
    @TsOptional
    String argumentHint();

    /** Markdown the model is shown, after any shell output. */
    @TsOptional
    String body();

    /** A shell line run before the body, which may use $ARGUMENTS and {{BUNDLE}}. */
    @TsOptional
    String shell();
}
