package org.Kajeka.CoreUI;

import org.Kajeka.Correlation.Dialogs.CorrelationLoaderDialog;
import org.Kajeka.Correlation.Dialogs.CorrelationLoaderSummaryDialog;
import org.Kajeka.Correlation.CorrelationData;
import org.Kajeka.Correlation.CorrelationLoader;
import org.Kajeka.Files.webservice.ImportWebService;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.*;
import javax.swing.border.*;
import org.Kajeka.Analysis.AnnotationTypeManagerBG;
import org.Kajeka.BuildConfig;
import org.Kajeka.ClassViewerUI.*;
import org.Kajeka.Clustering.MCL.*;
import org.Kajeka.CoreUI.Dialogs.*;
import org.Kajeka.CoreUI.MenuBars.*;
import org.Kajeka.CoreUI.Services.*;
import org.Kajeka.CoreUI.Tables.TableModels.*;
import org.Kajeka.CoreUI.ToolBars.*;
import org.Kajeka.Environment.*;
import org.Kajeka.Environment.Preferences.*;
import org.Kajeka.Files.*;
import org.Kajeka.Files.Parsers.*;
import org.Kajeka.GPUComputing.OpenGLContext.*;
import org.Kajeka.GPUComputing.OpenGLContext.Dummy.*;
import org.Kajeka.Graph.*;
import org.Kajeka.Graph.Camera.CameraUI.*;
import org.Kajeka.Graph.GraphElements.*;
import org.Kajeka.Graph.Selection.SelectionUI.Dialogs.*;
import org.Kajeka.Models.Lathe3D.*;
import org.Kajeka.Models.SuperQuadric.*;
import org.Kajeka.Network.*;
import org.Kajeka.Simulation.*;
import org.Kajeka.Simulation.Dialogs.*;
import org.Kajeka.StaticLibraries.*;
import org.Kajeka.Textures.*;
import static org.Kajeka.StaticLibraries.EnumUtils.*;
import static org.Kajeka.Environment.AnimationEnvironment.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;
import static org.Kajeka.Correlation.CorrelationEnvironment.*;
import static org.Kajeka.DebugConsole.ConsoleOutput.*;
import org.Kajeka.Files.Dialogs.ColumnDataConfigurationDialog;
import org.Kajeka.Licensing.LicenseKeyValidator;
import org.Kajeka.Licensing.RevocationChecker;
import org.Kajeka.Utils.ToolbarLayout;
import org.Kajeka.Utils.UsageTracker;

/**
*
* This is the main window frame. The two renderers are outputting their graphics
* here and all actions/GUI windows are to be initiated from this central application frame.
*
* @author Full refactoring by Thanos Theo, 2008-2009
* @version 3.0.0.0
*
*/

public final class LayoutFrame extends JFrame implements GraphListener
{
    /**
    *  Serial version UID variable for the LayoutFrame class.
    */
    public static final long serialVersionUID = 111222333444555678L;

    private NetworkRootContainer nc = null;
    private LayoutClassSetsManager layoutClassSetsManager;
    private FileOpenHistory fileOpenHistory = null;
    private Graph graph = null;
    private LayoutGraphPropertiesToolBar layoutGraphPropertiesToolBar = null;
    private LayoutGeneralToolBar layoutGeneralToolBar = null;
    private LayoutNavigationToolBar layoutNavigationToolBar = null;
    private LayoutHomeToolBar layoutHomeToolBar = null;
    private JLabel nodeLabel = null;
    private JLabel statusLabel = null;
    private JPanel globalPanel = null;
    private JPanel toolbarPanel = null;
    private LayoutAboutDialog layoutAboutDialog = null;
    private LayoutMenuBar layoutMenuBar = null;
    private LayoutProgressBarDialog layoutProgressBarDialog = null;
    private CoreSaver saver = null;
    private LayoutPrintServices layoutPrintServices = null;
    private FilterNodesByEdgesDialog filterNodesByEdgesDialog = null;
    private FilterEdgesByWeightDialog filterEdgesByWeightDialog = null;
    private LayoutCustomizeNodeNamesDialog layoutCustomizeNodeNamesDialog = null;
    private LayoutShowClassesLegendsDialog layoutShowClassesLegendsDialog = null;
    private FindNameDialog findNameDialog = null;
    private FindClassDialog findClassDialog = null;
    private FindMultipleClassesDialog findMultipleClassesDialog = null;
    private SignalingPetriNetSimulationDialog SPNSimulationDialog = null;
    private SignalingPetriNetLoadSimulation signalingPetriNetLoadSimulation = null;
    private LayoutGraphPropertiesDialog layoutGraphPropertiesDialog = null;
    private LayoutGraphStatisticsDialog layoutGraphStatisticsDialog = null;
    private CorrelationData correlationData = null;
    private ClassViewerFrame classViewerFrame = null;
    private LayoutAnimationControlDialog layoutAnimationControlDialog = null;
    private LayoutClusterMCL layoutClusterMCL = null;
    private LayoutNavigationWizardDialog layoutNavigationWizardDialog = null;
    private LayoutTipOfTheDayDialog layoutTipOfTheDayDialog = null;
    private LayoutLicensesDialog layoutLicensesDialog = null;
    private LayoutOpenGLDriverCapsDialog layoutOpenGLDriverCapsDialog = null;
    private LayoutJavaPlatformCapsDialog layoutJavaPlatformCapsDialog = null;
    private LayoutValidateLicenseKeyDialog layoutValidateLicenseKeyDialog = null;
    private UsageTracker usageTracker;

    private AbstractAction fileMenuOpenAction = null;
    private AbstractAction fileMenuExitAction = null;
    private AbstractAction _2D3DSwitchAction = null;
    private AbstractAction homeAction = null;
    private AbstractAction blockAllAction = null;
    private AbstractAction blockAllExceptNavigationToolBarAction = null;
    private ActionEvent blockEvent = null;
    private ActionEvent unblockEvent = null;
    private AbstractAction toolsMenuSavePreferencesAction = null;
    private AbstractAction toolsMenuRevertToDefaultPreferencesAction = null;

    private FileDragNDrop fileDragNDrop = null;
    private CustomFileFilter fileFilter = null;
    private boolean loadingFile = false;
    private String fileNameAbsolutePath = "";
    private String fileNameLoaded = "";

    private ImportClassSetsParser importClassSetsParser = null;
    private ImportWebService importWebService = null;

    private ExportClassSets exportClassSets = null;
    private ExportCorrelationNodesEdgesTable exportCorrelationNodesEdgesTable = null;
    private ExportD3 exportD3 = null;

    private RevocationChecker revocationChecker = null;

    private boolean navigationWizardShownOnce = false;

    /**
     * Multiplier to adjust resizing of nodes in resizeNodesAndArrowHeadsToKvalue()
     */
    private double nodeResizeFactor = 1;

    /**
    *  The constructor of LayoutFrame.
    */
    public LayoutFrame()
    {
        super(DISPLAY_PRODUCT_NAME_AND_VERSION);
    }

    /**
    *  Initializes the frame. This method is specified to return the 'this' reference upon which it is invoked. This allows the method invocation to be chained.
    */
    public LayoutFrame initializeFrame(boolean useDefaults, Map<String, String> preferences,
            final boolean startWithAutomaticFileLoading)
    {
        if (!useDefaults)
        {
            LayoutPreferences.getLayoutPreferencesSingleton().loadPreferences();
        }

        LayoutPreferences.getLayoutPreferencesSingleton().useSpecifiedPreferences(preferences);

        loadRestOfPreferences();

        LicenseKeyValidator lkv = new LicenseKeyValidator(LICENSE_EMAIL.get());
        layoutValidateLicenseKeyDialog = new LayoutValidateLicenseKeyDialog(this);

        IS_LICENSED = lkv.valid(LICENSE_KEY.get());

        if (IS_LICENSED)
        {
            revocationChecker = new RevocationChecker(LICENSE_EMAIL.get());
            while (revocationChecker.start())
            {
                IS_LICENSED = false;
                layoutValidateLicenseKeyDialog.setSuggestRestart(false);
                layoutValidateLicenseKeyDialog.getValidateLicenseKeyAction().actionPerformed(
                        new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Validate"));

                if(!layoutValidateLicenseKeyDialog.isLicensed())
                {
                    this.dispose();
                    System.exit(0);
                }
            }
        }

        DISPLAY_PRODUCT_NAME = PRODUCT_NAME + (!IS_LICENSED ? " Evaluation" : "");
        DISPLAY_PRODUCT_NAME_AND_VERSION = DISPLAY_PRODUCT_NAME + (BuildConfig.VERSION.equals("development") ?
            " internal development version" :
            " Version " + BuildConfig.VERSION);

        Insets insets = this.getInsets();
        this.setSize( new Dimension(APPLICATION_SCREEN_DIMENSION.width + insets.left + insets.right - 2, APPLICATION_SCREEN_DIMENSION.height + insets.top + insets.bottom - 2) );
        this.setMinimumSize(new Dimension(320, 240));
        this.setIconImages(ICON_IMAGES);

        long prevTimeInMSecs = System.nanoTime() / 1000000;
        LayoutAboutDialog splashScreen = new LayoutAboutDialog(this, true);

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        fileOpenHistory = new FileOpenHistory(MAX_FILE_HISTORY);
        layoutProgressBarDialog = new LayoutProgressBarDialog(this);
        layoutNavigationWizardDialog = new LayoutNavigationWizardDialog(this);
        layoutTipOfTheDayDialog = new LayoutTipOfTheDayDialog(this);

        usageTracker = new UsageTracker();

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Initializing Classes...");
        layoutClassSetsManager = new LayoutClassSetsManager();
        layoutClassSetsManager.createNewClassSet("Default Classes...");

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Loading Network Container...");
        nc = new NetworkRootContainer(layoutClassSetsManager, this);

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Loading Main OpenGL Canvas...");
        nodeLabel = new JLabel();
        nodeLabel.setMinimumSize( new Dimension(150, 10) );

        layoutOpenGLDriverCapsDialog = new LayoutOpenGLDriverCapsDialog(this);
        layoutJavaPlatformCapsDialog = new LayoutJavaPlatformCapsDialog(this);

        detectOpenGLSupportAndExtensions();

        RENDERER_MODE_3D = RENDERER_MODE_START_3D.get();
        graph = new Graph(this, nc);
        graph.setListener(this);
        graph.addAllEvents();
        setRenderModeSwitchUISettings();

        statusLabel = new JLabel();
        setStatusLabel("Ready");

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Creating Toolbars...");
        layoutGraphPropertiesToolBar = new LayoutGraphPropertiesToolBar(JToolBar.HORIZONTAL);
        layoutGeneralToolBar = new LayoutGeneralToolBar(JToolBar.HORIZONTAL);
        layoutNavigationToolBar = new LayoutNavigationToolBar(JToolBar.HORIZONTAL);
        layoutHomeToolBar = new LayoutHomeToolBar(JToolBar.HORIZONTAL);

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Creating Menus & All UIs...");
        layoutClusterMCL = new LayoutClusterMCL(this, graph);
        layoutGraphStatisticsDialog = new LayoutGraphStatisticsDialog(this, graph);
        correlationData = new CorrelationData(this);
        classViewerFrame = new ClassViewerFrame(this);
        SPNSimulationDialog = new SignalingPetriNetSimulationDialog(nc, this);
        layoutAnimationControlDialog = new LayoutAnimationControlDialog(this);
        signalingPetriNetLoadSimulation = new SignalingPetriNetLoadSimulation(nc, this);
        layoutMenuBar = new LayoutMenuBar();

        JPanel propertiesPanel = new JPanel(true);

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Building Layout Graph Properties Dialog...");
        layoutGraphPropertiesDialog = new LayoutGraphPropertiesDialog(this, layoutClassSetsManager, nc);

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Building Search Dialogs...");
        findNameDialog = new FindNameDialog(this, this);
        findClassDialog = new FindClassDialog(this, this);
        findMultipleClassesDialog = new FindMultipleClassesDialog(this, this);

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Creating Panels...");
        globalPanel = new JPanel(true);
        globalPanel.setLayout( new BorderLayout() );

        JPanel statusLabelPanel = new JPanel(true);
        statusLabelPanel.setLayout( new BorderLayout() );
        statusLabelPanel.setPreferredSize( new Dimension(500, 20) );

        JPanel labelPanel = new JPanel(true);
        labelPanel.setLayout( new BorderLayout() );
        labelPanel.setPreferredSize( new Dimension(200, 20) );
        labelPanel.setBorder( BorderFactory.createEtchedBorder(EtchedBorder.LOWERED) );

        nodeLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0) );
        propertiesPanel.setLayout( new BorderLayout(50, 10) );

