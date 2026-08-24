package io.github.intisy.ai.api.seam;

import io.github.intisy.ai.tsemit.TsInterface;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Key/value store of JSON strings, keyed by file-like names. {@code update} must be atomic; that is
 * the implementation's concern.
 *
 * @implNote The key examples this once carried named plugin categories, which the contract may not
 * know, and became visible to that gate the moment this interface started emitting TypeScript.
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
