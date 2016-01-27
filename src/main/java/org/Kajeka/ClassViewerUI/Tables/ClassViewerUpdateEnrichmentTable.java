package org.Kajeka.ClassViewerUI.Tables;

import java.awt.Component;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import org.Kajeka.Analysis.*;
import org.Kajeka.ClassViewerUI.*;
import org.Kajeka.ClassViewerUI.Tables.TableModels.*;
import org.Kajeka.CoreUI.*;
import org.Kajeka.CoreUI.Dialogs.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;
import static org.Kajeka.DebugConsole.ConsoleOutput.*;
import org.Kajeka.Network.VertexClass;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultHeatMapDataset;
import org.jfree.ui.TextAnchor;

public final class ClassViewerUpdateEnrichmentTable implements Runnable {

  private ClassViewerFrame classViewerFrame = null;
  private HashSet<String> selectedGenes = null;

  private HashMap<VertexClass, HashSet<String>> geneGroups = null;

  private HashSet<String> selectedClasses = null;
  private JTabbedPane tabbedPane = null;
  private Boolean performIndividually = false;
  private ClassViewerFrame.JHeatMap heatmap = null;
  private JPanel chartPanel = null;

  // JFreeChart
  private JFreeChart fisherBarChart = null;

  private RelativeEntropyCalc relEntropyCalc = null;
  private ClassViewerTableModelEnrichment modelDetail = null;
  private LayoutProgressBarDialog layoutProgressBarDialog = null;

  private JTable enrichmentTable = null;

  private DefaultHeatMapDataset hmds = null;
  private DefaultCategoryDataset pValueChartDataset = null;
  private HashMap<String, Integer> elementPosition = null;

  /**
   * The abortThread variable is used to silently abort the Runnable/Thread.
   */
  private volatile boolean abortThread = false;

