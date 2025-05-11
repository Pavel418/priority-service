package org.volunteer.client.network;

import org.volunteer.client.model.AssignmentUpdateResponse;

/**
 * Callback interface for handling WebSocket connection events and message reception.
 * <p>
 * Implementations should be aware that all methods are called on WebSocket listener threads.
 * Thread synchronization is required if interacting with shared resources.
 * </p>
 */
public interface NetworkListener {
    /**
     * Handles incoming assignment updates from the server.
     *
     * @param response Parsed assignment update object containing validated data
     * @implNote Called only for successfully deserialized messages with valid schema
     */
    void onAssignmentUpdate(AssignmentUpdateResponse response);

    /**
     * Signals successful WebSocket connection establishment.
     * <p>
     * Called after both initial connections and successful reconnections.
     * Safe to send messages immediately after this callback.
     * </p>
     */
    void onConnectionEstablished();

    /**
     * Indicates permanent connection failure after exhausting all retries.
     * <p>
     * Called when consecutive reconnection attempts exceed configured maximum.
     * No further automatic reconnections will be attempted after this callback.
     * </p>
     */
    void onConnectionFailed();

    /**
     * Notifies of connection closure from either endpoint.
     *
     * @param statusCode WebSocket closure status code as defined by RFC 6455
     * @param reason Optional human-readable closure reason (may be empty)
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4">RFC 6455 Status Codes</a>
     */
    void onConnectionClosed(int statusCode, String reason);
}