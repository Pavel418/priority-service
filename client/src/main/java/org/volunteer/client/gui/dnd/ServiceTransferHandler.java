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
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

/**
 * Handles drag-and-drop of {@link ServiceCard}s between the list on the left
 * and the five preference slots in the middle column.
 *
 * <ul>
 *   <li>The payload is the {@link Service} instance itself (not a String).</li>
 *   <li>{@code MOVE} is the only supported action – the card always exists in
 *       exactly one container.</li>
 * </ul>
 */
public final class ServiceTransferHandler extends TransferHandler {

    /** Single, application-wide flavour for Service objects. */
    public static final DataFlavor SERVICE_FLAVOUR =
            new DataFlavor(Service.class, "application/x-java-service");

    /** Optional callback (set by MainFrame) that runs after a successful drop. */
    private java.util.function.Consumer<Service> onImportSuccess = s -> {};

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /** keeps dangling cards when "Clear" is pressed */
    private final List<ServiceCard> stash = new ArrayList<>();

    /* ---------------------------------------------------------------------- */
    /*  Outgoing side – drag from ServiceCard                                 */
    /* ---------------------------------------------------------------------- */

    @Override
    protected Transferable createTransferable(JComponent src) {
        if (src instanceof ServiceCard) {
            ServiceCard card = (ServiceCard) src;
            return new ServiceSelection(card.getService());
        }
        return null;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected void exportDone(JComponent src, Transferable data, int action) {
        // If we moved a card out of a slot, replace it with the placeholder label.
        if (action == MOVE && src.getParent() != null) {
            // Handle differently based on source type
            if (src.getParent() instanceof JComponent
                    && Boolean.TRUE.equals(((JComponent) src.getParent()).getClientProperty("isSlot"))) {
                // This is a slot - replace with placeholder
                JPanel slot = (JPanel) src.getParent();
                slot.removeAll();

                int idx = ((Container) slot.getParent()).getComponentZOrder(slot) + 1;
                JLabel placeholder = new JLabel(idx + ". Drag service here", SwingConstants.CENTER);
                slot.add(placeholder, BorderLayout.CENTER);
                slot.revalidate();
                slot.repaint();
            } else {
                // This is a card in the services list - remove it
                Container parent = src.getParent();
                if (parent != null) {
                    // Find and remove the strut that follows the card too
                    int cardIndex = -1;
                    for (int i = 0; i < parent.getComponentCount(); i++) {
                        if (parent.getComponent(i) == src) {
                            cardIndex = i;
                            break;
                        }
                    }
                    
                    parent.remove(src); // Remove the card
                    
                    // If we find a Box.Filler (strut) after the card, remove it too
                    if (cardIndex >= 0 && cardIndex < parent.getComponentCount()) {
                        Component nextComp = parent.getComponent(cardIndex);
                        if (nextComp instanceof Box.Filler) {
                            parent.remove(nextComp);
                        }
                    }
                    
                    parent.revalidate();
                    parent.repaint();
                }
            }
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  Incoming side – drop into a slot or back into the list                */
    /* ---------------------------------------------------------------------- */

    @Override
    public boolean canImport(TransferSupport supp) {
        if (!supp.isDrop()) return false;
        return supp.isDataFlavorSupported(SERVICE_FLAVOUR);
    }

    @Override
    public boolean importData(TransferSupport supp) {
        if (!canImport(supp)) return false;

        try {
            Service svc = (Service) supp.getTransferable()
                                        .getTransferData(SERVICE_FLAVOUR);

            JComponent target = (JComponent) supp.getComponent();
            Component sourceCard = supp.getComponent();   // not used but kept for clarity

            /* 1. Build or retrieve the visual card ------------------------ */
            ServiceCard card;
            if (supp.getComponent() instanceof ServiceCard) {
                ServiceCard existing = (ServiceCard) supp.getComponent();
                card = existing; // dragged back into the list
            } else {
                card = new ServiceCard(svc); // dropped into an empty slot
            }

            /* 2. Remove the card from its old parent ---------------------- */
            Container oldParent = card.getParent();
            if (oldParent != null) oldParent.remove(card);

            /* 3. Add to new parent ---------------------------------------- */
            if (Boolean.TRUE.equals(target.getClientProperty("isSlot"))) {
                target.removeAll();
                target.add(card, BorderLayout.CENTER);
            } else {               // dropped back into the scrolling list
                target.add(card);
            }

            target.revalidate();
            target.repaint();

            onImportSuccess.accept(svc);
            return true;

        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  Helper classes & callbacks                                            */
    /* ---------------------------------------------------------------------- */

    /** Simple Transferable wrapper for a Service object. */
    private static final class ServiceSelection implements Transferable {
        private final Service svc;

        private ServiceSelection(Service svc) { this.svc = svc; }

        @Override public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { SERVICE_FLAVOUR };
        }
        @Override public boolean isDataFlavorSupported(DataFlavor f) {
            return SERVICE_FLAVOUR.equals(f);
        }
        @Override public Object getTransferData(DataFlavor f)
                throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(f)) throw new UnsupportedFlavorException(f);
            return svc;
        }
    }

    public void setOnImportSuccess(java.util.function.Consumer<Service> cb) {
        this.onImportSuccess = cb == null ? s -> {} : cb;
    }

    public List<ServiceCard> releaseAllCards() {
        List<ServiceCard> all = List.copyOf(stash);
        stash.clear();
        return all;
    }
}