package org.volunteer.client;

import java.net.http.HttpClient;
// import java.util.ArrayList; // No longer needed
import java.util.List; // Keep
import java.util.UUID; // Keep

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger; // Added logger
import org.slf4j.LoggerFactory; // Added logger
import org.volunteer.client.gui.MainFrame;
import org.volunteer.client.model.ClientInitResponse; // Added import
import org.volunteer.client.model.Service; // Keep
import org.volunteer.client.network.RestClient;
import org.volunteer.client.network.config.Environment;
import org.volunteer.client.session.SessionManager;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class); // Added logger
    public final static HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    public static void main(String[] args) {
        logger.info("Starting volunteer client application...");
        // Verify environment first
        try {
            Environment.getRestBaseUrl(); // Triggers config validation
            logger.debug("Environment configuration loaded.");
        } catch (ExceptionInInitializerError e) {
            logger.error("Failed to load environment configuration", e.getCause());
            showFatalError("Configuration Error", "Failed to load environment.properties: " + e.getCause().getMessage());
            return;
        }

        // Start GUI initialization on EDT
        logger.info("Scheduling application initialization on EDT...");
        SwingUtilities.invokeLater(() -> {
            initializeApplication();
        });
        logger.info("main method finished.");
    }

    private static void initializeApplication() {
        logger.info("Initializing application...");
        // Create network components
        RestClient restClient = new RestClient(httpClient);

        logger.info("Calling server for initialization data...");
        restClient.initializeClient()
            .thenAccept(response -> {
                // Run UI creation on EDT
                SwingUtilities.invokeLater(() -> {
                    String clientId = response.clientId();
                    List<Service> services = response.services();
                    logger.info("Received initialization data: ClientId={}, Services={}", clientId, services.size());

                    SessionManager.setClientId(clientId);
                    logger.debug("Client ID set in session: {}", clientId);

                    logger.info("Initializing main UI frame...");
                    new MainFrame(services, restClient);
                    logger.info("MainFrame initialized and visible.");
                });
            })
            .exceptionally(ex -> {
                // Handle initialization errors (e.g., server unreachable)
                logger.error("Error during application initialization call", ex);
                showFatalError("Initialization Failed", "Could not contact server: " + ex.getMessage());
                return null; // Required for exceptionally
            });
    }

    private static void showFatalError(String title, String message) {
        logger.error("Fatal Error - Title: {}, Message: {}", title, message);
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        null,
                        message,
                        title,
                        JOptionPane.ERROR_MESSAGE
                )
        );
        // Consider if immediate exit is always desired
        // System.exit(1);
    }
}