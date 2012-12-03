package org.BioLayoutExpress3D.Expression.Dialogs;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.BioLayoutExpress3D.Utils.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.Expression.ExpressionEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
*
* @author Anton Enright, full refactoring by Thanos Theo, 2008-2009
* @version 3.0.0.0
* 
*/

public final class ExpressionLoaderDialog extends JDialog implements ActionListener
{ 
    /** 
    *  Serial version UID variable for the ExpressionLoaderDialog class.
    */        
    public static final long serialVersionUID = 111222333444555706L;     

    private static final int NUMBER_OF_ROWS_TO_DISPLAY = 6;

    private FloatNumberField correlationField = null;
    private JComboBox dataStart = null;
    private JComboBox correlationMetric = null;
    private JEditorPane textArea = null;
    private File expressionFile = null;

    private int startColumn = 1;
    private boolean proceed = false;

    private AbstractAction okAction = null;
    private AbstractAction cancelAction = null;

    public ExpressionLoaderDialog(JFrame frame, File expressionFile)
    {
        super(frame, "Load Expression Data", true);
        
        this.expressionFile = expressionFile;
        
        initActions(frame);
        initComponents();
        createDialogElements( guessStart(expressionFile) );

        this.pack();
        this.setSize(700, 300);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocation( ( SCREEN_DIMENSION.width - this.getWidth() ) / 2, ( SCREEN_DIMENSION.height - this.getHeight() ) / 2 );
    }

    private void initComponents()
    {
        JPanel topPanel = new JPanel(true);
        JPanel centrePanel = new JPanel(true);
        JPanel downPanel = new JPanel(true);

        Container container = this.getContentPane();
        container.setLayout( new BorderLayout() );

        correlationMetric = new JComboBox();
        String correlationType = "";
        CorrelationTypes[] allCorrelationTypes = CorrelationTypes.values();
        for (int i = 0; i < allCorrelationTypes.length; i++)
        {
            correlationType = allCorrelationTypes[i].toString().toLowerCase();
            correlationType = Character.toUpperCase( correlationType.charAt(0) ) + correlationType.substring(1);
            correlationMetric.addItem(correlationType);

        }
        correlationMetric.setSelectedIndex(0);
        correlationMetric.setToolTipText("Corr. Metric");

        correlationField = new FloatNumberField(0, 5);
        correlationField.setDocument( new TextFieldFilter(TextFieldFilter.FLOAT) );
        correlationField.setValue(STORED_CORRELATION_THRESHOLD);
        correlationField.setToolTipText("Min Correlation");
        dataStart = new JComboBox();
        dataStart.addActionListener(this);
        dataStart.setToolTipText("Data Columns Start");
        
        topPanel.add(correlationField);
        topPanel.add( new JLabel("Min Correlation    ") );
        topPanel.add(correlationMetric);
        topPanel.add( new JLabel("Corr. Metric    ") );
        topPanel.add(dataStart);
        topPanel.add( new JLabel("Data Columns Start    ") );

        centrePanel.setLayout(new BorderLayout());
        textArea = new JEditorPane("text/html", "");
        textArea.setToolTipText("Expression Data");

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize( new Dimension(500, 200) );
        centrePanel.add(scrollPane, BorderLayout.CENTER);

        JButton okButton = new JButton(okAction);
        okButton.setToolTipText("OK");
        JButton cancelButton = new JButton(cancelAction);
        cancelButton.setToolTipText("Cancel");
        downPanel.add(okButton);
        downPanel.add(cancelButton);

        container.add(topPanel, BorderLayout.NORTH);
        container.add(centrePanel, BorderLayout.CENTER);
        container.add(downPanel, BorderLayout.SOUTH);
    }

    private void initActions(final JFrame frame)
    {
        okAction = new AbstractAction("OK")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555707L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ( correlationField.isEmpty() )
                {
                    JOptionPane.showMessageDialog(frame, "A Correlation value must be given.\nPlease try inserting a Correlation value (default 0.7).", "Correlation value not given!", JOptionPane.INFORMATION_MESSAGE);
                    correlationField.setValue(DEFAULT_STORED_CORRELATION_THRESHOLD);
                    return;
                }
                else if ( ( STORED_CORRELATION_THRESHOLD = correlationField.getValue() ) <= 0.0f )
                {
                    JOptionPane.showMessageDialog(frame, "The Correlation value cannot be 0.0.\nPlease try inserting a Correlation value larger than 0.0.", "Correlation value of 0.0!", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                else if ( ( STORED_CORRELATION_THRESHOLD = correlationField.getValue() ) >= 1.0f )
                {
                    JOptionPane.showMessageDialog(frame, "The Correlation value cannot be equal or bigger than 1.0.\nPlease try inserting a smaller Correlation value.", "Correlation value too large!", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                CURRENT_METRIC = CorrelationTypes.values()[correlationMetric.getSelectedIndex()];
                startColumn = dataStart.getSelectedIndex() + 2;
                proceed = true;
                setVisible(false);
            }
        };

        cancelAction = new AbstractAction("Cancel")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555708L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                proceed = false;
                setVisible(false);
            }
        };
    }

