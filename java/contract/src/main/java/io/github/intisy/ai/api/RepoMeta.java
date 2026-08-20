package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

@TsInterface(data = true)
public interface RepoMeta {
    String role();

    String category();

    @TsOptional
    List<String> domains();

    String tech();
}
