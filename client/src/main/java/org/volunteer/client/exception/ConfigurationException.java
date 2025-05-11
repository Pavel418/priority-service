/**
 * Thrown to indicate serious configuration errors that prevent proper application operation.
 * This runtime exception typically occurs during system initialization or critical
 * configuration changes.
 */
package org.volunteer.client.exception;

/**
 * Unchecked exception indicating a fatal configuration error in the application.
 * This exception should be thrown when configuration problems prevent normal
 * operation and require immediate attention.
 *
 * <p>As a {@link RuntimeException}, it doesn't need explicit declaration in
 * method signatures but should be reserved for truly unrecoverable configuration
 * issues.
 *
 */
public class ConfigurationException extends RuntimeException {

    /**
     * Constructs a new configuration exception with the specified detail message.
     *
     * @param message The detailed error message (preferred format: "[Component] Failed to...")
     * @apiNote Messages should clearly identify the configuration source and nature of failure
     */
    public ConfigurationException(String message) { super(message); }

    /**
     * Constructs a new configuration exception with the specified detail message and root cause.
     *
     * @param message The contextual error description
     * @param cause The original exception (typically IOException, SecurityException, etc.)
     * @throws NullPointerException if the cause is {@code null}
     * @implSpec Use this constructor when wrapping lower-level exceptions
     */
    public ConfigurationException(String message, Throwable cause) { super(message, cause); }
}