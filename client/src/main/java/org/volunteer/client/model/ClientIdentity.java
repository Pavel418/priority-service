package org.volunteer.client.model;

import java.util.concurrent.atomic.AtomicReference;

public final class ClientIdentity {
    private static final AtomicReference<String> clientId = new AtomicReference<>();
    private static final Object lock = new Object();

    private ClientIdentity() {} // Prevent instantiation

    public static void initialize(String id) {
        synchronized(lock) {
            if (clientId.get() != null) {
                throw new IllegalStateException("Client ID already initialized");
            }
            clientId.set(id);
        }
    }

    public static String getId() {
        String id = clientId.get();
        if (id == null) {
            throw new IllegalStateException("Client ID not initialized");
        }
        return id;
    }

    public static void clear() {
        synchronized(lock) {
            clientId.set(null);
        }
    }
}