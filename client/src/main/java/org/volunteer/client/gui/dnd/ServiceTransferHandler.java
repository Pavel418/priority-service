/**
 * Handles drag-and-drop operations for {@link ServiceCard} components between containers.
 * Manages full transfer lifecycle including export validation and post-import cleanup.
 */
package org.volunteer.client.gui.dnd;

import org.volunteer.client.gui.ServiceCard;
import org.volunteer.client.model.Service;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom transfer handler implementing drag-and-drop functionality for service cards.
 * Features:
 * <ul>
 *   <li>Single-card movement between designated slots</li>
 *   <li>Slot occupancy validation</li>
 *   <li>Visual cleanup after transfers</li>
 *   <li>Optional success callback</li>
 * </ul>
 *
 * <p>Requires target containers to be marked with client property "isSlot"=true
 *
 * @see TransferHandler
 * @see ServiceTransferable
 */
public class ServiceTransferHandler extends TransferHandler {
    /**
     * Custom data flavor for local ServiceCard transfers matching {@link ServiceTransferable#FLAVOR}
     */
    public static final DataFlavor FLAVOR = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType +
                    ";class=" + ServiceCard.class.getName(),
            "ServiceCard"
    );

    private Consumer<Service> onImportSuccess;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Registers a callback for successful card imports
     *
     * @param callback Consumer receiving the transferred service (nullable)
     */
    public void setOnImportSuccess(Consumer<Service> callback) {
        this.onImportSuccess = callback;
    }

    /**
     * Returns supported transfer actions
     *
     * @return {@link TransferHandler#MOVE} - Only move operations allowed
     */
    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    /**
     * Creates transferable data package for dragged component
     *
     * @param c The source ServiceCard component
     * @return Transferable containing ServiceCard reference
     * @implNote Stores original parent container in card's client properties
     */
    @Override
    protected Transferable createTransferable(JComponent c) {
        ServiceCard card = (ServiceCard) c;
        card.putClientProperty("dndSourceContainer", c.getParent());
        return new ServiceTransferable(card);
    }

    /**
     * Validates potential drop targets
     *
     * @param support Transfer context information
     * @return true if:
     * <ul>
     *   <li>Target is a marked slot panel</li>
     *   <li>Data flavor matches</li>
     *   <li>Target slot is empty</li>
     * </ul>
     */
    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDataFlavorSupported(FLAVOR)) return false;

        Component comp = support.getComponent();
        if (!(comp instanceof JPanel panel)) return false;

        return Boolean.TRUE.equals(panel.getClientProperty("isSlot")) &&
                panel.getComponents().length == 0;
    }

    /**
     * Handles successful drop operation
     *
     * @param support Transfer context
     * @return true on successful import
     * @implSpec Performs:
     * <ol>
     *   <li>Card retrieval from transfer data</li>
     *   <li>Target panel population</li>
     *   <li>Success callback invocation</li>
     * </ol>
     */
    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;

        try {
            ServiceCard dropped = (ServiceCard) support.getTransferable().getTransferData(FLAVOR);
            JPanel target = (JPanel) support.getComponent();

            target.removeAll();
            target.add(dropped);
            target.revalidate();
            target.repaint();

            if (onImportSuccess != null) {
                SwingUtilities.invokeLater(() ->
                        onImportSuccess.accept(dropped.getService()));
            }
            return true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Service transfer failed", ex);
            return false;
        }
    }

    /**
     * Cleans up source location after successful move
     *
     * @param source Original card component
     * @param data Transfer data
     * @param action Completed action
     */
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (action == MOVE) {
            ServiceCard card = (ServiceCard) source;
            Container oldParent = (Container) card.getClientProperty("dndSourceContainer");

            if (oldParent != null) {
                oldParent.remove(card);
                oldParent.revalidate();
                oldParent.repaint();
            }
        }
    }
}