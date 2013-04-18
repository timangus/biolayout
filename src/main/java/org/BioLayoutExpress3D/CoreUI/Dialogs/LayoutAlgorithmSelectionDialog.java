package org.BioLayoutExpress3D.CoreUI.Dialogs;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import org.BioLayoutExpress3D.StaticLibraries.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 */

public final class LayoutAlgorithmSelectionDialog extends JDialog implements ActionListener
{
    JRadioButton frRadioButton;
    JRadioButton fmmmRadioButton;

    public LayoutAlgorithmSelectionDialog(JFrame frame)
    {
        super(frame, "Select a Layout Algorithm", true);
        this.setIconImage(BIOLAYOUT_ICON_IMAGE);

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

        frRadioButton = new JRadioButton("Fruchterman-Rheingold");
        frRadioButton.setToolTipText("Fruchterman-Rheingold");
        layoutAlgorithmGroup.add(frRadioButton);
        algorithmPanel.add(frRadioButton);

        fmmmRadioButton = new JRadioButton("FMMM");
        fmmmRadioButton.setToolTipText("FMMM");
        layoutAlgorithmGroup.add(fmmmRadioButton);
        algorithmPanel.add(fmmmRadioButton);

        frRadioButton.setSelected(true);

        this.setLayout(new BorderLayout());
        this.add(algorithmPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(true);
        JButton okButton = new JButton("OK");
        okButton.setToolTipText("OK");
        okButton.addActionListener(this);
        buttonsPanel.add(okButton);

        this.add(buttonsPanel, BorderLayout.SOUTH);

        this.setSize(new Dimension(320, 120));
        this.setLocation((SCREEN_DIMENSION.width - this.getWidth()) / 2, (SCREEN_DIMENSION.height - this.getHeight()) / 2);
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
        if (fmmmRadioButton.isSelected())
        {
            return GraphLayoutAlgorithm.FMMM;
        }
        else // default if (frRadioButton.isSelected())
        {
            return GraphLayoutAlgorithm.FRUCHTERMAN_RHEINGOLD;
        }
    }
}