        statusLabel.setBorder( BorderFactory.createEtchedBorder(EtchedBorder.LOWERED) );
        statusLabelPanel.add(statusLabel, BorderLayout.EAST);
        labelPanel.add(nodeLabel, BorderLayout.WEST);

        this.setJMenuBar(layoutMenuBar);
        propertiesPanel.add(statusLabelPanel, BorderLayout.EAST);
        propertiesPanel.add(labelPanel, BorderLayout.WEST);

        toolbarPanel = new JPanel(new ToolbarLayout());
        globalPanel.add(toolbarPanel, BorderLayout.NORTH);

        rebuildToolbars();

        // wrapping the Graph GLCanvas reference in a JPanel so the JFrame can contain other (lightweight) UI
        JPanel graphPanel = new JPanel(new BorderLayout(), true);
        graphPanel.add(graph, BorderLayout.CENTER);
        globalPanel.add(graphPanel, BorderLayout.CENTER);
        globalPanel.add(propertiesPanel, BorderLayout.SOUTH);
        globalPanel.setOpaque(false);

        this.getContentPane().add(globalPanel);

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Initializing Rest of Graphics...");
        layoutCustomizeNodeNamesDialog = new LayoutCustomizeNodeNamesDialog(this);
        layoutShowClassesLegendsDialog = new LayoutShowClassesLegendsDialog(this);
        layoutLicensesDialog = new LayoutLicensesDialog(this);

        filterNodesByEdgesDialog = new FilterNodesByEdgesDialog(this);
        filterEdgesByWeightDialog = new FilterEdgesByWeightDialog(this);

        saver = new CoreSaver(nc, this);

        importClassSetsParser = new ImportClassSetsParser(nc, this);
        importWebService = new ImportWebService(this);

        exportClassSets = new ExportClassSets(this);
        exportCorrelationNodesEdgesTable = new ExportCorrelationNodesEdgesTable(this, correlationData);
        exportD3 = new ExportD3(this);

        layoutPrintServices = new LayoutPrintServices();

        sleepMaxTime(prevTimeInMSecs);
        prevTimeInMSecs = System.nanoTime() / 1000000;

        splashScreen.setText(" Done Loading All Components. Building Main View UI...");
        layoutAboutDialog = new LayoutAboutDialog(this, false);
        fileFilter = new CustomFileFilter();

        initActions(this);

        blockEvent = new ActionEvent(this, 1, BLOCK_ALL);
        unblockEvent = new ActionEvent(this, 2, UNBLOCK_ALL);

        sleep(3 * MAX_TIME_IN_MSECS_TO_SLEEP_FOR_LOADING);

