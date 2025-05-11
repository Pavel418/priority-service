/**
 * The {@code Environment} class provides centralized access to application configuration
 * properties loaded from the {@code environment.properties} file. This final utility class
 * serves as a singleton configuration source for network-related settings.
 *
 * <p>Configuration properties are loaded once during class initialization from the
 * classpath resource {@code environment.properties}. The class provides static methods
 * to access various typed configuration values.
 *
 * <h2>Required Properties</h2>
 * The following properties must be present in {@code environment.properties}:
 * <ul>
 *   <li>{@code rest.base.url} - Base URL for REST API endpoints</li>
 *   <li>{@code websocket.base.url} - Base URL for WebSocket connections</li>
 *   <li>{@code connection.timeout} - Connection timeout in milliseconds</li>
 *   <li>{@code read.timeout} - Read timeout in milliseconds</li>
 *   <li>{@code max.retries} - Maximum number of connection retries</li>
 *   <li>{@code retry.delay} - Delay between retries in milliseconds</li>
 * </ul>
 *
 * @throws IllegalStateException If the {@code environment.properties} file is not found
 * @throws NumberFormatException If numeric properties contain invalid values
 * @throws ExceptionInInitializerError If there's an error loading the properties file
 */
package org.volunteer.client.network.config;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.time.Duration;

/**
 * Utility class for accessing network configuration properties.
 * This class cannot be instantiated or extended.
 */
public final class Environment {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = Environment.class.getClassLoader()
                .getResourceAsStream("environment.properties")) {
            if (input == null) {
                throw new IllegalStateException("environment.properties not found!");
            }
            props.load(input);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Private constructor to prevent instantiation
    private Environment() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Retrieves the base URL for REST API endpoints.
     *
     * @return Base URL string configured with {@code rest.base.url} property
     * @throws NullPointerException if the property is not found
     */
    public static String getRestBaseUrl() {
        return props.getProperty("rest.base.url");
    }

    /**
     * Retrieves the base URL for WebSocket connections.
     *
     * @return WebSocket URL string configured with {@code websocket.base.url} property
     * @throws NullPointerException if the property is not found
     */
    public static String getWebSocketUrl() {
        return props.getProperty("websocket.base.url");
    }

    /**
     * Gets the connection timeout duration for network operations.
     *
     * @return Timeout duration parsed from {@code connection.timeout} property (milliseconds)
     * @throws NumberFormatException if the property value is not a valid long
     * @throws NullPointerException if the property is not found
     */
    public static Duration getConnectionTimeout() {
        return Duration.ofMillis(Long.parseLong(props.getProperty("connection.timeout")));
    }

    /**
     * Gets the read timeout duration for network operations.
     *
     * @return Read timeout duration parsed from {@code read.timeout} property (milliseconds)
     * @throws NumberFormatException if the property value is not a valid long
     * @throws NullPointerException if the property is not found
     */
    public static Duration getReadTimeout() {
        return Duration.ofMillis(Long.parseLong(props.getProperty("read.timeout")));
    }

    /**
     * Retrieves the maximum number of connection retry attempts.
     *
     * @return Integer value from {@code max.retries} property
     * @throws NumberFormatException if the property value is not a valid integer
     * @throws NullPointerException if the property is not found
     */
    public static int getMaxRetries() {
        return Integer.parseInt(props.getProperty("max.retries"));
    }

    /**
     * Gets the delay duration between connection retry attempts.
     *
     * @return Retry delay duration parsed from {@code retry.delay} property (milliseconds)
     * @throws NumberFormatException if the property value is not a valid long
     * @throws NullPointerException if the property is not found
     */
    public static Duration getRetryDelay() {
        return Duration.ofMillis(Long.parseLong(props.getProperty("retry.delay")));
    }
}