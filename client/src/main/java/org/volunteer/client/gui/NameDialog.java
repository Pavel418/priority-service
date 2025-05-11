/**
 * Modal dialog for capturing and storing username through a GUI form.
 * Manages user session initialization and application termination flow.
 */
package org.volunteer.client.gui;

import org.volunteer.client.session.SessionManager;
import javax.swing.*;
import java.awt.event.*;

/**
 * Username entry dialog with confirmation controls. Provides:
 * <ul>
 *   <li>Text input field for name entry</li>
 *   <li>OK/Cancel action buttons</li>
 *   <li>Session persistence on confirmation</li>
 *   <li>Application exit on cancellation</li>
 * </ul>
 *
 * <p>The dialog enforces modal interaction and implements multiple cancellation
 * pathways (button, window close, ESC key).
 *
 * @see SessionManager
 * @see JDialog
 */
public class NameDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;

    /**
     * Constructs the name entry dialog and initializes UI event handling.
     */
    public NameDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(
                e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * Handles OK button confirmation:
     * <ol>
     *   <li>Stores entered name in session</li>
     *   <li>Disposes the dialog</li>
     * </ol>
     *
     * @implNote Blank names are accepted but should be handled by validation
     */
    private void onOK() {
        SessionManager.setUserName(textField1.getText());
        dispose();
    }

    /**
     * Handles cancellation through all pathways:
     * <ol>
     *   <li>Disposes the dialog</li>
     *   <li>Initiates full application shutdown</li>
     * </ol>
     */
    private void onCancel() {
        dispose();
        System.exit(0);
    }
}