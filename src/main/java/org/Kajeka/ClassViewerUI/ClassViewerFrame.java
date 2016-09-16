package org.Kajeka.ClassViewerUI;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import org.Kajeka.Analysis.AnnotationTypeManagerBG;
import org.Kajeka.ClassViewerUI.Dialogs.*;
import org.Kajeka.ClassViewerUI.Tables.*;
import org.Kajeka.ClassViewerUI.Tables.TableModels.*;
import org.Kajeka.CoreUI.*;
import org.Kajeka.Graph.GraphElements.*;
import org.Kajeka.Graph.Selection.SelectionUI.Dialogs.*;
import org.Kajeka.Network.*;
import org.Kajeka.StaticLibraries.*;
import org.Kajeka.Utils.*;
import static org.Kajeka.ClassViewerUI.ClassViewerFrame.ClassViewerTabTypes.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;
import static org.Kajeka.DebugConsole.ConsoleOutput.*;
import org.Kajeka.Correlation.Panels.CorrelationGraphPanel;
import org.Kajeka.Simulation.Panels.SimulationResultsPanel;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultHeatMapDataset;
import org.jfree.data.general.HeatMapDataset;

/**
 *
 * The Class Viewer Frame class.
 *
 * @author Thanos Theo, 2008-2009-2010-2011
 * @version 3.0.0.0
 *
 */
public final class ClassViewerFrame extends JFrame implements ActionListener, ListSelectionListener, ChangeListener, ItemListener {

    private static final Logger logger = Logger.getLogger(ClassViewerFrame.class.getName());
    /**
     * Serial version UID variable for the ClassViewerFrame class.
     */
    public static final long serialVersionUID = 111222333444555791L;

    public static enum ClassViewerTabTypes {

        GENERAL_TAB, ENRICHMENT_TAB
    }
    private static final int NAME_COLUMN = 1;
    private static final int TIME_TO_SLEEP_TO_ABORT_THREADS = 50;

    private LayoutFrame layoutFrame = null;
    private JTabbedPane tabbedPane = null;
    private HashSet<GraphNode> oldSelection = null;

    // general table
    private ClassViewerTable generalTable = null;
    private ClassViewerTableModelGeneral tableModelGeneral = null;
    private TableRowSorter<ClassViewerTableModelGeneral> generalTableSorter = null;
    private AbstractAction classViewerDialogAction = null;
    private AbstractAction okAction = null;
    private JComboBox<String> classSetsBox = null;
    private JCheckBox viewAllClassSets = null;
    private AbstractAction refreshSelectionInTableAction = null;
    private JCheckBox autoSizeColumnsCheckBox = null;
    private JCheckBox showTransposePlotsCheckbox = null;
    private JButton selectDeselectAllButton = null;
    private boolean selectDeselectAllButtonModeState = false;
    private boolean updateResetSelectDeselectAllButton = true;
    private JCheckBox highlightIsSelectionCheckbox = null;

    private ClassViewerPlotPanel plotPanel = null;
    private FindNameDialog findNameDialog = null;
    private FindClassDialog findClassDialog = null;
    private FindMultipleClassesDialog findMultipleClassesDialog = null;

    private JPanel tabGeneralPanel = null;
    private JPanel generalTablePanel = null;
    private JSplitPane splitPane = null;
    private JPanel chartPane = null;
    private JScrollPane scrollChartPane = null;

    private JButton renderAllCurrentClassSetPlotImagesToFilesButton = null;
    private JButton renderPlotImageToFileButton = null;

    private JButton findNameButton = null;
    private JButton findClassButton = null;
    private JButton findMultipleClassesButton = null;
    private JButton previousClassButton = null;
    private JButton nextClassButton = null;

    private AbstractAction findNameAction = null;
    private AbstractAction findClassAction = null;
    private AbstractAction findMultipleClassesAction = null;
    private AbstractAction previousClassAction = null;
    private AbstractAction nextClassAction = null;

    private ClassViewerHideColumnsDialog classViewerHideColumnsDialog = null;
    private JButton refreshSelectionInTableButton = null;
    private JButton chooseColumnsToHideButton = null;
    private JButton exportTableAsButton = null;
    private AbstractAction chooseColumnsToHideAction = null;
    private AbstractAction exportTableToFileAction = null;
    private JHeatMap heatmap = null;
    private JScrollPane heatmapPane = null;
    private JPanel topBox = null;
    private JLabel lblStats = null;

    //search database
    private JButton searchDatabaseButton = null;

    // entropy table
    private ClassViewerTable entropyTable = null;
    private ClassViewerTableModelAnalysis entropyTableModel = null;
    private HashSet<String> selectedGenes = null;
    private String annotationClass = "";
    private boolean rebuildClassSets = false;
    private boolean isCombinedSet = false;

    private JButton detailsButton = null;
    private JButton detailsForAllButton = null;
    private AbstractAction detailsAction = null;
    private AbstractAction detailsOfAllAction = null;
    private AbstractAction enrichmentAction = null;
    private AbstractAction exportTableAction = null;
    private AbstractAction heatmapAction = null;
    private AbstractAction displayTableAction = null;
    private AbstractAction displayPValueAction = null;
    private AbstractAction saveHeatmapAction = null;

    // entropy analysis details table
    private ClassViewerTableModelDetail analysisTableModel = null;
    private SelectorTableModel enrichmentSelectorTableModel = null;
    private ClassViewerTableModelEnrichment enrichmentTableModel = null;

    JTable enrichmentSelectorTable = null;
    JTable enrichmentTable = null;
    JScrollPane scrollPaneEnrichmentSelector = null;
    JScrollPane scrollPaneEnrichment = null;
    JPanel selectorContainer = null;
    JPanel tabEnrichmentPanel = null;
    JButton btnDisplayHeatmap = null;
    JButton btnDisplayTable = null;
    JButton btnDisplayPValue = null;
    JButton btnSaveHeatmap = null;
    JButton btnRunEnrichment = null;
    JButton btnExportTable = null;
    JLabel lblComparisonMode = null;
    JCheckBox chkHeatmapGrid = null;
    JCheckBox chkDisplayPValue = null;
    JCheckBox chkDisplayLogValue = null;
    JCheckBox chkShowOnlyEnriched = null;
    JPanel leftBox = null;
    JPanel rightBox = null;
    JSplitPane enrichmentSplitPane = null;

    private JComboBox<String> cmbClassSelector;
    private JComboBox<String> cmbComparisonMode;

    private ClassViewerUpdateEntropyTable updateEntropyTableRunnable = null;
    private ClassViewerUpdateDetailedEntropyTable updateDetailedEntropyTableRunnable = null;
    private ClassViewerUpdateEnrichmentTable updateDetailedEnrichmentRunnable = null;

    // variables used for proper window event usage
    private boolean isWindowIconified = false;
    private boolean isWindowMaximized = false;
    private boolean windowWasMaximizedBeforeIconification = false;

    private JFileChooser exportTableViewToFileChooser = null;
    private FileNameExtensionFilter fileNameExtensionFilterText = null;

    private int classViewerWidthValue = 0;
    private int prevSplitPaneDividerLocation = 0;
    private String currentClassName = "";

    public ClassViewerFrame(LayoutFrame layoutFrame) {
        super("Class Viewer");

        this.layoutFrame = layoutFrame;

        oldSelection = new HashSet<GraphNode>();
        selectedGenes = new HashSet<String>();

        initFrame(this);
        initActions(this);
        initComponents();
        initExportTableViewToFileChooser();
    }

    private void initFrame(final ClassViewerFrame classViewerFrame) {
        this.setIconImages(ICON_IMAGES);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeClassViewerWindow();
            }

