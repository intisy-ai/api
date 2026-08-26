package io.github.intisy.ai.api.seam;

import java.util.Map;

/** One outgoing HTTP request, passed to {@link HttpClient#send}. */
public class HttpRequest {
    /** The HTTP method, for example {@code GET} or {@code POST}. */
    public String method;

    /** The full request URL. */
    public String url;

    /** Request headers, or {@code null} to send none beyond what the transport adds. */
    public Map<String, String> headers;

    /** The request body, or {@code null} for a request with no body. */
    public String body;
}
