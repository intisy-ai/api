package io.github.intisy.ai.api.seam;

/**
 * Blocking-shaped HTTP boundary; implementations handle any async plumbing internally.
 */
public interface HttpClient {
    /**
     * Sends one request and waits for the response.
     *
     * @param req the request to send
     * @return the response
     */
    HttpResponse send(HttpRequest req);
}
