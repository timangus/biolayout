package org.Kajeka.Correlation;

import java.io.*;
import static java.lang.Math.*;
import org.Kajeka.Analysis.*;
import org.Kajeka.CoreUI.*;
import org.Kajeka.CoreUI.Dialogs.*;
import org.Kajeka.Network.*;
import org.Kajeka.Utils.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;
import static org.Kajeka.DebugConsole.ConsoleOutput.*;

/**
*
* @author Anton Enright, full refactoring by Thanos Theo, 2008-2009
* @version 3.0.0.0
*
*/

public final class CorrelationLoader
{
    private File file = null;
    private CorrelationData correlationData = null;
    private LayoutClassSetsManager layoutClassSetsManager = null;
    private String[] rowAnnotationLabels = null;
    private int firstDataColumn = 0;
    private int firstDataRow = 0;
    private boolean transpose = false;
    boolean isSuccessful = false;
    public String reasonForFailure = "";

    public CorrelationLoader(LayoutClassSetsManager layoutClassSetsManager)
    {
        this.layoutClassSetsManager = layoutClassSetsManager;
    }

    public boolean init(File file, CorrelationData correlationData,
            int firstDataColumn, int firstDataRow, boolean transpose)
    {
        this.firstDataColumn = firstDataColumn;
        this.firstDataRow = firstDataRow;
        this.transpose = transpose;
        this.file = file;
        this.correlationData = correlationData;

        return true;
    }

    class ParseProgressIndicator implements TextDelimitedMatrix.ProgressIndicator
    {
        private LayoutProgressBarDialog layoutProgressBarDialog;

        public ParseProgressIndicator(LayoutProgressBarDialog layoutProgressBarDialog)
        {
            this.layoutProgressBarDialog = layoutProgressBarDialog;
        }

        @Override
        public void notify(int percent)
        {
            layoutProgressBarDialog.incrementProgress(percent);
        }
    }

    private void setReasonForFailure(int row, int column, String reason)
    {
        if (row >= 0 && column >= 0)
        {
            reasonForFailure += "At row " + (row + 1) + ", column " + (column + 1) + ":\n";
        }

        reasonForFailure += reason;
    }

    public boolean parse(LayoutFrame layoutFrame, boolean tabDelimited)
    {
        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();

        layoutProgressBarDialog.prepareProgressBar(100, "Reading Correlation Data: ");
        layoutProgressBarDialog.startProgressBar();

        ParseProgressIndicator ppi = new ParseProgressIndicator(layoutProgressBarDialog);

        TextDelimitedMatrix tdm;
        int row = -1;
        int column = -1;

        try
        {
            reasonForFailure = "";

            String delimiter = tabDelimited ? "\t" : "";
            tdm = new TextDelimitedMatrix(file, ppi, delimiter);

            layoutProgressBarDialog.setText("Parsing " + tdm.numLines() + " lines");

            if (!tdm.parse())
            {
                reasonForFailure = tdm.reasonForFailure;
                return false;
            }

            tdm.setTranspose(transpose);
            int numColumns = tdm.numColumns();
            int numRows = tdm.numRows();

            // - 1 because the first column is always the row ID
            rowAnnotationLabels = new String[firstDataColumn - 1];

            correlationData.initialize(
                    numRows - firstDataRow,
                    numColumns - firstDataColumn,
                    transpose);

            layoutProgressBarDialog.setText("Loading data");

            for (row = 0; row < numRows; row++)
            {
                int percent = (100 * row) / numRows;
                layoutProgressBarDialog.incrementProgress(percent);

                for (column = 0; column < numColumns; column++)
                {
                    String value = tdm.valueAt(column, row);
                    int dataColumn = column - firstDataColumn;
                    int dataRow = row - firstDataRow;

                    if (row == 0)
                    {
                        if (column >= firstDataColumn)
                        {
                            // Data column names
                            correlationData.setColumnName(dataColumn, value);
                        }
                        else if (column >= 1) // First column is always the row ID
                        {
                            // Annotation classes
                            String annotation = cleanString(value);
                            layoutClassSetsManager.createNewClassSet(annotation);
                            rowAnnotationLabels[column - 1] = annotation;
                        }
                    }
                    else if (row < firstDataRow)
                    {
                        if (column == 0)
                        {
                            // Column annotation name
                            String annotation = cleanString(value);
                            correlationData.addColumnAnnotation(row - 1, annotation);
                        }
                        else if (column >= firstDataColumn)
                        {
                            // Column annotation
                            CorrelationData.ColumnAnnotation columnAnnotation =
                                    correlationData.getColumnAnnotationByIndex(row - 1);

                            columnAnnotation.setValue(dataColumn, value);

                        }
                    }
                    else if (row >= firstDataRow)
                    {
                        if (column == 0)
                        {
                            // Row names
                            correlationData.setRowID(dataRow, value);
                        }
                        else if (column >= firstDataColumn)
                        {
                            // The data itself
                            float currentValue = Float.parseFloat(value.replace(',', '.'));
                            correlationData.setDataValue(dataRow, dataColumn, currentValue);
                        }
                    }
                }
            }

            correlationData.sumRows();
        }
        catch (NumberFormatException nfe)
        {
            if (DEBUG_BUILD)
            {
                println("NumberFormatException in parse():\n" + nfe.getMessage());
            }

            setReasonForFailure(row, column, nfe.toString());
            return false;
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD)
            {
                println("IOException in parse():\n" + ioe.getMessage());
            }

            setReasonForFailure(row, column, ioe.toString());
            return false;
        }
        finally
        {
            layoutProgressBarDialog.endProgressBar();
        }

