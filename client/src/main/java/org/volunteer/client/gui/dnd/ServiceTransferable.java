package org.volunteer.client.gui.dnd;

import org.volunteer.client.gui.ServiceCard;

import java.awt.*;
import java.awt.datatransfer.*;

public class ServiceTransferable implements Transferable {
    public static final DataFlavor FLAVOR =
            new DataFlavor(ServiceCard.class, "ServiceCard");

    private final ServiceCard card;

    public ServiceTransferable(ServiceCard card) {
        this.card = card;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{ FLAVOR };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);
        return card;
    }
}
