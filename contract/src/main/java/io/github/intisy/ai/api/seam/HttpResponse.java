package io.github.intisy.ai.api.seam;

import java.util.Map;

/**
 * One HTTP outcome, carrying either a buffered {@link #body} or a streamed {@link #bodyStream}.
 *
 * @implNote Status and headers are committed to the wire the moment a streamed response starts, so a
 * caller retrying across a fallback chain may only do so while {@link #bodyStream} has not yet
 * yielded its first event. After that the attempt is unretryable and the failure has to surface
 * inside the stream instead.
 */
public class HttpResponse {
    /** The HTTP status code. */
    public int status;

    /** Response headers. */
    public Map<String, String> headers;

    /** The whole body, when this response is buffered. Null when {@link #bodyStream} is set. */
    public String body;

    /** The body as it is produced, when this response streams. Null when {@link #body} is set. */
    public EventSource bodyStream;
}
