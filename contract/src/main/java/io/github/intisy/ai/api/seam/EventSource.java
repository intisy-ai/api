package io.github.intisy.ai.api.seam;

/**
 * Blocking-shaped pull source of string events; implementations handle any async plumbing
 * internally, exactly as {@link HttpClient} does for a single exchange.
 *
 * @implNote Carries strings rather than a typed event, because this layer may not know what any
 * higher layer streams. A caller that needs structure parses each event with its own
 * {@link JsonCodec}.
 */
public interface EventSource {

    /**
     * The next event, or {@code null} once the source is drained.
     *
     * @throws RuntimeException when the underlying source fails. A failure is terminal: the source
     * must not be pulled again afterwards.
     */
    String next();
}
