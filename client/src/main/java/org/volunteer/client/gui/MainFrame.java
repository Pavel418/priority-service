package org.volunteer.client.gui;

import org.volunteer.client.gui.dnd.ServiceTransferHandler;
import org.volunteer.client.model.Assignment;
import org.volunteer.client.model.AssignmentUpdateResponse;
import org.volunteer.client.model.Service;
import org.volunteer.client.network.NetworkListener;
import org.volunteer.client.network.RestClient;
import org.volunteer.client.network.WebSocketHandler;
import org.volunteer.client.session.SessionManager;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFrame extends JFrame implements NetworkListener {
    private JPanel panel1;
    private JButton submitButton;
    private JButton clearButton;
    private JPanel servicesList;
    private JPanel slot1;
    private JPanel slot2;
    private JPanel slot3;
    private JPanel slot4;
    private JPanel slot5;
    private List<JPanel> slots;
    private JTextPane assignmentsText;
    private JSplitPane rightSplitPane;
    private JSplitPane mainSplitPane;
    private JPanel leftPanel;
    private JPanel centerPanel;
    private JPanel btnPanel;
    private JPanel rightPanel;
    private JTextArea description;
    private final ServiceTransferHandler dndHandler = new ServiceTransferHandler();
    private final RestClient restClient;
    private final WebSocketHandler webSocketHandler;

    public MainFrame(List<Service> serviceList, RestClient restClient) {
        this.restClient = restClient;
        this.webSocketHandler = new WebSocketHandler(this);
        this.slots = List.of(slot1, slot2, slot3, slot4, slot5);

        initUIComponents();
        initAssignmentsPane();
        initSlotPanels();
        setupServiceList(serviceList);
        registerHandlers();

        pack();
        setVisible(true);
    }

    private void initUIComponents() {
        // Split pane configuration
        mainSplitPane.setResizeWeight(0.35);
        mainSplitPane.setContinuousLayout(true);
        rightSplitPane.setResizeWeight(0.6);
        rightSplitPane.setContinuousLayout(true);

        // Panel sizing
        leftPanel.setMinimumSize(new Dimension(300, 800));
        leftPanel.setPreferredSize(new Dimension(300, 800));
        centerPanel.setMinimumSize(new Dimension(300, 0));
        centerPanel.setPreferredSize(new Dimension(350, 600));
        btnPanel.setPreferredSize(new Dimension(0, 40));
        rightPanel.setMinimumSize(new Dimension(300, 800));
        rightPanel.setPreferredSize(new Dimension(300, 800));

        // Text area configuration
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);

        // Frame setup
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void registerHandlers() {
        clearButton.addActionListener(e -> clearSlots());
        submitButton.addActionListener(e -> submitPreferences());
    }

    private void clearSlots() {
        for (int i = 0; i < slots.size(); i++) {
            JPanel slot = slots.get(i);
            if (slot.getComponentCount() == 1 && slot.getComponent(0) instanceof ServiceCard card) {
                slot.removeAll();
                // restore placeholder
                configureSlot(slot, i + 1);
                // return card to list
                makeDraggable(card);
                servicesList.add(card);
            }
        }
        servicesList.revalidate();
        servicesList.repaint();
    }

    private void submitPreferences() {
        List<String> idsInPriority = new ArrayList<>();
        for (JPanel slot : slots) {
            if (slot.getComponentCount() == 1 && slot.getComponent(0) instanceof ServiceCard) {
                idsInPriority.add(((ServiceCard) slot.getComponent(0)).getService().serviceId());
            }
        }
        try {
            restClient.submitPreferences(idsInPriority);
            JOptionPane.showMessageDialog(this, "Preferences submitted!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to submit: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeDraggable(ServiceCard card) {
        card.setTransferHandler(dndHandler);

        // remove any old MouseAdapters (both drag and click) so we don't stack them
        for (MouseListener ml : card.getMouseListeners()) {
            if (ml instanceof MouseAdapter) {
                card.removeMouseListener(ml);
            }
        }

        // add one MouseAdapter that handles both drag and click
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JComponent comp = (JComponent) e.getSource();
                showServiceDescription(card.getService());
                comp.getTransferHandler().exportAsDrag(comp, e, TransferHandler.MOVE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // whenever the user clicks the card, show its description
                showServiceDescription(card.getService());
            }
        });
    }

    @Override
    public void onAssignmentUpdate(AssignmentUpdateResponse response) {
        SwingUtilities.invokeLater(() -> {
            String myId = SessionManager.getClientId();
            List<Assignment> assigns = response.assignments();

            // Find only this client’s assignment
            Assignment myAssign = assigns.stream()
                    .filter(a -> myId.equals(a.volunteerId()))
                    .findFirst()
                    .orElse(null);

            if (myAssign == null) {
                assignmentsText.setText("No assignment available.");
            } else {
                Service svc = myAssign.assignedService();
                String sb = "Your assigned service is:\n" +
                        svc.serviceName() +
                        "\n\n" +
                        "Service description:\n" +
                        svc.serviceDescription();
                assignmentsText.setText(sb);
                assignmentsText.setCaretPosition(0);

            }
            StyledDocument doc = assignmentsText.getStyledDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);

            doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
        });
    }

    /**
     * Fill the right‐hand text area with the full service description.
     */
    private void showServiceDescription(Service svc) {
        if (svc == null) {
            description.setText("");
            return;
        }
        // Pull the serviceDescription field out of your model:
        String fullDesc = svc.serviceDescription();
        // Optionally format with header:
        String header = svc.serviceName() + "\n"
                + "-------------------------\n\n";
        description.setText(header + fullDesc);
        // Scroll back to top:
        description.setCaretPosition(0);
    }

    private void initAssignmentsPane() {
        // 1) Set the initial text
        String initial = "No assignment available.";
        assignmentsText.setText(initial);

        // 2) Center horizontally
        StyledDocument doc = assignmentsText.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, initial.length(), attrs, false);
    }

    private void setupServiceList(List<Service> serviceList) {
        servicesList.setLayout(new BoxLayout(servicesList, BoxLayout.Y_AXIS));
        for (Service service : serviceList) {
            ServiceCard card = new ServiceCard(service);
            servicesList.add(card);
            card.setMinimumSize(new Dimension(300, 60));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            makeDraggable(card);
        }
        servicesList.revalidate();
        servicesList.repaint();

        dndHandler.setOnImportSuccess(this::showServiceDescription);
    }

    /**
     * Initialize and configure the five drop slots.
     */
    private void initSlotPanels() {
        List<JPanel> slots = Arrays.asList(slot1, slot2, slot3, slot4, slot5);
        for (int i = 0; i < slots.size(); i++) {
            configureSlot(slots.get(i), i + 1);
        }
    }

    /**
     * Apply common configuration to a slot panel.
     */
    private void configureSlot(JPanel panel, int index) {
        panel.putClientProperty("isSlot", Boolean.TRUE);
        panel.setLayout(new BorderLayout());
        panel.setTransferHandler(dndHandler);

        JLabel label = new JLabel(index + ". Drag service here");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);

        Dimension size = new Dimension(300, 150);
        panel.setMinimumSize(size);
        panel.setPreferredSize(size);
    }

    @Override
    public void onConnectionEstablished() {
        SwingUtilities.invokeLater(() ->
                System.out.println("[STATUS] Connection established!"));
    }

    @Override
    public void onConnectionFailed() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not connect to the server.\nPlease try again later and relaunch the application.",
                    "Connection Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1); // Exit the application
        });
    }

    @Override
    public void onConnectionClosed(int statusCode, String reason) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Connection to the server was closed.\nPlease try again later and relaunch the application.",
                    "Connection Closed",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1); // Exit the application
        });
    }
}
