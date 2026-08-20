package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;

@TsInterface
public interface Logger {
    void info(String message);

    void warn(String message);

    void debug(String message);

    void error(String message);

    void error(String message, Object cause);
}
