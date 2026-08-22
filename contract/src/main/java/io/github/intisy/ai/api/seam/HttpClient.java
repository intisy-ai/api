package io.github.intisy.ai.api.seam;

/**
 * Blocking-shaped HTTP boundary; implementations handle any async plumbing internally.
 */
public interface HttpClient {
    HttpResponse send(HttpRequest req);
}
