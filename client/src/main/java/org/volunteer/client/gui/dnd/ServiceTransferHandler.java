package org.volunteer.client.gui.dnd;

import org.volunteer.client.gui.ServiceCard;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;

public class ServiceTransferHandler extends TransferHandler {
    public static final DataFlavor FLAVOR =
            new DataFlavor(ServiceCard.class, "ServiceCard");

    @Override
    public int getSourceActions(JComponent c) {
        // we only support MOVE
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        // stash the original parent into the card itself
        ServiceCard card = (ServiceCard) c;
        Container source = c.getParent();
        card.putClientProperty("dndSourceContainer", source);
        return new ServiceTransferable(card);
    }

    @Override
    public boolean canImport(TransferSupport support) {
        // only accept our flavor into a JPanel
        return support.isDataFlavorSupported(FLAVOR)
                && (support.getComponent() instanceof JPanel);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;

        try {
            // pull out the card
            ServiceCard dropped = (ServiceCard)
                    support.getTransferable()
                            .getTransferData(FLAVOR);

            // insert into the drop‐slot
            JPanel target = (JPanel) support.getComponent();
            target.removeAll();
            target.add(dropped, BorderLayout.CENTER);
            target.revalidate();
            target.repaint();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        // this is called *after* importData returns true for a MOVE
        if (action == MOVE) {
            ServiceCard card = (ServiceCard) source;
            Container oldParent = (Container)
                    card.getClientProperty("dndSourceContainer");

            if (oldParent != null) {
                oldParent.remove(card);
                oldParent.revalidate();
                oldParent.repaint();
            }
        }
    }
}
