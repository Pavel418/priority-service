package org.volunteer.client.network;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.volunteer.client.exception.ConfigurationException;
import org.volunteer.client.model.ClientInitResponse;
import org.volunteer.client.model.PreferenceUpdate;
import org.volunteer.client.network.config.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
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
    private static final Gson gson = new Gson();

    private static final String INIT_ENDPOINT = "/client/initialize";
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

    public CompletableFuture<ClientInitResponse> initializeClient() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Environment.getRestBaseUrl() + INIT_ENDPOINT))
                .timeout(Environment.getReadTimeout())
                .header("Accept", "application/json")
                .GET()
                .build();

        return handleTypedResponse(
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                ClientInitResponse.class
        );
    }

    public CompletableFuture<String> submitPreferences(List<String> serviceIds) {
        PreferenceUpdate payload = new PreferenceUpdate(serviceIds);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Environment.getRestBaseUrl() + PREFERENCES_ENDPOINT))
                .timeout(Environment.getReadTimeout())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        return handleResponse(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
    }

    /**
     * Generic response handler for typed responses.
     */
    private <T> CompletableFuture<T> handleTypedResponse(
            CompletableFuture<HttpResponse<String>> future,
            Class<T> responseType
    ) {
        return future.thenApply(response -> {
            validateStatusCode(response);
            return gson.fromJson(response.body(), responseType);
        }).exceptionally(this::handleError);
    }

    private void validateStatusCode(HttpResponse<?> response) {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            logger.error("Request failed with status {}: {}", status, response.body());
            throw new RuntimeException("HTTP error: " + status);
        }
    }

    private <T> T handleError(Throwable ex) {
        logger.error("HTTP operation failed", ex);
        throw new RuntimeException("Network operation failed", ex);
    }

    private CompletableFuture<String> handleResponse(CompletableFuture<HttpResponse<String>> future) {
        return future.thenApply(response -> {
            validateStatusCode(response);
            return response.body();
        }).exceptionally(this::handleError);
    }
}