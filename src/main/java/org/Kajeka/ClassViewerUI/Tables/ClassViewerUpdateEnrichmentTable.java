package org.Kajeka.ClassViewerUI.Tables;

import java.awt.Component;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import org.Kajeka.Analysis.*;
import org.Kajeka.ClassViewerUI.*;
import org.Kajeka.ClassViewerUI.Tables.TableModels.*;
import org.Kajeka.CoreUI.*;
import org.Kajeka.CoreUI.Dialogs.*;
import static org.Kajeka.ClassViewerUI.ClassViewerFrame.ClassViewerTabTypes.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;
import static org.Kajeka.DebugConsole.ConsoleOutput.*;
import org.Kajeka.Network.Vertex;
import org.Kajeka.Network.VertexClass;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultHeatMapDataset;

public final class ClassViewerUpdateEnrichmentTable implements Runnable {

    private ClassViewerFrame classViewerFrame = null;
    private HashSet<String> selectedGenes = null;

    private HashMap<VertexClass, HashSet<String>> geneGroups = null;

    private HashSet<String> selectedClasses = null;
    private JTabbedPane tabbedPane = null;
    private Boolean performIndividually = false;
    private JLabel heatmap = null;

    // JFreeChart
    private JFreeChart fisherBarChart = null;

    private RelativeEntropyCalc relEntropyCalc = null;
    private ClassViewerTableModelEnrichment modelDetail = null;
    private LayoutProgressBarDialog layoutProgressBarDialog = null;

    private JTable enrichmentTable = null;

    /**
     * The abortThread variable is used to silently abort the Runnable/Thread.
     */
    private volatile boolean abortThread = false;

    public ClassViewerUpdateEnrichmentTable(ClassViewerFrame classViewerFrame, LayoutFrame layoutFrame, ClassViewerTableModelEnrichment modelDetail, HashSet<String> selectedClasses, HashMap<VertexClass, HashSet<String>> selectedGeneGroups, JTabbedPane tabbedPane, Boolean performIndividually, JLabel heatmap, JTable enrichmentTable) {
        this.classViewerFrame = classViewerFrame;
        this.modelDetail = modelDetail;
        this.geneGroups = selectedGeneGroups;
        this.tabbedPane = tabbedPane;
        this.selectedClasses = selectedClasses;
        this.heatmap = heatmap;
        this.enrichmentTable = enrichmentTable;

        this.performIndividually = performIndividually;

        relEntropyCalc = new RelativeEntropyCalc(layoutFrame.getNetworkRootContainer());
        layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();
    }

    public ClassViewerUpdateEnrichmentTable(ClassViewerFrame classViewerFrame, LayoutFrame layoutFrame, ClassViewerTableModelEnrichment modelDetail, HashSet<String> selectedClasses, HashSet<String> selectedGenes, JTabbedPane tabbedPane) {
        this.classViewerFrame = classViewerFrame;
        this.modelDetail = modelDetail;
        this.selectedGenes = selectedGenes;
        this.tabbedPane = tabbedPane;
        this.selectedClasses = selectedClasses;

        this.performIndividually = performIndividually;

        relEntropyCalc = new RelativeEntropyCalc(layoutFrame.getNetworkRootContainer());
        layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();

    }

