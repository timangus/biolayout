package org.BioLayoutExpress3D.Network;

import java.awt.*;
import java.io.*;
import java.util.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.CoreUI.Dialogs.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
*
* @author Leon Goldovsky, full refactoring by Thanos Theo, 2008-2009-2010
* @version 3.0.0.0
*
*/

public final class NetworkRootContainer extends NetworkContainer 
{ 
    private ArrayList<NetworkComponentContainer> componentCollection = null;
    private TilingLevelsContainer tilingLevelsContainer = null;

    public NetworkRootContainer(LayoutClassSetsManager layoutClassSetsManager, LayoutFrame layoutFrame)
    {
        super(layoutClassSetsManager, layoutFrame);

        componentCollection = new ArrayList<NetworkComponentContainer>();      
        tilingLevelsContainer = new TilingLevelsContainer();
    }

    public void createNetworkComponentsContainer() 
    {
        componentCollection.clear();
        tilingLevelsContainer.clear();

        // REMOVE SINGLETONS HERE FASTEST METHOD == IF DESIRED
        if (MINIMUM_COMPONENT_SIZE.get() > 1)
           removeSingletons();        

        // NOW REMOVE CONNECTED COMPONENTS SMALLER THAN A SPECIFIED SIZE BUT NOT SINGLETONS
        findOrRemovePolygons( MINIMUM_COMPONENT_SIZE.get() );
    }

    private void findOrRemovePolygons(int size) 
    {
        // This code partitions the graph into connected components, also removing those smaller than a certain threshold
        int counter = 0;
        Collection<Vertex> allVerticesCopy = new HashSet<Vertex>( getVertices() );
        HashSet<Vertex> vertexDone = new HashSet<Vertex>();
        int initialSize = getNumberOfVertices();
        NetworkComponentContainer ncc = null;
        Vertex vertex = null;

        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();
        layoutProgressBarDialog.prepareProgressBar(initialSize, "Finding Components");
        layoutProgressBarDialog.startProgressBar();

        while ( !allVerticesCopy.isEmpty() )
        {
            counter = initialSize - allVerticesCopy.size();

            layoutProgressBarDialog.incrementProgress(counter);

            ncc = new NetworkComponentContainer(layoutClassSetsManager, layoutFrame);
            vertex = allVerticesCopy.iterator().next();

            // has to add the keyset in a new HashSet for the tiling algorithm to work
            addToNcc( vertexDone, ncc, vertex, new HashSet<Vertex>( vertex.getEdgeConnectionsMap().keySet() ) );
            
            if (ncc.getNumberOfComponents() < size)
            {
                removeComponents( ncc.getVertices() );                
                ncc.removeComponents();
            }
            else
            {
                componentCollection.add(ncc);
            }

            allVerticesCopy.removeAll(vertexDone);
        }
        
        renumberVertices();

        layoutProgressBarDialog.endProgressBar();
    }

    private void removeSingletons() 
    {
        HashSet<Vertex> singletons = new HashSet<Vertex>();

        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();
        layoutProgressBarDialog.prepareProgressBar(verticesMap.size(), "Finding Singletons");
        layoutProgressBarDialog.startProgressBar();
        
        for ( Vertex vertex : getVertices() )
        {            
            if ( vertex.getEdgeConnectionsMap().isEmpty() )
               singletons.add(vertex);            
            layoutProgressBarDialog.incrementProgress();
        }
        
        layoutProgressBarDialog.setText("Removing " + singletons.size() + " Singletons...");
        layoutProgressBarDialog.endProgressBar();

        removeComponents(singletons);
    }

    private void removeComponents(Collection<Vertex> singletons)
    {
        HashSet<Edge> singletonEdges = new HashSet<Edge>();
        for (Vertex vertex : singletons)
        {
            verticesMap.remove( vertex.getVertexName() );
            singletonEdges.addAll( vertex.getEdgeConnectionsMap().values() );
            singletonEdges.add( vertex.getSelfEdge() );
        }

        edges.removeAll(singletonEdges);
    }

    private void renumberVertices()
    {
        int count = 0;
        for ( Vertex vertex : getVertices() )
            vertex.setVertexID(count++);
    }

