/**
 * The {@code SessionManager} class provides thread-safe management of global session state
 * including client identification and user information. This utility class maintains
 * process-wide session data with atomic access guarantees.
 *
 * <p>All access and modification methods are synchronized to ensure thread safety in
 * multi-threaded environments. Session data persists until modified or until JVM shutdown.
 *
 * <h2>Thread Safety</h2>
 * This class is designed for concurrent use:
 * <ul>
 *   <li>All read/write operations use intrinsic locking on a static monitor</li>
 *   <li>Atomic visibility guarantees through synchronized blocks</li>
 *   <li>Consistent state management across all access points</li>
 * </ul>
 *
 * </pre>
 *
 * @implNote This class should only be used for process-wide session storage and
 *           not for long-term persistence
 */
package org.volunteer.client.session;

/**
 * Thread-safe singleton managing global session state information.
 * This class cannot be instantiated or extended.
 */
public final class SessionManager {
    private static String clientId;
    private static String userName;
    private static final Object lock = new Object();

    // Private constructor to prevent instantiation
    private SessionManager() {
        throw new AssertionError("Cannot instantiate session manager");
    }

    /**
     * Sets the current client's unique identifier atomically.
     *
     * @param clientId The client ID to store (may be {@code null})
     * @apiNote Typically called during authentication/authorization flows
     */
    public static void setClientId(final String clientId) {
        synchronized (lock) {
            SessionManager.clientId = clientId;
        }
    }

    /**
     * Sets the authenticated user's name atomically.
     *
     * @param userName The user name to store (may be {@code null})
     * @apiNote Should be cleared on session invalidation
     */
    public static void setUserName(final String userName) {
        synchronized (lock) {
            SessionManager.userName = userName;
        }
    }

    /**
     * Retrieves the current client ID atomically.
     *
     * @return The stored client ID or {@code null} if not set
     * @implSpec Return value may change between invocations
     */
    public static String getClientId() {
        synchronized(lock) {
            return clientId;
        }
    }

    /**
     * Retrieves the authenticated user's name atomically.
     *
     * @return The stored username or {@code null} if not set
     * @implNote Return value should be considered volatile
     */
    public static String getUserName() {
        synchronized(lock) {
            return userName;
        }
    }
}