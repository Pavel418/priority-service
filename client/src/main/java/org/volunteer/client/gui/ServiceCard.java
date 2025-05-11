/**
 * Custom Swing component representing a visual card displaying service information.
 * Provides a consistent UI element for presenting {@link Service} data in lists or grids.
 */
package org.volunteer.client.gui;

import org.volunteer.client.model.Service;
import javax.swing.*;
import java.awt.*;

/**
 * Interactive panel component that visually represents a service entry.
 *
 * <p>The component features:
 * <ul>
 *   <li>Service name display in a clean label format</li>
 *   <li>Gray border for visual separation</li>
 *   <li>Non-focusable text element to prevent UI distraction</li>
 * </ul>
 *
 * @see JPanel
 * @see Service
 */
public class ServiceCard extends JPanel {
    private JPanel panel1;
    private final Service service;

    /**
     * Constructs a service card component for the specified service.
     *
     * @param service The service data to display (cannot be {@code null})
     * @throws NullPointerException if service parameter is null
     * @implNote Initializes UI components and configures visual properties
     */
    public ServiceCard(Service service) {
        this.service = service;
        JLabel textLabel = new JLabel(service.serviceName());
        textLabel.setFocusable(false);
        textLabel.setOpaque(false);
        textLabel.setBorder(null);

        add(textLabel, BorderLayout.CENTER);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    /**
     * Retrieves the service entity associated with this card.
     *
     * @return The underlying {@link Service} instance (never {@code null})
     */
    public Service getService() {
        return service;
    }
}