    private void addToNcc(HashSet<Vertex> vertexDone, NetworkComponentContainer ncc, Vertex vertex, HashSet<Vertex> toDoVertices)
    {
        vertexDone.add(vertex);
        ncc.addNetworkConnection(vertex);

        Vertex currentVertex = null;
        Iterator<Vertex> iterator = toDoVertices.iterator();
        while ( iterator.hasNext() )
        {
            currentVertex = iterator.next();            
            if ( !vertexDone.contains(currentVertex) )
            {
                toDoVertices.addAll( currentVertex.getEdgeConnectionsMap().keySet() );
                vertexDone.add(currentVertex);
                ncc.addNetworkConnection(currentVertex);
            } 
            else 
            {
                toDoVertices.remove(currentVertex);
            }
            
            iterator = toDoVertices.iterator(); // so as to avoid a concurrent modification exception
        }
    }

    @Override
    public void optimize() 
    {
        if (WEIGHTED_EDGES)
        {
            normaliseWeights();
            layoutClassSetsManager.getCurrentClassSetAllClasses().setClassColor( 0, new Color(0, 144, 0) );
        }

        setKvalue();

        if ( !TILED_LAYOUT.get() )
        {
            super.optimize();
        }
        else
        {
            int componentNumber = 0;
            for (NetworkComponentContainer ncc : componentCollection)
                ncc.optimize(++componentNumber);
            
            frLayout.clean();

            Collections.sort( componentCollection, new NetworkComponentSorter() );

            LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();
            layoutProgressBarDialog.prepareProgressBar(componentCollection.size(), "Tiling Graph Components");
            layoutProgressBarDialog.startProgressBar();
            
            for (NetworkComponentContainer ncc :componentCollection) 
            {
                layoutProgressBarDialog.incrementProgress();
                tilingLevelsContainer.addNetworkComponentContainer(ncc);
            }

            tilingLevelsContainer.optimize();

            layoutProgressBarDialog.endProgressBar();
        }
    }

    @Override
    public void optimize(int componentID) {}

    @Override
    public void relayout() 
    {        
        isOptimized = false;
        isRelayout = true;

        if ( !TILED_LAYOUT.get() )
        {
            super.relayout();
        }
        else
        {
            LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();
            layoutProgressBarDialog.prepareProgressBar(componentCollection.size(), "Now Processing Burst Layout Iterations...");
            layoutProgressBarDialog.startProgressBar();

            tilingLevelsContainer.clear();

            if (DEBUG_BUILD) println("Number of groups: " + componentCollection.size());
            
            Collections.sort( componentCollection, new NetworkComponentSorter() );

            for (NetworkComponentContainer ncc : componentCollection) 
            {                
                layoutProgressBarDialog.incrementProgress();
                
                if (DEBUG_BUILD) println("Adding Group Dimension with width: " + ncc.getWidth() + " and height: " + ncc.getHeight());
                
                tilingLevelsContainer.addNetworkComponentContainer(ncc);
            }

            tilingLevelsContainer.optimize();
            tilingLevelsContainer.debug();

            layoutProgressBarDialog.endProgressBar();
            layoutProgressBarDialog.stopProgressBar();
        }

        isRelayout = false;
    }

    public void setKvalue()
    {
        frLayout.setKvalue( layoutFrame, getVertices() );
    }

    public boolean isOptimized() 
    {
        return isOptimized;
    }

    @Override
    public void clear()
    {
        super.clear();
        
        componentCollection.clear();
        tilingLevelsContainer.clear();     
    }

    public void clearRoot()
    {
        componentCollection.clear();
        tilingLevelsContainer.clear();
    }
    
    private static class NetworkComponentSorter implements Comparator<NetworkComponentContainer>, Serializable
    {

        /**
        *  Serial version UID variable for the NetworkComponentSorter class.
        */
        public static final long serialVersionUID = 111222333444555624L;

        @Override
        public int compare(NetworkComponentContainer ncc1, NetworkComponentContainer ncc2) 
        {
            return ( ncc1.getWidth() < ncc2.getWidth() ) ? 1 : ( ncc1.getWidth() > ncc2.getWidth() ) ? -1 : 0;
        }
        
        
    }
    
    
}