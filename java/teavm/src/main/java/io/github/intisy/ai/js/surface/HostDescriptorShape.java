package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/** What a host tells a plugin about itself. */
@TsInterface(data = true)
public interface HostDescriptorShape {
    String app();

    int api();

    List<String> surfaces();
}
