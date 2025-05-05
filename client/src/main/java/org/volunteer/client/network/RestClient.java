package org.volunteer.client.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.volunteer.client.network.config.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Provides RESTful HTTP client functionality for interacting with volunteer service endpoints.
 * <p>
 * This class handles both retrieval of available services and submission of volunteer preferences
 * using Java's HttpClient with async/non-blocking operations. It features:
 * - Configurable timeouts from environment settings
 * - Standardized JSON content handling
 * - Centralized error processing
 * - HTTP status code validation
 * </p>
 *
 * <p>This class is thread-safe when used with a thread-safe {@link HttpClient} implementation.</p>
 */
public final class RestClient {
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

    private static final String SERVICES_ENDPOINT = "/services";
    private static final String PREFERENCES_ENDPOINT = "/preferences";

    private final HttpClient client;

    /**
     * Constructs a RestClient with the specified HttpClient instance.
     *
     * @param client Pre-configured HttpClient to use for all requests.
     *               Allows for centralized configuration and connection pooling.
     *               Must not be null.
     */
    public RestClient(HttpClient client) {
        this.client = client;
    }

    /**
     * Asynchronously retrieves the list of available volunteer services.
     *
     * @return CompletableFuture that completes with the JSON string of services
     *         or completes exceptionally with a RuntimeException wrapping either:
     *         - HTTP status errors (non-2xx responses)
     *         - IO/Network errors
     *         - Timeout exceptions
     */
    public CompletableFuture<String> getServices() {
        // Build GET request with configured base URL and timeout
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Environment.getRestBaseUrl() + SERVICES_ENDPOINT))
                .timeout(Environment.getReadTimeout())
                .header("Accept", "application/json")
                .GET()
                .build();

        return handleResponse(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
    }

    /**
     * Asynchronously submits volunteer preferences to the server.
     *
     * @param volunteerId     Unique identifier for the volunteer
     * @param jsonPreferences Preferences data in JSON format
     * @return CompletableFuture that completes with the server response body
     *         or completes exceptionally as described in {@link #getServices()}
     */
    public CompletableFuture<String> submitPreferences(String volunteerId, String jsonPreferences) {
        // Build POST request with JSON payload and timeout configuration
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Environment.getRestBaseUrl() + PREFERENCES_ENDPOINT))
                .timeout(Environment.getReadTimeout())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPreferences))
                .build();

        return handleResponse(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
    }

    /**
     * Centralized response handler that validates HTTP status codes and processes errors.
     *
     * @param future Raw HTTP response future to process
     * @return Transformed future that either provides the response body
     *         or fails with meaningful exception information
     */
    private CompletableFuture<String> handleResponse(CompletableFuture<HttpResponse<String>> future) {
        return future.thenApply(response -> {
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return response.body();
            } else {
                // Log full error details while exposing sanitized message to callers
                logger.error("Request failed with status {}: {}", status, response.body());
                throw new RuntimeException("Unexpected response status: " + status
                        + ", body: " + response.body());
            }
        }).exceptionally(ex -> {
            // Wrap all exceptions to prevent caller exposure of implementation details
            logger.error("HTTP request failed", ex);
            throw new RuntimeException("HTTP request failed", ex);
        });
    }
}