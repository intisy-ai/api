package io.github.intisy.ai.api.seam;

import io.github.intisy.ai.tsemit.TsInterface;

/**
 * Where a component writes what happened.
 *
 * @implNote The host decides where the lines go, whether they mirror to a console, and how they are
 * attributed, so a component never opens a log file itself.
 */
@TsInterface
public interface Logger {
    /**
     * Normal operation.
     *
     * @param message the line to log
     */
    void info(String message);

    /**
     * Something unexpected that did not stop the operation.
     *
     * @param message the line to log
     */
    void warn(String message);

    /**
     * Detail only useful while debugging.
     *
     * @param message the line to log
     */
    void debug(String message);

    /**
     * Something that failed.
     *
     * @param message the line to log
     */
    void error(String message);

    /**
     * Something that failed, with the cause.
     *
     * @param message the line to log
     * @param cause the failure, logged alongside the message
     */
    void error(String message, Object cause);
}
