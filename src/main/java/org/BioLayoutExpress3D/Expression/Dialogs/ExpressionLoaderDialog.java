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

    private FloatNumberField correlationField = null;
    private JComboBox firstDataColumn = null;
    private JComboBox firstDataRow = null;
    private JComboBox correlationMetric = null;
    private JCheckBox transposeCheckBox = null;
    private JEditorPane textArea = null;
    private File expressionFile = null;

    private boolean proceed = false;

    private AbstractAction okAction = null;
    private AbstractAction cancelAction = null;
    private AbstractAction transposeChangedAction = null;

    private boolean creatingDialogElements = false;

    class DataBounds
    {
        public int firstColumn;
        public int firstRow;

        public DataBounds(int firstColumn, int firstRow)
        {
            this.firstColumn = firstColumn;
            this.firstRow = firstRow;
        }
    }

    private DataBounds dataBounds;

    public ExpressionLoaderDialog(JFrame frame, File expressionFile)
    {
        super(frame, "Load Expression Data", true);

        this.expressionFile = expressionFile;
        this.dataBounds = new DataBounds(0, 0);

        initActions(frame);
        initComponents();
        createDialogElements( transposeCheckBox.isSelected(), true );

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocation( ( SCREEN_DIMENSION.width - this.getWidth() ) / 2, ( SCREEN_DIMENSION.height - this.getHeight() ) / 2 );
    }

    private void initComponents()
    {
        JPanel topPanel = new JPanel(true);
        JPanel centrePanel = new JPanel(true);
        JPanel bottomPanel = new JPanel(true);

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
        correlationMetric.setToolTipText("Correlation Metric");

        correlationField = new FloatNumberField(0, 5);
        correlationField.setDocument( new TextFieldFilter(TextFieldFilter.FLOAT) );
        correlationField.setValue(STORED_CORRELATION_THRESHOLD);
        correlationField.setToolTipText("Minimum Correlation");

        transposeCheckBox = new JCheckBox(transposeChangedAction);
        transposeCheckBox.setText("Transpose");

        firstDataColumn = new JComboBox();
        firstDataColumn.addActionListener(this);
        firstDataColumn.setToolTipText("First Data Column");

        firstDataRow = new JComboBox();
        firstDataRow.addActionListener(this);
        firstDataRow.setToolTipText("First Data Row");

        topPanel.add(new JLabel("Minimum Correlation:"));
        topPanel.add(correlationField);
        topPanel.add(new JLabel("Correlation Metric:"));
        topPanel.add(correlationMetric);
        topPanel.add(new JLabel("First Data Column:"));
        topPanel.add(firstDataColumn);
        topPanel.add(new JLabel("First Data Row:"));
        topPanel.add(firstDataRow);
        topPanel.add(transposeCheckBox);

        centrePanel.setLayout(new BorderLayout());
        textArea = new JEditorPane("text/html", "");
        textArea.setToolTipText("Expression Data");

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize( new Dimension(500, 400) );
        centrePanel.add(scrollPane, BorderLayout.CENTER);

        JButton okButton = new JButton(okAction);
        okButton.setToolTipText("OK");
        JButton cancelButton = new JButton(cancelAction);
        cancelButton.setToolTipText("Cancel");
        bottomPanel.add(okButton);
        bottomPanel.add(cancelButton);

        container.add(topPanel, BorderLayout.NORTH);
        container.add(centrePanel, BorderLayout.CENTER);
        container.add(bottomPanel, BorderLayout.SOUTH);
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

        transposeChangedAction = new AbstractAction("TransposeToggle")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                refreshDataPreview(true);
            }
        };
    }

    private void guessDataBounds(TextDelimitedMatrix tdm)
    {
        firstDataColumn.removeAllItems();
        firstDataRow.removeAllItems();

        int mostNumericsInColumn = 0;
        int mostNumericColumn = 0;

        for (int column = 0; column < tdm.numColumns(); column++)
        {
            int numericFieldCount = 0;

            for (int row = 0; row < tdm.numRows(); row++)
            {
                String value = tdm.valueAt(column, row);

                if (column == 0)
                {
                    firstDataRow.addItem((row + 1) + ": " + value);
                }

                if (isNumeric(value))
                {
                    numericFieldCount++;
                }
            }

            if (numericFieldCount > mostNumericsInColumn)
            {
                mostNumericsInColumn = numericFieldCount;
                mostNumericColumn = column;
            }
        }

        int mostNumericsInRow = 0;
        int mostNumericRow = 0;

        for (int row = 0; row < tdm.numRows(); row++)
        {
            int numericFieldCount = 0;

            for (int column = 0; column < tdm.numColumns(); column++)
            {
                String value = tdm.valueAt(column, row);

                if (row == 0)
                {
                    firstDataColumn.addItem((column + 1) + ": " + value);
                }

                if (isNumeric(value))
                {
                    numericFieldCount++;
                }
            }

            if (numericFieldCount > mostNumericsInRow)
            {
                mostNumericsInRow = numericFieldCount;
                mostNumericRow = row;
            }
        }

        firstDataColumn.setSelectedIndex(mostNumericColumn);
        firstDataRow.setSelectedIndex(mostNumericRow);

        dataBounds = new DataBounds(mostNumericColumn, mostNumericRow);
    }

    private void createDialogElements(boolean transpose, boolean guessDataBounds)
    {
        if (creatingDialogElements)
        {
            // Already doing this
            return;
        }

        final int NUM_PREVIEW_COLUMNS = 128;
        final int NUM_PREVIEW_ROWS = 16;

        TextDelimitedMatrix tdm;

        try
        {
            creatingDialogElements = true;
            setCursor(BIOLAYOUT_WAIT_CURSOR);

            if (transpose)
            {
                tdm = new TextDelimitedMatrix(expressionFile, "\t",
                        NUM_PREVIEW_ROWS, NUM_PREVIEW_COLUMNS);
            } else
            {
                tdm = new TextDelimitedMatrix(expressionFile, "\t",
                        NUM_PREVIEW_COLUMNS, NUM_PREVIEW_ROWS);
            }


            if (!tdm.parse())
            {
                return;
            }

            tdm.setTranspose(transpose);
            int numColumns = tdm.numColumns();
            int numRows = tdm.numRows();

            if (guessDataBounds)
            {
                guessDataBounds(tdm);
            }

            StringBuilder pageText = new StringBuilder();
            pageText.append("<HTML>");

            Font font = this.getContentPane().getFont();
            String fontFamily = font.getFamily();
            int fontSize = font.getSize();
            pageText.append("<BODY STYLE=\"");
            pageText.append("font-family: " + fontFamily + ";");
            pageText.append("font-size: 10px;");
            pageText.append("\">");

            pageText.append("<TABLE STYLE=\"background-color: black\" " +
                    "WIDTH=\"100%\" CELLSPACING=\"1\" CELLPADDING=\"2\">\n");

            pageText.append("<TR><TD NOWRAP BGCOLOR=\"#FFFFFF\"></TD>");
            for (int column = 0; column < numColumns; column++)
            {
                pageText.append("<TD NOWRAP ");
                pageText.append("BGCOLOR=\"#FFFFFF\"");
                pageText.append(">" + "Column ");
                pageText.append(Integer.toString(column + 1));
                pageText.append("</TD>");
            }

            if (tdm.hasUnparsedColumns())
            {
                pageText.append("<TD NOWRAP BGCOLOR=\"#FFFFFF\">More columns</TD>");
            }
            pageText.append("</TR>\n");

            for (int row = 0; row < numRows; row++)
            {
                pageText.append("<TR>");

                pageText.append("<TD NOWRAP ");
                pageText.append("BGCOLOR=\"#FFFFFF\"");
                pageText.append(">" + "Row ");
                pageText.append(Integer.toString(row + 1));
                pageText.append("</TD>");

                for (int column = 0; column < numColumns; column++)
                {
                    String options;

                    if (column >= dataBounds.firstColumn && row >= dataBounds.firstRow)
                    {
                        options = "BGCOLOR=\"#CCFFCC\"";
                    }
                    else
                    {
                        options = "BGCOLOR=\"#FFCCCC\"";
                    }

                    String value = tdm.valueAt(column, row);

                    pageText.append("<TD NOWRAP ");
                    pageText.append(options);
                    pageText.append(">");
                    pageText.append(value);
                    pageText.append("</TD>");
                }

                if (tdm.hasUnparsedColumns())
                {
                    pageText.append("<TD NOWRAP BGCOLOR=\"#FFFFFF\">...</TD>");
                }

                pageText.append("</TR>\n");
            }

            if (tdm.hasUnparsedRows())
            {
                pageText.append("<TR>");

                // Row label column
                pageText.append("<TD NOWRAP BGCOLOR=\"#FFFFFF\">More rows</TD>");

                // Data columns
                for (int column = 0; column < numColumns; column++)
                {
                    pageText.append("<TD NOWRAP BGCOLOR=\"#FFFFFF\">...</TD>");
                }

                // Bottom right corner
                if (tdm.hasUnparsedColumns())
                {
                    pageText.append("<TD NOWRAP BGCOLOR=\"#FFFFFF\">...</TD>");
                }

                pageText.append("</TR>\n");
            }

            pageText.append("</TABLE>");
            pageText.append("</BODY>");
            pageText.append("</HTML>");

            textArea.setText(pageText.toString());
            textArea.setCaretPosition(0);
        }
        catch (IOException ioe)
        {
           if (DEBUG_BUILD)
           {
               println("IOException when parsing expression file:\n" + ioe.getMessage());
           }
        }
        finally
        {
            if (guessDataBounds)
            {
                this.pack();
            }

            setCursor(BIOLAYOUT_NORMAL_CURSOR);
            creatingDialogElements = false;
        }
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

    private void refreshDataPreview(boolean guessDataBounds)
    {
        createDialogElements(transposeCheckBox.isSelected(), guessDataBounds);
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {
        if ( e.getSource().equals(firstDataColumn) || e.getSource().equals(firstDataRow))
        {
            dataBounds = new DataBounds(firstDataColumn.getSelectedIndex(),
                    firstDataRow.getSelectedIndex());
            refreshDataPreview(false);
        }
    }

    public boolean proceed()
    {
        return proceed;
    }

    public int getFirstDataColumn()
    {
        return firstDataColumn.getSelectedIndex();
    }

    public int getFirstDataRow()
    {
        return firstDataRow.getSelectedIndex();
    }

    public boolean transpose()
    {
        return transposeCheckBox.isSelected();
    }
}