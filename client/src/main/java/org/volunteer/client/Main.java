package org.volunteer.client;

import org.volunteer.client.exception.NetworkException;
import org.volunteer.client.network.config.Environment;
import org.volunteer.client.exception.ConfigurationException;
import org.volunteer.client.gui.MainFrame;
import org.volunteer.client.network.RestClient;
import org.volunteer.client.network.WebSocketHandler;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Verify environment first
        try {
            Environment.getRestBaseUrl(); // Triggers config validation
        } catch (ExceptionInInitializerError e) {
            showFatalError("Configuration Error", e.getCause().getMessage());
            return;
        }

        // Start GUI on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                initializeApplication();
            } catch (Exception e) {
                showFatalError("Initialization Failed", e.getMessage());
            }
        });
    }

    private static void initializeApplication() {
        // Create network components
        RestClient restClient = new RestClient();
//        WebSocketHandler webSocketHandler = new WebSocketHandler();

        // Initialize main UI
        MainFrame mainFrame = new MainFrame();
//        mainFrame.setVisible(true);

        // Load initial data
//        try {
//            mainFrame.loadInitialServices();
//        } catch (NetworkException e) {
//            mainFrame.showErrorDialog(
//                    "Connection Error",
//                    "Failed to load services: " + e.getMessage()
//            );
//        }
    }

    private static void showFatalError(String title, String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        null,
                        message,
                        title,
                        JOptionPane.ERROR_MESSAGE
                )
        );
        System.exit(1);
    }
}