  public ClassViewerUpdateEnrichmentTable(ClassViewerFrame classViewerFrame, LayoutFrame layoutFrame, ClassViewerTableModelEnrichment modelDetail, HashSet<String> selectedClasses, HashMap<VertexClass, HashSet<String>> selectedGeneGroups, JTabbedPane tabbedPane, Boolean performIndividually, ClassViewerFrame.JHeatMap heatmap, JTable enrichmentTable, JPanel chartFrame) {
    this.classViewerFrame = classViewerFrame;
    this.modelDetail = modelDetail;
    this.geneGroups = selectedGeneGroups;
    this.tabbedPane = tabbedPane;
    this.selectedClasses = selectedClasses;
    this.heatmap = heatmap;
    this.enrichmentTable = enrichmentTable;
    this.chartPanel = chartFrame;

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
    if (geneGroups.size() == 0) {
      modelDetail.setSize(0);
      return;
    }
    setThreadStarted();
    selectedGenes = geneGroups.values().iterator().next();

    Set<String> annotationClasses = AnnotationTypeManagerBG.getInstanceSingleton().getAllTypes();
    int numberOfAllAnnotationClasses = annotationClasses.size();

    // analysis calc
    int overallEntropiesEntries = 0;

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

    layoutProgressBarDialog.prepareProgressBar(numberOfAllAnnotationClasses * loopCount, " Calculating analysis values for all terms of all classes...");
    layoutProgressBarDialog.startProgressBar();

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

          Map<String, Integer> numberOfMember = relEntropyCalc.totalTermCount(selectedGenes, type);
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
        hmds.setZValue(i, j, 0);
      }
    }

    elementPosition = new HashMap<String, Integer>();

    System.out.println("Overall entropies: " + overallEntropiesEntries);

    layoutProgressBarDialog.endProgressBar();
    layoutProgressBarDialog.stopProgressBar();

    // add these calculated values to model
    modelDetail.setSize(overallEntropiesEntries);
    layoutProgressBarDialog.prepareProgressBar(numberOfAllAnnotationClasses * loopCount, " Now updating table and inserting values...");
    layoutProgressBarDialog.startProgressBar();

    String[] termNames = subTermList.toArray(new String[1]);
    String[] clusterNames = new String[loopCount];
    chartPanel.removeAll();
    Iterator<HashSet<String>> iterator = geneGroups.values().iterator();
    int indexCounter = 0;
    for (int i = 0; i < loopCount; i++) {
      selectedGenes = iterator.next();
      EnrichmentData enrichmentData = list.get(i);
      clusterNames[i] = enrichmentData.clusterName;
      DefaultCategoryDataset chartData = new DefaultCategoryDataset();

      Set<String> keys = enrichmentData.perType.keySet();
      for (String type : keys) {
        layoutProgressBarDialog.incrementProgress();
        Set<String> terms = enrichmentData.perType.get(type).keySet();
        for (String term : terms) {
          int index = subTermList.indexOf(term);
          modelDetail.setHeatmapData(i + " " + index, indexCounter);
          int multiplyer = (Double.parseDouble(enrichmentData.OverRep.get(type).get(term)) < 1) ? -1 : 1;
          double adjustPValue = enrichmentData.fishers.get(type).get(term) * enrichmentData.fishers.get(type).size();
          if (adjustPValue < 0.05) {
            chartData.setValue(0.05 - adjustPValue, term, type);
          }
          hmds.setZValue(i, index, adjustPValue * multiplyer);

          indexCounter++;
        }
        if (abortThread) {
          layoutProgressBarDialog.endProgressBar();
          layoutProgressBarDialog.stopProgressBar();
          setThreadFinished();
          return;
        }

        final Map<String, Double> entropies = enrichmentData.perType.get(type);
        final Map<String, Double> fisher = enrichmentData.fishers.get(type);
        final Map<String, Integer> members = enrichmentData.numberOfMembers.get(type);

        final EnrichmentData enrichmentFinal = enrichmentData;
        final String typeFinal = type;

        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            modelDetail.addAnalysisValues(enrichmentFinal.Observed.get(typeFinal), enrichmentFinal.Expected.get(typeFinal), enrichmentFinal.ExpectedTrial.get(typeFinal), enrichmentFinal.Fobs.get(typeFinal), enrichmentFinal.Fexp.get(typeFinal),
                    enrichmentFinal.OverRep.get(typeFinal), enrichmentFinal.Zscore.get(typeFinal), entropies, fisher, members, typeFinal, enrichmentFinal.clusterName);
            modelDetail.fireTableStructureChanged();
            if (performIndividually) {
              enrichmentTable.getColumnModel().getColumn(5).setCellRenderer(enrichmentTable.getDefaultRenderer(enrichmentTable.getColumnClass(5)));
              enrichmentTable.getColumnModel().getColumn(6).setCellRenderer(new FishersPRenderer());
              enrichmentTable.getColumnModel().getColumn(7).setCellRenderer(new FishersPRenderer());
            } else {
              enrichmentTable.getColumnModel().getColumn(6).setCellRenderer(new FishersPRenderer());
              enrichmentTable.getColumnModel().getColumn(5).setCellRenderer(new FishersPRenderer());
              enrichmentTable.getColumnModel().getColumn(7).setCellRenderer(enrichmentTable.getDefaultRenderer(enrichmentTable.getColumnClass(7)));
            }
          }
        });
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

      if (chartData.getRowKeys().size() > 0){
        String title = enrichmentData.clusterName;
        if (enrichmentData.clusterName == null){
          title = "Combined ";
        }
        JFreeChart chart = ChartFactory.createBarChart(
                title + " Significant Values", // chart title
                title, // domain axis label
                "(0.05 - p-Value)", // range axis label
                chartData, // data
                PlotOrientation.HORIZONTAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
        );
        chart.getCategoryPlot().getRangeAxis().setRange(0.0, 0.05d);
        chartPanel.add(new ChartPanel(chart));
      }

      if (abortThread) {
        layoutProgressBarDialog.endProgressBar();
        layoutProgressBarDialog.stopProgressBar();
        setThreadFinished();
        return;
      }

    }
    //tabbedPane.setEnabledAt(ENTROPY_DETAILS_TAB.ordinal(), true);

    layoutProgressBarDialog.endProgressBar();
    layoutProgressBarDialog.stopProgressBar();

    if (classViewerFrame.isVisible()) {
      classViewerFrame.processAndSetWindowState();
    }
    if (performIndividually) {
      heatmap.updateHeatMap(hmds, clusterNames, termNames);

    }

    // Set the columns to render in enough precision
    try {
      Thread.sleep(5000);
    } catch (InterruptedException ex) {
      Logger.getLogger(ClassViewerUpdateEnrichmentTable.class.getName()).log(Level.SEVERE, null, ex);
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

      if (value == null) {
        return super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
      }

      // First format the cell value as required]
      if ((Double) value < 0.1) {
        value = SCIENCEFORMATTER.format((Number) value);
      } else {
        value = FORMATTER.format((Number) value);
      }

      // And pass it on to parent class
      return super.getTableCellRendererComponent(
              table, value, isSelected, hasFocus, row, column);
    }
  }

}
