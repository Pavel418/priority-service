package org.volunteer.client.network.config;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.time.Duration;

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

    public static String getRestBaseUrl() {
        return props.getProperty("rest.base.url");
    }

    public static String getWebSocketUrl() {
        return props.getProperty("websocket.base.url");
    }

    public static Duration getConnectionTimeout() {
        return Duration.ofMillis(Long.parseLong(props.getProperty("connection.timeout")));
    }

    public static Duration getReadTimeout() {
        return Duration.ofMillis(Long.parseLong(props.getProperty("read.timeout")));
    }

    public static int getMaxRetries() {
        return Integer.parseInt(props.getProperty("max.retries"));
    }

    public static Duration getRetryDelay() {
        return Duration.ofMillis(Long.parseLong(props.getProperty("retry.delay")));
    }
}