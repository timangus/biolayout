package org.BioLayoutExpress3D.Simulation;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.CoreUI.Dialogs.*;
import org.BioLayoutExpress3D.CPUParallelism.*;
import org.BioLayoutExpress3D.CPUParallelism.Executors.*;
import org.BioLayoutExpress3D.DataStructures.*;
import org.BioLayoutExpress3D.Network.*;
import org.BioLayoutExpress3D.StaticLibraries.*;
import static org.BioLayoutExpress3D.StaticLibraries.ArraysAutoBoxUtils.*;
import static org.BioLayoutExpress3D.Environment.AnimationEnvironment.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
*
* SignalingPetriNetSimulation is the class representing the SPN simulation.
*
* @see org.BioLayoutExpress3D.Simulation.Dialogs.SignalingPetriNetSimulationDialog
* @author Benjamin Boyer, code updates/heavy optimizations/modifications/native C code/N-Core parallelization support Thanos Theo, 2009-2010-2011
* @version 3.0.0.0
*
*/

public class SignalingPetriNetSimulation
{

    // package access variables
    static final String SAVE_DETAILS_DATA_COLUMN_NAME_NODES = "Nodes: ";
    static final String SAVE_DETAILS_DATA_COLUMN_NAME_TIMEBLOCKS = "TimeBlocks: ";
    static final String SAVE_DETAILS_DATA_COLUMN_NAME_RUNS = "Runs: ";

    private static final String WEIGHTS_TIMEBLOCK_DELIMITER = "-";
    private static final String WEIGHTS_VALUE_DELIMITER = ",";
    private static final String WEIGHTS_SEPARATOR_DELIMITER = ";";

    private static final double STANDARD_NORMAL_DISTRIBUTION_HALF_RANGE = 6.0;
    private static final double STANDARD_NORMAL_DISTRIBUTION_RANGE = 2.0 * STANDARD_NORMAL_DISTRIBUTION_HALF_RANGE;
    private static final double STANDARD_NORMAL_DISTRIBUTION_MIN_VALUE = 0.0;
    private static final double STANDARD_NORMAL_DISTRIBUTION_MAX_VALUE = 0.9999999;
    private static final double DETERMINISTIC_PROCESS_CONSTANT_PROBABILITY = 0.5;

    private NetworkContainer nc = null;
    private LayoutFrame layoutFrame = null;
    private LayoutProgressBarDialog layoutProgressBarDialog = null;

    private int numberOfVertices = 0;
    private SPNDistributionTypes SPNDistributionType = SPNDistributionTypes.UNIFORM;
    private SPNTransitionTypes SPNTransitionType = SPNTransitionTypes.CONSUMPTIVE;
    private java.util.Random random = null;

    private int[] transitionIDs = null;
    private int[][] parentsIDs = null;
    private int[][] childrenIDs = null;
    private float[][] childrenWeights = null;
    private int[][] totalInhibitorsIDs = null;
    private int[][] partialInhibitorsIDs = null;
    private int[][] allArraysLengths = null;

    private long timeTaken = 0;
    private float[][][] results = null;

    // variables needed for N-CP
    private static final int MINIMUM_NUMBER_OF_SPN_RUNS_FOR_PARALLELIZATION = 2;
    private final CyclicBarrierTimer cyclicBarrierTimer = (USE_MULTICORE_PROCESS) ? new CyclicBarrierTimer() : null;
    private final CyclicBarrier threadBarrier = (USE_MULTICORE_PROCESS) ? new CyclicBarrier(NUMBER_OF_AVAILABLE_PROCESSORS + 1, cyclicBarrierTimer) : null;

    /**
    *  Variable used for loading the native library only once (no use of re-loading the library).
    */
    private static boolean hasOnceLoadedNativeLibrary = false;

    /**
    *  The constructor of the SignalingPetriNetSimulation class.
    */
    public SignalingPetriNetSimulation(NetworkContainer nc, LayoutFrame layoutFrame)
    {
        this.nc = nc;
        this.layoutFrame = layoutFrame;

        random = new java.util.Random();
        layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();
    }

    /**
    *  Executes the SPN simulation.
    */
    public void executeSPNSimulation(int totalTimeBlocks, int totalRuns)
    {
        long prevTime = System.nanoTime();
        initAllCachedDataStructures(totalTimeBlocks, totalRuns);
        performSPNSimulation(totalTimeBlocks, totalRuns);
        clean(totalRuns);
        timeTaken = System.nanoTime() - prevTime;
    }

