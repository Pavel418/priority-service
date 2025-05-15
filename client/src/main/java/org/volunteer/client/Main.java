package org.volunteer.client;

import org.volunteer.client.gui.NameDialog;
import org.volunteer.client.model.ClientInitResponse;
import org.volunteer.client.network.config.Environment;
import org.volunteer.client.exception.ConfigurationException;
import org.volunteer.client.gui.MainFrame;
import org.volunteer.client.network.RestClient;
import org.volunteer.client.session.SessionManager;

import javax.swing.*;
import java.net.http.HttpClient;

public class Main {
    public final static HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

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
        RestClient restClient = new RestClient(httpClient);

        NameDialog dialog = new NameDialog();
        dialog.pack();
        dialog.setVisible(true);

        try {
            ClientInitResponse response = restClient.initializeClient().get();
            SessionManager.setClientId(response.clientId());
            // Initialize main UI
            new MainFrame(response.services(), restClient);
        } catch (Exception e) {
            showFatalError("Initialization Failed", e.getMessage());
        }
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