        return true;
    }

    public boolean parseAnnotations(LayoutFrame layoutFrame, NetworkContainer nc, boolean tabDelimited)
    {
        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();

        layoutProgressBarDialog.prepareProgressBar(100, "Reading Correlation Data: ");
        layoutProgressBarDialog.startProgressBar();

        ParseProgressIndicator ppi = new ParseProgressIndicator(layoutProgressBarDialog);

        TextDelimitedMatrix tdm;

        int chipGeneCount = 0;

        try
        {
            String delimiter = tabDelimited ? "\t" : "";
            tdm = new TextDelimitedMatrix(file, ppi, delimiter);

            layoutProgressBarDialog.setText("Parsing " + tdm.numLines() + " lines");

            if (!tdm.parse())
            {
                reasonForFailure = tdm.reasonForFailure;
                return false;
            }

            tdm.setTranspose(transpose);
            int numColumns = tdm.numColumns();
            int numRows = tdm.numRows();

            layoutProgressBarDialog.setText("Loading annotations");

            for (int row = firstDataRow; row < numRows; row++)
            {
                int percent = (100 * row) / numRows;
                layoutProgressBarDialog.incrementProgress(percent);

                Vertex vertex = null;

                for (int column = 0; column < firstDataColumn; column++)
                {
                    String value = tdm.valueAt(column, row);
                    int dataColumn = column - firstDataColumn;
                    int dataRow = row - firstDataRow;

                    if (column == 0)
                    {
                        vertex = nc.getVerticesMap().get(correlationData.getRowID(dataRow));
                    }
                    else if (vertex != null)
                    {
                        chipGeneCount++;
                        String annotation = cleanString(value);
                        LayoutClasses layoutClasses =
                                layoutClassSetsManager.getClassSetByName(rowAnnotationLabels[column - 1]);

                        if (annotation.isEmpty())
                        {
                            // if the annotation is empty set it to the noclass class
                            layoutClasses.setClass(vertex, 0);
                        }
                        else
                        {
                            VertexClass vc = layoutClasses.createClass(annotation);
                            layoutClasses.setClass(vertex, vc);
                            AnnotationTypeManagerBG.getInstanceSingleton().add(vertex.getVertexName(),
                                    layoutClasses.getClassSetName(), vc.getName());
                        }
                    }
                }
            }
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD)
            {
                println("IOException in parseAnnotations():\n" + ioe.getMessage());
            }

            return false;
        }
        finally
        {
            AnnotationTypeManagerBG.getInstanceSingleton().setChipGeneCount(chipGeneCount);
            layoutProgressBarDialog.endProgressBar();
        }

        return true;
    }

    private String cleanString(String string)
    {
        // This is apparently marginally faster than replaceAll("[\"\']", " ")
        return string.replace('\"', ' ').replace('\'', ' ');
    }
}