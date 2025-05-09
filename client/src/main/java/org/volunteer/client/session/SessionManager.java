package org.volunteer.client.session;

public final class SessionManager {
    private static String clientId;
    private static String userName;
    private static final Object lock = new Object();

    public static void setClientId(final String clientId) {
        synchronized (lock) {
            SessionManager.clientId = clientId;
        }
    }

    public static void setUserName(final String userName) {
        synchronized (lock) {
            SessionManager.userName = userName;
        }
    }

    public static String getClientId() {
        synchronized(lock) {
            return clientId;
        }
    }

    public static String getUserName() {
        synchronized(lock) {
            return userName;
        }
    }
}