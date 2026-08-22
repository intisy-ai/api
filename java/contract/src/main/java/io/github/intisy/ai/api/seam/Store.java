package io.github.intisy.ai.api.seam;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Key/value store of JSON strings (e.g. keys like {@code accounts.json}, {@code models.json},
 * {@code auth.json}). {@code update} must be atomic; that is the implementation's concern.
 */
@TsInterface
public interface Store {
    /** The value stored under {@code key}, or {@code null} when nothing is stored there. */
    String get(String key);

    /** Stores {@code value} under {@code key}, replacing whatever was there. */
    void put(String key, String value);

    /** Whether anything is stored under {@code key}. */
    boolean exists(String key);

    /** Removes {@code key}, and does nothing when it holds nothing. */
    void delete(String key);

    /**
     * Replaces {@code key}'s value with what {@code mutator} returns, reading and writing as one
     * step, and hands the mutator {@code null} when the key holds nothing yet.
     */
    void update(String key, UnaryOperator<String> mutator);

    /** Every key beginning with {@code prefix}, in the order the store holds them. */
    List<String> listKeys(String prefix);
}
