package org.volunteer.client.gui;

import org.volunteer.client.model.Service;

import javax.swing.*;
import java.awt.*;

public class ServiceCard extends JPanel {
    JPanel panel1;
    Service service;
    public ServiceCard(Service service) {
        this.service = service;
        JTextField textField1 = new JTextField(service.serviceName());
        textField1.setEditable(false);
        textField1.setColumns(20);

        add(textField1, BorderLayout.CENTER);

        setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    public Service getService() {
        return service;
    }
}

