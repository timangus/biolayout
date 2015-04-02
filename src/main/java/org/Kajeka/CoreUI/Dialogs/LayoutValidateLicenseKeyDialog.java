package org.Kajeka.CoreUI.Dialogs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import static org.Kajeka.Environment.GlobalEnvironment.DISPLAY_PRODUCT_NAME;
import static org.Kajeka.Environment.GlobalEnvironment.IS_LICENSED;
import static org.Kajeka.Environment.GlobalEnvironment.LICENSE_EMAIL;
import static org.Kajeka.Environment.GlobalEnvironment.LICENSE_KEY;
import org.Kajeka.Utils.LicenseKeyValidator;

public final class LayoutValidateLicenseKeyDialog extends JDialog implements DocumentListener, ActionListener
{
    public LayoutValidateLicenseKeyDialog(JFrame frame)
    {
        super(frame, "License Validation");

        initComponents();
        initActions();
    }

    private AbstractAction validateLicenseKeyAction;
    public AbstractAction getValidateLicenseKeyAction()
    {
        return validateLicenseKeyAction;
    }

    private void initActions()
    {
        validateLicenseKeyAction = new AbstractAction("Validate License Key")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(IS_LICENSED)
                {
                    // Already licensed, ask if a different license should be entered
                    int dialogReturnValue = JOptionPane.showConfirmDialog(null, "The license is already valid. Do you wish to enter a new license?",
                            "License already valid", JOptionPane.YES_NO_OPTION);

                    if (dialogReturnValue == JOptionPane.NO_OPTION)
                    {
                        return;
                    }
                }

                setLocationRelativeTo(null);
                setVisible(true);
            }
        };
    }

    JTextField emailAddressField;
    JTextField licenseKeyField;
    JButton validateButton;

    private void initComponents()
    {
        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("Please enter your license details below."));

        JPanel detailsPanel = new JPanel(new GridBagLayout());

        GridBagConstraints cs = new GridBagConstraints();

        cs.insets = new Insets(5, 5, 5, 5);

        cs.gridx = 0;
        cs.gridy = 0;
        cs.anchor = GridBagConstraints.EAST;
        cs.gridwidth = 1;
        detailsPanel.add(new JLabel("Email Address:"), cs);

        cs.gridx = 1;
        cs.gridy = 0;
        cs.anchor = GridBagConstraints.WEST;
        cs.gridwidth = 2;
        emailAddressField = new JTextField(20);
        emailAddressField.getDocument().addDocumentListener(this);
        detailsPanel.add(emailAddressField, cs);

        cs.gridx = 0;
        cs.gridy = 1;
        cs.anchor = GridBagConstraints.EAST;
        cs.gridwidth = 1;
        detailsPanel.add(new JLabel("License Key:"), cs);

        cs.gridx = 1;
        cs.gridy = 1;
        cs.anchor = GridBagConstraints.WEST;
        cs.gridwidth = 2;
        licenseKeyField = new JTextField(8);
        licenseKeyField.getDocument().addDocumentListener(this);
        detailsPanel.add(licenseKeyField, cs);

        JPanel buttonPanel = new JPanel();

        validateButton = new JButton("Validate");
        validateButton.setEnabled(false);
        validateButton.addActionListener(this);
        buttonPanel.add(validateButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(infoPanel, BorderLayout.PAGE_START);
        getContentPane().add(detailsPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.PAGE_END);

        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void validateKey()
    {
        LicenseKeyValidator lkv = new LicenseKeyValidator(emailAddressField.getText());
        boolean valid = lkv.valid(licenseKeyField.getText());

        validateButton.setEnabled(valid);
    }

    @Override
    public void changedUpdate(DocumentEvent de)
    {
        validateKey();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        validateKey();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        validateKey();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == validateButton)
        {
            LICENSE_EMAIL.set(emailAddressField.getText());
            LICENSE_KEY.set(licenseKeyField.getText());

            this.setVisible(false);

            // Tell user to restart
            JOptionPane.showMessageDialog(null, "License accepted. Please restart " + DISPLAY_PRODUCT_NAME + " to complete the process.",
                            "License accepted", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
