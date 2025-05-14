package org.volunteer.client.gui;

import org.volunteer.client.gui.dnd.ServiceTransferHandler;
import org.volunteer.client.model.*;
import org.volunteer.client.network.NetworkListener;
import org.volunteer.client.network.RestClient;
import org.volunteer.client.network.WebSocketHandler;
import org.volunteer.client.session.SessionManager;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

/**
 * Three‑column Swing window built 100 % in code
 * – no IntelliJ GUI‑designer artefacts, hence no nulls.
 */
public final class MainFrame extends JFrame implements NetworkListener {

    /* ─────────── constant layout helpers ─────────── */
    private static final Dimension CARD_DIM = new Dimension(280, 60);
    private static final Dimension SLOT_DIM = new Dimension(300, 120);

    /* ─────────── Swing fields ─────────── */
    private final JPanel servicesBox   = new JPanel();
    private final List<JPanel> slots   = new ArrayList<>(5);
    private final JTextArea  svcDesc   = new JTextArea();
    private final JTextPane  assignOut = new JTextPane();

    private final JButton submitBtn = new JButton("Save");
    private final JButton clearBtn  = new JButton("Clear");

    /* ─────────── helpers ─────────── */
    private final List<Service> masterServices;
    private final ServiceTransferHandler dnd = new ServiceTransferHandler();
    private final RestClient  rest;
    @SuppressWarnings("unused")
    private final WebSocketHandler ws;          // kept to avoid GC

    /* ─────────── ctor ─────────── */
    public MainFrame(List<Service> services, RestClient restClient) {
        super("Volunteer Preference Selector");
        this.masterServices = List.copyOf(services);
        this.rest = restClient;
        this.ws   = new WebSocketHandler(this);  // auto‑connects

        buildUiSkeleton();
        populateServiceList(this.masterServices);
        wireActions();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /* ====================================================================== */
    /*  UI construction                                                       */
    /* ====================================================================== */
    private void buildUiSkeleton() {
        /* LEFT – service list + buttons */
        servicesBox.setLayout(new BoxLayout(servicesBox, BoxLayout.Y_AXIS));
        JScrollPane listScroller = new JScrollPane(servicesBox,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(clearBtn);
        btnPanel.add(submitBtn);

        JPanel left = new JPanel(new BorderLayout());
        left.add(listScroller, BorderLayout.CENTER);
        left.add(btnPanel,      BorderLayout.SOUTH);
        left.setPreferredSize(new Dimension(300, 600));

        /* CENTER – 5 drop slots */
        JPanel mid = new JPanel(new GridLayout(5,1,4,4));
        SLOT_DIM.setSize(300, 150); // ★ same height AOOP used
        for (int i = 0; i < 5; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBorder(BorderFactory.createDashedBorder(Color.GRAY));
            slot.setPreferredSize(SLOT_DIM);
            slot.putClientProperty("isSlot", Boolean.TRUE);
            slot.setTransferHandler(dnd);
            JLabel hint = new JLabel((i+1) + ". Drag service here",
                    SwingConstants.CENTER);
            slot.add(hint, BorderLayout.CENTER);
            slots.add(slot);
            mid.add(slot);
        }

        /* RIGHT – description & assignment */
        svcDesc.setWrapStyleWord(true);
        svcDesc.setLineWrap(true);
        svcDesc.setEditable(false);

        assignOut.setEditable(false);
        assignOut.setText("No assignment yet.");
        centerText(assignOut);

        // ★ Add a tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Description", new JScrollPane(svcDesc));
        tabs.addTab("My assignment", new JScrollPane(assignOut));

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tabs, // ★ use tabs instead of bare area
                new JPanel()); // placeholder if you want 2-way split
        rightSplit.setResizeWeight(1.0); // keep whole height for tabs

        /* ROOT split */
        JSplitPane root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                left,
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mid, rightSplit));
        root.setResizeWeight(0.33);
        root.setContinuousLayout(true);

        setContentPane(root);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void populateServiceList(List<Service> services) {
        services.forEach(svc -> {
            ServiceCard card = new ServiceCard(svc);
            makeDraggable(card);
            servicesBox.add(card);
            servicesBox.add(Box.createVerticalStrut(4));
        });
    }

    /* ====================================================================== */
    /*  Drag‑&‑drop helpers                                                   */
    /* ====================================================================== */
    private void makeDraggable(ServiceCard card) {
        card.setPreferredSize(CARD_DIM);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.setTransferHandler(dnd);

        MouseAdapter adapter = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                showDescription(card.getService());            // preview on click
            }
            @Override public void mouseDragged(MouseEvent e) { // start MOVE transfer
                card.getTransferHandler()
                    .exportAsDrag(card, e, TransferHandler.MOVE);
            }
        };
        card.addMouseListener(adapter);
        card.addMouseMotionListener(adapter);
    }
    

    /* ====================================================================== */
    /*  Button logic                                                          */
    /* ====================================================================== */
    private void wireActions() {

        dnd.setOnImportSuccess(this::showDescription);

        clearBtn.addActionListener(e -> {
            // 1) empty every slot
            slots.forEach(slot -> {
                slot.removeAll();
                slot.add(new JLabel((slots.indexOf(slot)+1)+". Drag service here",
                                    SwingConstants.CENTER), BorderLayout.CENTER);
            });

            // 2) put ALL services back in original order
            servicesBox.removeAll();
            populateServiceList(masterServices);

            // 3) wipe description + assignment preview
            showDescription(null);
            revalidate(); repaint();
        });

        submitBtn.addActionListener(e -> {
            List<String> ordered = slots.stream()
                    .map(p -> {
                        if (p.getComponentCount() == 1 && p.getComponent(0) instanceof ServiceCard) {
                            ServiceCard sc = (ServiceCard) p.getComponent(0);
                            return sc.getService().serviceId();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
            rest.submitPreferences(ordered)
                .thenAccept(r -> JOptionPane.showMessageDialog(
                        this, "Preferences saved!", "Done", JOptionPane.INFORMATION_MESSAGE))
                .exceptionally(ex -> {
                    JOptionPane.showMessageDialog(
                            this, "Failed: "+ex.getCause().getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                });
        });
    }

    /* ====================================================================== */
    /*  Helper UI utils                                                       */
    /* ====================================================================== */
    private static void centerText(JTextPane pane) {
        StyledDocument doc = pane.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
    }
    private void showDescription(Service svc) {
        if (svc == null) { svcDesc.setText(""); return; }
        svcDesc.setText(svc.serviceName() + "\n\n" + svc.serviceDescription());
        svcDesc.setCaretPosition(0);
    }

    /* ====================================================================== */
    /*  Web‑socket callbacks                                                  */
    /* ====================================================================== */
    @Override public void onAssignmentUpdate(AssignmentUpdateResponse r) {
        Assignment mine = r.assignments().stream()
                .filter(a -> a.volunteerId().equals(SessionManager.getClientId()))
                .findFirst().orElse(null);

        SwingUtilities.invokeLater(() -> {
            if (mine == null) {
                assignOut.setText("No assignment yet.");
            } else {
                Service s = mine.assignedService();
                assignOut.setText("You are assigned to:\n\n"
                        + s.serviceName()+"\n\n"+s.serviceDescription());
            }
            assignOut.setCaretPosition(0);
            centerText(assignOut);
        });
    }
    @Override public void onConnectionEstablished() { System.out.println("[WS] connected"); }
    @Override public void onConnectionFailed()      { /* show dialog if you like */ }
    @Override public void onConnectionClosed(int c, String r) { /* ignore */ }
}
