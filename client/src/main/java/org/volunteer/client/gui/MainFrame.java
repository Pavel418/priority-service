package org.volunteer.client.gui;

import org.volunteer.client.gui.dnd.ServiceTransferHandler;
import org.volunteer.client.model.Assignment;
import org.volunteer.client.model.AssignmentUpdateResponse;
import org.volunteer.client.model.Service;
import org.volunteer.client.network.NetworkListener;
import org.volunteer.client.network.RestClient;
import org.volunteer.client.network.WebSocketHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFrame extends JFrame implements NetworkListener {
    private JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JTextField textField1;
    private JTextArea textArea1;
    private JTextField noServiceIsAssignedTextField;
    private JButton submitButton;
    private JButton clearButton;
    private JPanel servicesList;
    private JPanel slot1;
    private JPanel slot2;
    private JPanel slot3;
    private JPanel slot4;
    private JPanel slot5;
    private JTextArea assignmentsText;
    private ServiceTransferHandler dndHandler = new ServiceTransferHandler();
    private RestClient restClient;
    private WebSocketHandler webSocketHandler;

    public MainFrame(List<Service> serviceList, RestClient restClient) {
        this.restClient = restClient;
        this.webSocketHandler = new WebSocketHandler(this);

        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List<JPanel> panels = Arrays.asList(slot1, slot2, slot3, slot4, slot5);
        for (JPanel panel : panels) {
            panel.setTransferHandler(dndHandler);
            panel.setPreferredSize(new Dimension(150, 60));
        }
//        ImageIcon icon = new ImageIcon("icon.png"); // Replace with your image path
//        setIconImage(icon.getImage());

        servicesList.setLayout(new BoxLayout(servicesList, BoxLayout.Y_AXIS));
        for (Service service : serviceList) {
            ServiceCard serviceCard = new ServiceCard(service);
            servicesList.add(serviceCard);
            serviceCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, serviceCard.getPreferredSize().height));
            makeDraggable(serviceCard);
        }

        servicesList.revalidate();
        servicesList.repaint();

        clearButton.addActionListener(e -> {
            List<JPanel> slots = Arrays.asList(slot1, slot2, slot3, slot4, slot5);

            for (JPanel slot : slots) {
                if (slot.getComponentCount() == 1) {
                    Component comp = slot.getComponent(0);
                    if (comp instanceof ServiceCard) {
                        ServiceCard card = (ServiceCard) comp;

                        // 1) Remove from slot
                        slot.remove(card);
                        slot.revalidate();
                        slot.repaint();

                        // 2) Put placeholder back
                        slot.add(new JLabel("Drag service here"), BorderLayout.CENTER);

                        // 3) Re‑make draggable and add back to the list
                        makeDraggable(card);
                        servicesList.add(card);
                    }
                }
            }

            servicesList.revalidate();
            servicesList.repaint();
        });

        submitButton.addActionListener(e -> {
            List<String> idsInPriority = new ArrayList<>();
            for (JPanel slot : panels) {
                // each slot has at most one ServiceCard
                if (slot.getComponentCount() == 1 &&
                        slot.getComponent(0) instanceof ServiceCard) {

                    ServiceCard card = (ServiceCard)slot.getComponent(0);
                    idsInPriority.add(card.getService().serviceId());
                }
            }
            // now idsInPriority is [slot1, slot2, ..., slot5] in descending priority
            try {
                restClient.submitPreferences(idsInPriority);
                JOptionPane.showMessageDialog(
                        this,
                        "Preferences submitted!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to submit: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        pack();
        setVisible(true);
    }

    private void makeDraggable(ServiceCard card) {
        card.setTransferHandler(dndHandler);
        // Remove any old adapters so we don’t stack them
        for (MouseListener ml : card.getMouseListeners()) {
            if (ml instanceof MouseAdapter) {
                card.removeMouseListener(ml);
            }
        }
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JComponent comp = (JComponent) e.getSource();
                comp.getTransferHandler().exportAsDrag(comp, e, TransferHandler.MOVE);
            }
        });
    }

    @Override
    public void onAssignmentUpdate(AssignmentUpdateResponse response) {
        System.out.println("call");
        // Always update Swing components on the EDT
        SwingUtilities.invokeLater(() -> {
            List<Assignment> assigns = response.assignments();
            if (assigns.isEmpty()) {
                noServiceIsAssignedTextField.setText(
                        "No assignments available");
                assignmentsText.setText("No assignments available");
            } else {
                noServiceIsAssignedTextField.setText("");
                StringBuilder sb = new StringBuilder();
                for (Assignment a : assigns) {
                    sb.append(a.volunteerName())
                            .append(" → ")
                            .append(a.assignedService().serviceName())
                            .append("\n");
                }
                assignmentsText.setText(sb.toString());
            }
        });
    }

    @Override
    public void onConnectionEstablished() {
        SwingUtilities.invokeLater(() ->
                statusBarMessage("Connected to server"));
    }

    @Override
    public void onConnectionFailed() {
        SwingUtilities.invokeLater(() ->
                statusBarMessage("Connection failed"));
    }

    @Override
    public void onConnectionClosed(int statusCode, String reason) {
        SwingUtilities.invokeLater(() ->
                statusBarMessage(
                        "Connection closed: " + statusCode + " / " + reason));
    }

    private void statusBarMessage(String msg) {
        // you can push this into a status bar or dialog
        System.out.println("[STATUS] " + msg);
    }
}
