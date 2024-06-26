package org.biolayout.Files.Parsers;

import org.biolayout.Correlation.CorrelationData;
import java.io.*;
import java.util.HashSet;
import org.biolayout.CoreUI.*;
import org.biolayout.CoreUI.Dialogs.*;
import org.biolayout.Network.*;
import static org.biolayout.Environment.GlobalEnvironment.*;
import static org.biolayout.Correlation.CorrelationEnvironment.*;
import static org.biolayout.DebugConsole.ConsoleOutput.*;

/**
 *
 * @author Anton Enright, full refactoring by Thanos Theo, 2008-2009-2010-2011
 * @version 3.0.0.0
 *
 */
public class CorrelationParser extends CoreParser
{

    private ObjectInputStream iistream = null;
    File file = null;
    private CorrelationData correlationData = null;
    private int[][] counts = null;

    public CorrelationParser(NetworkContainer nc, LayoutFrame layoutFrame, CorrelationData correlationData)
    {
        super(nc, layoutFrame);

        this.correlationData = correlationData;
        this.counts = correlationData.getCounts();
    }

    @Override
    public boolean init(File file, String fileExtension)
    {
        try
        {
            this.file = file;
            iistream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

            return true;
        }
        catch (Exception exc)
        {
            try
            {
                iistream.close();
            }
            catch (IOException ioe)
            {
                if (DEBUG_BUILD)
                {
                    println("IOException while closing streamers in init():\n" + ioe.getMessage());
                }
            }
            finally
            {

            }

            return false;
        }
    }

    @Override
    public boolean parse()
    {
        int counter = 0;
        HashSet<Integer> filterSet = new HashSet<Integer>();
        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();
        layoutProgressBarDialog.prepareProgressBar(100, "Reading in Graph Data:");
        layoutProgressBarDialog.startProgressBar();

        isSuccessful = false;
        nc.setOptimized(false);

        try
        {
            iistream.readInt();    // magic number

            int nodeId = 0;
            int percent = 0;
            String nodeOne = "";
            String nodeTwo = "";
            int otherId = 0;
            float weight = 0.0f;
            while (iistream.available() != 0)
            {
                nodeId = iistream.readInt();
                percent = (int) (100.0f * ((float) counter / (float) correlationData.getTotalRows()));

                layoutProgressBarDialog.incrementProgress(percent);

                nodeOne = correlationData.getRowID(nodeId);
                for (;;) // while (true)
                {
                    otherId = iistream.readInt();
                    if (nodeId == otherId)
                    {
                        break;
                    }
                    else
                    {
                        weight = iistream.readFloat();
                        if (weight > CURRENT_CORRELATION_THRESHOLD)
                        {
                            boolean filterOne = CURRENT_FILTER_SET.contains(nodeId);
                            boolean filterTwo = CURRENT_FILTER_SET.contains(otherId);

                            if (!filterOne && !filterTwo)
                            {
                                nodeTwo = correlationData.getRowID(otherId);
                                nc.addNetworkConnection(nodeOne, nodeTwo, weight);
                            }
                        }
                    }
                }

                counter++;
            }

            WEIGHTED_EDGES = true;

            isSuccessful = true;
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD)
            {
                println("IOException in parse():\n" + ioe.getMessage());
            }
        }
        finally
        {
            try
            {
                iistream.close();
            }
            catch (IOException ioe)
            {
                if (DEBUG_BUILD)
                {
                    println("IOException while closing streams in parse():\n" + ioe.getMessage());
                }
            }
            finally
            {
                layoutProgressBarDialog.endProgressBar();
            }
        }

        return isSuccessful;
    }

    public void close()
    {
        try
        {
            iistream.close();
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD)
            {
                println("IOException while closing streams in close():\n" + ioe.getMessage());
            }
        }
    }

    public void scan()
    {
        try
        {
            iistream.readInt();    // magic number

            int nodeId = 0;
            int otherId = 0;
            int index = 0;
            float weight = 0.0f;
            while (iistream.available() != 0)
            {
                nodeId = iistream.readInt();

                for (;;) // while (true)
                {
                    otherId = iistream.readInt();
                    if (nodeId == otherId)
                    {
                        break;
                    }
                    else
                    {
                        weight = iistream.readFloat();
                        index = (int) Math.floor(100.0f * weight);

                        boolean filterOne = CURRENT_FILTER_SET.contains(nodeId);
                        boolean filterTwo = CURRENT_FILTER_SET.contains(otherId);

                        if (!filterOne && !filterTwo)
                        {
                            counts[nodeId][index]++;
                            counts[otherId][index]++;
                        }
                    }
                }
            }
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD)
            {
                println("IOException in scan():\n" + ioe.getMessage());
            }
        }
    }

    public void rescan()
    {
        try
        {
            counts = correlationData.clearCounts();
            iistream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            scan();
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD)
            {
                println("IOException in rescan():\n" + ioe.getMessage());
            }
        }
    }

    public boolean checkFile()
    {
        int magicNumber = 0;

        try
        {
            magicNumber = iistream.readInt();
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD)
            {
                println("IOException in check_file():\n" + ioe.getMessage());
            }

            return false;
        }

        if (magicNumber == CorrelationData.FILE_MAGIC_NUMBER)
        {
            return true;
        }

        return false;
    }

    public int getNodeCount()
    {
        return nc.getNumberOfVertices();
    }

    public int getEdgeCount()
    {
        return nc.getEdges().size();
    }

    public void removeSingletons()
    {
        for (Vertex vertex : nc.getVertices())
        {
            if (vertex.getEdgeConnectionsMap().isEmpty())
            {
                nc.getEdges().remove(vertex.getSelfEdge());
                nc.getVerticesMap().remove(vertex.getVertexName());
                nc.getVertices().remove(vertex);
            }
        }
    }

}
