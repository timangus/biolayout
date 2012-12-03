package org.BioLayoutExpress3D.Files.Parsers;

import java.io.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.CoreUI.Dialogs.*;
import org.BioLayoutExpress3D.Expression.*;
import org.BioLayoutExpress3D.Network.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.Expression.ExpressionEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
*
* @author Anton Enright, full refactoring by Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public class ExpressionParser extends CoreParser 
{ 
    private ObjectInputStream iistream = null;
    private ExpressionData expressionData = null;
    private int[][] counts = null;

    public ExpressionParser(NetworkContainer nc, LayoutFrame layoutFrame, ExpressionData expressionData) 
    {
        super(nc, layoutFrame);
        
        this.expressionData = expressionData;
        this.counts = expressionData.getCounts();
    }

    @Override
    public boolean init(File file, String fileExtension) 
    {
        try 
        {
            iistream = new ObjectInputStream( new BufferedInputStream( new FileInputStream(file) ) );
            
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
                if (DEBUG_BUILD) println("IOException while closing streamers in ExpressionParser.init():\n" + ioe.getMessage());
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
        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();
        layoutProgressBarDialog.prepareProgressBar(100, "Reading in Graph Data:");
        layoutProgressBarDialog.startProgressBar();

        isSuccessful = false;
        nc.setOptimized(false);

        try 
        {
            iistream.readInt();    // move forward file seeker
            iistream.readFloat();  // move forward file seeker

            int nodeId = 0;
            int percent = 0;
            String nodeOne = "";
            String nodeTwo = "";
            int otherId = 0;
            float weight = 0.0f;
            while (iistream.available() != 0) 
            {
                nodeId = iistream.readInt();
                percent = (int)( 100.0f * ( (float)counter / (float)expressionData.getTotalRows() ) );

                layoutProgressBarDialog.incrementProgress(percent);
                
                nodeOne = expressionData.getRowID(nodeId);
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
                            nodeTwo = expressionData.getRowID(otherId);
                            nc.addNetworkConnection(nodeOne, nodeTwo, weight);
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
            if (DEBUG_BUILD) println("IOException in ExpressionParser.parse():\n" + ioe.getMessage());
        }
        finally
        {
            try 
            {
                iistream.close();
            }
            catch (IOException ioe)
            {
                if (DEBUG_BUILD) println("IOException while closing streams in ExpressionParser.parse():\n" + ioe.getMessage());
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
            if (DEBUG_BUILD) println("IOException while closing streams in ExpressionParser.close():\n" + ioe.getMessage());
        }
    }

    public void scan() 
    {
        try 
        {
            iistream.readInt();    // move forward file seeker
            iistream.readFloat();  // move forward file seeker

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
                        index = (int)Math.floor(100.0f * weight);

                        counts[nodeId][index]++;
                        counts[otherId][index]++;
                    }
                }
            }
        } 
        catch (IOException ioe) 
        {
            if (DEBUG_BUILD) println("IOException in ExpressionParser.scan():\n" + ioe.getMessage());
        }
    }

    public boolean checkFile(int givenMetric, float givenThreshold)
    {
        int metric = -1;
        float savedThreshold = -1.0f;

        try 
        {
            metric = iistream.readInt();
            savedThreshold = iistream.readFloat();
        } 
        catch (IOException ioe) 
        {
            if (DEBUG_BUILD) println("IOException in ExpressionParser.check_file():\n" + ioe.getMessage());
            
            return false;
        }

        if ( (metric == -1) || (savedThreshold == -1) )
           return false;
        
        if ( (givenMetric == metric) && (givenThreshold >= savedThreshold) )
           return true;        

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
        for ( Vertex vertex : nc.getVertices() )
        {
            if ( vertex.getEdgeConnectionsMap().isEmpty() )
            {
                nc.getEdges().remove( vertex.getSelfEdge() );
                nc.getVerticesMap().remove( vertex.getVertexName() );
                nc.getVertices().remove(vertex);
            }
        }
    }


}