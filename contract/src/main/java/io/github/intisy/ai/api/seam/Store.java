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
    /**
     * The value stored under {@code key}, or {@code null} when nothing is stored there.
     *
     * @param key the entry's name
     * @return the stored value, or {@code null} when nothing is stored under {@code key}
     */
    String get(String key);

    /**
     * Stores {@code value} under {@code key}, replacing whatever was there.
     *
     * @param key the entry's name
     * @param value the value to store
     */
    void put(String key, String value);

    /**
     * Whether anything is stored under {@code key}.
     *
     * @param key the entry's name
     * @return true when an entry is stored under {@code key}, false when there is none
     */
    boolean exists(String key);

    /**
     * Removes {@code key}, and does nothing when it holds nothing.
     *
     * @param key the entry's name
     */
    void delete(String key);

    /**
     * Replaces {@code key}'s value with what {@code mutator} returns, reading and writing as one
     * step, and hands the mutator {@code null} when the key holds nothing yet.
     *
     * @param key the entry's name
     * @param mutator computes the new value from the current one
     */
    void update(String key, UnaryOperator<String> mutator);

    /**
     * Every key beginning with {@code prefix}, in the order the store holds them.
     *
     * @param prefix the key prefix to match, or an empty string to match every key
     * @return the matching keys, or an empty list when none match
     */
    List<String> listKeys(String prefix);
}
