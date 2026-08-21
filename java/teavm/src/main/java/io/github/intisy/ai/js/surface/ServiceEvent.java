package io.github.intisy.ai.js.surface;

/**
 * The two things that can happen to a watched service, matching {@code ServiceEvent} in
 * {@code src/services.ts}.
 *
 * @implNote Lower-case constant names by design: this enum is never itself annotated, only
 * referenced as a {@code watch} listener's second parameter type, and the processor emits an enum
 * used that way as the literal union of its constant names.
 */
public enum ServiceEvent {
    register,
    unregister
}