    private void createDialogElements(int dataStart) 
    {
        try 
        {
            BufferedReader reader = new BufferedReader( new FileReader(expressionFile) );
            String line = "";
            String options = null;
            int linecount = 0;
            int columns = 0;

            StringBuilder pageText = new StringBuilder();
            pageText.append("<HTML><TABLE BORDER=\"1\">\n");

            String[] split = null;
            while ( ( (line = reader.readLine() ) != null ) && (linecount <= NUMBER_OF_ROWS_TO_DISPLAY) )
            {
                split = line.split("\t");
                columns = split.length;

                // add the 'columns' line
                if (linecount == 0)
                {
                    pageText.append("<TR>");
                    for (int i = 0; i < columns; i++)
                    {
                        options = "BGCOLOR=\"#FFFFFF\"";
                        pageText.append("<TD NOWRAP ").append(options).append(">" + "Column ").append(Integer.toString(i + 1)).append("</TD>");
                    }
                    pageText.append("</TR>\n");
                }

                pageText.append("<TR>");
                for (int i = 0; i < columns; i++)
                {
                    // to get rid of the names being in 'name' format
                    if ( split[i].startsWith("\"") ) 
                        split[i] = split[i].substring( 1, split[i].length() );
                    
                    if ( split[i].endsWith("\"") ) 
                        split[i] = split[i].substring( 0, split[i].length() - 1 );

                    if (linecount == 0)
                    {
                        options = "BGCOLOR=\"#6699FF\"";
                    }
                    else
                    {
                        options = "BGCOLOR=\"#CCFFCC\"";

                        if (i == 0)
                            options = "BGCOLOR=\"#FFCCCC\"";

                        if (i >= dataStart)
                            options = "BGCOLOR=\"#CCCCFF\"";
                    }

                    pageText.append("<TD NOWRAP ").append(options).append(">").append(split[i]).append("</TD>");
                }                
                pageText.append("</TR>\n");
                linecount++;
            }

            pageText.append("</TABLE></HTML>\n");

            textArea.setText( pageText.toString() );
            reader.close();
        } 
        catch (IOException ioe)
        {
           if (DEBUG_BUILD) println("IOException in ExpressionLoaderDialog.createDialogElements():\n" + ioe.getMessage());
        }
    }

    private int guessStart(File file)
    {
        int maxColumns = 0;
        int totalColumns = 0;

        try
        {
            BufferedReader reader = new BufferedReader( new FileReader(file) );
            String line = "";
            String[] split = null;
            int lineCount = 0;
            String[] headers = null;

            while( ( ( line = reader.readLine() ) != null ) && (lineCount <= NUMBER_OF_ROWS_TO_DISPLAY) )
            {
                split = line.split("\t");
                totalColumns = split.length;

                if (lineCount == 0)
                {
                    headers = new String[totalColumns];
                    for (int i = 0; i < totalColumns; i++)
                    {
                        // to get rid of the names being in 'name' format
                        if ( split[i].startsWith("\"") )
                            split[i] = split[i].substring(1, split[i].length());

                        if ( split[i].endsWith("\"") )
                            split[i] = split[i].substring(0, split[i].length() - 1);

                        headers[i] = split[i];
                    }
                }
                else
                {
                    for (int i = 0; i < split.length; i++)
                        if ( !isNumeric(split[i]) )
                            if (i > maxColumns)
                                maxColumns = i;
                }

                lineCount++;
            }

            // skip first column as it has the unique identifiers
            for (int i = 1; i < totalColumns; i++)
                dataStart.addItem("Column " + Integer.toString(i + 1) + ": " + headers[i]);

            dataStart.setSelectedIndex(maxColumns);
            reader.close();
        } 
        catch (IOException ioe)
        {
            if (DEBUG_BUILD) println("IOException in ExpressionLoaderDialog.guessStart():\n" + ioe.getMessage());
        }
        
        return (maxColumns + 1);
    }

    private boolean isNumeric(String value)
    {
        try 
        {
            // instead of numberFormatter.parse(value).floatValue() so as to avoid problems with header columns & parse(value)
            Float.parseFloat( value.replace(',', '.') );
            
            return true;
        } 
        catch (NumberFormatException nfe)
        {
            return false;
        }
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {
        if ( e.getSource().equals(dataStart) )
            createDialogElements(dataStart.getSelectedIndex() + 1);
    }

    public boolean proceed()
    {
        return proceed;
    }
    
    public int getStartColumn()
    {
        return startColumn;
    }


}