package org.volunteer.client.gui;

import org.volunteer.client.model.Service;

import javax.swing.*;

public class ServiceCard extends JPanel {
    private JTextField sigmaTextField;
    private JPanel panel1;

    public ServiceCard(Service service) {
        sigmaTextField.setText(service.serviceName());
        sigmaTextField.setEditable(false);
    }
}
