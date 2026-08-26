package io.github.intisy.ai.api.seam;

/** Source of the current time, so a caller that needs it under test can substitute one. */
public interface Clock {
    /**
     * The current time.
     *
     * @return milliseconds since the Unix epoch
     */
    long now();
}
