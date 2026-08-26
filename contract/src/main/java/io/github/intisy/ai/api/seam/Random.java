package io.github.intisy.ai.api.seam;

/** Source of randomness, so a caller that needs it under test can substitute one. */
public interface Random {
    /**
     * The next random value.
     *
     * @return a value in the half-open range 0.0 (inclusive) to 1.0 (exclusive)
     */
    double next();
}
