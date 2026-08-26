package io.github.intisy.ai.api.seam;

/**
 * Push destination for string events, written to as they are produced rather than collected first.
 *
 * @implNote The counterpart to {@link EventSource}, and deliberately not a return value: a caller
 * that accumulated events before handing them over would buffer the whole stream, which is what
 * streaming this way exists to avoid.
 */
public interface EventSink {

    /**
     * Delivers one event.
     *
     * @param event the event payload
     */
    void emit(String event);

    /**
     * Signals that no further events will be emitted.
     *
     * @param error the failure that ended the stream, or {@code null} on normal completion.
     */
    void close(String error);
}
