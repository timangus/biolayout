package org.BioLayoutExpress3D.CoreUI.Dialogs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
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

        JTextArea textArea1 = new JTextArea();
        textArea1.setFont( this.getFont() );
        setJavaPlatformCaps(textArea1);
        textArea1.setEditable(false);
        textArea1.setCaretPosition(0); // so as to have the vertical scrollbar resetted to position 0
        JScrollPane scrollPane1 = new JScrollPane(textArea1);

        JTextArea textArea2 = new JTextArea();
        textArea2.setFont( this.getFont() );
        setDetailedJavaPlatformCaps(textArea2);
        textArea2.setEditable(false);
        textArea2.setCaretPosition(0); // so as to have the vertical scrollbar resetted to position 0
        JScrollPane scrollPane2 = new JScrollPane(textArea2);

        okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setToolTipText("OK");

        scrollPane1.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Java Platform Capabilities") );
        topPanel.add(scrollPane1, BorderLayout.NORTH);
        scrollPane2.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Detailed Java Platform Capabilities") );
        topPanel.add(scrollPane2, BorderLayout.CENTER);
        topPanel.add(okButton, BorderLayout.SOUTH);

        this.getContentPane().add(topPanel);
        this.setResizable(false);
        this.pack();
        this.setSize(550, 700);
        this.setLocation( ( SCREEN_DIMENSION.width - this.getWidth() ) / 2, ( SCREEN_DIMENSION.height - this.getHeight() ) / 2 );
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void setJavaPlatformCaps(JTextArea textArea)
    {
        int ONE_MB = 1 << 20; // (1024 * 1024);

        textArea.append("\nOS Name:\t\t" + System.getProperty("os.name") + "\n");
        textArea.append("OS Version:\t\t" + System.getProperty("os.version") + "\n");
        textArea.append("OS Architecture:\t" + ( (IS_WIN) ? "\t" :"" ) + System.getProperty("os.arch") + "\n");
        textArea.append("JVM bitness:\t\t" + (IS_64BIT ? "64 bit" : "32 bit") + "\n");
        textArea.append("\n");
        textArea.append("Java Runtime Name:\t" + System.getProperty("java.runtime.name") + "\n");
        textArea.append("Java Runtime Version:\t" + System.getProperty("java.runtime.version") + "\n");
        textArea.append("Java Version:\t\t" + System.getProperty("java.version") + "\n");
        textArea.append("JVM Version:\t\t" + System.getProperty("java.vm.version") + "\n");        
        textArea.append("JVM Name:\t\t" + System.getProperty("java.vm.name") + "\n");
        textArea.append("JVM Vendor:\t\t" + System.getProperty("java.vm.vendor") + "\n");        
        textArea.append("Available processor cores:\t" + RUNTIME.availableProcessors() + "\n");
        textArea.append("Max JVM memory usage:\t" + (RUNTIME.maxMemory() / ONE_MB) + " MB\n");
        textArea.append("Total JVM memory usage:\t" + (RUNTIME.totalMemory() / ONE_MB) + " MB\n");
    }

    private void setDetailedJavaPlatformCaps(JTextArea textArea)
    {
        textArea.append("\nuser.name:\t\t" + System.getProperty("user.name") + "\n");
        String property = System.getProperty("java.library.path");
        if ( property.startsWith("\\") )
            property = property.substring( 1, property.length() );
        textArea.append("java.library.path:\t" + property + "\n");
        textArea.append("java.io.tmpdir:\t\t" + System.getProperty("java.io.tmpdir") + "\n");
        textArea.append("\n");
        textArea.append("sun.boot.library.path:\t" + System.getProperty("sun.boot.library.path") + "\n");
        textArea.append("file.encoding.pkg:\t" + System.getProperty("file.encoding.pkg") + "\n");
        textArea.append("user.country:\t\t" + System.getProperty("user.country") + "\n");
        textArea.append("sun.java.launcher:\t" + System.getProperty("sun.java.launcher") + "\n");
        textArea.append("user.dir:\t\t" + System.getProperty("user.dir") + "\n");
        textArea.append("java.awt.graphicsenv:\t" + System.getProperty("java.awt.graphicsenv") + "\n");
        textArea.append("java.endorsed.dirs:\t" + System.getProperty("java.endorsed.dirs") + "\n");
        textArea.append("sun.java2d.opengl:\t" + System.getProperty("sun.java2d.opengl") + "\n");
        textArea.append("sun.java2d.noddraw:\t" + System.getProperty("sun.java2d.noddraw") + "\n");
        textArea.append("sun.jnu.encoding:\t" + System.getProperty("sun.jnu.encoding") + "\n");
        textArea.append("java.class.version:\t" + System.getProperty("java.class.version") + "\n");
        textArea.append("sun.management.compiler:\t" + System.getProperty("sun.management.compiler") + "\n");
        textArea.append("user.home:\t\t" + System.getProperty("user.home") + "\n");
        textArea.append("user.timezone:\t\t" + System.getProperty("user.timezone") + "\n");
        textArea.append("java.awt.printerjob:\t" + System.getProperty("java.awt.printerjob") + "\n");
        textArea.append("file.encoding:\t\t" + System.getProperty("file.encoding") + "\n");
        textArea.append("java.specification.version:\t" + System.getProperty("java.specification.version") + "\n");
        textArea.append("java.class.path:\t" + ( (IS_WIN) ? "\t" :"" ) + System.getProperty("java.class.path") + "\n");
        textArea.append("java.vm.specification.version:\t" + System.getProperty("java.vm.specification.version") + "\n");
        textArea.append("java.home:\t\t" + System.getProperty("java.home") + "\n");
        textArea.append("user.language:\t\t" + System.getProperty("user.language") + "\n");
        textArea.append("awt.toolkit:\t\t" + System.getProperty("awt.toolkit") + "\n");
        textArea.append("java.vm.info:\t\t" + System.getProperty("java.vm.info") + "\n");
        String[] propertyStringArray = System.getProperty("java.ext.dirs").split(";");
        textArea.append("java.ext.dirs:\t\t" + propertyStringArray[0] + "\n");
        for (int i = 1; i < propertyStringArray.length; i++)
            textArea.append("\t\t" + propertyStringArray[i] + "\n");
        propertyStringArray = System.getProperty("sun.boot.class.path").split(";");
        textArea.append("sun.boot.class.path:\t" + propertyStringArray[0] + "\n");
        for (int i = 1; i < propertyStringArray.length; i++)
            textArea.append("\t\t" + propertyStringArray[i] + "\n");
        textArea.append("sun.cpu.endian:\t" + System.getProperty("sun.cpu.endian") + "\n");
        textArea.append("sun.io.unicode.encoding:\t" + System.getProperty("sun.io.unicode.encoding") + "\n");
        textArea.append("sun.desktop:\t\t" + System.getProperty("sun.desktop") + "\n");
        propertyStringArray = System.getProperty("sun.cpu.isalist").split("\\s|\\+");
        textArea.append("sun.cpu.isalist:\t\t" + propertyStringArray[0] + "\n");
        for (int i = 1; i < propertyStringArray.length; i++)
            textArea.append("\t\t" + propertyStringArray[i] + "\n");
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