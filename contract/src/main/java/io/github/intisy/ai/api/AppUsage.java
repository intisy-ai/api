package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/** The session-storage formats an app writes, for a usage reader. */
@TsInterface(data = true)
public interface AppUsage {
    /** Format ids, each of which a consumer maps to a parser of its own. */
    List<String> formats();
}