    /**
    *  Initializes all the relevant SPN data structures.
    */
    private void initAllCachedDataStructures(int totalTimeBlocks, int totalRuns)
    {
        numberOfVertices = nc.getNumberOfVertices();
        String SPNDistributionTypeString = USE_SPN_DISTRIBUTION_TYPE.get();
        if ( SPNDistributionTypeString.equals( SPNDistributionTypes.UNIFORM.toString() ) )
            SPNDistributionType = SPNDistributionTypes.UNIFORM;
        else if ( SPNDistributionTypeString.equals( SPNDistributionTypes.STANDARD_NORMAL.toString() ) )
            SPNDistributionType = SPNDistributionTypes.STANDARD_NORMAL;
        else if ( SPNDistributionTypeString.equals( SPNDistributionTypes.DETERMINISTIC_PROCESS.toString() ) )
            SPNDistributionType = SPNDistributionTypes.DETERMINISTIC_PROCESS;
        String SPNTransitionTypeString = USE_SPN_TRANSITION_TYPE.get();
        if ( SPNTransitionTypeString.equals( SPNTransitionTypes.CONSUMPTIVE.toString() ) )
            SPNTransitionType = SPNTransitionTypes.CONSUMPTIVE;
        else if ( SPNTransitionTypeString.equals( SPNTransitionTypes.ORIGINAL.toString() ) )
            SPNTransitionType = SPNTransitionTypes.ORIGINAL;

        // Note:
        // We need to initialize the four arrays below with a dimention of 0 or above instead of plain []
        // Otherwise the C/JNI native version of the SPN simulation will crash the JVM trying to lock these 2D arrays!
        parentsIDs = new int[numberOfVertices][0];
        childrenIDs = new int[numberOfVertices][0];
        childrenWeights = new float[numberOfVertices][0];
        totalInhibitorsIDs = new int[numberOfVertices][0];
        partialInhibitorsIDs = new int[numberOfVertices][0];

        results = ( !( USE_MULTICORE_PROCESS && USE_SPN_N_CORE_PARALLELISM.get() ) || (totalRuns < MINIMUM_NUMBER_OF_SPN_RUNS_FOR_PARALLELIZATION) )
                  ? new float[1][totalTimeBlocks][numberOfVertices]
                  : new float[NUMBER_OF_AVAILABLE_PROCESSORS][totalTimeBlocks][numberOfVertices];

        ArrayList<Integer> arraylistTransitionIDs = new ArrayList<Integer>();
        Tuple5<int[], int[], float[], int[], int[]> familyTuple5 = null;
        for ( Vertex vertex: nc.getVertices() )
        {
            if( vertex.ismEPNTransition() )
            {
                arraylistTransitionIDs.add( vertex.getVertexID() );
                familyTuple5 = getSPNSimulationDetailsForTransition(vertex, vertex.getEdgeConnectionsMap(), totalTimeBlocks);
                parentsIDs[vertex.getVertexID()] = familyTuple5.first;
                childrenIDs[vertex.getVertexID()] = familyTuple5.second;
                childrenWeights[vertex.getVertexID()] = familyTuple5.third;
                totalInhibitorsIDs[vertex.getVertexID()] = familyTuple5.fourth;
                partialInhibitorsIDs[vertex.getVertexID()] = familyTuple5.fifth;
            }
        }

        transitionIDs = toPrimitiveListInteger(arraylistTransitionIDs);
    }

