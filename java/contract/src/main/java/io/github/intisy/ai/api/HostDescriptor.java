package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;

/**
 * What a plugin may know about its host.
 *
 * @implNote Deliberately not a plugin registry: a plugin adapts to which surfaces exist, never to
 * which app it happens to be in.
 */
@TsInterface(data = true)
public interface HostDescriptor {
    String app();

    int api();

    List<String> surfaces();
}
