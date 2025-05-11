/**
 * Enables drag-and-drop transfer of {@link ServiceCard} components within the application.
 * Provides a Transferable implementation for local object transfer of service cards.
 */
package org.volunteer.client.gui.dnd;

import org.volunteer.client.gui.ServiceCard;
import java.awt.*;
import java.awt.datatransfer.*;

/**
 * Transferable wrapper for serializing ServiceCard instances during drag operations.
 * Supports intra-JVM transfers using a custom {@link DataFlavor}.
 *
 * <p>This implementation:
 * <ul>
 *   <li>Defines a custom local object flavor for ServiceCard transfers</li>
 *   <li>Maintains strong reference to the original card instance</li>
 *   <li>Enforces single-flavor data transfer</li>
 * </ul>
 *
 * @see Transferable
 * @see DataFlavor
 */
public class ServiceTransferable implements Transferable {

    /**
     * Custom data flavor for ServiceCard transfers within the same JVM.
     * Uses Java local object MIME type with strict class binding.
     */
    public static final DataFlavor FLAVOR = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=" + ServiceCard.class.getName(),
            "ServiceCard"
    );

    private final ServiceCard card;

    /**
     * Creates a transferable wrapper for the specified service card.
     *
     * @param card The ServiceCard to transfer (cannot be {@code null})
     * @throws NullPointerException if card parameter is null
     */
    public ServiceTransferable(ServiceCard card) {
        this.card = card;
    }

    /**
     * Returns supported data flavors for this transferable.
     *
     * @return Single-element array containing only {@link #FLAVOR}
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{FLAVOR};
    }

    /**
     * Verifies support for a specific data flavor.
     *
     * @param flavor The flavor to check
     * @return {@code true} only if flavor matches {@link #FLAVOR}
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOR.equals(flavor);
    }

    /**
     * Retrieves the transfer data in the requested format.
     *
     * @param flavor The requested data flavor
     * @return The wrapped ServiceCard instance
     * @throws UnsupportedFlavorException if flavor doesn't match {@link #FLAVOR}
     */
    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);
        return card;
    }
}