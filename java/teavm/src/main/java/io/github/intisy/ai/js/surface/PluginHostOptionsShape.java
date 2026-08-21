package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/** What a host says about itself when it builds its plugin host, the {@code createPluginHost} argument. */
@TsInterface(data = true)
public interface PluginHostOptionsShape {
    String app();

    @TsOptional
    Integer api();

    @TsOptional
    List<String> surfaces();

    @TsOptional
    List<String> vocabulary();

    @TsOptional
    List<String> wellKnownServices();
}