            @Override
            public void windowIconified(WindowEvent e) {
                isWindowIconified = true;
                windowWasMaximizedBeforeIconification = isWindowMaximized; // maximized state is not 'remembered' once frame is iconified, so has to be done manually!
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                isWindowIconified = false;
            }
        });
        this.addWindowStateListener(new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                isWindowMaximized = (getExtendedState() == JFrame.MAXIMIZED_VERT || getExtendedState() == JFrame.MAXIMIZED_HORIZ || getExtendedState() == JFrame.MAXIMIZED_BOTH);
                if (isWindowMaximized) {
                    //FIXME this whole thing smells bad
                    if (splitPane != null && splitPane.getDividerLocation() == (classViewerWidthValue / 2)) // only do this if the slit pane divider is in original location (classViewerWidthValue / 2) of the Class Viewer
                    {
                        validate();
                        splitPane.setDividerLocation(classViewerFrame.getWidth() / 2);
                        prevSplitPaneDividerLocation = splitPane.getDividerLocation();
                    }
                }
            }
        });
    }

    /**
     * Displays the Class Viewer. May be called by an Action or
     * programmatically. Initializes the Class Viewer if not already visible. If
     * visible, deiconifies the frame, maximizes and brings to the front.
     */
    public void displayClassViewer() {
        if (!isVisible()) {
            initializeCommonComponents();

            if (getExtendedState() != JFrame.NORMAL) {
                setExtendedState(JFrame.NORMAL);
            }

            classViewerWidthValue = (SCREEN_DIMENSION.width > 1280) ? (int) (0.75 * SCREEN_DIMENSION.width) : 1010;
            int classViewerHeightValue = (SCREEN_DIMENSION.height > 1024) ? (int) (0.75 * SCREEN_DIMENSION.height) : 680;
            setSize(classViewerWidthValue, classViewerHeightValue);
            setLocationRelativeTo(null);
            setVisible(true);

            if ((this.getWidth() + 1.5 * classViewerHideColumnsDialog.getWidth()) > SCREEN_DIMENSION.width) {
                classViewerHideColumnsDialog.setLocation((SCREEN_DIMENSION.width - this.getWidth()) / 2, (SCREEN_DIMENSION.height - classViewerHideColumnsDialog.getHeight()) / 2);
            } else {
                classViewerHideColumnsDialog.setLocation((SCREEN_DIMENSION.width - this.getWidth()) / 2 - classViewerHideColumnsDialog.getWidth(), (SCREEN_DIMENSION.height - classViewerHideColumnsDialog.getHeight()) / 2);
            }

            // only if data is loaded, otherwise the divider location will have been already set to 0
            if (splitPane != null) {
                splitPane.setDividerLocation(this.getWidth() / 2);
                prevSplitPaneDividerLocation = splitPane.getDividerLocation();
            }

            // make sure to clear all plot/tables if current selection is empty
            if (layoutFrame.getGraph().getSelectionManager().getSelectedNodes().isEmpty()) {
                populateClassViewer();
            }

            if (plotPanel != null) {
                plotPanel.onFirstShown();
            }
        } else {
            processAndSetWindowState();
        }
    }

    private void initActions(final ClassViewerFrame classViewerFrame) {
        classViewerDialogAction = new AbstractAction("Class Viewer") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 111222333444555992L;

            @Override
            public void actionPerformed(ActionEvent e) {
                displayClassViewer();
            }
        };
        classViewerDialogAction.setEnabled(false);

        findNameAction = new AbstractAction("Find By Name") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 112222333444555993L;

            @Override
            public void actionPerformed(ActionEvent e) {
                findNameDialog.setVisible(true);
            }
        };

        findClassAction = new AbstractAction("Find By Class") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 112222333444555993L;

            @Override
            public void actionPerformed(ActionEvent e) {
                findClassDialog.setVisible(true);
            }
        };

        findMultipleClassesAction = new AbstractAction("Find By Multiple Classes") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 112222333444555993L;

            @Override
            public void actionPerformed(ActionEvent e) {
                findMultipleClassesDialog.openDialogWindow();
            }
        };

        previousClassAction = new AbstractAction("◄◄ (Previous Class)") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 112222333444555993L;

            @Override
            public void actionPerformed(ActionEvent e) {
                navigateToPreviousClass();
            }
        };

        nextClassAction = new AbstractAction("►► (Next Class)") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 112222333444555993L;

            @Override
            public void actionPerformed(ActionEvent e) {
                navigateToNextClass();
            }
        };

        refreshSelectionInTableAction = new AbstractAction("Hide Unselected Rows") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 111222333444555993L;

            @Override
            public void actionPerformed(ActionEvent e) {
                populateClassViewer(false, true);
            }
        };

        chooseColumnsToHideAction = new AbstractAction("Choose Columns To Hide") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 111222333444555993L;

            @Override
            public void actionPerformed(ActionEvent e) {
                classViewerHideColumnsDialog.setVisible(true);
            }
        };

        exportTableToFileAction = new AbstractAction("Export Table As...") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 111222333444555793L;

            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        };

        okAction = new AbstractAction("Close") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 111222333444555793L;

            @Override
            public void actionPerformed(ActionEvent e) {
                closeClassViewerWindow();
            }
        };

        detailsAction = new AbstractAction("Details") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 111222333444555794L;

            @Override
            public void actionPerformed(ActionEvent e) {
                // disable any running thread
                checkAndAbortUpdateEntropyTableRunnable();
                checkAndAbortUpdateDetailedEntropyTableRunnable();

                updateEntropyTableRunnable = new ClassViewerUpdateEntropyTable(classViewerFrame, layoutFrame, annotationClass, analysisTableModel, selectedGenes, tabbedPane);
                executeRunnableInThread(updateEntropyTableRunnable);
            }
        };

        detailsOfAllAction = new AbstractAction("Details For All") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 111222333444555795L;

            @Override
            public void actionPerformed(ActionEvent e) {
                // disable any running thread
                checkAndAbortUpdateEntropyTableRunnable();
                checkAndAbortUpdateDetailedEntropyTableRunnable();

                updateDetailedEntropyTableRunnable = new ClassViewerUpdateDetailedEntropyTable(classViewerFrame, layoutFrame, analysisTableModel, selectedGenes, tabbedPane);
                executeRunnableInThread(updateDetailedEntropyTableRunnable);
            }
        };

        exportTableAction = new AbstractAction("Export Table to CSV") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // parent component of the dialog
                JFrame parentFrame = new JFrame();

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save CSV file");
                fileChooser.setSelectedFile(new File("enrichment.csv"));

                ArrayList<String> list = generateEnrichmentCSVTable();

                int userSelection = fileChooser.showSaveDialog(parentFrame);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();

                    BufferedImage bi = heatmap.getImg();
                    File outputfile = fileToSave;

                    try {
                        Files.write(outputfile.toPath(), list, Charset.forName("UTF-8"));
                    } catch (Exception ex) {

                    }

                }
            }
        };

        enrichmentAction = new AbstractAction("Perform Enrichment analysis") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 111222333444555795L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (enrichmentSelectorTableModel.getSelectedClasses().size() == 0) {
                    return;
                }
                if (cmbComparisonMode.getSelectedItem() == "Individually") {
                    btnDisplayHeatmap.setVisible(true);
                    isCombinedSet = false;
                    if (chkShowOnlyEnriched.isSelected()) {
                        filterEnrichmentTable(false);
                    }
                } else {
                    btnDisplayHeatmap.setVisible(false);
                    isCombinedSet = true;
                    if (chkShowOnlyEnriched.isSelected()) {
                        filterEnrichmentTable(true);
                    }
                }

                chkDisplayLogValue.setSelected(true);
                chkDisplayPValue.setSelected(true);

                displayTable();

                // disable any running thread
                checkAndAbortUpdateEntropyTableRunnable();
                checkAndAbortUpdateDetailedEntropyTableRunnable();

                // Collect Nodes + identify relevant class groups
                Set<GraphNode> selectedNodes = layoutFrame.getGraph().getSelectionManager().getSelectedNodes();
                LinkedHashMap<VertexClass, HashSet<String>> groups = new LinkedHashMap<>();
                Collection<VertexClass> classes = layoutFrame.getNetworkRootContainer().getLayoutClassSetsManager().getCurrentClassSetAllClasses().getAllVertexClasses();
                for (VertexClass className : classes) {
                    HashSet<String> tempList = new HashSet<>();
                    groups.put(className, tempList);
                    for (GraphNode node : selectedNodes) {
                        VertexClass res = layoutFrame.getNetworkRootContainer().getLayoutClassSetsManager().getCurrentClassSetAllClasses().getVertexClass(node.getVertex());
                        if (res.getClassID() == className.getClassID()) {
                            if (groups.containsKey(className)) {
                                groups.get(res).add(node.getNodeName());
                            }
                        }
                    }
                    if (groups.get(className).isEmpty()) {
                        groups.remove(className);
                    }
                }

                HashSet<String> genes = new HashSet<String>();
                for (GraphNode graphNode : selectedNodes) {
                    genes.add(graphNode.getNodeName());
                }

                checkAndAbortUpdateEntropyTableRunnable();
                checkAndAbortUpdateDetailedEntropyTableRunnable();

                updateDetailedEnrichmentRunnable = new ClassViewerUpdateEnrichmentTable(classViewerFrame, layoutFrame, enrichmentTableModel, enrichmentSelectorTableModel.getSelectedClasses(), groups, tabbedPane, cmbComparisonMode.getSelectedItem() == "Individually", heatmap, enrichmentTable, chartPane);
                executeRunnableInThread(updateDetailedEnrichmentRunnable);

            }
        };

        saveHeatmapAction = new AbstractAction("Save Heatmap") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // parent component of the dialog
                JFrame parentFrame = new JFrame();

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Heatmap image");
                fileChooser.setSelectedFile(new File("heatmap.png"));

                int userSelection = fileChooser.showSaveDialog(parentFrame);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();

                    BufferedImage bi = heatmap.getImg();
                    File outputfile = fileToSave;

                    try {
                        ImageIO.write(bi, "png", outputfile);
                    } catch (Exception ex) {

                    }

                }
            }
        };

        heatmapAction = new AbstractAction("Display Heatmap") {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayHeatmap();
            }
        };

        displayPValueAction = new AbstractAction("Display P-value chart") {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayPValueChart();
            }
        };

        displayTableAction = new AbstractAction("Display Enrichment Table") {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayTable();
            }
        };
    }

    public ArrayList<String> generateEnrichmentCSVTable() {
        ArrayList<String> csvFile = new ArrayList<>();
        String columnTitles = "";
        for (int c = 0; c < enrichmentTableModel.getColumnCount(); c++) {
            columnTitles += escapeCommas(enrichmentTableModel.getColumnName(c).toString()) + ",";
        }
        csvFile.add(columnTitles);

        for (int r = 0; r < enrichmentTable.getRowCount(); r++) {
            String line = "";
            for (int c = 0; c < enrichmentTableModel.getColumnCount(); c++) {
                line += escapeCommas(enrichmentTableModel.getValueAt(enrichmentTable.convertRowIndexToModel(r), c).toString()) + ",";
            }
            csvFile.add(line);
        }

        return csvFile;
    }

    private String escapeCommas(String input) {
        return "\"" + input + "\"";
    }

    public void processAndSetWindowState() {
        // this process deiconifies a frame, the maximized bits are not affected
        if (isWindowIconified) {
            int iconifyState = this.getExtendedState();

            // set the iconified bit, inverse process
            // deIconifyState |= Frame.ICONIFIED;
            // clear the iconified bit
            iconifyState &= ~JFrame.ICONIFIED;

            // deiconify the frame
            this.setExtendedState(iconifyState);

            if (windowWasMaximizedBeforeIconification) {
                // this process maximizes a frame; the iconified bit is not affected
                int maximizeState = this.getExtendedState();

                // clear the maximized bits, inverse process
                // minimizeState &= ~Frame.MAXIMIZED_BOTH;
                // set the maximized bits
                maximizeState |= JFrame.MAXIMIZED_BOTH;

                // maximize the frame
                this.setExtendedState(maximizeState);
            }
        }

        this.toFront();
    }
    
    private void hideTransposePlots()
    {
        tabGeneralPanel.remove(splitPane);
        splitPane.remove(generalTablePanel);
        splitPane.setRightComponent(null);
        
        tabGeneralPanel.add(generalTablePanel, BorderLayout.CENTER);

        renderAllCurrentClassSetPlotImagesToFilesButton.setVisible(false);
        renderPlotImageToFileButton.setVisible(false);
    }
    
    private void showTransposePlots()
    {
        tabGeneralPanel.remove(generalTablePanel);
        splitPane.setRightComponent(generalTablePanel);
        tabGeneralPanel.add(splitPane, BorderLayout.CENTER);
        
        renderAllCurrentClassSetPlotImagesToFilesButton.setVisible(true);
        renderPlotImageToFileButton.setVisible(true);
    }

    private void initializeCommonComponents() {
        if (DATA_TYPE.equals(DataTypes.CORRELATION)) {
            // Correlation data
            plotPanel = new CorrelationGraphPanel(this, layoutFrame, layoutFrame.getCorrelationData());
        } else if (DATA_TYPE.equals(DataTypes.GRAPHML) && layoutFrame.getNetworkRootContainer().getIsPetriNet()) {
            // SPN simulation data
            plotPanel = new SimulationResultsPanel(this, layoutFrame);
        } else {
            // No plot at all
            plotPanel = null;
        }

        if (splitPane != null && Arrays.asList(tabGeneralPanel.getComponents()).contains(splitPane)) {
            tabGeneralPanel.remove(splitPane);
        } else if (Arrays.asList(tabGeneralPanel.getComponents()).contains(generalTablePanel)) {
            tabGeneralPanel.remove(generalTablePanel);
        }

        if (plotPanel != null) {
            plotPanel.setMinimumSize(new Dimension(300, 300));

            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, plotPanel, generalTablePanel);
            splitPane.setOneTouchExpandable(true);
            splitPane.setContinuousLayout(false);
            tabGeneralPanel.add(splitPane, BorderLayout.CENTER);

            AbstractAction renderAllCurrentClassSetPlotImagesToFilesAction
                    = plotPanel.getRenderAllCurrentClassSetPlotImagesToFilesAction();
            if (renderAllCurrentClassSetPlotImagesToFilesAction != null) {
                renderAllCurrentClassSetPlotImagesToFilesAction.setEnabled(true);
                renderAllCurrentClassSetPlotImagesToFilesButton.setAction(
                        renderAllCurrentClassSetPlotImagesToFilesAction);
                renderAllCurrentClassSetPlotImagesToFilesButton.setVisible(true);
            } else {
                renderAllCurrentClassSetPlotImagesToFilesButton.setVisible(false);
            }

            AbstractAction renderPlotImageToFileAction = plotPanel.getRenderPlotImageToFileAction();
            if (renderPlotImageToFileAction != null) {
                renderPlotImageToFileAction.setEnabled(true);
                renderPlotImageToFileButton.setAction(renderPlotImageToFileAction);
                renderPlotImageToFileButton.setVisible(true);
            } else {
                renderPlotImageToFileButton.setVisible(false);
            }
        } else {
            tabGeneralPanel.add(generalTablePanel, BorderLayout.CENTER);
            splitPane = null;

            renderAllCurrentClassSetPlotImagesToFilesButton.setVisible(false);
            renderPlotImageToFileButton.setVisible(false);
        }
        
        if (layoutFrame.getCorrelationData().isTransposed())
        {
            showTransposePlotsCheckbox.setVisible(true);
            showTransposePlotsCheckbox.setSelected(false);
            hideTransposePlots();
        } else 
            showTransposePlotsCheckbox.setVisible(false);


        tableModelGeneral.proccessSelected(viewAllClassSets.isSelected());
        selectedGenes = entropyTableModel.proccessSelected();

        ClassComboBox classComboBox = new ClassComboBox(layoutFrame.getLayoutClassSetsManager().getCurrentClassSetAllClasses(), false, false);

        if (!selectedGenes.isEmpty()) {
            classSetsBox.setSelectedItem(layoutFrame.getLayoutClassSetsManager().getCurrentClassSetAllClasses().getClassSetName());
        }

        // Clear it out
        ClassViewerTableModelGeneral model = (ClassViewerTableModelGeneral) generalTable.getModel();
        model.clear();

        generalTable.setDefaultEditor(VertexClass.class, new DefaultCellEditor(classComboBox));
        generalTable.setDefaultRenderer(VertexClass.class, classComboBox.getClassRenderer());
        generalTable.setHighlightIsSelection(false);
        highlightIsSelectionCheckbox.setSelected(false);

        if (DEBUG_BUILD) {
            println("Reinit Due to Initial Init.");
        }

        if (!selectedGenes.isEmpty()) {
            populateClassViewer(false, true); //to update the classComboBox and generalTable with the current selection
        }
    }

    private void checkAndAbortUpdateEntropyTableRunnable() {
        // abort previous thread & sleep before initializing a new one!
        if (updateEntropyTableRunnable != null) {
            if (!updateEntropyTableRunnable.getAbortThread()) {
                updateEntropyTableRunnable.setAbortThread(true);

                if (updateEntropyTableRunnable.getThreadStarted()) {
                    while (!updateEntropyTableRunnable.getThreadFinished());
                }
            }
        }
    }

    private void checkAndAbortUpdateDetailedEntropyTableRunnable() {
        // abort previous thread & sleep before initializing a new one!
        if (updateDetailedEntropyTableRunnable != null) {
            if (!updateDetailedEntropyTableRunnable.getAbortThread()) {
                updateDetailedEntropyTableRunnable.setAbortThread(true);

                if (updateDetailedEntropyTableRunnable.getThreadStarted()) {
                    while (!updateDetailedEntropyTableRunnable.getThreadFinished());
                }
            }
        }
    }

    private void executeRunnableInThread(Runnable runnable) {
        Thread executeThread = new Thread(runnable);
        executeThread.setPriority(Thread.NORM_PRIORITY);
        executeThread.start();
    }

    private void initComponents() {
        if (DEBUG_BUILD) {
            println("Create Class Viewer Frame Elements.");
        }

        this.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                /* code run when component hidden*/
            }

            public void componentShown(ComponentEvent e) {
                populateEnrichmentTab();
            }
        });

        //// GENERAL PANEL ////
        tabGeneralPanel = new JPanel(true);
        tabGeneralPanel.setLayout(new BorderLayout());
        tabGeneralPanel.setBackground(Color.WHITE);

        // topPanel, north
        JPanel generalTopPanel = new JPanel(true);

        classSetsBox = new JComboBox<String>();
        classSetsBox.addItemListener(this);
        classSetsBox.setToolTipText("Class Set");

        viewAllClassSets = new JCheckBox("Show All Class Sets");
        viewAllClassSets.addActionListener(this);
        viewAllClassSets.setToolTipText("Show All Class Sets");

        autoSizeColumnsCheckBox = new JCheckBox("Auto Size Columns");
        autoSizeColumnsCheckBox.addActionListener(this);
        autoSizeColumnsCheckBox.setToolTipText("Auto Size Columns");
        autoSizeColumnsCheckBox.setSelected(CV_AUTO_SIZE_COLUMNS.get());
        
        showTransposePlotsCheckbox = new JCheckBox("Show Transpose Plots");
        showTransposePlotsCheckbox.addActionListener(this);
        showTransposePlotsCheckbox.setToolTipText("Show Transpose Plots");
        showTransposePlotsCheckbox.setVisible(false);
        
        

        // generalTable, center
        tableModelGeneral = new ClassViewerTableModelGeneral(layoutFrame, this);
        generalTable = new ClassViewerTable(tableModelGeneral, ClassViewerTableModelGeneral.ORIGINAL_COLUMN_NAMES, CV_AUTO_SIZE_COLUMNS.get());
        generalTableSorter = new TableRowSorter<ClassViewerTableModelGeneral>(tableModelGeneral);
        generalTable.setRowSorter(generalTableSorter); // provide a sorting mechanism to the table
        generalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        generalTable.setAutoscrolls(true);
        generalTable.sortTableByColumn(NAME_COLUMN, generalTableSorter);

        JScrollPane scrollPane = new JScrollPane(generalTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setUpStringEditor(generalTable);

        generalTablePanel = new JPanel(true);
        generalTablePanel.setLayout(new BoxLayout(generalTablePanel, BoxLayout.Y_AXIS));
        generalTablePanel.add(scrollPane);

        selectDeselectAllButton = createSelectDeselectAllButton();
        selectDeselectAllButton.setToolTipText("Deselect All");

        highlightIsSelectionCheckbox = createHighlightIsSelectionButton();
        highlightIsSelectionCheckbox.setToolTipText("Highlight Is Selection");

        JPanel generalTableButtonPanel = new JPanel(true);
        generalTableButtonPanel.setLayout(new BoxLayout(generalTableButtonPanel, BoxLayout.X_AXIS));
        generalTableButtonPanel.add(highlightIsSelectionCheckbox);
        generalTableButtonPanel.add(selectDeselectAllButton);

        // generalTableButtonPanel.add( Box.createRigidArea( new Dimension(10, 10) ) );
        // generalTableButtonPanel.add( new JButton("Dummy Button 2") );
        generalTablePanel.add(Box.createRigidArea(new Dimension(10, 10)));
        generalTablePanel.add(generalTableButtonPanel);
        generalTablePanel.add(Box.createRigidArea(new Dimension(10, 10)));

        // button panel, south
        JPanel generalButtonPanel = new JPanel(true);

        // graph GUI component
        findNameDialog = new FindNameDialog(layoutFrame, this);
        findClassDialog = new FindClassDialog(layoutFrame, this);
        findMultipleClassesDialog = new FindMultipleClassesDialog(layoutFrame, this);

        //Provide minimum sizes for the two components in the split pane
        scrollPane.setMinimumSize(new Dimension(400, 300));

        findNameButton = new JButton(findNameAction);
        findNameButton.setToolTipText("Find By Name");
        findClassButton = new JButton(findClassAction);
        findClassButton.setToolTipText("Find By Class");
        findMultipleClassesButton = new JButton(findMultipleClassesAction);
        findMultipleClassesButton.setToolTipText("Find By Multiple Classes");
        previousClassButton = new JButton(previousClassAction);
        previousClassButton.setEnabled(false);
        previousClassButton.setToolTipText("◄◄ (Previous Class)");
        nextClassButton = new JButton(nextClassAction);
        nextClassButton.setEnabled(false);
        nextClassButton.setToolTipText("►► (Next Class)");

        renderAllCurrentClassSetPlotImagesToFilesButton = new JButton();
        renderAllCurrentClassSetPlotImagesToFilesButton.setToolTipText("Render Class Set Plots To Files...");
        generalTopPanel.add(renderAllCurrentClassSetPlotImagesToFilesButton);

        renderPlotImageToFileButton = new JButton();
        renderPlotImageToFileButton.setToolTipText("Render Plot To File...");
        generalTopPanel.add(renderPlotImageToFileButton);

        JPanel generalButtonPanelLine1 = new JPanel();
        generalButtonPanelLine1.add(findNameButton);
        generalButtonPanelLine1.add(findClassButton);
        generalButtonPanelLine1.add(findMultipleClassesButton);
        generalButtonPanelLine1.add(Box.createRigidArea(new Dimension(10, 10)));
        generalButtonPanelLine1.add(previousClassButton);
        generalButtonPanelLine1.add(nextClassButton);

        refreshSelectionInTableButton = new JButton(refreshSelectionInTableAction);
        refreshSelectionInTableButton.setEnabled(false);
        refreshSelectionInTableButton.setToolTipText("Hide Unselected Rows");

        exportTableAsButton = new JButton(exportTableToFileAction);
        exportTableAsButton.setEnabled(false);
        exportTableAsButton.setToolTipText("Export Table As...");

        chooseColumnsToHideButton = new JButton(chooseColumnsToHideAction);
        chooseColumnsToHideButton.setEnabled(false);
        chooseColumnsToHideButton.setToolTipText("Choose Columns To Hide");

        /* want to add the same Action as the Import Network menu but if we try
     to add here causes NullPointerException as LayoutFrame is not fully set up yet
     so add the Action later when button is enabled */
        searchDatabaseButton = new JButton();
        setUpSearchDatabaseButton(false); //set texts and disable

        // topPanel, north
        generalTopPanel.add(showTransposePlotsCheckbox);
        generalTopPanel.add(new JLabel("Class Set:"));
        generalTopPanel.add(classSetsBox);
        generalTopPanel.add(viewAllClassSets);
        generalTopPanel.add(refreshSelectionInTableButton);
        generalTopPanel.add(autoSizeColumnsCheckBox);

        tabGeneralPanel.add(generalTopPanel, BorderLayout.NORTH);

        // button panel, south
        JPanel generalButtonPanelLine2 = new JPanel();
        generalButtonPanelLine2.add(chooseColumnsToHideButton);
        generalButtonPanelLine2.add(exportTableAsButton);
        JButton okButton = new JButton(okAction);
        okButton.setToolTipText("Close");
        generalButtonPanelLine2.add(okButton);

        generalButtonPanel.setLayout(new BoxLayout(generalButtonPanel, BoxLayout.Y_AXIS));
        generalButtonPanel.add(generalButtonPanelLine1);
        generalButtonPanel.add(generalButtonPanelLine2);
        tabGeneralPanel.add(generalButtonPanel, BorderLayout.SOUTH);
        entropyTableModel = new ClassViewerTableModelAnalysis(layoutFrame);

        //// ENTROPY PANEL ////
        JPanel tabEntropyPanel = new JPanel(true);
        tabEntropyPanel.setLayout(new BorderLayout());
        tabEntropyPanel.setBackground(Color.WHITE);

        // entropy, center
        entropyTable = new ClassViewerTable(entropyTableModel, ClassViewerTableModelAnalysis.COLUMN_NAMES, CV_AUTO_SIZE_COLUMNS.get());
        entropyTable.setRowSorter(new TableRowSorter<ClassViewerTableModelAnalysis>(entropyTableModel)); // provide a sorting mechanism to the table
        entropyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        entropyTable.setAutoscrolls(true);
        entropyTable.setDefaultRenderer(Double.class, new EntropyTableCellRenderer());
        ((DefaultTableCellRenderer) entropyTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        ListSelectionModel listModel = entropyTable.getSelectionModel();
        listModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listModel.addListSelectionListener(this);

        JScrollPane scrollPaneEntropy = new JScrollPane(entropyTable);
        scrollPaneEntropy.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneEntropy.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        tabEntropyPanel.add(scrollPaneEntropy, BorderLayout.CENTER);

        // ok, south
        JPanel okButtonPanel = new JPanel(true);
        detailsButton = new JButton(detailsAction);
        detailsButton.setEnabled(false);
        detailsButton.setToolTipText("Details");
        detailsForAllButton = new JButton(detailsOfAllAction);
        detailsForAllButton.setEnabled(false);
        detailsForAllButton.setToolTipText("Details For All");

        okButtonPanel.add(detailsButton);
        okButtonPanel.add(detailsForAllButton);
        okButton = new JButton(okAction);
        okButton.setToolTipText("Close");
        okButtonPanel.add(okButton);
        tabEntropyPanel.add(okButtonPanel, BorderLayout.SOUTH);

        // ANALYSIS DETAILS PANEL
        JPanel tabEntropyDetailPanel = new JPanel(true);
        tabEntropyDetailPanel.setLayout(new BorderLayout());
        tabEntropyDetailPanel.setBackground(Color.WHITE);

        // analysis table
        analysisTableModel = new ClassViewerTableModelDetail();
        JTable tblEntropy = new JTable(analysisTableModel);

        ClassViewerTable analysisDetailsTable = new ClassViewerTable(analysisTableModel, ClassViewerTableModelDetail.COLUMN_NAMES, CV_AUTO_SIZE_COLUMNS.get());
        analysisDetailsTable.setRowSorter(new TableRowSorter<ClassViewerTableModelDetail>(analysisTableModel)); // provide a sorting mechanism to the table
        analysisDetailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        analysisDetailsTable.setAutoscrolls(true);
        analysisDetailsTable.setDefaultRenderer(Double.class, new EntropyTableCellRenderer());
        analysisDetailsTable.setDefaultRenderer(Integer.class, new EntropyTableCellRenderer());
        ((DefaultTableCellRenderer) analysisDetailsTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPaneEntropyDetails = new JScrollPane(tblEntropy);
        scrollPaneEntropyDetails.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneEntropyDetails.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        tabEntropyDetailPanel.add(scrollPaneEntropyDetails, BorderLayout.CENTER);

        // ok, south
        JPanel okButtonPanelDetails = new JPanel(true);
        okButton = new JButton(okAction);
        okButton.setToolTipText("Close");
        okButtonPanelDetails.add(okButton);
        tabEntropyDetailPanel.add(okButtonPanelDetails, BorderLayout.SOUTH);

        // Enrichment
        // Create class selector
        cmbClassSelector = new JComboBox<String>();
        // Create Enrichment comparison selector
        cmbComparisonMode = new JComboBox<>(new String[]{"Combined", "Individually"});
        cmbComparisonMode.addItemListener(this);
        cmbComparisonMode.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectorContainer = new JPanel();

        topBox = new JPanel();

        JPanel bottomBox = new JPanel();
        leftBox = new JPanel();
        rightBox = new JPanel();
        rightBox.setLayout(new BorderLayout());
        //centerBox.setLayout();

        lblComparisonMode = new JLabel("Compare Selected Classes:");
        lblComparisonMode.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel comparisonPanel = new JPanel();
        leftBox.setLayout(new GridBagLayout());

        comparisonPanel.add(lblComparisonMode);
        comparisonPanel.add(cmbComparisonMode);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;

        lblStats = new JLabel("Stats");
        lblStats.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftBox.add(lblStats, gbc);
        gbc.gridy = 1;
        leftBox.add(comparisonPanel, gbc);

        tabEnrichmentPanel = new JPanel(true);
        tabEnrichmentPanel.setLayout(new BorderLayout());
        tabEnrichmentPanel.setBackground(Color.WHITE);
        String[] columnNames = {"Class Set",
            "Include in Enrichment?"};
        BarRenderer.setDefaultBarPainter(new StandardBarPainter());

        Set<String> annotationClasses = AnnotationTypeManagerBG.getInstanceSingleton().getAllTypes();
        Object[][] data = new Object[annotationClasses.size()][2];
        int i = 0;
        for (String obj : annotationClasses) {
            data[i][0] = obj;
            data[i][1] = new Boolean(false);
            i++;
        }
        enrichmentSelectorTableModel = new SelectorTableModel(data, columnNames);
        enrichmentSelectorTable = new JTable(enrichmentSelectorTableModel) {
//            @Override
//            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
//                Component component = super.prepareRenderer(renderer, row, column);
//                int rendererWidth = component.getPreferredSize().width;
//                TableColumn tableColumn = getColumnModel().getColumn(column);
//                Component comp = getTableHeader().getDefaultRenderer().getTableCellRendererComponent(this, tableColumn.getHeaderValue(), false, false, 0, 0);
//                int width = comp.getPreferredSize().width;
//                int headerHeight = comp.getPreferredSize().height;
//
//                tableColumn.setPreferredWidth(java.lang.Math.max(java.lang.Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()), width));
//
//                int totalWidth = 0;
//                int totalHeight = headerHeight;
//                for (int i = 0; i < enrichmentSelectorTable.getColumnModel().getColumnCount(); i++) {
//                    totalWidth += enrichmentSelectorTable.getColumnModel().getColumn(i).getPreferredWidth() + enrichmentSelectorTable.getColumnModel().getColumnMargin();
//                }
//                totalHeight += (enrichmentSelectorTable.getRowHeight() + enrichmentSelectorTable.getRowMargin()) * enrichmentSelectorTable.getRowCount();
//                int calcHeight = java.lang.Math.min(totalHeight, leftBox.getSize().height - 10);
//                totalWidth += scrollPaneEnrichmentSelector.getVerticalScrollBar().getPreferredSize().width*5;
//                scrollPaneEnrichmentSelector.setPreferredSize(new Dimension(totalWidth, calcHeight));
//                leftBox.validate();
//                enrichmentSplitPane.validate();
//                scrollPaneEnrichmentSelector.validate();
//
//                return component;
//            }
        };
        enrichmentSelectorTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        enrichmentSelectorTable.getTableHeader().setResizingAllowed(true);
        // Create Enrichment Results table
        enrichmentTableModel = new ClassViewerTableModelEnrichment();
        enrichmentTable = new JTable(enrichmentTableModel);

        //enrichmentTable.setAutoCreateRowSorter(true);
        btnRunEnrichment = new JButton(enrichmentAction);

        btnExportTable = new JButton(exportTableAction);

        btnExportTable.setVisible(
                false);
        topBox.add(btnExportTable);

        chkShowOnlyEnriched = new JCheckBox("Show Only Enriched");
        chkShowOnlyEnriched.setVisible(false);
        chkShowOnlyEnriched.setSelected(true);
        chkShowOnlyEnriched.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e
            ) {
                if (chkShowOnlyEnriched.isSelected()) {
                    filterEnrichmentTable(enrichmentTableModel.getColumnCount() == 7);
                } else {
                    removeFilterEnrichmentTable();
                }
            }
        }
        );
        topBox.add(chkShowOnlyEnriched);

        chkHeatmapGrid = new JCheckBox("Grid lines");
        chkHeatmapGrid.setVisible(false);

        chkHeatmapGrid.setSelected(
                true);
        chkHeatmapGrid.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e
            ) {
                heatmap.setGridLines(chkHeatmapGrid.isSelected());
                heatmap.validate();
            }
        }
        );
        topBox.add(chkHeatmapGrid);

        chkDisplayPValue = new JCheckBox("Display Adj. p-Value Charts");
        chkDisplayPValue.setVisible(false);

        chkDisplayPValue.setSelected(
                true);
        chkDisplayPValue.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                for (int j = 1; j < chartPane.getComponentCount(); j += 2) {
                    chartPane.getComponent(j).setVisible(chkDisplayPValue.isSelected());
                }
            }
        }
        );
        topBox.add(chkDisplayPValue);

        chkDisplayLogValue = new JCheckBox("Display Scaled Adj. p-Value Charts");
        chkDisplayLogValue.setVisible(false);

        chkDisplayLogValue.setSelected(
                true);
        chkDisplayLogValue.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                for (int j = 0; j < chartPane.getComponentCount(); j += 2) {
                    chartPane.getComponent(j).setVisible(chkDisplayLogValue.isSelected());
                }
            }
        }
        );
        topBox.add(chkDisplayLogValue);

        btnSaveHeatmap = new JButton(saveHeatmapAction);

        btnSaveHeatmap.setVisible(false);

        topBox.add(btnSaveHeatmap);

        btnDisplayHeatmap = new JButton(heatmapAction);
        btnDisplayHeatmap.setVisible(false);
        topBox.add(btnDisplayHeatmap);

        btnDisplayTable = new JButton(displayTableAction);
        btnDisplayTable.setVisible(false);
        topBox.add(btnDisplayTable);

        btnDisplayPValue = new JButton(displayPValueAction);
        btnDisplayPValue.setVisible(false);
        topBox.add(btnDisplayPValue);

        rightBox.add(topBox, BorderLayout.PAGE_START);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;

        // Create dataset and heatmap
        heatmap = new JHeatMap();

        heatmap.setVisible(
                true);
        heatmapPane = new JScrollPane(heatmap);

        heatmapPane.setVisible(
                false);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        chartPane = new JPanel();
        chartPane.setLayout(new BoxLayout(chartPane, BoxLayout.Y_AXIS));
        scrollChartPane = new JScrollPane(chartPane);

        heatmapPane.setVisible(
                false);

        // TODO: Enrichment fix
        scrollPaneEnrichmentSelector = new JScrollPane(enrichmentSelectorTable);
        //scrollPaneEntropySelector.setPreferredSize(enrichmentSelectorTable);

        scrollPaneEnrichmentSelector.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPaneEnrichmentSelector.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPaneEnrichmentSelector.setAlignmentX(Component.CENTER_ALIGNMENT);

        gbc.weighty = 1;
        //gbc.insets = new Insets(0, scrollPaneEnrichmentSelector.getVerticalScrollBar().getPreferredSize().width, 0, scrollPaneEnrichmentSelector.getVerticalScrollBar().getPreferredSize().width);
        leftBox.add(scrollPaneEnrichmentSelector, gbc);

        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 3;
        gbc.weighty = 0;
        leftBox.add(btnRunEnrichment, gbc);

        scrollPaneEnrichment = new JScrollPane(enrichmentTable);

        scrollPaneEnrichment.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPaneEnrichment.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPaneEnrichment.setVisible(true);

        rightBox.add(scrollPaneEnrichment, BorderLayout.CENTER);

        enrichmentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftBox, rightBox);

        enrichmentSplitPane.setOneTouchExpandable(true);
        enrichmentSplitPane.setDividerLocation(leftBox.getMinimumSize().width);
        //centerBox.add(scrollPaneEnrichment);

        tabEnrichmentPanel.add(enrichmentSplitPane, BorderLayout.CENTER);

        tabEnrichmentPanel.add(bottomBox, BorderLayout.PAGE_END);

        populateEnrichmentTab();

        JPanel okButtonPanelSelector = new JPanel(true);
        okButton = new JButton(okAction);

        okButton.setToolTipText(
                "Close");
        okButtonPanelSelector.add(okButton);

        bottomBox.add(okButtonPanelDetails);

        // create & add to tab pane
        tabbedPane = new JTabbedPane();

        tabbedPane.insertTab(
                "General", null, tabGeneralPanel, "General Node Information", GENERAL_TAB.ordinal());
        // tabbedPane.insertTab("Analysis", null, tabEntropyPanel, "Analysis Calculations", ENTROPY_TAB.ordinal());
        //tabbedPane.add("Analysis Per Term", tabEntropyDetailPanel);
        //tabbedPane.insertTab("Analysis Detailed", null, tabEntropyDetailPanel, "Shows Analysis Per Term", ENTROPY_DETAILS_TAB.ordinal());
        tabbedPane.insertTab("Enrichment", null, tabEnrichmentPanel, "Shows Enrichment", ENRICHMENT_TAB.ordinal());
        //tabbedPane.setEnabledAt(ENTROPY_DETAILS_TAB.ordinal(), false);
        tabbedPane.addChangeListener(
                this);

        // add tab pane to content pane
        this.getContentPane()
                .add(tabbedPane);

        // this.pack();
        this.setSize(
                800, 680);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.setLocationRelativeTo(
                null);

        // at end the ClassViewerHideColumns initialization, to have already initialized the ClassViewer setLocation() method
        classViewerHideColumnsDialog = new ClassViewerHideColumnsDialog(this);
    }

    public void centerView(final JScrollPane scrollPane) {
        scrollPane.revalidate();
        scrollPane.repaint();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Dimension portSize = scrollPane.getViewport().getSize();
                Dimension viewSize = scrollPane.getViewport().getView()
                        .getSize();
                scrollPane.scrollRectToVisible(new Rectangle(
                        (viewSize.width - portSize.width) / 2,
                        (viewSize.height - portSize.height) / 2,
                        portSize.width, portSize.height));
            }
        });
    }

    public static double Lerp(double value, double value2, double amount) {
        amount = java.lang.Math.min(amount, 1);
        return (value + amount * (value2 - value));
    }

    public static MouseAdapter generateMouseListener(HeatMapDataset dataset, String[] columnTitles, String[] rowTitles) {
        final int CELLWIDTH = 20;
        final int CELLHEIGHT = 20;
        final int xCount = dataset.getXSampleCount();
        final int yCount = dataset.getYSampleCount();
        Font font = new Font(new JLabel().getFont().getFontName(), Font.PLAIN, CELLHEIGHT);
        FontRenderContext frc = new FontRenderContext(null, false, false);
        int xTextOffset = 0;
        int yTextOffset = 0;

        // Calculate max xoffset;y
        for (int i = 0; i < rowTitles.length; i++) {
            xTextOffset = (int) java.lang.Math.max((double) new TextLayout(rowTitles[i], font, frc).getBounds().getWidth(), (double) xTextOffset);
        }
        xTextOffset += 5;
        // Calculate max yoffset;
        for (int i = 0; i < columnTitles.length; i++) {
            yTextOffset = (int) java.lang.Math.max((double) new TextLayout(columnTitles[i], font, frc).getBounds().getWidth(), (double) yTextOffset);
        }
        yTextOffset += 5;

        final int offsetX = xTextOffset;
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JHeatMap src = (JHeatMap) e.getSource();
                int cellX = 0;
                int cellY = 0;
                int clickX = e.getX();
                int clickY = e.getY();
                cellX = (clickX - offsetX) / CELLWIDTH;
                cellY = yCount - (clickY) / CELLHEIGHT;
            }
        };
    }

    private JButton createSelectDeselectAllButton() {
        return new JButton(new AbstractAction("Deselect All") {
            /**
             * Serial version UID variable for the AbstractAction class.
             */
            public static final long serialVersionUID = 111222333444555691L;

            @Override
            public void actionPerformed(ActionEvent e) {
                selectDeselectAllButtonModeState = !selectDeselectAllButtonModeState;
                String buttonText = ((!selectDeselectAllButtonModeState) ? "Deselect" : "Select") + " All";
                selectDeselectAllButton.setText(buttonText);
                selectDeselectAllButton.setToolTipText(buttonText);
                tableModelGeneral.setSelectedAllRows(!selectDeselectAllButtonModeState);
                generalTable.setHighlightIsSelection(highlightIsSelectionCheckbox.isSelected());
            }
        });
    }

    private JCheckBox createHighlightIsSelectionButton() {
        return new JCheckBox(new AbstractAction("Highlight Is Selection") {
            @Override
            public void actionPerformed(ActionEvent e) {
                generalTable.setHighlightIsSelection(highlightIsSelectionCheckbox.isSelected());
            }
        });
    }

    private void resetSelectDeselectAllButton() {
        this.selectDeselectAllButtonModeState = false;
        selectDeselectAllButton.setText("Deselect All");
    }

    private void initExportTableViewToFileChooser() {
        String saveFilePath = FILE_CHOOSER_PATH.get().substring(0, FILE_CHOOSER_PATH.get().lastIndexOf(System.getProperty("file.separator")) + 1);
        exportTableViewToFileChooser = new JFileChooser(saveFilePath);
        fileNameExtensionFilterText = new FileNameExtensionFilter("Save as a Text File", "txt");
        exportTableViewToFileChooser.setFileFilter(fileNameExtensionFilterText);
        exportTableViewToFileChooser.setDialogTitle("Export Table View As");
    }

    public AbstractAction getClassViewerAction() {
        return classViewerDialogAction;
    }

    private void setUpStringEditor(JTable table) {
        final JTextField textField = new JTextField();

        DefaultCellEditor stringEditor = new DefaultCellEditor(textField) {
            /**
             * Serial version UID variable for the DefaultCellEditor class.
             */
            public static final long serialVersionUID = 111222333444555796L;

            @Override
            public Object getCellEditorValue() {
                return textField.getText();
            }
        };

        table
                .setDefaultEditor(String.class, stringEditor);
    }

    public void populateEnrichmentTab() {
        addClassSets(cmbClassSelector);
        //enrichmentSelectorTableModel.refreshContent(layoutFrame.getNetworkRootContainer().getLayoutClassSetsManager().getClassSetNames());
        enrichmentSelectorTableModel.refreshContent(AnnotationTypeManagerBG.getInstanceSingleton().getAllTypes());
        enrichmentSplitPane.validate();
        leftBox.validate();
        enrichmentSelectorTable.validate();
        updateStatsLabel();
    }

    public void populateClassViewer() {
        populateClassViewer(null, false, false, true);
    }

    public void populateClassViewer(Object[][] hideColumnsData) {
        populateClassViewer(hideColumnsData, false, false, true);
    }

    public void populateClassViewer(boolean updateCorrelationGraphViewOnly, boolean notUpdateTitleBar) {
        populateClassViewer(null, updateCorrelationGraphViewOnly, notUpdateTitleBar, true);
    }

    public void populateClassViewer(Object[][] hideColumnsData, boolean updateCorrelationGraphViewOnly, boolean notUpdateTitleBar, boolean refreshPlot) {
        NetworkContainer nc = layoutFrame.getNetworkRootContainer();
        if (nc != null) {
            if (!updateCorrelationGraphViewOnly) {
                if (DEBUG_BUILD) {
                    println("populateClassViewer(): " + layoutFrame.getLayoutClassSetsManager().getCurrentClassSetAllClasses().getClassSetName());
                }

                Set<GraphNode> currentSelection = layoutFrame.getGraph().getSelectionManager().getSelectedNodes();

                // only update, if a new set of nodes is selected or it is mandatory
                if (!oldSelection.equals(currentSelection)) {
                    oldSelection = new HashSet<GraphNode>(currentSelection); // don't use the reference
//                    if (!(tabbedPane.getTabCount() < ENTROPY_DETAILS_TAB.ordinal())) {
//                        tabbedPane.setEnabledAt(ENTROPY_DETAILS_TAB.ordinal(), false);
//                        if (tabbedPane.getSelectedIndex() == ENTROPY_DETAILS_TAB.ordinal()) {
//                            tabbedPane.setSelectedIndex(ENTROPY_TAB.ordinal());
//                        }
//
//                        analysisTableModel.setTerm2Entropy(null, null, null, null, null, null, null, null, null, null, null);
//                    }
                }

                tableModelGeneral.proccessSelected(viewAllClassSets.isSelected(), hideColumnsData);
                generalTable.updateTableColumnNames(getGeneralTableColumnNames());

                refreshTables();

                generalTable.setAutoCreateColumnsFromModel(false);

                boolean enableHideColumnsAndExportButtons = (generalTable.getRowCount() > 0);
                refreshSelectionInTableButton.setEnabled(enableHideColumnsAndExportButtons);
                exportTableAsButton.setEnabled(enableHideColumnsAndExportButtons);
                chooseColumnsToHideButton.setEnabled(enableHideColumnsAndExportButtons || classViewerHideColumnsDialog.isVisible());

                //reuse the Action from the Import Network menu option
                searchDatabaseButton.setAction(layoutFrame.getImportWebService().getImportWebServiceAction());
                setUpSearchDatabaseButton(enableHideColumnsAndExportButtons); //set texts and enable

                boolean enableDetailsForAllButton = (entropyTable.getRowCount() > 0);
                detailsForAllButton.setEnabled(enableDetailsForAllButton);

                // disable any running thread
                checkAndAbortUpdateEntropyTableRunnable();
                checkAndAbortUpdateDetailedEntropyTableRunnable();

                if (hideColumnsData == null && classViewerHideColumnsDialog != null) {
                    classViewerHideColumnsDialog.updateClassViewerHideColumnsTable(this, enableHideColumnsAndExportButtons, updateCorrelationGraphViewOnly, notUpdateTitleBar);
                }

                if (generalTable.getColumnCount() < NAME_COLUMN + 1) {
                    System.out.println("generalTable.getColumnCount() " + generalTable.getColumnCount() + " < NAME_COLUMN + 1 " + Thread.currentThread().getStackTrace());
                    /*JOptionPane.showMessageDialog(this, "It is possible a crash is about to occur in relation to the number of columns in the class viewer (currently " +
           generalTable.getColumnCount() + "). Please note down what you were doing up until this point.",
           "Probable crash imminent", JOptionPane.INFORMATION_MESSAGE);*/
                }
                generalTable.sortTableByColumn(NAME_COLUMN, generalTableSorter);
            }

            if (plotPanel != null) {
                if (refreshPlot) {
                    plotPanel.refreshPlot();
                }

                plotPanel.repaint();
            }

            generalTable.repaint();

            checkClassViewerNavigationButtons();
            if (updateResetSelectDeselectAllButton) {
                resetSelectDeselectAllButton();
            }
            if (!notUpdateTitleBar) {
                setCurrentClassName("");
            }
        }
    }

    public void setCurrentClassIndex(int currentClassIndex) {
        findClassDialog.setCurrentClassIndex(currentClassIndex);
    }

    public int numberOfAllClasses() {
        return findClassDialog.numberOfAllClasses();
    }

    public int getClassIndex() {
        return findClassDialog.getClassIndex();
    }

    public void displayHeatmap() {
        chkShowOnlyEnriched.setVisible(false);
        btnExportTable.setVisible(false);
        scrollPaneEnrichment.setVisible(false);

        rightBox.remove(scrollPaneEnrichment);
        rightBox.remove(scrollChartPane);
        if (heatmapPane.getParent() != rightBox) {
            rightBox.add(heatmapPane, BorderLayout.CENTER);
        }
        //tabEnrichmentPanel.add(scrollPaneEnrichment, BorderLayout.CENTER);
        rightBox.validate();
        heatmapPane.setVisible(true);
        scrollChartPane.setVisible(false);

        tabEnrichmentPanel.validate();

        btnDisplayHeatmap.setVisible(false);
        btnDisplayTable.setVisible(true);
        btnDisplayPValue.setVisible(true);

        chkDisplayLogValue.setVisible(false);
        chkDisplayPValue.setVisible(false);

        btnSaveHeatmap.setVisible(true);
        chkHeatmapGrid.setVisible(true);
    }

    public void displayPValueChart() {
        // Update the UI
        btnExportTable.setVisible(false);
        enrichmentTable.setVisible(false);
        chkShowOnlyEnriched.setVisible(false);

        heatmapPane.setVisible(false);
        rightBox.remove(heatmapPane);
        rightBox.remove(scrollPaneEnrichment);
        if (scrollChartPane.getParent() != rightBox) {
            rightBox.add(scrollChartPane, BorderLayout.CENTER);
        }
        //tabEnrichmentPanel.add(scrollPaneEnrichment, BorderLayout.CENTER);
        scrollPaneEnrichment.setVisible(false);
        rightBox.validate();
        scrollChartPane.setVisible(true);

        if (!isCombinedSet) {
            btnDisplayHeatmap.setVisible(true);
        }
        btnDisplayTable.setVisible(true);
        btnDisplayPValue.setVisible(false);

        btnSaveHeatmap.setVisible(false);
        chkHeatmapGrid.setVisible(false);

        chkDisplayLogValue.setVisible(true);
        chkDisplayPValue.setVisible(true);

        if (chartPane.getComponentCount() == 0) {
            JOptionPane.showMessageDialog(this, "No enriched clusters found", "No enriched clusters found", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    public void displayTable() {
        // Update the UI
        btnExportTable.setVisible(true);
        enrichmentTable.setVisible(true);
        chkShowOnlyEnriched.setVisible(true);

        rightBox.remove(heatmapPane);
        rightBox.remove(scrollChartPane);
        if (scrollPaneEnrichment.getParent() != rightBox) {
            rightBox.add(scrollPaneEnrichment, BorderLayout.CENTER);
        }
        //tabEnrichmentPanel.add(scrollPaneEnrichment, BorderLayout.CENTER);
        scrollChartPane.setVisible(false);
        scrollPaneEnrichment.setVisible(true);
        rightBox.validate();

        heatmapPane.setVisible(false);

        if (!isCombinedSet) {
            btnDisplayHeatmap.setVisible(true);
        }

        btnDisplayTable.setVisible(false);
        btnDisplayPValue.setVisible(true);

        chkDisplayLogValue.setVisible(false);
        chkDisplayPValue.setVisible(false);

        btnSaveHeatmap.setVisible(false);
        chkHeatmapGrid.setVisible(false);
    }

    public void displayFullTable() {
        // Update the UI
        btnExportTable.setVisible(true);
        enrichmentTable.setVisible(true);
        chkShowOnlyEnriched.setVisible(true);
        chkShowOnlyEnriched.setSelected(false);

        rightBox.remove(heatmapPane);
        rightBox.remove(scrollChartPane);
        if (scrollPaneEnrichment.getParent() != rightBox) {
            rightBox.add(scrollPaneEnrichment, BorderLayout.CENTER);
        }
        //tabEnrichmentPanel.add(scrollPaneEnrichment, BorderLayout.CENTER);
        scrollChartPane.setVisible(false);
        scrollPaneEnrichment.setVisible(true);
        rightBox.validate();

        heatmapPane.setVisible(false);

        if (!isCombinedSet) {
            btnDisplayHeatmap.setVisible(true);
        }

        btnDisplayTable.setVisible(false);
        btnDisplayPValue.setVisible(true);

        chkDisplayLogValue.setVisible(false);
        chkDisplayPValue.setVisible(false);

        btnSaveHeatmap.setVisible(false);
        chkHeatmapGrid.setVisible(false);
    }

    private void setEnrichmentTabViewForTableResult() {
        // Update the UI
        JButton btn = btnRunEnrichment;
        btn.setText("Select Classes for Enrichment analysis");
        btnExportTable.setVisible(true);

        tabEnrichmentPanel.remove(leftBox);
        tabEnrichmentPanel.remove(heatmapPane);
        //tabEnrichmentPanel.add(scrollPaneEnrichment, BorderLayout.CENTER);
        tabEnrichmentPanel.validate();

        scrollPaneEnrichmentSelector.setVisible(false);
        scrollPaneEnrichment.setVisible(true);

        heatmapPane.setVisible(false);
        cmbComparisonMode.setVisible(false);
        lblComparisonMode.setVisible(false);

        btnDisplayHeatmap.setText("Display Heatmap");
        btnSaveHeatmap.setVisible(false);
        chkHeatmapGrid.setVisible(false);
    }

    private void setEnrichmentTabViewForHeatmap() {
        btnExportTable.setVisible(false);
        scrollPaneEnrichment.setVisible(false);
        heatmapPane.setVisible(true);
        tabEnrichmentPanel.remove(scrollPaneEnrichment);
        //tabEnrichmentPanel.add(heatmapPane, BorderLayout.CENTER);
        tabEnrichmentPanel.validate();
        JButton btn = btnDisplayHeatmap;
        btn.setText("Display Enrichment Table");
        btnSaveHeatmap.setVisible(true);
        chkHeatmapGrid.setVisible(true);
    }

    private void updateStatsLabel() {
        // Count Nodes + Collect Class Group counts
        Set<GraphNode> selectedNodes = layoutFrame.getGraph().getSelectionManager().getSelectedNodes();
        LinkedHashMap<VertexClass, HashSet<String>> groups = new LinkedHashMap<>();
        Collection<VertexClass> classes = layoutFrame.getNetworkRootContainer().getLayoutClassSetsManager().getCurrentClassSetAllClasses().getAllVertexClasses();
        for (VertexClass className : classes) {
            HashSet<String> tempList = new HashSet<>();
            groups.put(className, tempList);
            for (GraphNode node : selectedNodes) {
                VertexClass res = layoutFrame.getNetworkRootContainer().getLayoutClassSetsManager().getCurrentClassSetAllClasses().getVertexClass(node.getVertex());
                if (res.getClassID() == className.getClassID()) {
                    if (groups.containsKey(className)) {
                        groups.get(res).add(node.getNodeName());
                    }
                }
            }
            if (groups.get(className).isEmpty()) {
                groups.remove(className);
            }
        }
        if (selectedNodes.isEmpty()) {
            btnRunEnrichment.setEnabled(false);
        } else {
            btnRunEnrichment.setEnabled(true);
        }

        lblStats.setText(selectedNodes.size() + " Nodes selected across " + groups.size() + " Classes");
    }

    public void synchroniseHighlightWithSelection() {
        if (generalTable != null) {
            generalTable.synchroniseHighlightWithSelection();
        }
        updateStatsLabel();
    }
    
    public void resetView(){
        enrichmentTableModel.setSize(0);
        enrichmentTableModel.fireTableDataChanged();
        displayTable();
    }

    public VertexClass navigateToCurrentClass() {
        VertexClass currentVertexClass = findClassDialog.currentVertexClass();
        if (currentVertexClass != null) {
            setUpdateResetSelectDeselectAllButton(false);
            layoutFrame.getGraph().getSelectionManager().selectByClass(currentVertexClass);
            generalTable
                    .getDefaultEditor(String.class
                    ).stopCellEditing();
            layoutFrame.getGraph().updateSelectedNodesDisplayList();
            setCurrentClassName(currentVertexClass.getName());

            nextClassButton.setEnabled(true);
            setUpdateResetSelectDeselectAllButton(true);
            synchroniseHighlightWithSelection();
        }

        updateStatsLabel();

        return currentVertexClass;
    }

    private VertexClass navigateToPreviousClass() {
        VertexClass previousVertexClass = findClassDialog.previousVertexClass();
        if (previousVertexClass != null) {
            setUpdateResetSelectDeselectAllButton(false);
            layoutFrame.getGraph().getSelectionManager().selectByClass(previousVertexClass);
            generalTable
                    .getDefaultEditor(String.class
                    ).stopCellEditing();
            layoutFrame.getGraph().updateSelectedNodesDisplayList();
            setCurrentClassName(previousVertexClass.getName());

            nextClassButton.setEnabled(true);
            setUpdateResetSelectDeselectAllButton(true);
            synchroniseHighlightWithSelection();
        }

        previousClassButton.setEnabled(findClassDialog.checkPreviousVertexClass());

        return previousVertexClass;
    }

    private VertexClass navigateToNextClass() {
        return navigateToNextClass(true);
    }

    public VertexClass navigateToNextClass(boolean enableTitleBarUpdate) {
        VertexClass nextVertexClass = findClassDialog.nextVertexClass();
        if (nextVertexClass != null) {
            setUpdateResetSelectDeselectAllButton(false);
            layoutFrame.getGraph().getSelectionManager().selectByClass(nextVertexClass);
            generalTable
                    .getDefaultEditor(String.class
                    ).stopCellEditing();
            layoutFrame.getGraph().updateSelectedNodesDisplayList();
            if (enableTitleBarUpdate) {
                setCurrentClassName(nextVertexClass.getName());
            }

            previousClassButton.setEnabled(findClassDialog.getClassIndex() != 0);
            setUpdateResetSelectDeselectAllButton(true);
            synchroniseHighlightWithSelection();
        }

        nextClassButton.setEnabled(findClassDialog.checkNextVertexClass());

        return nextVertexClass;
    }

    private void checkClassViewerNavigationButtons() {
        previousClassButton.setEnabled(findClassDialog.checkPreviousVertexClass());
        nextClassButton.setEnabled(findClassDialog.checkNextVertexClass());
    }

    private void refreshTables() {
        if (tabbedPane.getSelectedIndex() == GENERAL_TAB.ordinal()) {
            if (DEBUG_BUILD) {
                println("refreshTables() General Tab.");
            }

            setUpdateResetSelectDeselectAllButton(false);
            rebuildClassSets();
            tableModelGeneral.fireTableStructureChanged();
            setVertexClassSortingToGeneralTable();
            setUpdateResetSelectDeselectAllButton(true);
        }
//        } else if (tabbedPane.getSelectedIndex() == ENTROPY_TAB.ordinal()) {
//            if (DEBUG_BUILD) {
//                println("refreshTables() Entropy Tab.");
//            }
//
//            selectedGenes = entropyTableModel.proccessSelected();
//            entropyTableModel.fireTableStructureChanged();
//        }
    }

    private void rebuildClassSets() {
        if (DEBUG_BUILD) {
            println("Rebuilding ClassSets for the Class Viewer.");
        }

        rebuildClassSets = true;
        classSetsBox.removeAllItems();
        addClassSets(classSetsBox);

        classSetsBox.setSelectedItem(layoutFrame.getLayoutClassSetsManager().getCurrentClassSetAllClasses().getClassSetName());

        rebuildClassSets = false;
    }

    private void setVertexClassSortingToGeneralTable() {
        String[] generalTableColumnNames = getGeneralTableColumnNames();
        for (int i = 0; i < generalTableColumnNames.length; i++) {
            if (!tableModelGeneral.findNonVertexClassColumnNamesInOriginalColumnNameArray(generalTableColumnNames[i])) {
                generalTableSorter.setComparator(i, new ClassViewerTable.VertexClassSorting());
            }
        }
    }

    private void addClassSets(JComboBox<String> comboBox) {
        comboBox.removeAllItems();

        if (DEBUG_BUILD) {
            println("Adding Class Sets to Class Viewer.");
        }

        if (layoutFrame.getNetworkRootContainer() != null) {
            if (layoutFrame.getNetworkRootContainer().getLayoutClassSetsManager() != null) {
                for (LayoutClasses classes : layoutFrame.getNetworkRootContainer().getLayoutClassSetsManager().getClassSetNames()) {
                    comboBox.addItem(classes.getClassSetName());
                }
            }
        }
    }

    private void save() {
        int dialogReturnValue = 0;
        boolean doSaveFile = false;
        File saveFile = null;

        exportTableViewToFileChooser.setSelectedFile(new File(IOUtils.getPrefix(layoutFrame.getFileNameLoaded()) + "_Table_View"));

        if (exportTableViewToFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String extension = fileNameExtensionFilterText.getExtensions()[0];
            String fileName = exportTableViewToFileChooser.getSelectedFile().getAbsolutePath();
            if (fileName.endsWith(extension)) {
                fileName = IOUtils.getPrefix(fileName);
            }

            saveFile = new File(fileName + "." + extension);

            if (saveFile.exists()) {
                dialogReturnValue = JOptionPane.showConfirmDialog(this, "This File Already Exists.\nDo you want to Overwrite it?", "This File Already Exists. Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (dialogReturnValue == JOptionPane.YES_OPTION) {
                    doSaveFile = true;
                }
            } else {
                doSaveFile = true;
            }
        }

        if (doSaveFile) {
            saveExportTableViewFile(saveFile);
            FILE_CHOOSER_PATH.set(saveFile.getAbsolutePath());
        }
    }

    private void saveExportTableViewFile(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            for (int j = 1; j < generalTable.getColumnCount(); j++) {
                fileWriter.write(generalTable.getModel().getColumnName(j) + "\t");
            }
            fileWriter.write("\n");

            for (int i = 0; i < generalTable.getRowCount(); i++) {
                for (int j = 1; j < generalTable.getColumnCount(); j++) {
                    Object value = generalTable.getValueAt(i, j);

                    if (value != null) {
                        fileWriter.write(value.toString() + "\t");
                    } else {
                        fileWriter.write("\t");
                    }
                }
                fileWriter.write("\n");
            }

            fileWriter.flush();
            fileWriter.close();

            InitDesktop.edit(file);
        } catch (IOException ioe) {
            if (DEBUG_BUILD) {
                println("Exception in saveExportTableViewFile():\n" + ioe.getMessage());
            }

            JOptionPane.showMessageDialog(this, "Something went wrong while saving the file:\n" + ioe.getMessage() + "\nPlease try again with a different file name/path/drive.", "Error with saving the file!", JOptionPane.ERROR_MESSAGE);
            save();
        }
    }

    public void refreshCurrentClassSetSelection() {
        findClassDialog.resetCurrentClassIndex();

        // nextClassButton.doClick();
        // fire actionPerformed event directly, thus effectively avoiding the JButton being pressed down for some time
        nextClassAction.actionPerformed(new ActionEvent(nextClassAction, ActionEvent.ACTION_PERFORMED, ""));

        if (plotPanel != null) {
            if (plotPanel.getRenderAllCurrentClassSetPlotImagesToFilesAction() != null) {
                plotPanel.getRenderAllCurrentClassSetPlotImagesToFilesAction().setEnabled(
                        (layoutFrame.getLayoutClassSetsManager().getCurrentClassSetAllClasses().getTotalClasses() > 0));
            }

            if (plotPanel.getRenderPlotImageToFileAction() != null) {
                plotPanel.getRenderPlotImageToFileAction().setEnabled(true);
            }
        }
    }

    public void closeClassViewerWindow() {
        // disable any running threads
        checkAndAbortUpdateEntropyTableRunnable();
        checkAndAbortUpdateDetailedEntropyTableRunnable();

        generalTable
                .getDefaultEditor(String.class
                ).stopCellEditing();

        setVisible(false);
    }

    /**
     * Process a light-weight thread using the Adapter technique to avoid any
     * GUI latencies with the JButton setEnabled() update.
     */
    private void runLightWeightThread(int threadPriority) {
        Thread runLightWeightThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean enableDetailsButton = !annotationClass.isEmpty();

                // used as an 'animation' trick to visually show JButton update when moved to another row!
                if (detailsButton.isEnabled()) {
                    detailsButton.setEnabled(false);
                    LayoutFrame.sleep(TIME_TO_SLEEP_TO_ABORT_THREADS);
                }

                detailsButton.setEnabled(enableDetailsButton);
            }
        }, "runLightWeightThread");

        runLightWeightThread.setPriority(threadPriority);
        runLightWeightThread.start();
    }

    public String[] getGeneralTableColumnNames() {
        return tableModelGeneral.getColumnNames();
    }

    public JButton getChooseColumnsToHideButton() {
        return chooseColumnsToHideButton;
    }

    public void setCurrentClassName(String currentClassName) {
        this.currentClassName = currentClassName;

        int numberOfSelectedNodes = layoutFrame.getGraph().getSelectionManager().getSelectedNodes().size();
        this.setTitle("Class Viewer " + ((!currentClassName.isEmpty()) ? "(Current Class Selected: " + ((numberOfSelectedNodes > 0) ? currentClassName + " with " + numberOfSelectedNodes + " nodes" : currentClassName) + ")" : ""));
    }

    public String getCurrentClassName() {
        return currentClassName;
    }

    public void setUpdateResetSelectDeselectAllButton(boolean updateResetSelectDeselectAllButton) {
        this.updateResetSelectDeselectAllButton = updateResetSelectDeselectAllButton;
        generalTable.setUpdateResetSelectDeselectAllButton(updateResetSelectDeselectAllButton);
    }

    public void filterEnrichmentTable(boolean isCombined) {
        enrichmentTable.setAutoCreateRowSorter(false);
        chkShowOnlyEnriched.setSelected(true);
        int colOverRep, colFisherP;
        if (isCombined) {
            colOverRep = 4;
            colFisherP = 6;
        } else {
            colOverRep = 5;
            colFisherP = 7;
        }
        // Row Filters
        List<RowFilter<ClassViewerTableModelEnrichment, Object>> filters = new ArrayList<>(2);
        RowFilter<ClassViewerTableModelEnrichment, Object> overRepFilter = null;
        overRepFilter = RowFilter.numberFilter(ComparisonType.AFTER, 1.0, colOverRep);
        RowFilter<ClassViewerTableModelEnrichment, Object> pFilter = null;
        pFilter = RowFilter.numberFilter(ComparisonType.BEFORE, 0.05, colFisherP);
        filters.add(overRepFilter);
        filters.add(pFilter);
        TableRowSorter<ClassViewerTableModelEnrichment> sorter = new TableRowSorter<ClassViewerTableModelEnrichment>(enrichmentTableModel);
        sorter.setRowFilter(RowFilter.andFilter(filters));
        enrichmentTable.setRowSorter(sorter);
    }

    public void removeFilterEnrichmentTable() {
        enrichmentTable.setAutoCreateRowSorter(true);
        chkShowOnlyEnriched.setSelected(false);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

        if (!rebuildClassSets) {
            // if ( (e.getStateChange() == ItemEvent.SELECTED) && ( e.getSource().equals(classSetsBox) ) )
            if (e.getSource().equals(classSetsBox)) {
                layoutFrame.getNetworkRootContainer().getLayoutClassSetsManager().switchClassSet((String) classSetsBox.getSelectedItem());
                layoutFrame.getGraph().updateAllDisplayLists();
                layoutFrame.getGraph().refreshDisplay();

                refreshCurrentClassSetSelection();

                if (DEBUG_BUILD) {
                    println("Reinit Due to Action:" + e.toString());
                }

                populateClassViewer(false, true);

            }
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(viewAllClassSets)) {
            if (DEBUG_BUILD) {
                println("Reinit Due to Action:" + e.toString());
            }

            populateClassViewer(false, true);
        } else if (e.getSource().equals(autoSizeColumnsCheckBox)) {
            CV_AUTO_SIZE_COLUMNS.set(autoSizeColumnsCheckBox.isSelected());
            generalTable.setAutoSizeColumns(CV_AUTO_SIZE_COLUMNS.get());
            populateClassViewer(false, true);
        } else if (e.getSource().equals(showTransposePlotsCheckbox)) {
            if (showTransposePlotsCheckbox.isSelected())
                showTransposePlots();
            else
                hideTransposePlots();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        int selectedRow = entropyTable.getSelectedRow();
        boolean isDifferent = (selectedRow > -1) ? !annotationClass.equals((String) entropyTable.getModel().getValueAt(selectedRow, 0)) : true;
        annotationClass = (selectedRow > -1) ? (String) entropyTable.getModel().getValueAt(selectedRow, 0) : "";
        if (isDifferent) {
            runLightWeightThread(Thread.NORM_PRIORITY);
        }
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        if (DEBUG_BUILD) {
            println(changeEvent.toString());
        }

        if (changeEvent.getSource().equals(tabbedPane)) {
            if (DEBUG_BUILD) {
                println("Reinit due to change event: " + changeEvent.toString());
            }

            populateClassViewer(false, true);
        }
    }

    /**
     * Sets text, tooltip text and enabled/disabled state of
     * searchDatabaseButton
     *
     * @param enabled - true to enable, false to disable
     */
    private void setUpSearchDatabaseButton(boolean enabled) {
        searchDatabaseButton.setText("Search Pathway Commons");
        searchDatabaseButton.setToolTipText("Search Pathway Commons for selected node names");
        searchDatabaseButton.setEnabled(enabled);

    }

    public static class EntropyTableCellRenderer extends DefaultTableCellRenderer {

        /**
         * Serial version UID variable for the EntropyTableCellRenderer class.
         */
        public static final long serialVersionUID = 111222333444555797L;

        public static final DecimalFormat DECIMAL_FORMAT_1 = new DecimalFormat("0.##E0");
        public static final DecimalFormat DECIMAL_FORMAT_2 = new DecimalFormat("0.####");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof Double) {
                double val = ((Double) value).doubleValue();
                String text = ((val > 1000.0) || (val < 0.0001)) ? DECIMAL_FORMAT_1.format(value) : DECIMAL_FORMAT_2.format(value);
                setText(text);
                setToolTipText(text);
            }

            return this;
        }
    }

    public class JHeatMap extends JPanel implements MouseListener, MouseMotionListener {

        final int CELLWIDTH = 20;
        final int CELLHEIGHT = 20;
        private int xTextOffset = 0;
        private int yTextOffset = 0;
        private int keyHeight = 30;
        private int keyWidth = 600;
        private boolean isGridlines = true;

        Rectangle keyOneRect = null;
        Rectangle keyTwoRect = null;
        Rectangle keyThreeRect = null;
        Rectangle keyFourRect = null;

        Color colorNoSample = Color.GRAY;
        Color colorUnderrepSig = Color.BLUE;
        Color colorUnderrepNotSig = Color.PINK;
        Color colorSignificant = Color.YELLOW;
        Color colorPastSignificant = Color.GREEN;
        Color colorNotSignificant = Color.BLACK;

        BufferedImage img = null;
        DefaultHeatMapDataset hmds = null;
        String[] columnTitles = null;
        String[] rowTitles = null;
        PaintScale paintscale = null;

        public JHeatMap() {
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        }

        private void createPaintScale() {
            paintscale = new PaintScale() {

                double sigP = 0.01;
                double notSigP = 0.05;

                @Override
                public double getLowerBound() {
                    return 0;
                }

                @Override
                public double getUpperBound() {
                    return 1;
                }

                @Override
                public Paint getPaint(double d) {
                    // If value is 0.0 no data
                    if (d == 0.0) {
                        return colorNoSample;
                    }
                    // If value is negative it is underrepresented
                    if (d < 0) {
                        return colorNoSample;
                    }
                    if (d <= sigP) {
                        double blending = d / sigP;

                        double inverse_blending = 1 - d / sigP;

                        int red = (int) (colorSignificant.getRed() * blending + colorPastSignificant.getRed() * inverse_blending);
                        int green = (int) (colorSignificant.getGreen() * blending + colorPastSignificant.getGreen() * inverse_blending);
                        int blue = (int) (colorSignificant.getBlue() * blending + colorPastSignificant.getBlue() * inverse_blending);

                        return new Color(red, green, blue, 255);
                    }
                    // If value is approaching statistically significant
                    if (d <= notSigP) {

                        double range = notSigP - sigP;
                        double delta = (d - sigP) / (range);

                        double blending = delta;
                        double inverse_blending = 1 - delta;

                        int red = (int) (colorNotSignificant.getRed() * blending + colorSignificant.getRed() * inverse_blending);
                        int green = (int) (colorNotSignificant.getGreen() * blending + colorSignificant.getGreen() * inverse_blending);
                        int blue = (int) (colorNotSignificant.getBlue() * blending + colorSignificant.getBlue() * inverse_blending);
                        return new Color(red, green, blue, 255);
                    }
                    if (d > notSigP) {
                        return colorNotSignificant;
                    }
                    return Color.BLACK;
                }
            };
        }

        public void updateHeatMap(DefaultHeatMapDataset hmds, String[] columnTitles, String[] rowTitles) {
            this.hmds = hmds;
            this.columnTitles = columnTitles;
            this.rowTitles = rowTitles;
            generateImage();
        }

        public void setGridLines(boolean gridLines) {
            isGridlines = gridLines;
            generateImage();
        }

        public DefaultHeatMapDataset getHmds() {
            return hmds;
        }

        public void setHmds(DefaultHeatMapDataset hmds) {
            this.hmds = hmds;
        }

        public PaintScale getPaintScale() {
            return paintscale;
        }

        public void setPaintScale(PaintScale paintScale) {
            this.paintscale = paintScale;
        }

        public BufferedImage getImg() {
            return img;
        }

        public void setImg(BufferedImage img) {
            this.img = img;
            this.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
            this.revalidate();
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
            g.drawImage(img, 0, 0, null);

        }

        private void generateImage() {
            createPaintScale();
            int xCount = hmds.getXSampleCount();
            int yCount = hmds.getYSampleCount();
            Font font = new Font(new JLabel().getFont().getFontName(), Font.PLAIN, CELLHEIGHT);
            Font keyFont = new Font(new JLabel().getFont().getFontName(), Font.PLAIN, 15);
            FontRenderContext frc = new FontRenderContext(null, false, false);
            xTextOffset = 0;
            yTextOffset = 0;
            // Calculate max xoffset;y

            for (int i = 0; i < rowTitles.length; i++) {
                xTextOffset = (int) java.lang.Math.max((double) new TextLayout(rowTitles[i], font, frc).getBounds().getWidth(), (double) xTextOffset);
            }
            xTextOffset += 10;
            // Calculate max yoffset;
            for (int i = 0; i < columnTitles.length; i++) {
                yTextOffset = (int) java.lang.Math.max((double) new TextLayout(columnTitles[i], font, frc).getBounds().getWidth(), (double) yTextOffset);
            }
            yTextOffset += 5;

            // Setup Key Sizes
            int padding = 15;
            int tailPadding = (CELLWIDTH * 3);
            String keyOne = "Adjusted P-Value Key  0 - 0.01:";
            String keyTwo = "0.01 - 0.05:";
            String keyThree = "p > 0.05:";
            String keyFour = "Underrepresented/Absent";
            int lenKeyOne = getTextWidth(keyOne, keyFont);
            int lenKeyTwo = getTextWidth(keyTwo, keyFont);
            int lenKeyThree = getTextWidth(keyThree, keyFont);
            int lenKeyFour = getTextWidth(keyFour, keyFont);

            keyWidth = padding + lenKeyOne + lenKeyTwo + lenKeyThree + lenKeyFour + tailPadding * 3 + (CELLWIDTH / 2) + (CELLWIDTH * 2) + 5;

            int xWidth;
            xWidth = java.lang.Math.max(xCount * CELLWIDTH + xTextOffset, keyWidth + 15);
            BufferedImage image;
            image = new BufferedImage(xWidth, yCount * CELLHEIGHT + yTextOffset + 25 + keyHeight,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();

            g2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw Key
            g2.setFont(keyFont);

            g2.setPaint(Color.WHITE);
            g2.fillRect(10, 5, keyWidth, keyHeight);
            g2.setPaint(Color.GRAY);
            g2.drawRect(10, 5, keyWidth, keyHeight);
            g2.setPaint(Color.BLACK);

            g2.drawString(keyOne, padding, 5 + 5 + keyHeight / 2);
            g2.drawString(keyTwo, padding + lenKeyOne + tailPadding, 5 + 5 + keyHeight / 2);
            g2.drawString(keyThree, padding + lenKeyOne + lenKeyTwo + tailPadding * 2, 5 + 5 + keyHeight / 2);
            g2.drawString(keyFour, padding + lenKeyOne + lenKeyTwo + lenKeyThree + tailPadding * 3, 5 + 5 + keyHeight / 2);

            int keyOneColorX = padding + lenKeyOne + CELLWIDTH / 2;
            Paint greenToYellow = new GradientPaint(keyOneColorX, 0, colorPastSignificant, keyOneColorX + CELLWIDTH * 2, 0, colorSignificant);
            g2.setPaint(greenToYellow);
            keyOneRect = new Rectangle(keyOneColorX, 10, CELLWIDTH * 2, CELLHEIGHT);
            g2.fill(keyOneRect);
            g2.setPaint(Color.GRAY);
            g2.draw(keyOneRect);

            int keyTwoColorX = padding + lenKeyOne + lenKeyTwo + tailPadding + (CELLWIDTH / 2);
            Paint yellowToBlack = new GradientPaint(keyTwoColorX, 0, colorSignificant, keyTwoColorX + CELLWIDTH * 2, 0, colorNotSignificant);
            g2.setPaint(yellowToBlack);
            keyTwoRect = new Rectangle(keyTwoColorX, 10, CELLWIDTH * 2, CELLHEIGHT);
            g2.fill(keyTwoRect);
            g2.setPaint(Color.GRAY);
            g2.draw(keyTwoRect);

            int keyThreeColorX = padding + lenKeyOne + lenKeyTwo + lenKeyThree + tailPadding * 2 + (CELLWIDTH / 2);
            g2.setPaint(colorNotSignificant);
            keyThreeRect = new Rectangle(keyThreeColorX, 10, CELLWIDTH * 2, CELLHEIGHT);
            g2.fill(keyThreeRect);
            g2.setPaint(Color.GRAY);
            g2.draw(keyThreeRect);

            int keyFourColourX = padding + lenKeyOne + lenKeyTwo + lenKeyThree + lenKeyFour + tailPadding * 3 + (CELLWIDTH / 2);
            g2.setPaint(colorNoSample);
            keyFourRect = new Rectangle(keyFourColourX, 10, CELLWIDTH * 2, CELLHEIGHT);
            g2.fill(keyFourRect);
            g2.setPaint(Color.GRAY);
            g2.draw(keyFourRect);

            g2.setFont(font);
            g2.translate(0, 30);
            AffineTransform originalRotate = g2.getTransform();
            int lineHeight = yCount * CELLWIDTH;
            int lineWidth = xCount * CELLHEIGHT;
            // Draw Boxes
            for (int xIndex = 0; xIndex < xCount; xIndex++) {
                for (int yIndex = 0; yIndex < yCount; yIndex++) {
                    double z = hmds.getZValue(xIndex, yIndex);
                    Paint p = paintscale.getPaint(z);
                    g2.setPaint(p);
                    g2.fillRect(xTextOffset + (xIndex * CELLWIDTH), yCount * CELLHEIGHT - yIndex * CELLHEIGHT, CELLWIDTH, CELLHEIGHT);
                }
            }
            // Draw Gridlines
            if (isGridlines) {
                g2.setPaint(Color.DARK_GRAY);
                for (int xIndex = 0; xIndex < xCount + 1; xIndex++) {
                    g2.drawLine(xTextOffset + (xIndex * CELLWIDTH), CELLHEIGHT, xTextOffset + (xIndex * CELLWIDTH), lineHeight + CELLHEIGHT);
                }
                for (int yIndex = 0; yIndex < yCount + 1; yIndex++) {
                    g2.drawLine(xTextOffset, (yCount + 1) * CELLHEIGHT - yIndex * CELLHEIGHT, xTextOffset + lineWidth, (yCount + 1) * CELLHEIGHT - yIndex * CELLHEIGHT);
                }
            }
            g2.setPaint(Color.BLACK);
            g2.rotate(-java.lang.Math.PI / 2);
            // Draw Column titles
            for (int xIndex = 0; xIndex < xCount; xIndex++) {
                g2.drawString(columnTitles[xIndex], -CELLHEIGHT + (-yCount * CELLHEIGHT) - (int) new TextLayout(columnTitles[xIndex], font, frc).getBounds().getWidth() - 5, xTextOffset + CELLWIDTH + (xIndex * CELLWIDTH) - 2);
            }
            // Draw Row titles
            g2.setTransform(originalRotate);
            for (int yIndex = 0; yIndex < yCount; yIndex++) {
                g2.drawString(rowTitles[yIndex], (xTextOffset - (int) new TextLayout(rowTitles[yIndex], font, frc).getBounds().getWidth()) - 5, (CELLHEIGHT + yCount * CELLHEIGHT - yIndex * CELLHEIGHT) - 3);
            }
            this.setImg(image);
        }

        private int getTextWidth(String string, Font font) {
            FontRenderContext frc = new FontRenderContext(null, false, false);
            return (int) new TextLayout(string, font, frc).getBounds().getWidth();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (hmds == null) {
                return;
            }
            int keyOffset = keyHeight;
            int cellX = 0;
            int cellY = 0;
            int clickX = e.getX();
            int clickY = e.getY();
            cellX = (clickX - xTextOffset) / CELLWIDTH;
            cellY = hmds.getYSampleCount() - (clickY - keyOffset) / CELLHEIGHT;

            int modelId = enrichmentTableModel.getHeatmapTableIndex(cellX, cellY);
            if (modelId == -1) {
                if (keyOneRect.contains(e.getPoint())) {
                    Color picked = JColorChooser.showDialog(null, "Very Significant Colour", colorPastSignificant);
                    colorPastSignificant = (picked != null) ? picked : colorPastSignificant;
                } else if (keyTwoRect.contains(e.getPoint())) {
                    Color picked = JColorChooser.showDialog(null, "Significant Colour", colorSignificant);
                    colorSignificant = (picked != null) ? picked : colorSignificant;
                } else if (keyThreeRect.contains(e.getPoint())) {
                    Color picked = JColorChooser.showDialog(null, "Insignificant Colour", colorNotSignificant);
                    colorNotSignificant = (picked != null) ? picked : colorNotSignificant;
                } else if (keyFourRect.contains(e.getPoint())) {
                    Color picked = JColorChooser.showDialog(null, "No sample Colour", colorNoSample);
                    colorNoSample = (picked != null) ? picked : colorNoSample;
                } else {
                    return;
                }
                generateImage();
            } else {
                int rowId = enrichmentTable.convertRowIndexToView(modelId);

                displayTable();
                enrichmentTable.getSelectionModel().setSelectionInterval(rowId, rowId);
                enrichmentTable.scrollRectToVisible(new Rectangle(enrichmentTable.getCellRect(rowId, 0, true)));
            }
        }

        ;

        @Override
        public void mousePressed(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseExited(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (hmds == null) {
                return;
            }
            int keyOffset = keyHeight;
            int cellX = 0;
            int cellY = 0;
            int clickX = e.getX();
            int clickY = e.getY();
            cellX = (clickX - xTextOffset) / CELLWIDTH;
            cellY = hmds.getYSampleCount() - (clickY - keyOffset) / CELLHEIGHT;
            if (cellX >= hmds.getXSampleCount() || cellY >= hmds.getYSampleCount()
                    || cellX < 0 || cellY < 0) {
                this.setToolTipText(null);
                return;
            }

            double pValue = hmds.getZValue(cellX, cellY);
            if (pValue > 0) {
                this.setToolTipText("p: " + pValue);
            } else if (pValue < 0) {
                this.setToolTipText("(Underrepresented) p: " + (-pValue));
            } else {
                this.setToolTipText(null);
            }

        }

    }

}
