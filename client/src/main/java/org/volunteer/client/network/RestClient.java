package org.volunteer.client.network;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.volunteer.client.exception.ConfigurationException;
import org.volunteer.client.model.ClientInitResponse;
import org.volunteer.client.model.PreferenceUpdate;
import org.volunteer.client.network.config.Environment;
import org.volunteer.client.exception.HttpException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * REST client for interacting with volunteer service API endpoints.
 * <p>
 * Handles all HTTP communication with the volunteer service using Java's HttpClient
 * with non-blocking asynchronous operations.
 * <p>All methods return {@link CompletableFuture} that may complete with either:
 * <ul>
 *   <li>The parsed response object for successful requests (2xx status codes)</li>
 *   <li>A {@link RuntimeException} wrapping the error cause for failed requests</li>
 * </ul>
 */
public final class RestClient {
    /** Logger instance scoped to RestClient class */
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

    /** Shared Gson instance for JSON serialization/deserialization */
    private static final Gson gson = new Gson();

    // API endpoint paths (relative to base URL)
    private static final String INIT_ENDPOINT = "/client/initialize";
    private static final String PREFERENCES_ENDPOINT = "/preferences";

    /** Underlying HTTP client configured by the caller */
    private final HttpClient client;

    /**
     * Constructs a new RestClient with the specified HttpClient instance.
     *
     * @param client Preconfigured HTTP client with desired settings like:
     *              - Connection pooling
     *              - Proxy configuration
     *              - SSL context
     *              - Cookie handler
     * @throws NullPointerException if client is null
     */
    public RestClient(HttpClient client) {
        this.client = Objects.requireNonNull(client, "HttpClient must not be null");
    }

    /**
     * Initializes the client session with the volunteer service.
     *
     * @return Future completing with client configuration data or error
     * @see ClientInitResponse
     */
    public CompletableFuture<ClientInitResponse> initializeClient() {
        HttpRequest request = newRequestBuilder(INIT_ENDPOINT)
                .header("Accept", "application/json")
                .GET()
                .build();

        return handleTypedResponse(
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                ClientInitResponse.class
        );
    }

    /**
     * Submits volunteer service preferences to the API.
     *
     * @param serviceIds List of service IDs representing preference selection
     * @return Future completing with raw response body or error
     * @apiNote Returns String rather than parsed object to accommodate potential
     *          schema changes in success responses
     */
    public CompletableFuture<String> submitPreferences(List<String> serviceIds) {
        PreferenceUpdate payload = new PreferenceUpdate(serviceIds);

        HttpRequest request = newRequestBuilder(PREFERENCES_ENDPOINT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        return handleTypedResponse(
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                String.class
        );
    }

    /**
     * Creates a preconfigured request builder with common settings.
     *
     * @param endpoint API endpoint path (relative to base URL)
     * @return Configured request builder ready for method-specific customization
     */
    private HttpRequest.Builder newRequestBuilder(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(buildUri(endpoint))
                .timeout(Environment.getReadTimeout());
    }

    /**
     * Safely constructs full URI by resolving endpoint against base URL.
     *
     * @param endpoint Service endpoint path (may contain path parameters)
     * @return Full URI with proper path encoding
     * @throws ConfigurationException if base URL is invalid
     */
    private URI buildUri(String endpoint) {
        try {
            return URI.create(Environment.getRestBaseUrl()).resolve(endpoint);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Invalid base URL in configuration", e);
        }
    }

    /**
     * Centralized response handler for typed API responses.
     *
     * @param future Raw HTTP response future
     * @param responseType Target class for JSON deserialization
     * @return Future that completes with parsed object or exception
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

    /**
     * Validates HTTP status code and throws appropriate exception for non-2xx responses.
     *
     * @param response HTTP response to validate
     * @throws HttpException Containing status code and response body for error responses
     */
    private void validateStatusCode(HttpResponse<?> response) {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            String body = response.body() != null ?
                    response.body().toString() : "<empty body>";
            logger.error("HTTP Error {}: {}", status, body);
            throw new HttpException(status, body);
        }
    }

    /**
     * Centralized error handler that unwraps CompletionException and converts to
     * appropriate runtime exception.
     *
     * @param ex Original exception from future chain
     * @return Never returns normally, always throws
     * @throws RuntimeException Wrapping the root cause of the failure
     */
    private <T> T handleError(Throwable ex) {
        Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;

        if (cause instanceof HttpException httpEx) {
            logger.error("Service returned error status: {} - {}",
                    httpEx.getStatusCode(), httpEx.getResponseBody());
        } else {
            logger.error("Network failure during API operation", cause);
        }

        throw new RuntimeException("API operation failed", cause);
    }
}