    /**
    *  Main method of the SPN simulation execution code. Uses an N-Core paralellism algorithm in case of multiple core availability.
    */
    private void performSPNSimulation(int totalTimeBlocks, int totalRuns)
    {
        String progressBarParallelismTitle = (USE_MULTICORE_PROCESS && USE_SPN_N_CORE_PARALLELISM.get() ) ? " (Utilizing " + NUMBER_OF_AVAILABLE_PROCESSORS + "-Core Parallelism)" : "";
        layoutProgressBarDialog.prepareProgressBar(totalRuns, "Now Processing SPN Simulation For " + totalTimeBlocks + " Time Blocks & " + totalRuns + " Runs" + progressBarParallelismTitle + "...");
        layoutProgressBarDialog.startProgressBar();

        if ( !( USE_MULTICORE_PROCESS && USE_SPN_N_CORE_PARALLELISM.get() ) || (totalRuns < MINIMUM_NUMBER_OF_SPN_RUNS_FOR_PARALLELIZATION) )
        {
            allIterationsSPNSimulation(totalTimeBlocks, totalRuns, totalRuns, transitionIDs, results[0]);
        }
        else
        {
            int totalRunsPerProcess = totalRuns / NUMBER_OF_AVAILABLE_PROCESSORS;
            LoggerThreadPoolExecutor executor = new LoggerThreadPoolExecutor(NUMBER_OF_AVAILABLE_PROCESSORS, NUMBER_OF_AVAILABLE_PROCESSORS, 0L, TimeUnit.MILLISECONDS,
                                                                             new LinkedBlockingQueue<Runnable>(NUMBER_OF_AVAILABLE_PROCESSORS),
                                                                             new LoggerThreadFactory("SignalingPetriNetSimulation"),
                                                                             new ThreadPoolExecutor.CallerRunsPolicy() );

            cyclicBarrierTimer.clear();
            for (int threadId = 0; threadId < NUMBER_OF_AVAILABLE_PROCESSORS; threadId++)
                executor.execute( SPNSimulationProcessKernel(threadId, totalTimeBlocks, totalRuns, totalRunsPerProcess, results[threadId]) );

            try
            {
                threadBarrier.await(); // wait for all threads to be ready
                threadBarrier.await(); // wait for all threads to finish
                executor.shutdown();
            }
            catch (BrokenBarrierException ex)
            {
                if (DEBUG_BUILD) println("Problem with a broken barrier with the main SignalingPetriNetSimulation simulation thread in performSPNSimulation()!:\n" + ex.getMessage());
            }
            catch (InterruptedException ex)
            {
                // restore the interuption status after catching InterruptedException
                Thread.currentThread().interrupt();
                if (DEBUG_BUILD) println("Problem with pausing the main SignalingPetriNetSimulation simulation thread in performSPNSimulation()!:\n" + ex.getMessage());
            }

            if (DEBUG_BUILD) println("\nTotal SignalingPetriNetSimulation N-CP run time: " + (cyclicBarrierTimer.getTime() / 1e6) + " ms.\n");

            aggregateResultsFromAllProcesses(results, totalTimeBlocks);
        }

        layoutProgressBarDialog.endProgressBar();
        layoutProgressBarDialog.stopProgressBar();

        if ( SAVE_SPN_RESULTS.get() && AUTOMATICALLY_SAVE_SPN_RESULTS_TO_PRECHOSEN_FOLDER.get() && !SAVE_SPN_RESULTS_FILE_NAME.get().isEmpty() )
            SPNSimulationResultsWriteToFile(totalTimeBlocks, totalRuns);

        // do it here to ensure the SPN results are being fed to the popup Java2D plot even before the Animation Control dialog is initializing it
        ANIMATION_SIMULATION_RESULTS = results[0];
    }

    /**
    *  Performs all iterations of the SPN simulation (wrapper method for selecting between native and Java versions of the SPN simulation).
    */
    private void allIterationsSPNSimulation(int totalTimeBlocks, int totalRuns, int runs, int[] transitionIDs, float[][] results)
    {
        float[] places = new float[numberOfVertices];
        int placesIndex = 0;
        int run = runs;
        while (--run >= 0)
        {
            placesIndex = numberOfVertices;
            while (--placesIndex >= 0)
                places[placesIndex] = 0.0f;

            addResult(results, places, 1.0f, 0); // initialize first block

            for (int timeBlock = 1; timeBlock < totalTimeBlocks; timeBlock++) // start from 1
            {
                shuffleTransitions(transitionIDs);
                activateAllTransitions(transitionIDs, places, timeBlock);
                addResult(results, places, (float)totalRuns, timeBlock);
            }

            updateGUI();
        }
    }

