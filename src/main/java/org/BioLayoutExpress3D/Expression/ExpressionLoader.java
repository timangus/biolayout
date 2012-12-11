package org.BioLayoutExpress3D.Expression;

import java.io.*;
import static java.lang.Math.*;
import org.BioLayoutExpress3D.Analysis.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.CoreUI.Dialogs.*;
import org.BioLayoutExpress3D.Network.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
*
* @author Anton Enright, full refactoring by Thanos Theo, 2008-2009
* @version 3.0.0.0
*
*/

public final class ExpressionLoader
{
    private File file = null;
    private BufferedReader fileReaderBuffered = null;
    private BufferedReader fileReaderCounter = null;
    private ExpressionData expressionData = null;
    private LayoutClassSetsManager layoutClassSetsManager = null;
    private String[] annotationColumnLabels = null;
    private int dataStart = 0;
    boolean isSuccessful = false;

    public ExpressionLoader(LayoutClassSetsManager layoutClassSetsManager)
    {
        this.layoutClassSetsManager = layoutClassSetsManager;
    }

    public boolean init(File file, ExpressionData expressionData, int startColumn)
    {
        this.dataStart = startColumn - 1;
        this.file = file;
        this.expressionData = expressionData;

        return reInit();
    }

    public boolean reInit()
    {
        try
        {
            fileReaderBuffered = new BufferedReader( new FileReader(file) );
            fileReaderCounter = new BufferedReader( new FileReader(file) );

            return true;
        }
        catch (Exception exc)
        {
            if (DEBUG_BUILD) println("Exception in reInit:\n" + exc.getMessage());

            return false;
        }
    }

    public boolean parse(LayoutFrame layoutFrame)
    {

        isSuccessful = false;
        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();

        layoutProgressBarDialog.prepareProgressBar(100, "Reading Expression Data: ");
        layoutProgressBarDialog.startProgressBar();

        int totalRows = 0;
        int totalColumns = 0;
        int counter = 0;

        try
        {
            String line = "";
            String[] columnHeaders = null;
            while ( ( line = fileReaderCounter.readLine() ) != null)
            {
                if (totalRows == 0)
                {
                    columnHeaders = line.split("\t");
                    for (int i = dataStart; i < columnHeaders.length; i++)
                        totalColumns++;
                }

                totalRows++;
            }

            int totalAnnotationColumns = dataStart - 1;
            annotationColumnLabels = new String[totalAnnotationColumns];
            layoutProgressBarDialog.setText("Parsing: " + totalRows + " Rows, " + totalColumns + " Data Columns & " + totalAnnotationColumns + " Annotation Columns");
            expressionData.initialize(totalRows - 1, totalColumns, totalAnnotationColumns);

            int percent = 0;
            String[] lineSplit = null;
            int currentColumn = 0;
            float currentValue = 0.0f;
            String annotation = "";
            while ( ( line = fileReaderBuffered.readLine() ) != null)
            {
                percent = (int)rint(100.0 * counter / (double)totalRows);
                layoutProgressBarDialog.incrementProgress(percent);
                lineSplit = line.split("\t");

                if (counter == 0)
                {
                    currentColumn = 0;
                    for (int i = dataStart; i < lineSplit.length; i++)
                    {
                        expressionData.setColumnName(currentColumn, lineSplit[i]);
                        currentColumn++;
                    }

                    for (int i = 1; i < dataStart; i++)
                    {
                        annotation = cleanString(lineSplit[i]);
                        layoutClassSetsManager.createNewClassSet(annotation);
                        annotationColumnLabels[i - 1] = annotation;
                    }

                }
                else
                {
                    // to get rid of the NodeName being in "NodeName" format
                    if ( lineSplit[0].startsWith("\"") )
                        lineSplit[0] = lineSplit[0].substring(1, lineSplit[0].length());

                    if ( lineSplit[0].endsWith("\"") )
                        lineSplit[0] = lineSplit[0].substring(0, lineSplit[0].length() - 1);

                    expressionData.setRowID(counter - 1, lineSplit[0]);
                    expressionData.setIdentityMap(lineSplit[0], counter - 1);

                    currentColumn = 0;
                    for (int i = dataStart; i < lineSplit.length; i++)
                    {
                        currentValue = Float.parseFloat( lineSplit[i].replace(',', '.') );
                        expressionData.setExpressionDataValue(counter - 1, currentColumn, currentValue);
                        expressionData.addToSumX_cache(counter - 1, currentValue);
                        expressionData.addToSumX2_cache(counter - 1, currentValue * currentValue);
                        currentColumn++;
                    }
                }

                counter++;
            }

            isSuccessful = true;
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD) println("IOException in ExpressionLoader.parse():\n" + ioe.getMessage());
        }
        finally
        {
            try
            {
                fileReaderCounter.close();
                fileReaderBuffered.close();
            }
            catch (IOException ioe)
            {
                if (DEBUG_BUILD) println("IOException while closing streams in ExpressionLoader.parse():\n" + ioe.getMessage());
            }
            finally
            {
                layoutProgressBarDialog.endProgressBar();
            }
        }

