package org.volunteer.client.exception;

public class NetworkException extends VolunteerException {
    private final int statusCode;

    public NetworkException(int statusCode, String message) {
        super("NET-" + statusCode, message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}