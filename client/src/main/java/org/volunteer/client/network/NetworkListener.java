package org.volunteer.client.network;

public interface NetworkListener {
    void onAssignmentUpdate(String jsonAssignment);
    void onConnectionEstablished();
    void onConnectionFailed();
    void onConnectionClosed(int statusCode, String reason);
}