    /**
    *   Return a light-weight runnable using the Adapter technique for the SPN simulation so as to avoid any load latencies.
    *   The coding style simulates an OpenCL/CUDA kernel.
    */
    private Runnable SPNSimulationProcessKernel(final int threadId, final int totalTimeBlocks, final int totalRuns, final int totalRunsPerProcess, final float[][] processResults)
    {
        return new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    threadBarrier.await();
                    try
                    {
                        int[] transitionIDsForThread = new int[transitionIDs.length];
                        System.arraycopy(transitionIDs, 0, transitionIDsForThread, 0, transitionIDs.length); // fastest way, native method!

                        // last run process will add remainder runs
                        int runs = ( threadId == (NUMBER_OF_AVAILABLE_PROCESSORS - 1) ) ? totalRunsPerProcess + (totalRuns % NUMBER_OF_AVAILABLE_PROCESSORS) : totalRunsPerProcess;
                        allIterationsSPNSimulation(totalTimeBlocks, totalRuns, runs, transitionIDsForThread, processResults);
                    }
                    finally
                    {
                        threadBarrier.await();
                    }
                }
                catch (BrokenBarrierException ex)
                {
                    if (DEBUG_BUILD) println("Problem with a broken barrier with the N-Core thread with threadId " + threadId + " in SPNSimulationProcessKernel()!:\n" + ex.getMessage());
                }
                catch (InterruptedException ex)
                {
                    // restore the interuption status after catching InterruptedException
                    Thread.currentThread().interrupt();
                    if (DEBUG_BUILD) println("Problem with pausing the N-Core thread with threadId " + threadId + " in SPNSimulationProcessKernel()!:\n" + ex.getMessage());
                }
            }


        };
    }

    /**
    *  Aggregates results from all processes to the first dimention (threadId) of the results float[][][] array.
    */
    private void aggregateResultsFromAllProcesses(float[][][] results, int totalTimeBlocks)
    {
        int z = NUMBER_OF_AVAILABLE_PROCESSORS;
        int y = 0;
        int x = 0;
        while (--z >= 1) // does not need to add to the first threadId as all results are aggregated to the first one
        {
            y = totalTimeBlocks;
            while (--y >= 0)
            {
                x = numberOfVertices;
                while (--x >= 0)
                    results[0][y][x] += results[z][y][x];
            }
        }
    }

    /**
    *  Shuffles the transitions.
    */
    private void shuffleTransitions(int[] transitionIDs)
    {
        int index = 0;
        int temp = 0;
        for (int i = transitionIDs.length; i > 1; i--)
        {
            index = random.nextInt(i);
            temp = transitionIDs[i - 1];
            transitionIDs[i - 1] = transitionIDs[index];
            transitionIDs[index] = temp;
        }
    }

    /**
    *  Activates all transitions.
    *
    *  Note:
    *  The 'synchronized' keyword is being used because some machines/platforms/JVMs (Dell based ones?) slow down really badly without it.
    *  It does though hinder the unlimited scalability potential of the N-Core parallelization (4 cores onwards do not scale more).
    *  The C/JNI version does not have this problem & overhead on any platforms, so it is unlimitedly scalable with the N-Core parallelization.
    */
    private synchronized void activateAllTransitions(int[] transitionIDs, float[] places, int timeBlock)
    {
        int i = transitionIDs.length;
        while (--i >= 0)
            activationRuleSet(transitionIDs[i], places, timeBlock);
    }

    /**
    *  Activation of one transition according to a given rule set.
    */
    private void activationRuleSet(int transitionID, float[] places, int timeBlock)
    {
        int i = 0;
        int index = 0;
        boolean hasTotalInhibitorParents = false;
        boolean hasPartialInhibitorParents = false;
        float partialInihibitorTokens = 0.0f;
        float tokenInPlace = 0.0f;
        float tokenMoving = -1.0f;
        double randomValue = 0.0;

        int[] parentIDs = parentsIDs[transitionID];
        int[] childIDs = childrenIDs[transitionID];
        float[] childWeights = childrenWeights[transitionID];
        int[] totalInhibitorIDs = totalInhibitorsIDs[transitionID];
        int[] partialInhibitorIDs = partialInhibitorsIDs[transitionID];

        /* Differences from the SPN algorithm:
            1. Added support for multiple parents instead of only one in original SPN (which only supported a tree like structure) (case 2 in code below).
            2. Addition of Total Inhibitor edges to block a transition (case 1 in code below), old SPN algorithm did not support it.
            3. Addition of Partial Inhibitor edges to block a transition (case 1 in code below), old SPN algorithm did not support it.*
            4. For multiple parents we find the minimum token (case 3 in code below) and calculate a token randomly between 0 & that minimum token so as to move forward (add to the flow) (case 4 in code below).
            5. Token is substracted to all parents, than just the only one supported by the original SPN algorithm (case 5 in code below).
            6. Token is added to all children, times their weight, than just the only one (one child) supported by the original SPN algorithm (case 6 in code below).
        */

        // 1) check for parents, no parents token to 1.0
        if (parentIDs.length == 0)
        {
            tokenMoving = 1.0f;
        }
        else
        {

            // 2) check total inhibitor
            // totalInhibitorIDs is the 4th int array
            i = totalInhibitorIDs.length;
            while (--i >= 0)
                if (!hasTotalInhibitorParents)
                    hasTotalInhibitorParents = (places[totalInhibitorIDs[i]] > 0.0f);

            // 3) check partial inhibitor, only if no total inhibitor is present
            // partialInhibitorIDs is the 5th int array
            if (!hasTotalInhibitorParents)
            {
                i = partialInhibitorIDs.length;
                while (--i >= 0)
                    if ( (hasPartialInhibitorParents = (places[partialInhibitorIDs[i]] > 0.0f) ) )
                        partialInihibitorTokens += places[partialInhibitorIDs[i]];
            }

            // 4) find the minimum token of all parents
            // parentsIDs is the 1st int array
            i = parentIDs.length;
            while (--i >= 0)
            {
                tokenInPlace = places[parentIDs[i]];
                if ( (tokenMoving > tokenInPlace) || (tokenMoving < 0.0f) )
                {
                    tokenMoving = tokenInPlace;
                    index = i;
                }
            }

            randomValue = computeProbability();
            if (randomValue == 0.0) // if randomValue is zero, then tokenMoving will be zero, thus no need to calculate anything else
                return;
            else // randomly calculate a token between 0 - minimum token
                tokenMoving = (int)( randomValue * (tokenMoving + 1.0f) );
        }

        // 5) take the correct number of token to parents
        // parentsIDs is the 1st int array
        i = parentIDs.length;
        while (--i >= 0)
        {
            if ( SPNTransitionType.equals(SPNTransitionTypes.CONSUMPTIVE) )
                places[parentIDs[i]] -= ( (index == i) ? tokenMoving : (int)( randomValue * (places[parentIDs[i]] + 1.0f) ) );
            else // if ( SPNTransitionType.equals(SPNTransitionTypes.ORIGINAL) )
                // old way of parents substraction, same min token substracted from everywhere
                places[parentIDs[i]] -= tokenMoving;
        }

        // skip adding to children if it's an total inhibitor
        if (hasTotalInhibitorParents)
            return;

        // add to children all partial inhibitor tokens
        if (hasPartialInhibitorParents)
        {
            // so as to avoid getting below zero results
            if (partialInihibitorTokens > tokenMoving)
                return;

            tokenMoving -= partialInihibitorTokens;
        }

        // 6) put the correct number of token in child
        // childrenIDs & childrenWeigths are the 2nd & the 3rd int & float arrays
        i = childIDs.length;
        while (--i >= 0)
            places[childIDs[i]] += (tokenMoving * childWeights[i * timeBlock + timeBlock]);
    }

    /**
    *  Computes the SPN simulation probability.
    */
    private double computeProbability()
    {
        if ( SPNDistributionType.equals(SPNDistributionTypes.UNIFORM) )
            return java.lang.Math.random();
        else if ( SPNDistributionType.equals(SPNDistributionTypes.STANDARD_NORMAL) )
        {
            double randomValue = org.BioLayoutExpress3D.StaticLibraries.Random.nextGaussian();
            randomValue += STANDARD_NORMAL_DISTRIBUTION_HALF_RANGE;
            randomValue /= STANDARD_NORMAL_DISTRIBUTION_RANGE;
            if (randomValue < 0.0)
                randomValue = STANDARD_NORMAL_DISTRIBUTION_MIN_VALUE;
            if (randomValue >= 1.0)
                randomValue = STANDARD_NORMAL_DISTRIBUTION_MAX_VALUE;

            return randomValue;
        }
        else // if ( SPNDistributionType.equals(SPNDistributionTypes.DETERMINISTIC_PROCESS) )
            return DETERMINISTIC_PROCESS_CONSTANT_PROBABILITY;
    }

    /**
    *  Adds the given result.
    */
    private void addResult(float[][] results, float[] places, float totalRuns, int step)
    {
        int i = numberOfVertices;
        while (--i >= 0)
            results[step][i] += (places[i] / totalRuns);
    }

    /**
    *  Updates the GUI for the SPN simulation iterations.
    */
    private void updateGUI()
    {
         layoutProgressBarDialog.incrementProgress();
    }

    /**
    *  Gets all SPN simulation details for the given transition.
    */
    private Tuple5<int[], int[], float[], int[], int[]> getSPNSimulationDetailsForTransition(Vertex transition, HashMap<Vertex, Edge> connectionsMap, int totalTimeBlocks)
    {
        ArrayList<Integer> parentsIDsList = new ArrayList<Integer>();
        ArrayList<Integer> childrenIDsList = new ArrayList<Integer>();
        ArrayList<Float> childrenWeightsList = new ArrayList<Float>();
        ArrayList<Integer> totalInhibitorsList = new ArrayList<Integer>();
        ArrayList<Integer> partialInhibitorsList = new ArrayList<Integer>();
        Edge edge = null;
        float[] childrenWeightsArray = null;

        for ( Vertex vertex : connectionsMap.keySet() )
        {
            edge = connectionsMap.get(vertex);
            if ( edge.isTotalInhibitorEdge() )
            {
                totalInhibitorsList.add( vertex.getVertexID() );
            }
            else if ( edge.isPartialInhibitorEdge() )
            {
                partialInhibitorsList.add( vertex.getVertexID() );
            }
            else if ( edge.hasDualArrowHead() )
            {
                parentsIDsList.add( vertex.getVertexID() );
                childrenIDsList.add( vertex.getVertexID() );
                childrenWeightsArray = getWeigth(vertex.getVertexID(), edge.getEdgeName(), totalTimeBlocks);
                for (int i = 0; i < totalTimeBlocks; i++)
                    childrenWeightsList.add(childrenWeightsArray[i]);
            }
            else if ( edge.getSecondVertex().equals(transition) ) // arrow head goes to transition node, it's a parent node
            {
                parentsIDsList.add( vertex.getVertexID() );
            }
            else // arrow head goes from transition node to non-transition node, it's a child node
            {
                childrenIDsList.add( vertex.getVertexID() );
                childrenWeightsArray = getWeigth(vertex.getVertexID(), edge.getEdgeName(), totalTimeBlocks);
                for (int i = 0; i < totalTimeBlocks; i++)
                    childrenWeightsList.add(childrenWeightsArray[i]);
            }
        }

        return Tuples.tuple( toPrimitiveListInteger(parentsIDsList), toPrimitiveListInteger(childrenIDsList), toPrimitiveListFloat(childrenWeightsList),
                             toPrimitiveListInteger(totalInhibitorsList), toPrimitiveListInteger(partialInhibitorsList) );
    }

    /**
    *  Gets the weight for every timeblock.
    */
    private float[] getWeigth(int vertexID, String edgeName, int totalTimeBlocks)
    {
        float[] floatArray = new float[totalTimeBlocks];
        if ( (edgeName == null) || edgeName.isEmpty() )
            return initFloatArrayAllTimeBlocksToValue(1.0f, floatArray);

        try
        {
            return ( edgeName.contains(WEIGHTS_TIMEBLOCK_DELIMITER) && edgeName.contains(WEIGHTS_VALUE_DELIMITER) )
                   ? parseTimeBoundedTimeBlocksEdgeName(edgeName, floatArray, totalTimeBlocks)
                   : initFloatArrayAllTimeBlocksToValue(Float.parseFloat(edgeName), floatArray);
        }
        catch (NumberFormatException nfExc)
        {
            System.out.println("edgeName: " + edgeName);
            JOptionPane.showMessageDialog(layoutFrame, "VertexID " + vertexID + " has a wrong weight format:\n" + nfExc.getMessage() + "\nNow resorting to the default weight value of 1.0 for all timeblocks for this vertexID.", "Error with the vertex weight!", JOptionPane.ERROR_MESSAGE);
            return initFloatArrayAllTimeBlocksToValue(1.0f, floatArray);
        }
        catch (ArrayIndexOutOfBoundsException indxExc)
        {
            System.out.println("array edgeName: " + edgeName);
            JOptionPane.showMessageDialog(layoutFrame, "VertexID " + vertexID + " has a time blocks array index error:\n" + indxExc.getMessage() + "\nNow resorting to the default weight value of 1.0 for all timeblocks for this vertexID.", "Error with the time blocks array index!", JOptionPane.ERROR_MESSAGE);
            return initFloatArrayAllTimeBlocksToValue(1.0f, floatArray);
        }
    }

    /**
    *  Initializes the Float[] array with a given value for all time blocks.
    */
    private float[] initFloatArrayAllTimeBlocksToValue(float value, float[] floatArray)
    {
        for (int i = 0; i < floatArray.length; i++)
            floatArray[i] = value;

        return floatArray;
    }

    /**
    *  Parses the time bounded timeblocks based on the given label.
    */
    private float[] parseTimeBoundedTimeBlocksEdgeName(String edgeName, float[] floatArray, int totalTimeBlocks) throws NumberFormatException, ArrayIndexOutOfBoundsException
    {
        // make sure to first init the Float array with a value of 1.0f, in case some of the time block ranges are not specified
        initFloatArrayAllTimeBlocksToValue(1.0f, floatArray);

        float value = 0.0f;
        int startingTimeBlock = 0;
        int endingTimeBlock = 0;
        String[] timeBlockRanges = null;
        String[] timeBlocksAndWeights = null;
        String[] allTimeBlockWeights = edgeName.split(WEIGHTS_SEPARATOR_DELIMITER);
        for (String timeBlockWeight : allTimeBlockWeights)
        {
            timeBlocksAndWeights = timeBlockWeight.split(WEIGHTS_VALUE_DELIMITER);
            timeBlockRanges = timeBlocksAndWeights[0].split(WEIGHTS_TIMEBLOCK_DELIMITER);
            if ( (startingTimeBlock = Integer.parseInt(timeBlockRanges[0]) - 1) < 0 )
                startingTimeBlock = 0;
            if ( (endingTimeBlock = Integer.parseInt(timeBlockRanges[1])) > totalTimeBlocks )
                endingTimeBlock = totalTimeBlocks;
            value = Float.parseFloat(timeBlocksAndWeights[1]);
            for (int i = startingTimeBlock; i < endingTimeBlock; i++)
                floatArray[i] = value;
        }

        return floatArray;
    }

    /**
    *  Cleans (deletes) all data structures.
    */
    private void clean(int totalRuns)
    {
        transitionIDs = null;
        parentsIDs = null;
        childrenIDs = null;
        childrenWeights = null;
        totalInhibitorsIDs = null;
        partialInhibitorsIDs = null;
        allArraysLengths = null;

        timeTaken = 0;
        if ( ( USE_MULTICORE_PROCESS && USE_SPN_N_CORE_PARALLELISM.get() ) && (totalRuns >= MINIMUM_NUMBER_OF_SPN_RUNS_FOR_PARALLELIZATION) )
        {
            int z = NUMBER_OF_AVAILABLE_PROCESSORS;
            while (--z >= 1) // does not need to add to the first threadId as all results are aggregated to the first one
                results[z] = null;
        }

        System.gc();
    }

    /**
    *  Writes all the SPN simulation results in a file.
    */
    public void SPNSimulationResultsWriteToFile(int totalTimeBlocks, int totalRuns)
    {
        boolean doSaveFile = false;
        FileWriter fileWriter = null;

        try
        {
            String filename = layoutFrame.getFileNameLoaded() + "_SPN_Results_TimeBlocks_" + totalTimeBlocks + "_Runs_" + totalRuns + "." + SupportedSimulationFileTypes.SPN.toString().toLowerCase();
            String saveFile = ( !SAVE_SPN_RESULTS_FILE_NAME.get().isEmpty() ) ? SAVE_SPN_RESULTS_FILE_NAME.get() + System.getProperty("file.separator") + filename : filename;

            int dialogReturnValue = 0;
            if ( new File(saveFile).exists() )
            {
                // make sure previous progress bar is not on
                layoutProgressBarDialog.endProgressBar();
                layoutProgressBarDialog.stopProgressBar();

                // do we want to overwrite
                dialogReturnValue = JOptionPane.showConfirmDialog(layoutFrame, "This File Already Exists.\nDo you want to Overwrite it?", "This File Already Exists. Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);
                if (dialogReturnValue == JOptionPane.YES_OPTION)
                    doSaveFile = true;
            }
            else
            {
                doSaveFile = true;
            }

            if (doSaveFile)
            {
                layoutProgressBarDialog.prepareProgressBar(nc.getVertices().size(), "Now Saving SPN Results File...");
                layoutProgressBarDialog.startProgressBar();

                fileWriter = new FileWriter(saveFile);
                fileWriter.write("//" + VERSION + " " + " SPN Results File\n");
                fileWriter.write("//SPN_RESULTS\t\"" + layoutFrame.getFileNameLoaded() + "\"\t\"" + SAVE_DETAILS_DATA_COLUMN_NAME_NODES +      numberOfVertices +
                                                                                         "\"\t\"" + SAVE_DETAILS_DATA_COLUMN_NAME_TIMEBLOCKS + totalTimeBlocks +
                                                                                         "\"\t\"" + SAVE_DETAILS_DATA_COLUMN_NAME_RUNS +       totalRuns + "\"\n");
                fileWriter.write("Node ID\tGraphml Node Key\tNode Name\t");
                for (int i = 1; i <= results[0].length; i++) // for every timeblock
                    fileWriter.write("TimeBlock: " + i + "\t"); // write the timeblock ID
                fileWriter.write("\n");

                for ( Vertex vertex: nc.getVertices() )
                {
                    if ( !vertex.ismEPNTransition() )
                    {
                        fileWriter.write(vertex.getVertexID() + "\t" + vertex.getVertexName() + "\t" + nc.getNodeName( vertex.getVertexName() ).replace(" ", "").replace("\n", "_") + "\t"); // write the node's id & name
                        for (int i = 0; i < results[0].length; i++) // for every timeblock
                            fileWriter.write(results[0][i][vertex.getVertexID()] + "\t"); // write the node's value at this timeblock
                        fileWriter.write("\n");
                    }

                    layoutProgressBarDialog.incrementProgress();
                }

                fileWriter.flush();
            }
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD) println("IOException in SignalingPetriNetSimulation.SPNSimulationResultsWriteToFile():\n" + ioe.getMessage());

            layoutProgressBarDialog.endProgressBar();
            layoutProgressBarDialog.stopProgressBar();
            JOptionPane.showMessageDialog(layoutFrame, "Something went wrong while saving the file:\n" + ioe.getMessage() + "\nPlease try again with a different file name/path/drive.", "Error with saving the file!", JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            try
            {
                if (fileWriter != null) fileWriter.close();
            }
            catch (IOException ioe)
            {
                if (DEBUG_BUILD) println("IOException while closing streams in SignalingPetriNetSimulation.SPNSimulationResultsWriteToFile():\n" + ioe.getMessage());
            }
            finally
            {
                if (doSaveFile)
                {
                    layoutProgressBarDialog.endProgressBar();
                    layoutProgressBarDialog.stopProgressBar();
                }
            }
        }
    }

    /**
    *  Gets the time the SPN simulation results have taken to process.
    */
    public long getTimeTaken()
    {
        return timeTaken;
    }

    /**
    *  Gets all the SPN simulation results.
    */
    public float[][] getSPNSimulationResults()
    {
        return results[0];
    }

    /**
    *  Gets and clones all the SPN simulation results.
    */
    public float[][] getSPNSimulationClonedResults()
    {
        float[][] resultsCloned = new float[results[0].length][results[0][0].length];
        int x = results[0].length;
        while (--x >= 0)
        {
            /*
            int y = results[0][0].length;
            while(--y >= 0)
            {
                resultsCloned[x][y] = results[0][x][y];
            }
            */

            // for optimizing the array copy
            // resultsCloned[x] = Arrays.copyOf(results[0][x], results[0][x].length);
            System.arraycopy(results[0][x], 0, resultsCloned[x], 0, results[0][x].length); // fastest way, native method!
        }

        return resultsCloned;
    }

    /**
    *  Finds the max value from the SPN simulation results.
    */
    public float findMaxValueFromResultsArray()
    {
        float number = 0.0f;
        float maxValue = results[0][0][0];
        for ( Vertex vertex: nc.getVertices() )
        {
            if ( !vertex.ismEPNTransition() )
            {
                for (int i = 0; i < results[0].length; i++) // for every timeblock
                {
                    number = results[0][i][vertex.getVertexID()];
                    if (maxValue < number)
                        maxValue = number; // Max case
                }
            }
        }

        return maxValue;
    }

    /**
    *  Initializes the results array.
    */
    public void initializeResultsArray(int numberOfVertices, int totalTimeBlocks)
    {
        results = new float[1][totalTimeBlocks][numberOfVertices];
    }

    /**
    *  Adds a result to the result array.
    */
    public void addResultToResultsArray(int nodeID, int timeBlock, float result)
    {
        results[0][timeBlock][nodeID] = result;
    }


}