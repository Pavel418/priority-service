package org.volunteer.client.exception;

public class ConfigurationException extends VolunteerException {
    public ConfigurationException(String message) {
        super("CONFIG-001", message);
    }
}