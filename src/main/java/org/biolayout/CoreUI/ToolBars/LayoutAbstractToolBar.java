package org.biolayout.CoreUI.ToolBars;

import java.awt.*;
import javax.swing.*;
import org.biolayout.Textures.*;
import static org.biolayout.Environment.GlobalEnvironment.*;

/**
*
* LayoutAbstractToolBar is an abstract class used as a template for a toolbar.
* It also holds all relevant static final variables of relevant instantiating sub-classes.
*
* @author Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public abstract class LayoutAbstractToolBar extends JToolBar
{
    /**
    *  Serial version UID variable for the LayoutToolBar class.
    */
    public static final long serialVersionUID = 111222333444555694L;

    protected static enum GraphPropertiesToolBarButtons { GENERAL, LAYOUT, RENDERING, MCL, SIMULATION, SEARCH, NODES, EDGES, CLASSES }
    protected static enum GeneralToolBarButtons { GRAPH_OPEN, GRAPH_LAST_OPEN, GRAPH_SAVE, SNAPSHOT, GRAPH_STATISTICS, GRAPH_FIND, RUN_MCL, RUN_SPN, CLASS_VIEWER, ANIMATION_CONTROL, BURST_LAYOUT_ITERATIONS, _2D_3D }
    protected static enum NavigationToolBarButtons { UP, DOWN, LEFT, RIGHT, ROTATE_UP, ROTATE_DOWN, ROTATE_LEFT, ROTATE_RIGHT, ZOOM_IN, ZOOM_OUT, RESET_VIEW, NAVIGATION_WIZARD }
    protected static enum HomeToolBarButtons { HOME }
    protected static final int NUMBER_OF_GRAPH_PROPERTIES_TOOLBAR_BUTTONS = GraphPropertiesToolBarButtons.values().length;
    protected static final int NUMBER_OF_GENERAL_TOOLBAR_BUTTONS = GeneralToolBarButtons.values().length;
    protected static final int NUMBER_OF_NAVIGATION_TOOLBAR_BUTTONS = NavigationToolBarButtons.values().length;
    protected static final int NUMBER_OF_HOME_TOOLBAR_BUTTONS = HomeToolBarButtons.values().length;

    protected static final String BUTTON_PROPERTIES = " Properties";
    protected static final String BUTTON_HOVER = "Hover";
    protected static final String BUTTON_PRESSED = "Pressed";

    protected static final String GRAPH_PROPERTIES_TOOLBAR_TITLE = "Graph Properties Tool Bar";

    protected static final String GENERAL_TOOLBAR_TITLE = "General Tool Bar";
    protected static final String GENERAL_DIR_NAME = IMAGE_FILES_PATH + "GeneralToolBar/";
    protected static final String GENERAL_FILE_NAME = "GeneralToolBarData.txt";

    protected static final String NAVIGATION_TOOLBAR_TITLE = "Navigation Tool Bar";
    protected static final String NAVIGATION_DIR_NAME = IMAGE_FILES_PATH + "NavigationToolBar/";
    protected static final String NAVIGATION_FILE_NAME = "NavigationToolBarData.txt";

    protected static final String HOME_TOOLBAR_TITLE = "Home Tool Bar";
    protected static final String HOME_DIR_NAME = IMAGE_FILES_PATH + "HomeToolBar/";
    protected static final String HOME_FILE_NAME = "HomeToolBarData.txt";

    protected TexturesLoader texturesLoaderIcons = null;
    protected JButton[] allToolBarButtons = null;
    protected boolean constructorInitializationFinished = false;

    public LayoutAbstractToolBar(String name, int orientation)
    {
        super(name);

        // set tool tip to be heavyweight so as to be visible on top of the main OpenGL heavyweight canvas
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        this.setOrientation(orientation);
        this.setFloatable(true);

        constructorInitializationFinished = true;
    }

    protected abstract String getFirstButtonName();

    protected void setToolBarButtonImages(JButton button, String actionName)
    {
        button.setIcon( new ImageIcon( texturesLoaderIcons.getImage(actionName) ) );
        button.setDisabledIcon( new ImageIcon( texturesLoaderIcons.getDesaturatedImage(actionName) ) );
    }

    protected void setToolBarButtonAction(AbstractAction action, String actionName, int index)
    {
        setToolBarButtonAction(action, actionName, actionName, index);
    }

    protected void setToolBarButtonAction(AbstractAction action, String actionName, String actionToolTip, int index)
    {
        allToolBarButtons[index] = this.add(action);
        setToolBarButtonAction(allToolBarButtons[index], actionName, actionToolTip);
    }

    protected void setToolBarButtonAction(JButton button, String actionName)
    {
        setToolBarButtonAction(button, actionName, actionName);
    }

    protected void setToolBarButtonAction(JButton button, String actionName, String actionToolTip)
    {
        boolean textualButton = (texturesLoaderIcons == null) || !texturesLoaderIcons.isLoaded(actionName);

        if (!textualButton)
        {
            button.setText("");
            button.setToolTipText(actionToolTip);
            setToolBarButtonImages(button, actionName);
        }
        else
        {
            button.setText(actionName);
            button.setToolTipText(actionToolTip);
        }
    }
}