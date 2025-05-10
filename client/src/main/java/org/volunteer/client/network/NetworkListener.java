package org.volunteer.client.network;

import org.volunteer.client.model.AssignmentUpdateResponse;

public interface NetworkListener {
    void onAssignmentUpdate(AssignmentUpdateResponse response);
    void onConnectionEstablished();
    void onConnectionFailed();
    void onConnectionClosed(int statusCode, String reason);
}