package org.biolayout.CoreUI.Dialogs;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import org.biolayout.StaticLibraries.*;
import static org.biolayout.Environment.GlobalEnvironment.*;
import static org.biolayout.DebugConsole.ConsoleOutput.*;

/**
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 */

public final class LayoutAlgorithmSelectionDialog extends JDialog implements ActionListener
{
    JRadioButton frRadioButton;
    JRadioButton fmmmRadioButton;
    JRadioButton circleRadioButton;

    public LayoutAlgorithmSelectionDialog(JFrame frame)
    {
        super(frame, "Layout Algorithm", true);
        this.setIconImages(ICON_IMAGES);

        initComponents();
    }

    private void initComponents()
    {
        this.setLayout(new BorderLayout());
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel algorithmPanel = new JPanel(true);
        algorithmPanel.setBorder( BorderFactory.createTitledBorder("Algorithm") );
        algorithmPanel.setLayout(new BoxLayout(algorithmPanel, BoxLayout.Y_AXIS));

        ButtonGroup layoutAlgorithmGroup = new ButtonGroup();

        frRadioButton = new JRadioButton("Fruchterman-Reingold");
        frRadioButton.setToolTipText("Fruchterman-Reingold");
        layoutAlgorithmGroup.add(frRadioButton);
        algorithmPanel.add(frRadioButton);

        fmmmRadioButton = new JRadioButton("FMMM");
        fmmmRadioButton.setToolTipText("FMMM");
        layoutAlgorithmGroup.add(fmmmRadioButton);
        algorithmPanel.add(fmmmRadioButton);

        circleRadioButton = new JRadioButton("Circle");
        circleRadioButton.setToolTipText("Circle");
        layoutAlgorithmGroup.add(circleRadioButton);
        algorithmPanel.add(circleRadioButton);

        fmmmRadioButton.setSelected(true);

        this.setLayout(new BorderLayout());
        this.add(algorithmPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(true);
        JButton okButton = new JButton("OK");
        okButton.setToolTipText("OK");
        okButton.addActionListener(this);
        buttonsPanel.add(okButton);

        this.add(buttonsPanel, BorderLayout.SOUTH);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if ( e.getActionCommand().equals("OK") )
            setVisible(false);
    }

    public GraphLayoutAlgorithm getGraphLayoutAlgorithm()
    {
        if (circleRadioButton.isSelected())
        {
            return GraphLayoutAlgorithm.CIRCLE;
        }
        else if (fmmmRadioButton.isSelected())
        {
            return GraphLayoutAlgorithm.FMMM;
        }
        else // default if (frRadioButton.isSelected())
        {
            return GraphLayoutAlgorithm.FRUCHTERMAN_REINGOLD;
        }
    }
}