    @Override
    public void run() {
        setThreadStarted();
        selectedGenes = geneGroups.values().iterator().next();

        Set<String> annotationClasses = AnnotationTypeManagerBG.getInstanceSingleton().getAllTypes();
        int numberOfAllAnnotationClasses = annotationClasses.size();

        layoutProgressBarDialog.prepareProgressBar(numberOfAllAnnotationClasses, " Calculating analysis values for all terms of all classes...");
        layoutProgressBarDialog.startProgressBar();

        // analysis calc
        int overallEntropiesEntries = 0;

        DefaultHeatMapDataset hmds;

        int loopCount = 1;
        selectedGenes = new HashSet<String>();
        ArrayList<EnrichmentData> list = new ArrayList<>();
        if (!performIndividually) {
            for (Iterator<HashSet<String>> iterator = geneGroups.values().iterator(); iterator.hasNext();) {
                selectedGenes.addAll(iterator.next());
            }
        } else {
            loopCount = geneGroups.size();
        }

        Map<String, Set<String>> groupSubGroups = relEntropyCalc.getGroupSubTerms();
        Iterator<Entry<VertexClass, HashSet<String>>> geneIterator = geneGroups.entrySet().iterator();
        for (int i = 0; i < loopCount; i++) {
            Entry<VertexClass, HashSet<String>> entry = geneIterator.next();
            if (performIndividually) {
                selectedGenes = entry.getValue();
            }
            EnrichmentData enrichmentData = new EnrichmentData();
            list.add(enrichmentData);
            for (String type : annotationClasses) {
                if (performIndividually) {
                    enrichmentData.clusterName = entry.getKey().getName();
                }
                if (!selectedClasses.contains(type)) {
                    continue;
                }
                layoutProgressBarDialog.incrementProgress();

                if (abortThread) {
                    layoutProgressBarDialog.endProgressBar();
                    layoutProgressBarDialog.stopProgressBar();
                    setThreadFinished();
                    return;
                }

                Map<String, Double> entropies = relEntropyCalc.relEntropy4Selection(selectedGenes, type);
                if (entropies != null) {

                    enrichmentData.perType.put(type, entropies);

                    if (DEBUG_BUILD) {
                        println("Doing Fishers:");
                    }

                    Map<String, Double> fisher = relEntropyCalc.fisherTestForEachCluster(selectedGenes, type);

                    if (abortThread) {
                        layoutProgressBarDialog.endProgressBar();
                        layoutProgressBarDialog.stopProgressBar();
                        setThreadFinished();
                        return;
                    }

                    if (DEBUG_BUILD) {
                        println("Doing OverRep:");
                    }

                    enrichmentData.fishers.put(type, fisher);

                    Map<String, HashMap<String, String>> overRepData = relEntropyCalc.overRepForEachCluster(selectedGenes, type);

                    if (abortThread) {
                        layoutProgressBarDialog.endProgressBar();
                        layoutProgressBarDialog.stopProgressBar();
                        setThreadFinished();
                        return;
                    }

                    enrichmentData.Observed.put(type, overRepData.get("Obs"));
                    enrichmentData.Expected.put(type, overRepData.get("Exp"));
                    enrichmentData.Fobs.put(type, overRepData.get("Fobs"));
                    enrichmentData.Fexp.put(type, overRepData.get("Fexp"));
                    enrichmentData.OverRep.put(type, overRepData.get("OverRep"));
                    enrichmentData.ExpectedTrial.put(type, overRepData.get("ExpT"));
                    enrichmentData.Zscore.put(type, overRepData.get("Zscore"));

                    Map<String, Integer> numberOfMember = relEntropyCalc.clusterMembers(selectedGenes, type);
                    enrichmentData.numberOfMembers.put(type, numberOfMember);

                    if (abortThread) {
                        layoutProgressBarDialog.endProgressBar();
                        layoutProgressBarDialog.stopProgressBar();
                        setThreadFinished();
                        return;
                    }

                    overallEntropiesEntries += entropies.size();
                }
            }
        }

        // Generate complete list of used terms
        int countSubTerms = 0;
        Map<String, Set<String>> subTerms = new HashMap<String, Set<String>>();
        for (int i = 0; i < list.size(); i++) {
            int curTermCount = 0;
            for (Iterator<String> iterator = list.get(i).perType.keySet().iterator(); iterator.hasNext();) {
                String groupName = iterator.next();
                Set<String> types = new HashSet<>(list.get(i).perType.get(groupName).keySet());
                Set<String> copyTypes = new HashSet(types);
                if (subTerms.containsKey(groupName)) {
                    // Only add new subclasses
                    copyTypes.removeAll(subTerms.get(groupName));
                    subTerms.get(groupName).addAll(copyTypes);
                    countSubTerms += copyTypes.size();
                } else {
                    subTerms.put(groupName, types);
                    countSubTerms += types.size();
                }
            }
        }
        // Contigious list of subterms (for heatmap)
        ArrayList<String> subTermList = new ArrayList<>();
        for (Iterator<String> iterator = subTerms.keySet().iterator(); iterator.hasNext();) {
            subTermList.addAll(subTerms.get(iterator.next()));
        }

        hmds = new DefaultHeatMapDataset(geneGroups.size(), countSubTerms, 0, 100, 0, 100);
        for (int i = 0; i < hmds.getXSampleCount(); i++) {
            for (int j = 0; j < hmds.getYSampleCount(); j++) {
                hmds.setZValue(i, j, -1);
            }
        }

        System.out.println("Overall entropies: " + overallEntropiesEntries);

        layoutProgressBarDialog.endProgressBar();
        layoutProgressBarDialog.stopProgressBar();

        // add these calculated values to model
        modelDetail.setSize(overallEntropiesEntries);
        layoutProgressBarDialog.prepareProgressBar(list.iterator().next().perType.keySet().size() * list.size(), " Now updating table and inserting values...");
        layoutProgressBarDialog.startProgressBar();

        String[] termNames = subTermList.toArray(new String[1]);
        String[] clusterNames = new String[loopCount];

        Iterator<HashSet<String>> iterator = geneGroups.values().iterator();
        for (int i = 0; i < loopCount; i++) {
            selectedGenes = iterator.next();
            EnrichmentData enrichmentData = list.get(i);
            clusterNames[i] = enrichmentData.clusterName;

            Set<String> keys = enrichmentData.perType.keySet();
            for (String type : keys) {

                layoutProgressBarDialog.incrementProgress();
                Set<String> terms = enrichmentData.perType.get(type).keySet();
                for (String term : terms) {
                    int index = subTermList.indexOf(term);
                    hmds.setZValue(i, index, enrichmentData.fishers.get(type).get(term));
                }
                if (abortThread) {
                    layoutProgressBarDialog.endProgressBar();
                    layoutProgressBarDialog.stopProgressBar();
                    setThreadFinished();
                    return;
                }

                Map<String, Double> entropies = enrichmentData.perType.get(type);
                Map<String, Double> fisher = enrichmentData.fishers.get(type);
                Map<String, Integer> members = enrichmentData.numberOfMembers.get(type);

                modelDetail.addAnalysisValues(enrichmentData.Observed.get(type), enrichmentData.Expected.get(type), enrichmentData.ExpectedTrial.get(type), enrichmentData.Fobs.get(type), enrichmentData.Fexp.get(type),
                        enrichmentData.OverRep.get(type), enrichmentData.Zscore.get(type), entropies, fisher, members, type, enrichmentData.clusterName);
            }

            if (abortThread) {
                layoutProgressBarDialog.endProgressBar();
                layoutProgressBarDialog.stopProgressBar();
                setThreadFinished();
                return;
            }

            layoutProgressBarDialog.prepareProgressBar(0, " Please wait, table structure is rendered...");
            layoutProgressBarDialog.setText("Almost done");
            layoutProgressBarDialog.startProgressBar();

            if (abortThread) {
                layoutProgressBarDialog.endProgressBar();
                layoutProgressBarDialog.stopProgressBar();
                setThreadFinished();
                return;
            }

            tabbedPane.setEnabledAt(ENTROPY_DETAILS_TAB.ordinal(), true);

            if (abortThread) {
                layoutProgressBarDialog.endProgressBar();
                layoutProgressBarDialog.stopProgressBar();
                setThreadFinished();
                return;
            }

            if (abortThread) {
                layoutProgressBarDialog.endProgressBar();
                layoutProgressBarDialog.stopProgressBar();
                setThreadFinished();
                return;
            }

            layoutProgressBarDialog.endProgressBar();
            layoutProgressBarDialog.stopProgressBar();

            if (classViewerFrame.isVisible()) {
                classViewerFrame.processAndSetWindowState();
            }
        }
        if (performIndividually) {
            heatmap.setIcon(new ImageIcon(ClassViewerFrame.generateHeatMap(hmds, clusterNames, termNames)));
        }

        modelDetail.fireTableStructureChanged();

        if (performIndividually) {
            enrichmentTable.getColumnModel().getColumn(5).setCellRenderer(enrichmentTable.getDefaultRenderer(enrichmentTable.getColumnClass(5)));
            enrichmentTable.getColumnModel().getColumn(6).setCellRenderer(new FishersPRenderer());
        } else {
            enrichmentTable.getColumnModel().getColumn(6).setCellRenderer(enrichmentTable.getDefaultRenderer(enrichmentTable.getColumnClass(6)));
            enrichmentTable.getColumnModel().getColumn(5).setCellRenderer(new FishersPRenderer());
        }
        setThreadFinished();
    }

    public void setAbortThread(boolean abortThread) {
        this.abortThread = abortThread;

        relEntropyCalc.setAbortThread(abortThread);
    }

    public boolean getAbortThread() {
        return abortThread;
    }

    boolean threadStarted = false;

    void setThreadStarted() {
        threadStarted = true;
    }

    public boolean getThreadStarted() {
        return threadStarted;
    }

    boolean threadFinished = false;

    void setThreadFinished() {
        threadFinished = true;
    }

    public boolean getThreadFinished() {
        return threadFinished;
    }

    static class FishersPRenderer extends DefaultTableCellRenderer {

        private static final DecimalFormat SCIENCEFORMATTER = new DecimalFormat("0.##E0");
        private static final DecimalFormat FORMATTER = new DecimalFormat("0.##");

        public FishersPRenderer() {
            this.setHorizontalAlignment(RIGHT);
        }
        
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            // First format the cell value as required]
            if ((Double)value < 0.1)
                value = SCIENCEFORMATTER.format((Number) value);
            else
                value = FORMATTER.format((Number) value);

            // And pass it on to parent class
            return super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
        }
    }

}
