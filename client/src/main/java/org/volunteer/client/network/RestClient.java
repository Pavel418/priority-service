package org.volunteer.client.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.volunteer.client.network.config.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public final class RestClient {
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

    private static final String SERVICES_ENDPOINT = "/services";
    private static final String PREFERENCES_ENDPOINT = "/preferences";

    private final HttpClient client;

    public RestClient(HttpClient client) {
        this.client = client;
    }

    public CompletableFuture<String> getServices() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Environment.getRestBaseUrl() + SERVICES_ENDPOINT))
                .timeout(Environment.getReadTimeout())
                .header("Accept", "application/json")
                .GET()
                .build();

        return handleResponse(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
    }

    public CompletableFuture<String> submitPreferences(String volunteerId, String jsonPreferences) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Environment.getRestBaseUrl() + PREFERENCES_ENDPOINT))
                .timeout(Environment.getReadTimeout())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPreferences))
                .build();

        return handleResponse(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
    }

    private CompletableFuture<String> handleResponse(CompletableFuture<HttpResponse<String>> future) {
        return future.thenApply(response -> {
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return response.body();
            } else {
                logger.error("Request failed with status {}: {}", status, response.body());
                throw new RuntimeException("Unexpected response status: " + status + ", body: " + response.body());
            }
        }).exceptionally(ex -> {
            logger.error("HTTP request failed", ex);
            throw new RuntimeException("HTTP request failed", ex);
        });
    }
}
