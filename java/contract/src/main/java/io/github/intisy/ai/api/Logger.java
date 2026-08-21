package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;

/**
 * Where a plugin writes what happened.
 *
 * @implNote The host decides where the lines go, whether they mirror to a console, and how they are
 * attributed, so a plugin never opens a log file itself.
 */
@TsInterface
public interface Logger {
    /** Normal operation. */
    void info(String message);

    /** Something unexpected that did not stop the operation. */
    void warn(String message);

    /** Detail only useful while debugging. */
    void debug(String message);

    /** Something that failed. */
    void error(String message);

    /** Something that failed, with the cause. */
    void error(String message, Object cause);
}
