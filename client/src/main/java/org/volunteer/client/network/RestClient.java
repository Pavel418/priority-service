package org.volunteer.client.network;

import org.volunteer.client.network.config.Environment;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class RestClient {
    private final HttpClient client;

    public RestClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Environment.getConnectionTimeout())
                .build();
    }

    public CompletableFuture<String> getServices() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Environment.getRestBaseUrl() + "/services"))
                .timeout(Environment.getReadTimeout())
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public CompletableFuture<String> submitPreferences(String volunteerId, String jsonPreferences) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Environment.getRestBaseUrl() + "/preferences"))
                .timeout(Environment.getReadTimeout())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPreferences))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}