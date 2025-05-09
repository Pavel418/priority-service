package org.volunteer.client.gui;

import org.volunteer.client.model.Service;

import javax.swing.*;
import java.util.List;

public class MainFrame extends JFrame {
    private JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JTextField textField1;
    private JTextArea textArea1;
    private JTextField noServiceIsAssignedTextField;
    private JButton submitButton;
    private JButton clearButton;
    private JTextPane dropTop5PreferedTextPane;
    private JScrollPane scrollPane1;
    private JTextField sigmaTextField;
    private JPanel servicesPanel;

    public MainFrame(List<Service> serviceList) {
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        ImageIcon icon = new ImageIcon("icon.png"); // Replace with your image path
//        setIconImage(icon.getImage());

        pack();
        setVisible(true);
    }
}
