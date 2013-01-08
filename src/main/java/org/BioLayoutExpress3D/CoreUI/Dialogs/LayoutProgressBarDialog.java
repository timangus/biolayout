package org.BioLayoutExpress3D.CoreUI.Dialogs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.BioLayoutExpress3D.CoreUI.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;

/**
*
* @author Leon Goldovsky, full refactoring by Thanos Theo, 2008-2009-2010
* @version 3.0.0.0
*
*/

public class LayoutProgressBarDialog extends JDialog
{
    /**
    *  Serial version UID variable for the LayoutProgressBar class.
    */
    public static final long serialVersionUID = 111222333444555693L;

    private JProgressBar progressBar = null;
    private JLabel label = null;
    private JLabel statusLabel = null;
    private LayoutFrame layoutFrame = null;
    private Timer timer = null;

    private volatile boolean reset = false;

    public LayoutProgressBarDialog(LayoutFrame layoutFrame)
    {
        super(layoutFrame, false);

        this.layoutFrame = layoutFrame;

        progressBar  = new JProgressBar(0, 1000);
        label = new JLabel();
        statusLabel  = new JLabel();

        label.setText(" " + VERSION);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString(" Ready");

        timer = new Timer( 50, new TimerListener() );

        initProgressDialog();
    }

    private void initProgressDialog()
    {
        progressBar.setPreferredSize( new Dimension(700, 50) );

        this.getContentPane().setLayout( new BorderLayout() );
        this.getContentPane().add(label, BorderLayout.NORTH);
        this.getContentPane().add(progressBar, BorderLayout.CENTER);
        this.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        this.getContentPane().setSize( new Dimension(700, 500) );
        this.setUndecorated(true);

        this.pack();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocation( ( SCREEN_DIMENSION.width - this.getWidth() ) / 2, ( SCREEN_DIMENSION.height - this.getHeight() ) / 2 );
        this.setVisible(false);
    }

    public void prepareProgressBar(int max, String title)
    {
        reset = false;
        statusLabel.setText(" " + title);
        progressBar.setMaximum(max);
    }

    public void startProgressBar()
    {
        timer.start();
        layoutFrame.block();
        progressBar.setValue(0);
        progressBar.setString("0%");
    }

    public void endProgressBar()
    {
        reset = false;
        statusLabel.setText(" Ready");
        progressBar.setString(" Done");
        progressBar.setValue( progressBar.getMaximum() );
        layoutFrame.unblock();
    }

    public void stopProgressBar()
    {
        reset = true;
        this.setVisible(false);
    }

    public synchronized void incrementProgress(int iteration)
    {
        progressBar.setValue(iteration);
        int percentage = (int)( ( ( (double)progressBar.getValue() ) / ( (double)progressBar.getMaximum() ) * 100) );
        progressBar.setString(percentage + "%");
    }

    public synchronized void incrementProgress()
    {
        progressBar.setValue(progressBar.getValue() + 1);
        int percentage = (int)( ( ( (double)progressBar.getValue() ) / ( (double)progressBar.getMaximum() ) * 100) );
        progressBar.setString(percentage + "%");
    }

    public synchronized void setText(String text)
    {
        statusLabel.setText(" " + text);
    }

    public synchronized void appendText(String text)
    {
        statusLabel.setText(statusLabel.getText() + text);
    }

    public synchronized void setIndeterminate(Boolean value)
    {
        progressBar.setIndeterminate(value);
    }

    public synchronized String getText()
    {
        return statusLabel.getText();
    }

    /**
     * The actionPerformed method in this class is called each time the Timer "goes off".
     */
    private class TimerListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent evt)
        {
            if (!reset)
            {
                setVisible(true);
                timer.stop();
            }
        }
    }


}