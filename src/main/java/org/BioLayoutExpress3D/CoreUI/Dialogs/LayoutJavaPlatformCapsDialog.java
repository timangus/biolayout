package org.BioLayoutExpress3D.CoreUI.Dialogs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;

/**
*
*  A class designed to report the Java Platform Capabilities in a dialog.
*
* @author Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*/

public final class LayoutJavaPlatformCapsDialog extends JDialog implements ActionListener
{
    /**
    *  Serial version UID variable for the LayoutJavaPlatformCapsDialog class.
    */
    public static final long serialVersionUID = 111222333444555696L;

    private AbstractAction javaPlatformCapsAction = null;
    private JButton okButton = null;

    public LayoutJavaPlatformCapsDialog(JFrame jframe)
    {
        super(jframe, "Java Platform Capabilities", true);

        initActions();
        initComponents();
    }

    private void initActions()
    {
        javaPlatformCapsAction = new AbstractAction("Java Platform Caps")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555697L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                setLocation( ( SCREEN_DIMENSION.width - getWidth() ) / 2, ( SCREEN_DIMENSION.height - getHeight() ) / 2 );
                setVisible(true);
            }
        };
    }

    private void initComponents()
    {
        JPanel topPanel = new JPanel(true);
        topPanel.setLayout( new BorderLayout() );

        JTextArea textArea = new JTextArea();
        textArea.setFont( Font.decode("Monospaced") );
        setJavaPlatformCaps(textArea);
        textArea.setEditable(false);
        textArea.setCaretPosition(0); // so as to have the vertical scrollbar reset to position 0
        JScrollPane scrollPane = new JScrollPane(textArea);

        okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setToolTipText("OK");

        scrollPane.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Java Platform Capabilities") );
        topPanel.add(scrollPane, BorderLayout.CENTER);
        topPanel.add(okButton, BorderLayout.SOUTH);

        this.getContentPane().add(topPanel);
        this.setResizable(false);
        this.pack();
        this.setSize(550, 700);
        this.setLocation( ( SCREEN_DIMENSION.width - this.getWidth() ) / 2, ( SCREEN_DIMENSION.height - this.getHeight() ) / 2 );
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void appendPropertiesToTextArea(Properties properties, JTextArea textArea)
    {
        Set<String> keySet = properties.stringPropertyNames();
        ArrayList keyList = new ArrayList(keySet);
        Collections.sort(keyList);
        int longestKey = 0;

        for (Object keyObject : keyList)
        {
            String key = (String)keyObject;
            if (key.length() > longestKey)
                longestKey = key.length();
        }

        String pathSeparator = System.getProperty("path.separator");

        for (Object keyObject : keyList)
        {
            String key = (String)keyObject;
            int keyLength = key.length();
            int padding = 1 + longestKey - keyLength;

            String value = properties.getProperty(key);
            value = value.replace("\n", "\\n");
            value = value.replace("\r", "\\r");
            value = value.replace("\t", "\\t");

            textArea.append(key + ":");
            for( int i = 0; i < padding; i++)
                textArea.append(" ");

            if (key.matches(".*(path|dirs)$"))
            {
                // Property is probably a list of paths, so split it up
                String[] paths = value.split(pathSeparator);
                textArea.append(paths[0] + "\n");

                padding = 2 + longestKey;
                for( int i = 1; i < paths.length; i++)
                {
                    for( int j = 0; j < padding; j++)
                        textArea.append(" ");

                    textArea.append(paths[i] + "\n");
                }
            }
            else
                textArea.append(value + "\n");
        }
    }

    private void setJavaPlatformCaps(JTextArea textArea)
    {
        int ONE_MB = 1 << 20; // (1024 * 1024);

        Properties properties = new Properties();
        properties.put("Detected JVM bitness", (IS_64BIT ? "64 bit" : "32 bit"));
        properties.put("Number or cores", RUNTIME.availableProcessors());
        properties.put("Maximum JVM memory usage", (RUNTIME.maxMemory() / ONE_MB) + " MB");
        properties.put("Total JVM memory usage", (RUNTIME.totalMemory() / ONE_MB) + " MB");

        properties.putAll(System.getProperties());
        appendPropertiesToTextArea(properties, textArea);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if ( e.getActionCommand().equals("OK") )
            setVisible(false);
    }

    public AbstractAction getJavaPlatformCapsAction()
    {
        return javaPlatformCapsAction;
    }


}