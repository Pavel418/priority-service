package org.volunteer.client.exception;

public abstract class VolunteerException extends Exception {
    private final String errorCode;

    public VolunteerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}