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
    // private JPanel panel1; // This field seems unused and can be removed if it was from a GUI builder
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

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setBackground(new Color(245, 245, 245));   // ★ gives it its own surface
        setOpaque(true);                           // ★ <- crucial for mouse events

        JLabel title = new JLabel(service.serviceName(), SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        // title.setFocusable(false); // Not in the patch, but was there. Removing to match patch.
        // title.setOpaque(false); // Not in the patch, but was there. Removing to match patch.
        // title.setBorder(null); // Not in the patch, but was there. Removing to match patch.
        add(title, BorderLayout.CENTER);

        setFocusable(true); // Keep this as it was in previous version and generally good for DnD sources
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