        return isSuccessful;
    }

    public boolean parseAnnotations(LayoutFrame layoutFrame, NetworkContainer nc)
    {
        isSuccessful = false;
        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();

        layoutProgressBarDialog.prepareProgressBar(100, "Reading Expression Annotations: ");
        layoutProgressBarDialog.startProgressBar();

        int totalRows = 0;
        int totalColumns = 0;
        int counter = 0;
        int chipGeneCount = 0;

        try
        {
            String line = "";
            String[] columnHeaders = null;
            while ( ( line = fileReaderCounter.readLine() ) != null)
            {
                if (totalRows == 0)
                {
                    columnHeaders = line.split("\t");
                    for (int i = dataStart; i < columnHeaders.length; i++)
                        totalColumns++;
                }

                totalRows++;
            }

            int totalAnnotationColumns = dataStart - 1;
            layoutProgressBarDialog.setText("Parsing: " + totalRows + " Rows, " + totalColumns + " Data Columns & " + totalAnnotationColumns + " Annotation Columns");

            int percent = 0;
            String[] lineSplit = null;
            Vertex vertex = null;
            LayoutClasses layoutClasses = null;
            String annotation = "";
            while ( ( line = fileReaderBuffered.readLine() ) != null)
            {
                percent = (int)rint(100.0 * counter / (double)totalRows);
                layoutProgressBarDialog.incrementProgress(percent);
                lineSplit = line.split("\t");

                if (counter == 0)
                {
                    // do nothing
                }
                else
                {
                    // to get rid of the NodeName being in "NodeName" format
                    if ( lineSplit[0].startsWith("\"") )
                        lineSplit[0] = lineSplit[0].substring(1, lineSplit[0].length());

                    if ( lineSplit[0].endsWith("\"") )
                        lineSplit[0] = lineSplit[0].substring(0, lineSplit[0].length() - 1);

                    vertex = nc.getVerticesMap().get(lineSplit[0]);
                    if (vertex != null)
                    {
                        chipGeneCount++;
                        for (int i = 1; i < dataStart; i++)
                        {
                            annotation = cleanString(lineSplit[i]);
                            layoutClasses = layoutClassSetsManager.getClassSetByName(annotationColumnLabels[i - 1]);

                            if ( annotation.isEmpty() )
                            {
                                // if the annotation is empty set it to the noclass class
                                layoutClasses.setClass(vertex, 0);
                            }
                            else
                            {
                                VertexClass vc = layoutClasses.createClass(annotation);
                                layoutClasses.setClass(vertex, vc);
                                AnnotationTypeManagerBG.getInstanceSingleton().add(vertex.getVertexName(), layoutClasses.getClassSetName(), vc.getName());
                            }
                        }
                    }
                }

                counter++;
            }

            AnnotationTypeManagerBG.getInstanceSingleton().setChipGeneCount(chipGeneCount);
            layoutProgressBarDialog.endProgressBar();

            isSuccessful = true;
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD) println("IOException in ExpressionLoader.parseAnnot():\n" + ioe.getMessage());
        }
        finally
        {
            try
            {
                fileReaderCounter.close();
                fileReaderBuffered.close();
            }
            catch (IOException ioe)
            {
                if (DEBUG_BUILD) println("IOException while closing streams in ExpressionLoader.parseAnnot():\n" + ioe.getMessage());
            }
            finally
            {
                layoutProgressBarDialog.endProgressBar();
            }
        }

        return isSuccessful;
    }

    private String cleanString(String string)
    {
        char thisChar = ' ';
        String cleanString = "";

        for (int i = 0; i < string.length(); i++)
        {
            thisChar = string.charAt(i);

            if ( (thisChar == '\"') || (thisChar == '\'') )
                thisChar = ' ';

            cleanString += String.valueOf(thisChar);
        }

        return cleanString;
    }


}