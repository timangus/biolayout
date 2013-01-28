package org.BioLayoutExpress3D.Expression.Dialogs;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.BioLayoutExpress3D.Utils.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.Expression.ExpressionEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;
import org.BioLayoutExpress3D.StaticLibraries.Utils;

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
    private JComboBox linearTransformComboBox = null;
    private JComboBox scaleTransformComboBox = null;
    private JEditorPane textArea = null;
    private JCheckBox saveCorrelationTextFileCheckBox = null;
    private JCheckBox filterCheckBox = null;
    private FloatNumberField filterField = null;
    private static final float DEFAULT_FILTER_VALUE = 0.0f;
    private File expressionFile = null;

    private boolean proceed = false;

    private AbstractAction okAction = null;
    private AbstractAction cancelAction = null;
    private AbstractAction transposeChangedAction = null;
    private AbstractAction filterChangedAction = null;

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

    private JPanel generalTab()
    {
        JPanel tab = new JPanel(true);
        JPanel tabLine1 = new JPanel();
        JPanel tabLine2 = new JPanel();

        // Minimum correlation
        correlationField = new FloatNumberField(0, 5);
        correlationField.setDocument( new TextFieldFilter(TextFieldFilter.FLOAT) );
        correlationField.setValue(STORED_CORRELATION_THRESHOLD);
        correlationField.setToolTipText("Minimum Correlation");
        tabLine1.add(new JLabel("Minimum Correlation:"));
        tabLine1.add(correlationField);

        // Correlation metric
        correlationMetric = new JComboBox();
        for (CorrelationTypes type : CorrelationTypes.values())
        {
            String s = Utils.titleCaseOf(type.toString());
            correlationMetric.addItem(s);
        }
        correlationMetric.setSelectedIndex(0);
        correlationMetric.setToolTipText("Correlation Metric");
        tabLine1.add(new JLabel("Correlation Metric:"));
        tabLine1.add(correlationMetric);

        // Save text file
        saveCorrelationTextFileCheckBox = new JCheckBox();
        saveCorrelationTextFileCheckBox.setText("Save Cache As Text File");
        tabLine1.add(saveCorrelationTextFileCheckBox);

        // Data bounds
        firstDataColumn = new JComboBox();
        firstDataColumn.addActionListener(this);
        firstDataColumn.setToolTipText("First Data Column");
        tabLine2.add(new JLabel("First Data Column:"));
        tabLine2.add(firstDataColumn);
        firstDataRow = new JComboBox();
        firstDataRow.addActionListener(this);
        firstDataRow.setToolTipText("First Data Row");
        tabLine2.add(new JLabel("First Data Row:"));
        tabLine2.add(firstDataRow);

        tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));
        tab.add(tabLine1);
        tab.add(tabLine2);

        return tab;
    }

    private JPanel preprocessingTab()
    {
        JPanel tab = new JPanel(true);
        JPanel tabLine1 = new JPanel();
        JPanel tabLine2 = new JPanel();

        // Scale transform
        scaleTransformComboBox = new JComboBox();
        for (ScaleTransformType type : ScaleTransformType.values())
        {
            String s = Utils.titleCaseOf(type.toString());
            scaleTransformComboBox.addItem(s);
        }
        scaleTransformComboBox.setSelectedIndex(0);
        scaleTransformComboBox.setToolTipText("Scale Transform");
        tabLine1.add(new JLabel("Scale Transform:"));
        tabLine1.add(scaleTransformComboBox);

        // Linear transform
        linearTransformComboBox = new JComboBox();
        for (LinearTransformType type : LinearTransformType.values())
        {
            String s = Utils.titleCaseOf(type.toString());
            linearTransformComboBox.addItem(s);
        }
        linearTransformComboBox.setSelectedIndex(0);
        linearTransformComboBox.setToolTipText("Linear Transform");
        tabLine1.add(new JLabel("Linear Transform:"));
        tabLine1.add(linearTransformComboBox);

        // Transpose
        transposeCheckBox = new JCheckBox(transposeChangedAction);
        transposeCheckBox.setText("Transpose");
        tabLine1.add(transposeCheckBox);

        // Filter
        filterCheckBox = new JCheckBox(filterChangedAction);
        filterCheckBox.setText("Filter Rows With Standard Deviation Less Than");
        filterCheckBox.setSelected(false);
        filterField = new FloatNumberField(0, 5);
        filterField.setDocument( new TextFieldFilter(TextFieldFilter.FLOAT) );
        filterField.setEnabled(false);
        filterField.setValue(DEFAULT_FILTER_VALUE);
        tabLine2.add(filterCheckBox);
        tabLine2.add(filterField);

        tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));
        tab.add(tabLine1);
        tab.add(tabLine2);

        return tab;
    }

    private void initComponents()
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel centrePanel = new JPanel(true);
        JPanel bottomPanel = new JPanel(true);

        Container container = this.getContentPane();
        container.setLayout( new BorderLayout() );

        tabbedPane.addTab("General", generalTab());
        tabbedPane.addTab("Preprocessing", preprocessingTab());

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

        container.add(tabbedPane, BorderLayout.NORTH);
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

                if (filterCheckBox.isSelected())
                {
                    if (filterField.isEmpty() || filterField.getValue() < 0.0f)
                    {
                        JOptionPane.showMessageDialog(frame, "A positive filter value must be given.", "Invalid filter value", JOptionPane.INFORMATION_MESSAGE);
                        filterField.setValue(DEFAULT_FILTER_VALUE);
                        return;
                    }
                }

                CURRENT_METRIC = CorrelationTypes.values()[correlationMetric.getSelectedIndex()];
                CURRENT_LINEAR_TRANSFORM = LinearTransformType.values()[linearTransformComboBox.getSelectedIndex()];
                CURRENT_SCALE_TRANSFORM = ScaleTransformType.values()[scaleTransformComboBox.getSelectedIndex()];
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

        filterChangedAction = new AbstractAction("FilterToggle")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                filterField.setEnabled(filterCheckBox.isSelected());
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
                        options = "BGCOLOR=\"#CCCCFF\"";
                    }
                    else
                    {
                        if (row == 0)
                        {
                            options = "BGCOLOR=\"#6699FF\"";
                        }
                        else if (column == 0)
                        {
                            options = "BGCOLOR=\"#FFCCCC\"";
                        }
                        else
                        {
                            options = "BGCOLOR=\"#CCFFCC\"";
                        }
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

    public boolean saveCorrelationTextFile()
    {
        return saveCorrelationTextFileCheckBox.isSelected();
    }

    public float filterValue()
    {
        if (!filterCheckBox.isSelected())
        {
            return -1.0f;
        }

        return filterField.getValue();
    }
}