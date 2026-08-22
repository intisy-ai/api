package io.github.intisy.ai.seam;

import io.github.intisy.ai.api.seam.Logger;

/**
 * A {@link Logger} that discards every line.
 *
 * @implNote Exists so a caller with nowhere to log does not hand out {@code null}, which every call
 * site would then have to guard.
 */
public final class NoopLogger implements Logger {

    public static final Logger INSTANCE = new NoopLogger();

    private NoopLogger() {
    }

    @Override
    public void info(String message) {
    }

    @Override
    public void warn(String message) {
    }

    @Override
    public void debug(String message) {
    }

    @Override
    public void error(String message) {
    }

    @Override
    public void error(String message, Object cause) {
    }
}