        this.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                closeApplication();
            }
        } );

        this.setLocation( (SCREEN_DIMENSION.width - APPLICATION_SCREEN_DIMENSION.width) / 2, (SCREEN_DIMENSION.height - APPLICATION_SCREEN_DIMENSION.height) / 2 );
        this.setTitle(DISPLAY_PRODUCT_NAME_AND_VERSION);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        sleep(MAX_TIME_IN_MSECS_TO_SLEEP_FOR_LOADING);
        splashScreen.finishedLoading();
        frameInitializationFinish(startWithAutomaticFileLoading);

        return this;
    }

    public ImportWebService getImportWebService()
    {
        return importWebService;
    }

    /**
    *  Detects the OpenGL support and extensions.
    */
    private void detectOpenGLSupportAndExtensions()
    {
        if ( FIRST_RUN_DETECT_OPENGL_SUPPORT_AND_EXTENSIONS.get() )
        {
            if (!IS_MAC)
                useOpenGLContextForDetectingOpenGLSupportAndExtensions();
            else
                setHighQualityRenderingSettings();

            FIRST_RUN_DETECT_OPENGL_SUPPORT_AND_EXTENSIONS.set(false);
            LayoutPreferences.getLayoutPreferencesSingleton().savePreferences();
        }
    }

    /**
    *  Uses the OpenGL GPU Computing context for detecting the OpenGL support and extensions.
    */
    private void useOpenGLContextForDetectingOpenGLSupportAndExtensions()
    {
        OpenGLContext openGLContext = new DummyOpenGLContext(AllTextureParameters.TEXTURE_2D_ARB_RGBA_32, 4, 2, 2, false, true);
        openGLContext.doGPUComputingProcessing();

        // if OpenGL driver is equal & above minimum for quality rendering, enable FSAA 4x & node tesselation of 15
        if ( !GL_VERSION_STRING.isEmpty() && (Float.parseFloat( GL_VERSION_STRING.substring(0, 3) ) >= MINIMUM_OPENGL_VERSION_FOR_QUALITY_RENDERING_AND_SHADERS) )
            setHighQualityRenderingSettings();
    }

    /**
    *  Sets the rendering to high quality.
    */
    private void setHighQualityRenderingSettings()
    {
        SHOW_EDGES_WHEN_DRAGGING_NODES.set(true);
        HIGH_QUALITY_ANTIALIASING.set(true);
        NORMAL_QUALITY_ANTIALIASING.set(false);
        NODE_TESSELATION.set(15);
        CUSTOMIZE_NODE_NAMES_NAME_RENDERING_TYPE.set(1); // default node rendering type B/W instead of LogicOp
    }

    /**
    *  Sleeps for a max time given a previous time, depending on how much time has a process already been running.
    */
    private void sleepMaxTime(long prevTimeInMSecs)
    {
        long timeToSleep = MAX_TIME_IN_MSECS_TO_SLEEP_FOR_LOADING - (System.nanoTime() / 1000000 - prevTimeInMSecs);
        if (timeToSleep <= 0)
        {
            if (DEBUG_BUILD) println("sleepMaxTime() 1: timeToSleep <= 0: " + timeToSleep);
            return;
        }
        else
        {
            if (DEBUG_BUILD) println("sleepMaxTime() 2: timeToSleep > 0: " + timeToSleep);
            sleep(timeToSleep);
        }
    }

    /**
    *  Loads the rest of the preferences.
    */
    private void loadRestOfPreferences()
    {
        GRAPH_ANAGLYPH_GLASSES_TYPE = GraphAnaglyphGlassesTypes.values()[getEnumIndexForName( GraphAnaglyphGlassesTypes.class, ANAGLYPH_GLASSES_TYPE.get() )];
        GRAPH_INTRA_OCULAR_DISTANCE_TYPE = GraphIntraOcularDistanceTypes.values()[getEnumIndexForName( GraphIntraOcularDistanceTypes.class, INTRA_OCULAR_DISTANCE_TYPE.get() )];

        NODE_NAMES_OPENGL_FONT_TYPE = OpenGLFontTypes.values()[getEnumIndexForName( OpenGLFontTypes.class, CUSTOMIZE_NODE_NAMES_OPENGL_NAME_FONT_TYPE.get() )];

        LATHE3D_SETTINGS.xsIn = LayoutPreferences.readPreferenceFloatArrayString( LATHE3D_SETTINGS_XSIN.get() );
        LATHE3D_SETTINGS.ysIn = LayoutPreferences.readPreferenceFloatArrayString( LATHE3D_SETTINGS_YSIN.get() );
        LATHE3D_SETTINGS.splineStep = LATHE3D_SETTINGS_SPLINE_STEP.get();
        LATHE3D_SETTINGS.k = LATHE3D_SETTINGS_K.get();
        LATHE3D_SETTINGS.lathe3DShapeType = Lathe3DShapeTypes.values()[LATHE3D_SETTINGS_LATHE3D_SHAPE_TYPE.get()];

        SUPER_QUADRIC_SETTINGS.e = SUPER_QUADRIC_SETTINGS_E.get();
        SUPER_QUADRIC_SETTINGS.n = SUPER_QUADRIC_SETTINGS_N.get();
        SUPER_QUADRIC_SETTINGS.v1 = SUPER_QUADRIC_SETTINGS_V1.get();
        SUPER_QUADRIC_SETTINGS.alpha = SUPER_QUADRIC_SETTINGS_ALPHA.get();
        SUPER_QUADRIC_SETTINGS.superQuadricShapeType = SuperQuadricShapeTypes.values()[SUPER_QUADRIC_SETTINGS_SUPER_QUADRIC_SHAPE_TYPE.get()];

        EXTERNAL_OBJ_MODEL_FILE_PATH = MODEL_FILES_PATH;
        EXTERNAL_OBJ_MODEL_FILE_NAME = capitalizeFirstCharacter(OBJModelShapes.values()[getEnumIndexForName( OBJModelShapes.class, OBJ_MODEL_LOADER_CHOSEN_PRESET_SHAPE.get() )]);
    }

    /**
    *  Sleeps for a given time (in milliseconds) and gives other threads a chance to run.
    */
    public static void sleep(long sleepTimeInMSecs)
    {
        try
        {
            Thread.yield(); // Give another thread a chance to run
            TimeUnit.MILLISECONDS.sleep(sleepTimeInMSecs);
        }
        catch (InterruptedException ex)
        {
            // restore the interuption status after catching InterruptedException
            Thread.currentThread().interrupt();
            if (DEBUG_BUILD) println("Problem with the sleeping thread method in the LayoutFrame class:\n" + ex.getMessage() );
        }
    }

    /**
    *  Initializes all actions.
    */
    private void initActions(final LayoutFrame layoutFrame)
    {
        fileMenuOpenAction = new AbstractAction("Open")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555679L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                loadFile();
            }
        };

        fileMenuExitAction = new AbstractAction("Exit")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555680L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                closeApplication();
            }
        };

        toolsMenuSavePreferencesAction = new AbstractAction("Save Preferences")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555687L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                LayoutPreferences.getLayoutPreferencesSingleton().savePreferences();
                layoutGraphPropertiesDialog.setHasNewPreferencesBeenApplied(false);
            }
        };

        toolsMenuRevertToDefaultPreferencesAction = new AbstractAction("Revert To Default Preferences")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555688L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int option = JOptionPane.showConfirmDialog(layoutFrame, "Do you really want to revert to default preferences?", "Revert to Default Preferences", JOptionPane.YES_NO_CANCEL_OPTION);
                if (option == JOptionPane.YES_OPTION)
                {
                    LayoutPreferences.getLayoutPreferencesSingleton().revertToDefaultPreferences();
                    CHANGE_TEXTURE_ENABLED = true;
                    CHANGE_SPHERICAL_MAPPING_ENABLED = true;
                    CHANGE_NODE_TESSELATION = true;
                    INSTALL_DIR_FOR_SCREENSHOTS_HAS_CHANGED = false;
                    graph.updateDisplayLists(true, true, true);
                    layoutGraphPropertiesDialog.setHasNewPreferencesBeenApplied(true);
                }
            }
        };

        _2D3DSwitchAction = new AbstractAction("Toggle 2D/3D")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555689L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                rendererModeSwitchProcess(Thread.NORM_PRIORITY);
            }
        };

        homeAction = new AbstractAction("Home")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555689L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                InitDesktop.browse(DOMAIN_URL);
            }
        };

        blockAllAction = new AbstractAction("Block All")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555686L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                IS_BLOCKED = e.getActionCommand().equals(BLOCK_ALL);

                layoutGraphPropertiesToolBar.setEnabled(!IS_BLOCKED);
                layoutGeneralToolBar.setEnabled(!IS_BLOCKED);
                layoutNavigationToolBar.setEnabled(!IS_BLOCKED);
                layoutMenuBar.setEnabled(!IS_BLOCKED);

                layoutNavigationWizardDialog.setAlwaysOnTop(IS_BLOCKED);
                setCursor(IS_BLOCKED ? STATIC_WAIT_CURSOR : STATIC_NORMAL_CURSOR);
            }
        };

        blockAllExceptNavigationToolBarAction = new AbstractAction("Block All Except Navigation ToolBar")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555686L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                IS_BLOCKED = e.getActionCommand().equals(BLOCK_ALL);

                layoutGraphPropertiesToolBar.setEnabled(!IS_BLOCKED);
                layoutGeneralToolBar.setEnabled(! IS_BLOCKED);
                layoutMenuBar.setEnabled(!IS_BLOCKED);

                layoutNavigationWizardDialog.setAlwaysOnTop(IS_BLOCKED);
            }
        };

        initGraphPropertiesToolBarActions();
        initGeneralToolBarActions();
        initNavigationToolBarActions();
        initHomeToolBarActions();
        initMenuBarActions();
        initFileDragNDrop();
    }

    /**
    *  Initializes all menubar actions.
    */
    private void initMenuBarActions()
    {
        layoutMenuBar.cleanAllMenus();

        layoutMenuBar.setFileMenuOpenAction(fileMenuOpenAction);
        layoutMenuBar.setFileMenuOpenRecentAction( fileOpenHistory.getActionsList(this) );
        layoutMenuBar.setFileMenuSaveGraphAsAction( saver.getSaveAction() );
        layoutMenuBar.setFileMenuSaveGraphSelectionAsAction( saver.getSaveSelectedAction() );
        layoutMenuBar.setFileMenuSaveVisibleGraphAsAction( saver.getSaveVisibleAction() );

        //Import submenu
        layoutMenuBar.setFileSubMenuImportClassSetsAction(importClassSetsParser.getImportClassSetsAction() );
        layoutMenuBar.setFileSubMenuImportNetworkAction(importWebService.getImportWebServiceAction());

        layoutMenuBar.setFileMenuExportClassSetsAsFileAction( exportClassSets.getExportClassSetsFromGraphAction() );
        layoutMenuBar.setFileMenuExportClassSetsAsFileAction( exportClassSets.getExportClassSetsFromGraphSelectionAction() );
        layoutMenuBar.setFileMenuExportClassSetsAsFileAction( exportClassSets.getExportClassSetsFromVisibleGraphAction() );
        layoutMenuBar.setFileMenuExportAction( exportCorrelationNodesEdgesTable.getExportCorrelationNodesEdgesTableAction() );
        layoutMenuBar.setFileMenuExportAction( exportD3.getExportD3Action() );
        layoutMenuBar.setFileMenuPrintPageSetupAction( layoutPrintServices.getPrintGraphPageSetupDialogAction() );
        layoutMenuBar.setFileMenuExitAction(fileMenuExitAction);

        layoutMenuBar.setEditMenuUndoNodeDraggingOnSelectionAction( graph.getGraphRendererActions().getUndoNodeDraggingAction() );
        layoutMenuBar.setEditMenuRedoNodeDraggingOnSelectionAction( graph.getGraphRendererActions().getRedoNodeDraggingAction() );
        layoutMenuBar.setEditMenuDeleteSelectionAction( graph.getSelectionManager().getDeleteSelectionAction() );
        layoutMenuBar.setEditMenuDeleteHiddenAction( graph.getSelectionManager().getDeleteHiddenAction() );
        layoutMenuBar.setEditMenuDeleteUnselectedAction( graph.getSelectionManager().getDeleteUnselectedAction() );
        layoutMenuBar.setEditMenuUndoLastDeleteAction( graph.getSelectionManager().getUndoLastDeleteAction() );
        layoutMenuBar.setEditMenuUndeleteAllNodesAction( graph.getSelectionManager().getUndeleteAllNodesAction() );
        layoutMenuBar.setEditMenuCollapseNodesByClassAction( graph.getSelectionManager().getClassGroupAction() );
        layoutMenuBar.setEditMenuCollapseSelectionAction( graph.getSelectionManager().getGroupAction() );
        layoutMenuBar.setEditMenuPerformCompleteGrouping( graph.getSelectionManager().getCompleteGroupingAction() );
        layoutMenuBar.setEditMenuUnCollapseSelectedGroupsAction( graph.getSelectionManager().getUnGroupSelectedAction() );
        layoutMenuBar.setEditMenuUnCollapseAllGroupsAction( graph.getSelectionManager().getUnGroupAllAction() );
        layoutMenuBar.setEditMenuFilterNodesByEdgesAction( filterNodesByEdgesDialog.getFilterNodesByEdgesAction() );
        layoutMenuBar.setEditMenuFilterEdgesByWeightAction( filterEdgesByWeightDialog.getFilterEdgesByWeightAction() );

        layoutMenuBar.setEditSubMenuSelectAllAction( graph.getSelectionManager().getSelectAllAction() );
        layoutMenuBar.setEditSubMenuSelectNeighbours( graph.getSelectionManager().getSelectNeighbourAction() );
        layoutMenuBar.setEditSubMenuSelectAllNeighbours( graph.getSelectionManager().getSelectAllNeighbourAction() );
        layoutMenuBar.setEditSubMenuSelectParents( graph.getSelectionManager().getSelectParentsAction() );
        layoutMenuBar.setEditSubMenuSelectAllPArents( graph.getSelectionManager().getSelectAllParentsAction() );
        layoutMenuBar.setEditSubMenuSelectChildren( graph.getSelectionManager().getSelectChildrenAction() );
        layoutMenuBar.setEditSubMenuSelectAllChildren( graph.getSelectionManager().getSelectAllChildrenAction() );
        layoutMenuBar.setEditSubMenuSelectNodesWithinTheSameClassAction( graph.getSelectionManager().getSelectClassAction() );
        layoutMenuBar.setEditSubMenuReverseSelectionAction( graph.getSelectionManager().getReverseSelectionAction() );
        layoutMenuBar.setEditSubMenuDeselectAllAction( graph.getSelectionManager().getDeselectAllAction() );

        layoutMenuBar.setViewMenuToggle2D3DAction(_2D3DSwitchAction);
        layoutMenuBar.setViewMenuHideSelectionAction( graph.getSelectionManager().getHideSelectionAction() );
        layoutMenuBar.setViewMenuHideUnselectedAction( graph.getSelectionManager().getHideUnselectedAction() );
        layoutMenuBar.setViewMenuUnhideAllNodesAction( graph.getSelectionManager().getUnhideAllAction() );
        layoutMenuBar.setViewMenuShowAllNodeNamesAction( graph.getSelectionManager().getShowAllNodeNamesAction() );
        layoutMenuBar.setViewMenuShowSelectedNodeNamesAction( graph.getSelectionManager().getShowSelectedNodeNamesAction() );
        layoutMenuBar.setViewMenuShowAllEdgeNamesAction( graph.getSelectionManager().getShowAllEdgeNamesAction() );
        layoutMenuBar.setViewMenuShowSelectedNodesEdgeNamesAction( graph.getSelectionManager().getShowSelectedNodesEdgeNamesAction() );
        layoutMenuBar.setViewMenuHideAllNodeNamesAction( graph.getSelectionManager().getHideAllNodeNamesAction() );
        layoutMenuBar.setViewMenuHideSelectedNodeNamesAction( graph.getSelectionManager().getHideSelectedNodeNamesAction() );
        layoutMenuBar.setViewMenuHideAllEdgeNamesAction( graph.getSelectionManager().getHideAllEdgeNamesAction() );
        layoutMenuBar.setViewMenuHideSelectedNodesEdgeNamesAction( graph.getSelectionManager().getHideSelectedNodesEdgeNamesAction() );
        layoutMenuBar.setViewMenuCustomizeNodeNamesAction( layoutCustomizeNodeNamesDialog.getCustomizeNodeNamesAction() );
        layoutMenuBar.setViewMenuShowClassesLegendsAction( layoutShowClassesLegendsDialog.getShowClassesLegendsShowAction() );

        layoutMenuBar.setSearchMenuFindByNameAction( findNameDialog.getFindNameDialogAction() );
        layoutMenuBar.setSearchMenuFindByClassAction( findClassDialog.getFindClassDialogAction() );
        layoutMenuBar.setSearchMenuFindByMultipleClassesAction( findMultipleClassesDialog.getFindMultipleClassesDialogAction() );

        layoutMenuBar.setSimulationMenuSPNDialogAction( SPNSimulationDialog.getSignalingPetriNetSimulationDialogAction() );
        layoutMenuBar.setSimulationMenuLoadSimulationDataAction( signalingPetriNetLoadSimulation.getSignalingPetriNetLoadSimulationAction() );

        layoutMenuBar.setToolsMenuGraphPropertiesAction( layoutGraphPropertiesDialog.getGeneralPropertiesAction() );
        layoutMenuBar.setToolsMenuSavePreferences(toolsMenuSavePreferencesAction);
        layoutMenuBar.setToolsMenuRevertToDefaultPreferences(toolsMenuRevertToDefaultPreferencesAction);
        layoutMenuBar.setToolsMenuGraphStatisticsAction( layoutGraphStatisticsDialog.getGraphStatisticsDialogAction() );
        layoutMenuBar.setToolsMenuClassViewerAction( classViewerFrame.getClassViewerAction() );
        layoutMenuBar.setToolsMenuAnimationControlDialogAction( layoutAnimationControlDialog.getAnimationControlDialogAction() );
        layoutMenuBar.setToolsMenuClusterUsingMCL( layoutClusterMCL.getClusterMCLAction() );

        layoutMenuBar.setHelpMenuNavigationWizardAction( layoutNavigationWizardDialog.getNavigationWizardAction() );
        layoutMenuBar.setHelpMenuTipOfTheDayAction( layoutTipOfTheDayDialog.getTipOfTheDayAction() );
        layoutMenuBar.setHelpMenuLicensesAction( layoutLicensesDialog.getLicensesAction() );
        layoutMenuBar.setHelpMenuOpenGLDriverCapsAction( layoutOpenGLDriverCapsDialog.getOpenGLDriverCapsAction() );
        layoutMenuBar.setHelpMenuJavaPlatformCapsAction( layoutJavaPlatformCapsDialog.getJavaPlatformCapsAction() );
        layoutMenuBar.setHelpMenuAboutAction( layoutAboutDialog.getAboutAction() );
        layoutMenuBar.setHelpMenuValidateLicenseKeyAction(layoutValidateLicenseKeyDialog.getValidateLicenseKeyAction());

        // Adds the 2D/3D menus in 2D/3D mode
        if (RENDERER_MODE_3D)
        {
            layoutMenuBar.set3DMenuAutoRotateAction( graph.getGraphRendererActions().getAutoRotateAction() );
            layoutMenuBar.set3DMenuPulsation3DModeAction( graph.getGraphRendererActions().getPulsation3DModeAction() );
            layoutMenuBar.set3DMenuRotateAction( graph.getGraphRendererActions().getRotateAction() );
            layoutMenuBar.set3DMenuSelectAction( graph.getGraphRendererActions().getSelectAction() );
            layoutMenuBar.set3DMenuTranslateAction( graph.getGraphRendererActions().getTranslateAction() );
            layoutMenuBar.set3DMenuZoomAction( graph.getGraphRendererActions().getZoomAction() );
            layoutMenuBar.set3DMenuResetViewAction( graph.getGraphRendererActions().getResetViewAction() );
            layoutMenuBar.set3DMenuRenderAction( graph.getGraphRendererActions().getRenderImageToFileAction() );
            layoutMenuBar.set3DMenuHighResRenderAction( graph.getGraphRendererActions().getRenderHighResImageToFileAction() );
        }
        else
        {
            layoutMenuBar.set2DMenuAutoRotateAction( graph.getGraphRendererActions().getAutoRotateAction() );
            layoutMenuBar.set2DMenuScreenSaver2DModeAction( graph.getGraphRendererActions().getAutoScreenSaver2DModeAction() );
            layoutMenuBar.set2DMenuTranslateAction( graph.getGraphRendererActions().getTranslateAction() );
            layoutMenuBar.set2DMenuZoomAction( graph.getGraphRendererActions().getZoomAction() );
            layoutMenuBar.set2DMenuResetViewAction( graph.getGraphRendererActions().getResetViewAction() );
            layoutMenuBar.set2DMenuRenderAction( graph.getGraphRendererActions().getRenderImageToFileAction() );
            layoutMenuBar.set2DMenuHighResRenderAction( graph.getGraphRendererActions().getRenderHighResImageToFileAction() );
        }

        layoutMenuBar.set3DMenuEnabled(RENDERER_MODE_3D);
        layoutMenuBar.set2DMenuEnabled(!RENDERER_MODE_3D);
    }

    /**
    *  Initializes the graph properties toolbar all actions.
    */
    private void initGraphPropertiesToolBarActions()
    {
        layoutGraphPropertiesToolBar.removeAll();
        layoutGraphPropertiesToolBar.setGeneralAction(layoutGraphPropertiesDialog.getGeneralPropertiesAction() );
        layoutGraphPropertiesToolBar.setLayoutAction( layoutGraphPropertiesDialog.getLayoutPropertiesAction() );
        layoutGraphPropertiesToolBar.setRenderingAction( layoutGraphPropertiesDialog.getRenderingPropertiesAction() );
        layoutGraphPropertiesToolBar.setMCLAction( layoutGraphPropertiesDialog.getMCLPropertiesAction() );
        layoutGraphPropertiesToolBar.setSimulationAction( layoutGraphPropertiesDialog.getSimulationPropertiesAction() );
        layoutGraphPropertiesToolBar.setSearchAction( layoutGraphPropertiesDialog.getSearchPropertiesAction() );
        layoutGraphPropertiesToolBar.setNodesAction( layoutGraphPropertiesDialog.getNodesPropertiesAction() );
        layoutGraphPropertiesToolBar.setEdgesAction( layoutGraphPropertiesDialog.getEdgesPropertiesAction() );
        layoutGraphPropertiesToolBar.setClassesAction( layoutGraphPropertiesDialog.getClassesPropertiesAction() );
        layoutGraphPropertiesToolBar.setEnabled(true);
    }

    /**
    *  Initializes the general toolbar all actions.
    */
    private void initGeneralToolBarActions()
    {
        layoutGeneralToolBar.removeAll();
        layoutGeneralToolBar.setGraphOpenAction(fileMenuOpenAction);
        layoutGeneralToolBar.setGraphLastOpenAction(fileOpenHistory.getActionsList(this), this);
        layoutGeneralToolBar.setGraphSaveAction( saver.getSaveAction() );
        layoutGeneralToolBar.setSnapshotAction( graph.getGraphRendererActions().getRenderImageToFileAction() );
        layoutGeneralToolBar.setGraphInformationAction( layoutGraphStatisticsDialog.getGraphStatisticsDialogAction() );
        layoutGeneralToolBar.setGraphFindAction( findNameDialog.getFindNameDialogAction() );
        layoutGeneralToolBar.setRunMCLAction( layoutClusterMCL.getClusterMCLAction() );
        layoutGeneralToolBar.setRunSPNAction( SPNSimulationDialog.getSignalingPetriNetSimulationDialogAction() );
        layoutGeneralToolBar.setClassViewerAction( classViewerFrame.getClassViewerAction() );
        layoutGeneralToolBar.setAnimationControlAction( layoutAnimationControlDialog.getAnimationControlDialogAction() );
        layoutGeneralToolBar.setBurstLayoutIterationsAction( graph.getGraphActions().getBurstLayoutIterationsAction() );
        layoutGeneralToolBar.set2D3DSwitchAction(_2D3DSwitchAction);
        layoutGeneralToolBar.setEnabled(true);
    }

    /**
    *  Initializes the navigation toolbar all actions.
    */
    private void initNavigationToolBarActions()
    {
        layoutNavigationToolBar.removeAll();
        layoutNavigationToolBar.setUpAction( graph.getGraphActions().getTranslateUpAction() );
        layoutNavigationToolBar.setDownAction( graph.getGraphActions().getTranslateDownAction() );
        layoutNavigationToolBar.setLeftAction( graph.getGraphActions().getTranslateLeftAction() );
        layoutNavigationToolBar.setRightAction( graph.getGraphActions().getTranslateRightAction() );
        layoutNavigationToolBar.setRotateUpAction( graph.getGraphActions().getRotateUpAction() );
        layoutNavigationToolBar.setRotateDownAction( graph.getGraphActions().getRotateDownAction() );
        layoutNavigationToolBar.setRotateLeftAction( graph.getGraphActions().getRotateLeftAction() );
        layoutNavigationToolBar.setRotateRightAction( graph.getGraphActions().getRotateRightAction() );
        layoutNavigationToolBar.setZoomInAction( graph.getGraphActions().getZoomInAction() );
        layoutNavigationToolBar.setZoomOutAction( graph.getGraphActions().getZoomOutAction() );
        layoutNavigationToolBar.setResetViewAction( graph.getGraphRendererActions().getResetViewAction() );
        layoutNavigationToolBar.setNavigationWizardAction( layoutNavigationWizardDialog.getNavigationWizardAction() );
        layoutNavigationToolBar.setEnabled(true);
    }

    /**
    *  Initializes the navigation toolbar all actions.
    */
    private void initHomeToolBarActions()
    {
        layoutHomeToolBar.removeAll();
        layoutHomeToolBar.setHomeAction(homeAction);
        layoutHomeToolBar.setEnabled(true);
    }

    /**
    *  Graphical/GUI initialization of the LayoutFrame.
    */
    private void frameInitializationFinish(final boolean startWithAutomaticFileLoading)
    {
        try
        {
            // SwingUtilities.invokeAndWait( new Runnable()
            EventQueue.invokeAndWait( new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        setVisible(true);

                        usageTracker.log("open");
                        if (!startWithAutomaticFileLoading)
                            checkToShowNavigationWizardOnStartup();
                    }
                    catch (Exception exc)
                    {
                        if (DEBUG_BUILD) println(DISPLAY_PRODUCT_NAME + " frame initialization error: " + exc.getMessage());

                        dispose();
                        System.exit(1);
                    }
                }
            } );
        }
        catch (InterruptedException ex)
        {
            // restore the interuption status after catching InterruptedException
            Thread.currentThread().interrupt();
            if (DEBUG_BUILD) println("InterruptedException with the SwingUtilities.invokeAndWait() blocking thread method in the LayoutFrame class:\n" + ex.getMessage() );
        }
        catch (InvocationTargetException invEx)
        {
            if (DEBUG_BUILD) println("InvocationTargetException with the SwingUtilities.invokeAndWait() blocking thread method in the LayoutFrame class:\n" + invEx.getMessage() );
        }
    }

    /**
    *  Initializes the drag and drop process.
    */
    private void initFileDragNDrop()
    {
        fileDragNDrop = new FileDragNDrop(graph, new FileDragNDrop.FileDragNDropListener()
        {

            @Override
            public void filesDropped(File file)
            {
                loadDataSet(file);
            }


        }, fileFilter);
    }

    private void setRenderModeSwitchUISettings()
    {
        graph.setEnabledUndoNodeDragging(!RENDERER_MODE_3D);
        graph.setEnabledRedoNodeDragging(!RENDERER_MODE_3D);
        graph.getGraphActions().getRotateUpAction().setEnabled(RENDERER_MODE_3D);   // rotate   up is enabled in 3D mode only
        graph.getGraphActions().getRotateDownAction().setEnabled(RENDERER_MODE_3D); // rotate down is enabled in 3D mode only
    }

    /*
    *   Process a light-weight thread using the Adapter technique to avoid any load latencies.
    */
    private void rendererModeSwitchProcess(int threadPriority)
    {
        Thread rendererModeSwitchThread = new Thread( new Runnable()
        {

            @Override
            public void run()
            {
                graph.switchRendererMode();
            }


        }, "renderModeSwitchProcess" );

        rendererModeSwitchThread.setPriority(threadPriority);
        rendererModeSwitchThread.start();
    }

    /**
    *  Loads a file.
    */
    private void loadFile()
    {
        String loadFilePath = FILE_CHOOSER_PATH.get().substring(0, FILE_CHOOSER_PATH.get().lastIndexOf( System.getProperty("file.separator") ) + 1);
        JFileChooser fileChooser = new JFileChooser(loadFilePath);
        fileChooser.setDialogTitle("Open Graph File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(fileFilter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            FILE_CHOOSER_PATH.set( file.getAbsolutePath() );
            loadDataSet(file);
        }
    }

    /**
    *  Loads a given dataset.
    */
    public void loadDataSet(File file)
    {
        if (loadingFile)
        {
            JOptionPane.showMessageDialog(this, "A file is now being loaded, please wait to finish the loading process first!", "Error: A file is now being loaded", JOptionPane.WARNING_MESSAGE);
        }
        else if (IS_BLOCKED)
        {
            JOptionPane.showMessageDialog(this, "A process is currently activated, please wait for it to finish first!", "Error: A process is currently activated", JOptionPane.WARNING_MESSAGE);
        }
        else
        {
            if ( !file.exists() )
            {
                JOptionPane.showMessageDialog(this, "File does not exist, please check the file!", "Error: File does not exist", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                if ( fileFilter.accept(file) )
                {
                    runParseProcess(this, Thread.NORM_PRIORITY, file);
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "Not supported " + PRODUCT_NAME + " file type!\n" + fileFilter.getDescription(),
                                                        "Error: Not supported " + PRODUCT_NAME + " file type", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
    *  Loads an online dataset.
    */
    public void loadOnlineDataSet(String repository, String dataSets)
    {
        if ( !dataSets.isEmpty() )
        {
            DataSetsDownloader dataSetsDownloader = new DataSetsDownloader(repository, dataSets, layoutProgressBarDialog);
            boolean hasConnectedDataSetRetrieval = dataSetsDownloader.retrieveDataSetsFromRepository();

            if (DEBUG_BUILD) println("Has successfully connected for data set retrieval: " + hasConnectedDataSetRetrieval);

            if (!hasConnectedDataSetRetrieval)
            {
                layoutProgressBarDialog.endProgressBar();
                layoutProgressBarDialog.stopProgressBar();
                JOptionPane.showMessageDialog(this, "Data Set Downloading failed, probable connection error!", "Error: Data Set Downloading failed", JOptionPane.ERROR_MESSAGE);
            }
            else
                loadDataSet( new File( dataSetsDownloader.getDataSetName() ) );
        }

        if ( !nc.getHasStandardPetriNetTransitions() )
            checkToShowNavigationWizardOnStartup();
    }

    /**
    *   Process a light-weight thread using the Adapter technique to avoid any load latencies.
    */
    private void runParseProcess(final LayoutFrame layoutFrame, int threadPriority, final File file)
    {
        Thread runLightWeightThread = new Thread( new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    loadingFile = true;
                    // 
                    AnnotationTypeManagerBG.recreateSingleton();
                    layoutClassSetsManager.clearClassSets();
                    classViewerFrame.resetView();

                    blockAllAction.actionPerformed(blockEvent);
                    parseFile(file);
                    blockAllAction.actionPerformed(unblockEvent);

                    // so as to update history in the GUI
                    fileOpenHistory.addToHistory( file.getAbsolutePath() );
                    layoutMenuBar.setFileMenuOpenRecentAction( fileOpenHistory.getActionsList(layoutFrame) );
                    layoutGeneralToolBar.refreshGraphLastOpenAction(fileOpenHistory.getActionsList(layoutFrame), layoutFrame);

                }
                catch (OutOfMemoryError memErr)
                {
                    if (DEBUG_BUILD) println("Out of Memory Error with parsing the file in runLightWeightThread():\n" + memErr.getMessage());

                    layoutProgressBarDialog.endProgressBar();
                    layoutProgressBarDialog.stopProgressBar();
                    blockAllAction.actionPerformed(unblockEvent);
                    resetAllRelevantLoadingValues(true);

                    throwableErrorMessageDialogReport(layoutFrame, memErr, "Out of Memory Error", file.getName());
                }
                catch(IllegalArgumentException exc) //thrown by PaxTools when OWL file is not valid
                {
                    if (DEBUG_BUILD) println("IllegalArgumentException with parsing the file in runLightWeightThread():\n" + exc.getMessage());
                    layoutProgressBarDialog.endProgressBar();
                    layoutProgressBarDialog.stopProgressBar();

                    resetAllRelevantLoadingValues(false);

                    throwableErrorMessageDialogReport(layoutFrame, exc, "Parse Error", file.getName());
                }
                catch (Exception exc)
                {
                    if (DEBUG_BUILD) println("Exception in runLightWeightThread()\n" + exc.getMessage() );

                    layoutProgressBarDialog.endProgressBar();
                    layoutProgressBarDialog.stopProgressBar();
                    blockAllAction.actionPerformed(unblockEvent);
                    resetAllRelevantLoadingValues(true);

                    throwableErrorMessageDialogReport(layoutFrame, exc, "File Load Error", file.getName());
                }
                finally
                {
                    loadingFile = false;
                }
            }
        }, "runParseProcess" );

        runLightWeightThread.setPriority(threadPriority);
        runLightWeightThread.start();
    }

    /**
    *  Reports a throwable exception to the user through an UI dialog.
    */
    private void throwableErrorMessageDialogReport(LayoutFrame layoutFrame, Throwable thrw, String typeOfError, String fileName)
    {
        int option = JOptionPane.showConfirmDialog(layoutFrame, typeOfError + " for parsing file " + fileName + ":\n" + thrw.getMessage() + "\n\nWould you like to view a detailed StackTrace dump report?\n\n", typeOfError, JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (option == JOptionPane.YES_OPTION)
        {
            StringBuilder stackTraceStringBuffer = new StringBuilder();
            for ( StackTraceElement stackTraceElement : thrw.getStackTrace() )
                stackTraceStringBuffer.append( stackTraceElement.toString() ).append("\n");
            if (DEBUG_BUILD) println( "StackTrace Dump Report With " + typeOfError + " for parsing file " + fileName + ":\n\n" + stackTraceStringBuffer.toString() );
            JOptionPane.showMessageDialog(layoutFrame, "StackTrace Dump Report With " + typeOfError + " for parsing file " + fileName + ":\n\n" + stackTraceStringBuffer.toString() + "\n", "StackTrace Dump Report With " + typeOfError, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
    *  Parses a given file.
    */
    private void parseFile(File file)
    {
        classViewerFrame.closeClassViewerWindow();
        layoutAnimationControlDialog.closeDialogWindow();
        ANIMATION_CORRELATION_DATA = null;
        ANIMATION_SIMULATION_RESULTS = null;
        TEMPORARILY_DISABLE_ALL_GRAPH_RENDERING = true;
        graph.resetAllValues();
        graph.refreshDisplay();
        setStatusLabel("Loading");

        boolean isSuccessful = true; // so as to avoid parser error messages if parsing process is skipped
        boolean isNotSkipped = true; // so as to avoid parsing files and not updating the GUI
        boolean reachedRebuildNetwork = false;
        CoreParser parser = null;
        String absFileName = file.getAbsolutePath();
        String fileName = file.getName();
        String fileExtension = absFileName.substring( absFileName.lastIndexOf(".") + 1, absFileName.length() ).toUpperCase(); // tolerance to upper/lowercase mix-ups
        DataTypes prevDataType = DATA_TYPE;
        String prevCorrelationFile = CORRELATION_FILE;
        String prevCorrelationFilePath = CORRELATION_FILE_PATH;

        DATA_TYPE = DataTypes.NONE;
        CORRELATION_FILE = "";
        CORRELATION_FILE_PATH = "";
        WEIGHTED_EDGES = false;
        CorrelationLoader correlationLoader = null;
        String reasonForLoadFailure = null;
        double correlationCutOffValue = 0.0;

        // Blast data
        if ( fileExtension.equals( SupportedInputFileTypes.BLAST.toString() ) )
        {
            parser = new BlastParser(nc,  this);
            DATA_TYPE = DataTypes.BLAST;
        }
        // BioPAX OWL Data
        else if ( fileExtension.equals( SupportedInputFileTypes.OWL.toString() ) )
        {
            parser = new BioPaxParser(nc, this);
            DATA_TYPE = DataTypes.OWL;
        }
        // Correlation data (non-layed out)
        else if ( fileExtension.equals( SupportedInputFileTypes.CSV.toString() ) ||
                fileExtension.equals( SupportedInputFileTypes.EXPRESSION.toString() ))
        {
            boolean tabDelimited = fileExtension.equals(SupportedInputFileTypes.EXPRESSION.toString());
            CorrelationLoaderDialog correlationLoaderDialog = new CorrelationLoaderDialog(this, file, tabDelimited);

            if (!correlationLoaderDialog.failed())
            {
                // Only show if we haven't already failed
                correlationLoaderDialog.setVisible(true);
            }

            if ( isNotSkipped = correlationLoaderDialog.proceed() )
            {
                // call to clearNetwork() has to be here or the data parser will fail
                clearNetworkAndGraph();

                correlationLoader = new CorrelationLoader(layoutClassSetsManager);
                correlationLoader.init(file, correlationData,
                        correlationLoaderDialog.getFirstDataColumn(),
                        correlationLoaderDialog.getFirstDataRow(),
                        correlationLoaderDialog.transpose() );
                isSuccessful = correlationLoader.parse(this);
                reasonForLoadFailure = correlationLoader.reasonForFailure; // "" if no failure

                if (isSuccessful)
                {
                    CORRELATION_FILE = file.getName();
                    CORRELATION_FILE_PATH = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf( System.getProperty("file.separator") ) + 1);
                    CORRELATION_DATA_FIRST_COLUMN = correlationLoaderDialog.getFirstDataColumn();
                    CORRELATION_DATA_FIRST_ROW = correlationLoaderDialog.getFirstDataRow();
                    CORRELATION_DATA_TRANSPOSE = correlationLoaderDialog.transpose();
                    DATA_TYPE = DataTypes.CORRELATION;

                    boolean generateTextFile = correlationLoaderDialog.saveCorrelationTextFile();

                    correlationData.preprocess(layoutProgressBarDialog, CURRENT_SCALE_TRANSFORM);

                    if (DEBUG_BUILD) println("Correlation File is: " + CORRELATION_FILE_PATH + CORRELATION_FILE);
                    String metricName = CURRENT_METRIC.toString().toLowerCase();

                    String correlationFilename = IOUtils.getPrefix(file.getAbsolutePath());
                    correlationFilename += "_r-" + STORED_CORRELATION_THRESHOLD;

                    if (CORRELATION_DATA_TRANSPOSE)
                    {
                        correlationFilename += "_transpose";
                    }

                    if (CURRENT_SCALE_TRANSFORM != ScaleTransformType.NONE)
                    {
                        correlationFilename += "_" + Utils.hyphenatedOf(CURRENT_SCALE_TRANSFORM.toString());
                    }

                    correlationFilename += "_" + metricName;
                    correlationFilename += ".correlationcache";

                    File correlationFile = new File(correlationFilename);
                    if (!correlationFile.exists())
                    {
                        correlationData.buildCorrelationNetwork(layoutProgressBarDialog,
                                correlationFile, metricName, STORED_CORRELATION_THRESHOLD,
                                generateTextFile);

                        if (!layoutProgressBarDialog.userHasCancelled())
                        {
                            file = correlationFile;
                        }
                        else
                        {
                            isNotSkipped = false;
                        }
                    }
                    else
                    {
                        File correlationTextFile = new File(correlationFilename + ".txt");
                        boolean forceGeneration = generateTextFile && !correlationTextFile.exists();

                        // there seems to be saved correlations here, let's check they are good for our requirements
                        CorrelationParser checker = new CorrelationParser(nc, this, correlationData);
                        checker.init(correlationFile, fileExtension);

                        if (!forceGeneration && checker.checkFile())
                        {
                            // the file looks good, let's use it
                            file = correlationFile;
                        }
                        else
                        {
                            // We should only get here if the correlationcache file is created by a different version
                            //
                            // The file is not good, close file before deletion, delete it & rebuild it
                            checker.close();
                            correlationFile.delete();
                            correlationData.buildCorrelationNetwork(layoutProgressBarDialog,
                                    correlationFile, metricName, STORED_CORRELATION_THRESHOLD,
                                    generateTextFile);
                            file = correlationFile;
                        }
                    }

                    if (isNotSkipped)
                    {
                        CorrelationParser scanner = new CorrelationParser(nc, this, correlationData);
                        scanner.init(file, fileExtension);
                        scanner.scan();

                        CorrelationLoaderSummaryDialog correlationLoaderSummaryDialog =
                                new CorrelationLoaderSummaryDialog(this, correlationData, scanner);
                        correlationLoaderSummaryDialog.setVisible(true);

                        if (isNotSkipped = correlationLoaderSummaryDialog.proceed())
                        {
                            parser = new CorrelationParser(nc, this, correlationData);

                            if (!exportCorrelationNodesEdgesTable.getExportCorrelationNodesEdgesTableAction().isEnabled())
                            {
                                exportCorrelationNodesEdgesTable.getExportCorrelationNodesEdgesTableAction().setEnabled(true);
                            }
                        }
                    }
                }

                if (!isSuccessful || !isNotSkipped)
                {
                    // make sure previous network is deleted from the renderers if parsing is skipped,
                    // as the calls to clear() has cleaned it from network component memory
                    graph.rebuildGraph();

                    DATA_TYPE = DataTypes.NONE;
                    CORRELATION_FILE = "";
                    CORRELATION_FILE_PATH = "";
                    fileNameLoaded = "";
                    fileNameAbsolutePath = "";
                    setTitle(DISPLAY_PRODUCT_NAME_AND_VERSION);
                    INSTALL_DIR_FOR_SCREENSHOTS_HAS_CHANGED = false;

                    disableAllActions();

                    resetAllRelevantLoadingValues(true);
                    reasonForLoadFailure = correlationLoader.reasonForFailure;
                }
            }
            else
            {
                    // file parsing was canceled, restoring original datatypes/names
                    DATA_TYPE = prevDataType;
                    CORRELATION_FILE = prevCorrelationFile;
                    CORRELATION_FILE_PATH = prevCorrelationFilePath;
            }
        }
        // Graphml Data
        else if ( fileExtension.equals( SupportedInputFileTypes.GRAPHML.toString() ) || fileExtension.equals( SupportedInputFileTypes.MEPN.toString() ) )
        {
            parser = new WrapperGraphmlToLayoutParser( nc, this, new GraphmlParser(this), VALIDATE_XML_FILES.get() );
            DATA_TYPE = DataTypes.GRAPHML;
        }
        // Column data
        else if (fileExtension.equals(SupportedInputFileTypes.TXT.toString()))
        {
            ColumnDataConfigurationDialog columnDataConfigurationDialog = new ColumnDataConfigurationDialog(this, file);

            if (!columnDataConfigurationDialog.failed())
            {
                columnDataConfigurationDialog.setVisible(true);

                if (columnDataConfigurationDialog.proceed())
                {
                    parser = new CoreParser(nc, this,
                            columnDataConfigurationDialog.nodeIdColumns,
                            columnDataConfigurationDialog.edgeWeightColumn,
                            columnDataConfigurationDialog.edgeTypeColumn,
                            columnDataConfigurationDialog.filterValue());
                    DATA_TYPE = DataTypes.LAYOUT;
                }
            }
        }
        // Layed out data
        else if (fileExtension.equals(SupportedInputFileTypes.LAYOUT.toString()) ||
                fileExtension.equals(SupportedInputFileTypes.SIF.toString()) ||
                fileExtension.equals(SupportedInputFileTypes.TGF.toString()))
        {
            // Regular file
            parser = new CoreParser(nc, this);
            DATA_TYPE = DataTypes.LAYOUT;
        }
        // Matrix file
        else if ( fileExtension.equals( SupportedInputFileTypes.MATRIX.toString() ) )
        {
            parser = new MatrixParser(nc, this);
            DATA_TYPE = DataTypes.MATRIX;

            if ( !( isNotSkipped = ( (MatrixParser)parser ).proceed() ) )
                parser = null;
            else
                correlationCutOffValue = ( (MatrixParser)parser ).getCorrelationCutOffValue();
        }
        else if ( fileExtension.equals( SupportedInputFileTypes.XML.toString() ) )
        {
            parser = new OXLParser(nc, this);
            DATA_TYPE = DataTypes.ONDEX;
        }
        else if (fileExtension.equals(SupportedInputFileTypes.GML.toString()))
        {
            parser = new GmlFileParser(nc, this);
            DATA_TYPE = DataTypes.GML;
        }
        else
        {
            if (DEBUG_BUILD) println( "Unsupported file format! " + file.getAbsolutePath() );

            return;
        }

        // Parse the file here
        if (parser != null)
        {
            if ( parser.init(file, fileExtension) )
            {
                if (DEBUG_BUILD) println("Parsing: " + file.getName() );

                // rest of file types perform network clearing here
                if ( !DATA_TYPE.equals(DataTypes.CORRELATION) )
                    clearNetworkAndGraph();

                // load in a correlation or raw network
                isSuccessful = parser.parse();

                 // if fileType is .data, the previous parse() call has saveda layout file, now let's parse that file
                if ( DATA_TYPE.equals(DataTypes.MATRIX) )
                {
                    // if only matrix file parsing was not canceled
                    if (isSuccessful && isNotSkipped)
                    {
                        // Regular file
                        parser = new CoreParser(nc, this);
                        DATA_TYPE = DataTypes.MATRIX;

                        file = new File(IOUtils.getPrefix( file.getAbsolutePath() ) + ".layout");
                        if ( parser.init( file, "layout") )
                            isSuccessful = parser.parse();
                    }
                }

                // just loaded a correlation file here from raw data
                // loading annotations now
                if ( DATA_TYPE.equals(DataTypes.CORRELATION) )
                {
                    isSuccessful = isSuccessful && correlationLoader.parseAnnotations(this, nc);
                }
                // else loading presaved data that points to data
                // load data and annotations from original file
                else if ( !CORRELATION_FILE.isEmpty() )
                {
                    String pathName = ( !CORRELATION_FILE_PATH.isEmpty() ) ? CORRELATION_FILE_PATH : file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf( System.getProperty("file.separator") ) + 1);
                    File correlationFile = new File(pathName + CORRELATION_FILE);

                    if (DEBUG_BUILD) println("Loading Correlation Data from: " + correlationFile.toString() );

                    if ( correlationFile.exists() )
                    {
                        if (DEBUG_BUILD) println("Loading Correlation Data");

                        correlationLoader = new CorrelationLoader(layoutClassSetsManager);
                        correlationLoader.init(correlationFile, correlationData,
                                CORRELATION_DATA_FIRST_COLUMN,
                                CORRELATION_DATA_FIRST_ROW,
                                CORRELATION_DATA_TRANSPOSE);
                        isSuccessful = correlationLoader.parse(this);
                        reasonForLoadFailure = correlationLoader.reasonForFailure; // "" if no failure

                        correlationData.preprocess(layoutProgressBarDialog, CURRENT_SCALE_TRANSFORM);

                        isSuccessful = isSuccessful && correlationLoader.parseAnnotations(this, nc);
                        DATA_TYPE = DataTypes.CORRELATION;
                        CORRELATION_FILE_PATH = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf( System.getProperty("file.separator") ) + 1);
                    }
                    else
                    {
                        String correlationFilePathMessage = ( !CORRELATION_FILE_PATH.isEmpty() ) ? "'" + pathName + "'" : "currently loading layout file drive/folder '" + pathName + "'";
                        JOptionPane.showMessageDialog(this, "The correlation data file from where this layout file was derived from was not found.\nExpected drive/folder: " +
                                correlationFilePathMessage + "\nThe layout file will now be loaded with no underlying correlation data profile information.",
                                "Loading correlation data profile information warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }

            final int EVALUATION_NODES_LIMIT = 3000;
            boolean sizeLimitExceeded = !IS_LICENSED ? nc.getVertices().size() > EVALUATION_NODES_LIMIT : false;

            if(!sizeLimitExceeded)
            {
                setStatusLabel("Layout");

                // has to enable weights for SPN graphml graphs so as to have the correct rendering of the red inhibitor edges
                // has to be enabled at this, so the graph is built with weight support on
                if ( ( DATA_TYPE.equals(DataTypes.GRAPHML) || DATA_TYPE.equals(DataTypes.LAYOUT) ) && nc.getIsPetriNet() )
                    WEIGHTED_EDGES = true;

                // directional edges off by default, since directionality there has no meaning
                // for the rest of the data files, directionality can be on by default
                boolean disableDirectionalEdges = DATA_TYPE.equals(DataTypes.CORRELATION) || DATA_TYPE.equals(DataTypes.MATRIX);
                DIRECTIONAL_EDGES.set(!disableDirectionalEdges);
                layoutGraphPropertiesDialog.setHasNewPreferencesBeenApplied(true);
                graph.getSelectionManager().getGroupManager().resetState();
                nc.createNetworkComponentsContainer();

                GraphLayoutAlgorithm gla;
                if (!nc.getVertices().isEmpty()) // fail-safe check in case the parsed file is an empty graph
                {
                    if (!nc.isOptimized())
                    {
                        gla = GRAPH_LAYOUT_ALGORITHM.get();

                        if (gla == GraphLayoutAlgorithm.ALWAYS_ASK)
                        {
                            // Ask the user
                            LayoutAlgorithmSelectionDialog lasd = new LayoutAlgorithmSelectionDialog(this);
                            gla = lasd.getGraphLayoutAlgorithm();
                        }

                        nc.optimize(gla);
                    }
                    else
                    {
                        nc.setKvalue();
                    }
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this, "This graph contains more than " +
                        EVALUATION_NODES_LIMIT + " nodes.\n" +
                        "Please purchase the full version to remove this limitation.",
                        "Limit Exceeded", JOptionPane.ERROR_MESSAGE);
            }

            if (!layoutProgressBarDialog.userHasCancelled() && !sizeLimitExceeded)
            {
                nc.clearRoot();
                nc.normaliseWeights();
                graph.rebuildGraph();
                graph.resetAllValues();
                reachedRebuildNetwork = true;
            }
            else
            {
                isNotSkipped = false;
            }
        }

        if (isSuccessful && isNotSkipped)
        {
            fileNameLoaded = file.getName();
            fileNameAbsolutePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf( System.getProperty("file.separator") ) + 1);
            String correlationValueString = ( DATA_TYPE.equals(DataTypes.CORRELATION) ) ? " at " + CURRENT_CORRELATION_THRESHOLD : ( DATA_TYPE.equals(DataTypes.MATRIX) ? " at " + correlationCutOffValue : "" );
            this.setTitle(DISPLAY_PRODUCT_NAME_AND_VERSION + "  [ " + file.getAbsolutePath() + correlationValueString + " ] " + ( (WEIGHTED_EDGES) ? "(" : "(non-" ) + "weighted graph)");
            INSTALL_DIR_FOR_SCREENSHOTS_HAS_CHANGED = !USE_INSTALL_DIR_FOR_SCREENSHOTS.get();

            classViewerFrame.getClassViewerAction().setEnabled(true);
            classViewerFrame.populateClassViewer( null, false, DATA_TYPE.equals(DataTypes.CORRELATION) && !correlationData.isTransposed(), true);
            classViewerFrame.refreshCurrentClassSetSelection();

            filterNodesByEdgesDialog.getFilterNodesByEdgesAction().setEnabled(true);
            filterEdgesByWeightDialog.getFilterEdgesByWeightAction().setEnabled(WEIGHTED_EDGES); // enable weight filter dialog action only when weights have been parsed

            layoutGraphPropertiesDialog.setEnabledProportionalEdgesSizeToWeight(WEIGHTED_EDGES); // enable proportional edges sizes to weight only when weights have been parsed
            layoutGraphPropertiesDialog.setEnabledNodeNameTextFieldAndSelectNodesTab(false, null, 0);

            enableAllActions();
        }
        else
        {
            if (reachedRebuildNetwork)
            {
                DATA_TYPE = DataTypes.NONE;
                CORRELATION_FILE = "";
                CORRELATION_FILE_PATH = "";
                fileNameLoaded = "";
                fileNameAbsolutePath = "";
                setTitle(DISPLAY_PRODUCT_NAME_AND_VERSION);
                INSTALL_DIR_FOR_SCREENSHOTS_HAS_CHANGED = false;

                disableAllActions();
            }
        }

        layoutProgressBarDialog.endProgressBar();
        layoutProgressBarDialog.stopProgressBar();
        TEMPORARILY_DISABLE_ALL_GRAPH_RENDERING = false;
        graph.refreshDisplay();

        // do not do it if a Class Set is pre-selected while parsing the file
        if ( graph.getSelectionManager().getSelectedNodes().isEmpty() )
            setStatusLabel("Ready");

        boolean initCheckToShowNavigationWizardOnStartup = false;
        if ( reachedRebuildNetwork && nc.getHasStandardPetriNetTransitions() )
        {
            int option = JOptionPane.showConfirmDialog(this, "This looks like a Signaling Petri Net (SPN) Pathway.\nWould you like to run an SPN simulation now?", "Signaling Petri Net (SPN) Pathway", JOptionPane.YES_NO_CANCEL_OPTION);
            if (option == JOptionPane.YES_OPTION)
            {
                SPNSimulationDialog.getSignalingPetriNetSimulationDialogAction().actionPerformed( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "") );
            }
            else
            {
                initCheckToShowNavigationWizardOnStartup = true;
            }
        }

        if (!isSuccessful && isNotSkipped)
        {
            String errorMessage = "Parse error loading file " + file.getName() + ".\n";

            if (reasonForLoadFailure != null && !reasonForLoadFailure.isEmpty())
            {
                errorMessage += reasonForLoadFailure;
            }
            else
            {
                errorMessage += "Please check the file is formatted correctly.";
            }

            JOptionPane.showMessageDialog(this, errorMessage, "Parse Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!nc.getHasStandardPetriNetTransitions() || initCheckToShowNavigationWizardOnStartup)
            checkToShowNavigationWizardOnStartup();

        usageTracker.log(fileName + ",Nodes:," + nc.getNumberOfVertices() +
                ",Edges:," + nc.getNumberOfEdges() + "," + DATA_TYPE);

        //if BioPAX network, display the class viewer
        if(DATA_TYPE == DataTypes.OWL)
        {
            graph.getSelectionManager().selectAll();
            classViewerFrame.displayClassViewer();
        }
    }

    /**
     * Reset variables that have been populated when loading a file.
     * @param disableAllActions - disable user interface components
     */
    private void resetAllRelevantLoadingValues(boolean disableAllActions)
    {
        DATA_TYPE = DataTypes.NONE;
        CORRELATION_FILE = "";
        CORRELATION_FILE_PATH = "";
        fileNameLoaded = "";
        fileNameAbsolutePath = "";
        setTitle(DISPLAY_PRODUCT_NAME_AND_VERSION);
        INSTALL_DIR_FOR_SCREENSHOTS_HAS_CHANGED = false;

        if(disableAllActions)
        {
            disableAllActions();
        }
    }

    private void enableAllActions()
    {
        // note, save all & print actions are being enabled from inside the LayoutGraphPropertiesToolBar, LayoutGeneralToolBar, LayoutNavigationToolBar & LayoutMenuBar classes
        if ( !layoutMenuBar.isEnabled() )
            layoutMenuBar.setEnabled(true);

        if ( !layoutGraphPropertiesToolBar.isEnabled() )
            layoutGraphPropertiesToolBar.setEnabled(true);

        if ( !layoutGeneralToolBar.isEnabled() )
            layoutGeneralToolBar.setEnabled(true);

        if ( !layoutNavigationToolBar.isEnabled() )
            layoutNavigationToolBar.setEnabled(true);

        if ( !layoutHomeToolBar.isEnabled() )
            layoutHomeToolBar.setEnabled(true);


        if ( !saver.getSaveAction().isEnabled() && IS_LICENSED )
            saver.getSaveAction().setEnabled(true);

        if ( !importClassSetsParser.getImportClassSetsAction().isEnabled() )
            importClassSetsParser.getImportClassSetsAction().setEnabled(true);

        if ( !exportClassSets.getExportClassSetsFromGraphAction().isEnabled() )
            exportClassSets.getExportClassSetsFromGraphAction().setEnabled(true);


        if ( !layoutPrintServices.getPrintGraphPageSetupDialogAction().isEnabled() )
            layoutPrintServices.getPrintGraphPageSetupDialogAction().setEnabled(true);

        if ( !layoutCustomizeNodeNamesDialog.getCustomizeNodeNamesAction().isEnabled() )
            layoutCustomizeNodeNamesDialog.getCustomizeNodeNamesAction().setEnabled(true);

        if ( !layoutShowClassesLegendsDialog.getShowClassesLegendsShowAction().isEnabled() )
            layoutShowClassesLegendsDialog.getShowClassesLegendsShowAction().setEnabled(true);

        if ( !findNameDialog.getFindNameDialogAction().isEnabled() )
            findNameDialog.getFindNameDialogAction().setEnabled(true);

        if ( !findClassDialog.getFindClassDialogAction().isEnabled() )
            findClassDialog.getFindClassDialogAction().setEnabled(true);

        if ( !findMultipleClassesDialog.getFindMultipleClassesDialogAction().isEnabled() )
            findMultipleClassesDialog.getFindMultipleClassesDialogAction().setEnabled(true);

        if ( !layoutGraphStatisticsDialog.getGraphStatisticsDialogAction().isEnabled() )
            layoutGraphStatisticsDialog.getGraphStatisticsDialogAction().setEnabled(true);


        if ( !graph.getGraphActions().getTranslateUpAction().isEnabled() )
            graph.getGraphActions().getTranslateUpAction().setEnabled(true);

        if ( !graph.getGraphActions().getTranslateDownAction().isEnabled() )
            graph.getGraphActions().getTranslateDownAction().setEnabled(true);

        if ( !graph.getGraphActions().getTranslateLeftAction().isEnabled() )
            graph.getGraphActions().getTranslateLeftAction().setEnabled(true);

        if ( !graph.getGraphActions().getTranslateRightAction().isEnabled() )
            graph.getGraphActions().getTranslateRightAction().setEnabled(true);

        if ( !graph.getGraphActions().getRotateUpAction().isEnabled() )
            graph.getGraphActions().getRotateUpAction().setEnabled(RENDERER_MODE_3D);

        if ( !graph.getGraphActions().getRotateDownAction().isEnabled() )
            graph.getGraphActions().getRotateDownAction().setEnabled(RENDERER_MODE_3D);

        if ( !graph.getGraphActions().getRotateLeftAction().isEnabled() )
            graph.getGraphActions().getRotateLeftAction().setEnabled(true);

        if ( !graph.getGraphActions().getRotateRightAction().isEnabled() )
            graph.getGraphActions().getRotateRightAction().setEnabled(true);

        if ( !graph.getGraphActions().getZoomInAction().isEnabled() )
            graph.getGraphActions().getZoomInAction().setEnabled(true);

        if ( !graph.getGraphActions().getZoomOutAction().isEnabled() )
            graph.getGraphActions().getZoomOutAction().setEnabled(true);

        if ( !graph.getGraphActions().getBurstLayoutIterationsAction().isEnabled() )
            graph.getGraphActions().getBurstLayoutIterationsAction().setEnabled(true);


        if ( !graph.getGraphRendererActions().getSelectAction().isEnabled() )
            graph.getGraphRendererActions().getSelectAction().setEnabled(true);

        if ( !graph.getGraphRendererActions().getTranslateAction().isEnabled() )
            graph.getGraphRendererActions().getTranslateAction().setEnabled(true);

        if ( !graph.getGraphRendererActions().getRotateAction().isEnabled() )
            graph.getGraphRendererActions().getRotateAction().setEnabled(true);

        if ( !graph.getGraphRendererActions().getZoomAction().isEnabled() )
            graph.getGraphRendererActions().getZoomAction().setEnabled(true);

        if ( !graph.getGraphRendererActions().getResetViewAction().isEnabled() )
            graph.getGraphRendererActions().getResetViewAction().setEnabled(true);

        if ( !graph.getGraphRendererActions().getRenderImageToFileAction().isEnabled() )
            graph.getGraphRendererActions().getRenderImageToFileAction().setEnabled(true);

        if ( !graph.getGraphRendererActions().getRenderHighResImageToFileAction().isEnabled() )
            graph.getGraphRendererActions().getRenderHighResImageToFileAction().setEnabled(true);


        if ( !graph.getSelectionManager().getSelectAllAction().isEnabled() )
            graph.getSelectionManager().getSelectAllAction().setEnabled(true);

        if ( !graph.getSelectionManager().getHideUnselectedAction().isEnabled() )
            graph.getSelectionManager().getHideUnselectedAction().setEnabled(true);

        if ( !graph.getSelectionManager().getShowAllNodeNamesAction().isEnabled() )
            graph.getSelectionManager().getShowAllNodeNamesAction().setEnabled(true);

        if ( !graph.getSelectionManager().getShowAllEdgeNamesAction().isEnabled() )
            graph.getSelectionManager().getShowAllEdgeNamesAction().setEnabled(true);

        if ( !graph.getSelectionManager().getHideAllNodeNamesAction().isEnabled() )
            graph.getSelectionManager().getHideAllNodeNamesAction().setEnabled(true);

        if ( !graph.getSelectionManager().getHideAllEdgeNamesAction().isEnabled() )
            graph.getSelectionManager().getHideAllEdgeNamesAction().setEnabled(true);

        if ( graph.getSelectionManager().getUnhideAllAction().isEnabled() )
            graph.getSelectionManager().getUnhideAllAction().setEnabled(false);

        graph.getSelectionManager().getGroupManager().getClassGroupAction().setEnabled( !nc.getIsGraphml() );
        graph.getSelectionManager().getCompleteGroupingAction().setEnabled( !nc.getIsGraphml() );

        layoutGraphPropertiesDialog.setEnabledGraphmlRelatedOptions( nc.getIsGraphml() );

        SPNSimulationDialog.getSignalingPetriNetSimulationDialogAction().setEnabled( nc.getIsPetriNet() );
        signalingPetriNetLoadSimulation.getSignalingPetriNetLoadSimulationAction().setEnabled( nc.getIsPetriNet() );
        exportD3.getExportD3Action().setEnabled(true);

        layoutAnimationControlDialog.setIsCorrelationProfileAnimationMode( DATA_TYPE.equals(DataTypes.CORRELATION) );
        layoutAnimationControlDialog.getAnimationControlDialogAction().setEnabled( DATA_TYPE.equals(DataTypes.CORRELATION) );
    }

    private void disableAllActions()
    {
        // note, save all & print actions are being enabled from inside the LayoutGraphPropertiesToolBar, LayoutGeneralToolBar, LayoutNavigationToolBar & LayoutMenuBar classes
        if ( layoutMenuBar.isEnabled() )
            layoutMenuBar.setEnabled(false);

        if ( layoutGraphPropertiesToolBar.isEnabled() )
            layoutGraphPropertiesToolBar.setEnabled(false);

        if ( layoutGeneralToolBar.isEnabled() )
            layoutGeneralToolBar.setEnabled(false);

        if ( layoutNavigationToolBar.isEnabled() )
            layoutNavigationToolBar.setEnabled(false);

        if ( layoutHomeToolBar.isEnabled() )
            layoutHomeToolBar.setEnabled(false);


        if ( saver.getSaveAction().isEnabled() )
            saver.getSaveAction().setEnabled(false);

        if ( importClassSetsParser.getImportClassSetsAction().isEnabled() )
            importClassSetsParser.getImportClassSetsAction().setEnabled(false);

        if ( exportClassSets.getExportClassSetsFromGraphAction().isEnabled() )
            exportClassSets.getExportClassSetsFromGraphAction().setEnabled(false);


        if ( layoutPrintServices.getPrintGraphPageSetupDialogAction().isEnabled() )
            layoutPrintServices.getPrintGraphPageSetupDialogAction().setEnabled(false);

        if ( layoutCustomizeNodeNamesDialog.getCustomizeNodeNamesAction().isEnabled() )
            layoutCustomizeNodeNamesDialog.getCustomizeNodeNamesAction().setEnabled(false);

        if ( layoutShowClassesLegendsDialog.getShowClassesLegendsShowAction().isEnabled() )
            layoutShowClassesLegendsDialog.getShowClassesLegendsShowAction().setEnabled(false);

        if ( findNameDialog.getFindNameDialogAction().isEnabled() )
            findNameDialog.getFindNameDialogAction().setEnabled(false);

        if ( findClassDialog.getFindClassDialogAction().isEnabled() )
            findClassDialog.getFindClassDialogAction().setEnabled(false);

        if ( findMultipleClassesDialog.getFindMultipleClassesDialogAction().isEnabled() )
            findMultipleClassesDialog.getFindMultipleClassesDialogAction().setEnabled(false);

        if ( SPNSimulationDialog.getSignalingPetriNetSimulationDialogAction().isEnabled() )
            SPNSimulationDialog.getSignalingPetriNetSimulationDialogAction().setEnabled(false);

        if ( signalingPetriNetLoadSimulation.getSignalingPetriNetLoadSimulationAction().isEnabled() )
            signalingPetriNetLoadSimulation.getSignalingPetriNetLoadSimulationAction().setEnabled(false);

        if ( layoutGraphStatisticsDialog.getGraphStatisticsDialogAction().isEnabled() )
            layoutGraphStatisticsDialog.getGraphStatisticsDialogAction().setEnabled(false);

        if ( layoutAnimationControlDialog.getAnimationControlDialogAction().isEnabled() )
            layoutAnimationControlDialog.getAnimationControlDialogAction().setEnabled(false);


        if ( graph.getGraphActions().getTranslateUpAction().isEnabled() )
            graph.getGraphActions().getTranslateUpAction().setEnabled(false);

        if ( graph.getGraphActions().getTranslateDownAction().isEnabled() )
            graph.getGraphActions().getTranslateDownAction().setEnabled(false);

        if ( graph.getGraphActions().getTranslateLeftAction().isEnabled() )
            graph.getGraphActions().getTranslateLeftAction().setEnabled(false);

        if ( graph.getGraphActions().getTranslateRightAction().isEnabled() )
            graph.getGraphActions().getTranslateRightAction().setEnabled(false);

        if ( graph.getGraphActions().getRotateUpAction().isEnabled() )
            graph.getGraphActions().getRotateUpAction().setEnabled(false);

        if ( graph.getGraphActions().getRotateDownAction().isEnabled() )
            graph.getGraphActions().getRotateDownAction().setEnabled(false);

        if ( graph.getGraphActions().getRotateLeftAction().isEnabled() )
            graph.getGraphActions().getRotateLeftAction().setEnabled(false);

        if ( graph.getGraphActions().getRotateRightAction().isEnabled() )
            graph.getGraphActions().getRotateRightAction().setEnabled(false);

        if ( graph.getGraphActions().getZoomInAction().isEnabled() )
            graph.getGraphActions().getZoomInAction().setEnabled(false);

        if ( graph.getGraphActions().getZoomOutAction().isEnabled() )
            graph.getGraphActions().getZoomOutAction().setEnabled(false);

        if ( graph.getGraphActions().getBurstLayoutIterationsAction().isEnabled() )
            graph.getGraphActions().getBurstLayoutIterationsAction().setEnabled(false);


        if ( graph.getGraphRendererActions().getSelectAction().isEnabled() )
            graph.getGraphRendererActions().getSelectAction().setEnabled(false);

        if ( graph.getGraphRendererActions().getTranslateAction().isEnabled() )
            graph.getGraphRendererActions().getTranslateAction().setEnabled(false);

        if ( graph.getGraphRendererActions().getRotateAction().isEnabled() )
            graph.getGraphRendererActions().getRotateAction().setEnabled(false);

        if ( graph.getGraphRendererActions().getZoomAction().isEnabled() )
            graph.getGraphRendererActions().getZoomAction().setEnabled(false);

        if ( graph.getGraphRendererActions().getResetViewAction().isEnabled() )
            graph.getGraphRendererActions().getResetViewAction().setEnabled(false);

        if ( graph.getGraphRendererActions().getRenderImageToFileAction().isEnabled() )
            graph.getGraphRendererActions().getRenderImageToFileAction().setEnabled(false);

        if ( graph.getGraphRendererActions().getRenderHighResImageToFileAction().isEnabled() )
            graph.getGraphRendererActions().getRenderHighResImageToFileAction().setEnabled(false);


        if ( graph.getSelectionManager().getSelectAllAction().isEnabled() )
            graph.getSelectionManager().getSelectAllAction().setEnabled(false);

        if ( graph.getSelectionManager().getHideUnselectedAction().isEnabled() )
            graph.getSelectionManager().getHideUnselectedAction().setEnabled(false);

        if ( graph.getSelectionManager().getShowAllNodeNamesAction().isEnabled() )
            graph.getSelectionManager().getShowAllNodeNamesAction().setEnabled(false);

        if ( graph.getSelectionManager().getShowAllEdgeNamesAction().isEnabled() )
            graph.getSelectionManager().getShowAllEdgeNamesAction().setEnabled(false);

        if ( graph.getSelectionManager().getHideAllNodeNamesAction().isEnabled() )
            graph.getSelectionManager().getHideAllNodeNamesAction().setEnabled(false);

        if ( graph.getSelectionManager().getHideAllEdgeNamesAction().isEnabled() )
            graph.getSelectionManager().getHideAllEdgeNamesAction().setEnabled(false);

        if ( graph.getSelectionManager().getGroupManager().getClassGroupAction().isEnabled() )
            graph.getSelectionManager().getGroupManager().getClassGroupAction().setEnabled(false);

        if ( graph.getSelectionManager().getCompleteGroupingAction().isEnabled() )
            graph.getSelectionManager().getCompleteGroupingAction().setEnabled(false);

        if ( graph.getSelectionManager().getUnGroupSelectedAction().isEnabled() )
            graph.getSelectionManager().getUnGroupSelectedAction().setEnabled(false);

        if ( graph.getSelectionManager().getUnGroupAllAction().isEnabled() )
            graph.getSelectionManager().getUnGroupAllAction().setEnabled(false);

        if ( graph.getSelectionManager().getUnhideAllAction().isEnabled() )
            graph.getSelectionManager().getUnhideAllAction().setEnabled(false);


        // disable all the events below as well
        if ( classViewerFrame.getClassViewerAction().isEnabled() )
            classViewerFrame.getClassViewerAction().setEnabled(false);

        if ( filterNodesByEdgesDialog.getFilterNodesByEdgesAction().isEnabled() )
            filterNodesByEdgesDialog.getFilterNodesByEdgesAction().setEnabled(false);

        if ( filterEdgesByWeightDialog.getFilterEdgesByWeightAction().isEnabled() )
            filterEdgesByWeightDialog.getFilterEdgesByWeightAction().setEnabled(false);

        if ( exportCorrelationNodesEdgesTable.getExportCorrelationNodesEdgesTableAction().isEnabled() )
            exportCorrelationNodesEdgesTable.getExportCorrelationNodesEdgesTableAction().setEnabled(false);

        if ( exportD3.getExportD3Action().isEnabled() )
            exportD3.getExportD3Action().setEnabled(false);

        layoutGraphPropertiesDialog.setEnabledProportionalEdgesSizeToWeight(false);
        layoutGraphPropertiesDialog.setEnabledNodeNameTextFieldAndSelectNodesTab(false, null, 0);
        layoutGraphPropertiesDialog.setEnabledGraphmlRelatedOptions(false);
    }

    /**
    *  Clears any previously loaded network.
    */
    private void clearNetworkAndGraph()
    {
        nc.clear();
        graph.clear();
    }

    /**
     * Handler for when user quits the application.
     * Presents dialog to confirm or save preferences if changed.
     * Removes graph and shuts down the application.
     */
    private void closeApplication()
    {
        //Workaround to ignore Exception thrown by JOGL on OS X - TODO remove when JOGL bug fixed
        Thread.setDefaultUncaughtExceptionHandler(null);

        boolean savePreferences = true;

        if ( layoutGraphPropertiesDialog.getHasNewPreferencesBeenApplied() && CONFIRM_PREFERENCES_SAVE.get())
        {
            int option = JOptionPane.showConfirmDialog(this,
                    "Do you want to save your preferences before exiting " + DISPLAY_PRODUCT_NAME + "?",
                    "Save Preferences & Exit " + DISPLAY_PRODUCT_NAME, JOptionPane.YES_NO_CANCEL_OPTION);

            if (option == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
            else if (option == JOptionPane.NO_OPTION)
            {
                savePreferences = false;
            }
        }
        else if (!nc.getVertices().isEmpty())
        {
            if (JOptionPane.showConfirmDialog(this,
                    "Do you really want to exit " + DISPLAY_PRODUCT_NAME + "?",
                    "Exit " + DISPLAY_PRODUCT_NAME, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }

        if (savePreferences)
        {
            LayoutPreferences.getLayoutPreferencesSingleton().savePreferences();
        }

        usageTracker.log("close");
        usageTracker.upload();

        if(revocationChecker != null)
        {
            revocationChecker.stop();
        }

        fileDragNDrop.remove(graph, true);
        this.dispose();
        System.exit(0);
    }

    public void setMaterialAntiAliasShading(boolean selected)
    {
        layoutGraphPropertiesDialog.setMaterialAntiAliasShading(selected);
    }

    public void setMaterialAnimatedPerlinShading(boolean selected)
    {
        layoutGraphPropertiesDialog.setMaterialAnimatedPerlinShading(selected);
    }

    public void setMaterialStateShading(boolean selected)
    {
        layoutGraphPropertiesDialog.setMaterialStateShading(selected);
    }

    public void setDepthFog(boolean selected)
    {
        layoutGraphPropertiesDialog.setDepthFog(selected);
    }

    public void setSphericalMapping(boolean selected)
    {
        layoutGraphPropertiesDialog.setSphericalMapping(selected);
    }

    public void setNodeSurfaceImageTexture(boolean selected)
    {
        layoutGraphPropertiesDialog.setNodeSurfaceImageTexture(selected);
    }

    public boolean isAllShadingSFXSValueEnabled()
    {
        return layoutGraphPropertiesDialog.isAllShadingSFXSValueEnabled();
    }

    public void setShaderLightingSFXValue(ShaderLightingSFXs.ShaderTypes shaderType)
    {
        layoutGraphPropertiesDialog.setShaderLightingSFXValue(shaderType);
    }

    public void setEnabledNodeNameTextFieldAndSelectNodesTab(boolean enabled, GraphNode node, int howManyNodesSelected)
    {
        layoutGraphPropertiesDialog.setEnabledNodeNameTextFieldAndSelectNodesTab(enabled, node, howManyNodesSelected);
    }

    public LayoutClassLegendTableModel getClassLegendTableModel()
    {
        return layoutShowClassesLegendsDialog.getTableModel();
    }

    public void toggleLegend(AbstractAction action)
    {
        layoutMenuBar.toggleLegend(action);
    }

    public LayoutProgressBarDialog getLayoutProgressBar()
    {
        return layoutProgressBarDialog;
    }

    public CorrelationData getCorrelationData()
    {
        return correlationData;
    }

    public ClassViewerFrame getClassViewerFrame()
    {
        return classViewerFrame;
    }

    public CoreSaver getCoreSaver()
    {
        return saver;
    }

    public LayoutClassSetsManager getLayoutClassSetsManager()
    {
        return layoutClassSetsManager;
    }

    public ImportClassSetsParser getImportClassSets()
    {
        return importClassSetsParser;
    }

    public ExportClassSets getExportClassSets()
    {
        return exportClassSets;
    }

    public ExportCorrelationNodesEdgesTable getExportCorrelationNodesEdgesTable()
    {
        return exportCorrelationNodesEdgesTable;
    }

    public NetworkRootContainer getNetworkRootContainer()
    {
        return nc;
    }

    public Graph getGraph()
    {
        return graph;
    }

    public void block()
    {
        blockAllAction.actionPerformed(blockEvent);
    }

    public void unblock()
    {
        blockAllAction.actionPerformed(unblockEvent);
    }

    public void blockExceptNavigationToolBar()
    {
        blockAllExceptNavigationToolBarAction.actionPerformed(blockEvent);
    }

    public void unblockExceptNavigationToolBar()
    {
        blockAllExceptNavigationToolBarAction.actionPerformed(unblockEvent);
    }

    public void setNodeLabel(String label)
    {
        nodeLabel.setText(" " + label + " ");
    }

    public void setStatusLabel(String label)
    {
        statusLabel.setText(" " + label + " ");
    }

    public String getFileNameLoaded()
    {
        return fileNameLoaded;
    }

    public String getFileNameAbsolutePathLoaded()
    {
        return fileNameAbsolutePath;
    }

    public void setEnabledAllToolBars(boolean enabled)
    {
        layoutGraphPropertiesToolBar.setEnabled(enabled);
        layoutGeneralToolBar.setEnabled(enabled);
        layoutNavigationToolBar.setEnabled(enabled);
        layoutHomeToolBar.setEnabled(enabled);
    }

    public void checkToShowNavigationWizardOnStartup()
    {
        if (SHOW_NAVIGATION_WIZARD_ON_STARTUP.get() && !navigationWizardShownOnce)
        {
            navigationWizardShownOnce = true;
            layoutNavigationWizardDialog.openDialogWindow();
        }
    }

    public void rebuildToolbars()
    {
        toolbarPanel.removeAll();

        toolbarPanel.add(layoutGeneralToolBar);

        if (SHOW_NAVIGATION_TOOLBAR.get())
        {
            toolbarPanel.add(layoutNavigationToolBar);
        }

        toolbarPanel.add(layoutHomeToolBar);

        if (SHOW_GRAPH_PROPERTIES_TOOLBAR.get())
        {
            toolbarPanel.add(layoutGraphPropertiesToolBar);
        }

        toolbarPanel.repaint();
        toolbarPanel.revalidate();
    }

    public void updateLayoutGraphStatisticsDialog()
    {
        layoutGraphStatisticsDialog.updateGraphStatistics();
    }

    public LayoutPrintServices getLayoutPrintServices()
    {
        return layoutPrintServices;
    }

    public LayoutClusterMCL getLayoutClusterMCL()
    {
        return layoutClusterMCL;
    }

    public SignalingPetriNetSimulationDialog getSignalingPetriNetSimulationDialog()
    {
        return SPNSimulationDialog;
    }

    public LayoutAnimationControlDialog getLayoutAnimationControlDialog()
    {
        return layoutAnimationControlDialog;
    }

    public SignalingPetriNetLoadSimulation getSignalingPetriNetLoadSimulation()
    {
        return signalingPetriNetLoadSimulation;
    }

    public LayoutGraphPropertiesDialog getLayoutGraphPropertiesDialog()
    {
        return layoutGraphPropertiesDialog;
    }

    public void layoutGeneralToolBarRunSPNButtonResetRolloverState()
    {
        layoutGeneralToolBar.runSPNButtonResetRolloverState();
    }

    public void layoutGeneralToolBarAnimationControlButtonResetRolloverState()
    {
        layoutGeneralToolBar.animationControlButtonResetRolloverState();
    }

    @Override
    public void switchRendererModeCallBack()
    {
        // remove previous mode events
        graph.removeAllEvents();

        RENDERER_MODE_3D = !RENDERER_MODE_3D; // switch renderer code here
        if (DEBUG_BUILD) println("\nNow switching to " + (RENDERER_MODE_3D ? "3D" : "2D") + " renderer mode.\n");
        graph.setReInitializeRendererMode(true);
        graph.updateAllDisplayLists();
        graph.refreshDisplay();

        // add next mode events
        graph.addAllEvents();

        setRenderModeSwitchUISettings();
        initMenuBarActions();
        layoutGeneralToolBar.set2D3DButton();
        setEnabledAllToolBars(true);
    }

    /**
     * Gets multiplier by which to resize nodes.
     * @return multiplier by which to resize nodes
     */
    public double getNodeResizeFactor()
    {
        return nodeResizeFactor;
    }

    /**
     * Sets multiplier by which to resize nodes.
     * @param nodeResizeFactor - multiplier by which to resize nodes
     */
    public void setNodeResizeFactor(double nodeResizeFactor)
    {
        this.nodeResizeFactor = nodeResizeFactor;
    }

}
