package org.biolayout.CoreUI.ToolBars;

import javax.swing.*;
import org.biolayout.Textures.*;
import static org.biolayout.StaticLibraries.EnumUtils.*;
import static org.biolayout.CoreUI.ToolBars.LayoutAbstractToolBar.GraphPropertiesToolBarButtons.*;
import static org.biolayout.Environment.GlobalEnvironment.*;

/**
*
* The LayoutGraphPropertiesToolBar is the toolbar responsible for Graph Properties control.
*
* @author Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public final class LayoutGraphPropertiesToolBar extends LayoutAbstractToolBar
{

    public LayoutGraphPropertiesToolBar()
    {
        this(JToolBar.HORIZONTAL);
    }

    public LayoutGraphPropertiesToolBar(int orientation)
    {
        super(GRAPH_PROPERTIES_TOOLBAR_TITLE, orientation);

        allToolBarButtons = new JButton[NUMBER_OF_GRAPH_PROPERTIES_TOOLBAR_BUTTONS];
    }

    @Override
    protected final String getFirstButtonName()
    {
        return capitalizeFirstCharacter(GENERAL);
    }

    public void setGeneralAction(AbstractAction action)
    {
        String actionName = capitalizeFirstCharacter(GENERAL);
        setToolBarButtonAction( action, actionName, actionName + BUTTON_PROPERTIES, GENERAL.ordinal() );
        addSeparator();
    }

    public void setLayoutAction(AbstractAction action)
    {
        String actionName = capitalizeFirstCharacter(LAYOUT);
        setToolBarButtonAction( action, actionName, actionName + BUTTON_PROPERTIES, LAYOUT.ordinal() );
        addSeparator();
    }

    public void setRenderingAction(AbstractAction action)
    {
        String actionName = capitalizeFirstCharacter(RENDERING);
        setToolBarButtonAction( action, actionName, actionName + BUTTON_PROPERTIES, RENDERING.ordinal() );
        addSeparator();
    }

    public void setMCLAction(AbstractAction action)
    {
        String actionName = MCL.toString();
        setToolBarButtonAction( action, actionName, actionName + BUTTON_PROPERTIES, MCL.ordinal() );
        addSeparator();
    }

    public void setSimulationAction(AbstractAction action)
    {
        String actionName = capitalizeFirstCharacter(SIMULATION);
        setToolBarButtonAction( action, actionName, actionName + BUTTON_PROPERTIES, SIMULATION.ordinal() );
        addSeparator();
    }

    public void setSearchAction(AbstractAction action)
    {
        String actionName = capitalizeFirstCharacter(SEARCH);
        setToolBarButtonAction( action, actionName, actionName + BUTTON_PROPERTIES, SEARCH.ordinal() );
        addSeparator();
    }

    public void setNodesAction(AbstractAction action)
    {
        String actionName = capitalizeFirstCharacter(NODES);
        setToolBarButtonAction( action, actionName, actionName + BUTTON_PROPERTIES, NODES.ordinal() );
        addSeparator();
    }

    public void setEdgesAction(AbstractAction action)
    {
        String actionName = capitalizeFirstCharacter(EDGES);
        setToolBarButtonAction( action, actionName, actionName + BUTTON_PROPERTIES, EDGES.ordinal() );
        addSeparator();
    }

    public void setClassesAction(AbstractAction action)
    {
        String actionName = capitalizeFirstCharacter(CLASSES);
        setToolBarButtonAction( action, actionName, actionName + BUTTON_PROPERTIES, CLASSES.ordinal() );
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        for (int i = 0; i < NUMBER_OF_GRAPH_PROPERTIES_TOOLBAR_BUTTONS; i++)
            allToolBarButtons[i].setEnabled(enabled);
    }

    @Override
    public boolean isEnabled()
    {
        for (int i = 0; i < NUMBER_OF_GRAPH_PROPERTIES_TOOLBAR_BUTTONS; i++)
            if ( constructorInitializationFinished && !allToolBarButtons[i].isEnabled() )
                return false;

        return true;
    }


}