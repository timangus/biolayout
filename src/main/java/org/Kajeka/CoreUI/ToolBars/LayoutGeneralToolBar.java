package org.Kajeka.CoreUI.ToolBars;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.Kajeka.CoreUI.*;
import org.Kajeka.Textures.*;
import static org.Kajeka.StaticLibraries.EnumUtils.*;
import static org.Kajeka.CoreUI.ToolBars.LayoutAbstractToolBar.GeneralToolBarButtons.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;

/**
*
* The LayoutGeneralToolBar is the toolbar responsible for General control.
*
* @author Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public class LayoutGeneralToolBar extends LayoutAbstractToolBar
{

    public LayoutGeneralToolBar()
    {
        this(JToolBar.HORIZONTAL);
    }

    public LayoutGeneralToolBar(int orientation)
    {
        super(GENERAL_TOOLBAR_TITLE, orientation);

        texturesLoaderIcons = new TexturesLoader(GENERAL_DIR_NAME, GENERAL_FILE_NAME, false, false, true, false);
        allToolBarButtons = new JButton[NUMBER_OF_GENERAL_TOOLBAR_BUTTONS];
    }

    @Override
    protected final String getFirstButtonName()
    {
        return splitAndCapitalizeFirstCharacters(GRAPH_OPEN);
    }

    public void setGraphPropertiesAction(AbstractAction action)
    {
        String actionName = splitAndCapitalizeFirstCharacters(GRAPH_PROPERTIES);
        setToolBarButtonAction( action, actionName, actionName, GRAPH_PROPERTIES.ordinal() );
        addSeparator();
    }

    public void setGraphOpenAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(GRAPH_OPEN), splitCapitalizeFirstCharactersInvertOrderAndAddWhiteSpaceBetweenNames(GRAPH_OPEN), GRAPH_OPEN.ordinal() );
    }

    public void setGraphLastOpenAction(ArrayList<AbstractAction> actions, LayoutFrame layoutFrame)
    {
        allToolBarButtons[GRAPH_LAST_OPEN.ordinal()] = this.add( getGraphLastOpenAction(actions, layoutFrame) );
        setGraphLastOpenActionDetails();
    }

    public void refreshGraphLastOpenAction(ArrayList<AbstractAction> actions, LayoutFrame layoutFrame)
    {
        allToolBarButtons[GRAPH_LAST_OPEN.ordinal()].setAction( getGraphLastOpenAction(actions, layoutFrame) );
        setGraphLastOpenActionDetails();
    }

    private AbstractAction getGraphLastOpenAction(ArrayList<AbstractAction> actions, final LayoutFrame layoutFrame)
    {
        if ( !actions.isEmpty() )
            return actions.get(0);
        else
        {
            AbstractAction graphLastOpen = new AbstractAction("No Open Last Graph Available")
            {
                /**
                *  Serial version UID variable for the AbstractAction class.
                */
                public static final long serialVersionUID = 111222333444555712L;

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    allToolBarButtons[GRAPH_LAST_OPEN.ordinal()].getModel().setRollover(false);
                    JOptionPane.showMessageDialog(layoutFrame, "No Open Last Graph Available!", "Open Last Graph", JOptionPane.INFORMATION_MESSAGE);
                }
            };
            graphLastOpen.setEnabled(false);

            return graphLastOpen;
        }
    }

    private void setGraphLastOpenActionDetails()
    {
        setToolBarButtonAction(allToolBarButtons[GRAPH_LAST_OPEN.ordinal()], splitAndCapitalizeFirstCharacters(GRAPH_LAST_OPEN), splitCapitalizeFirstCharactersInvertOrderAndAddWhiteSpaceBetweenNames(GRAPH_LAST_OPEN) );
    }

    public void setGraphSaveAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(GRAPH_SAVE), splitCapitalizeFirstCharactersInvertOrderAndAddWhiteSpaceBetweenNames(GRAPH_SAVE), GRAPH_SAVE.ordinal() );
    }

    public void setSnapshotAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(SNAPSHOT), capitalizeFirstCharacter(SNAPSHOT), SNAPSHOT.ordinal() );

        addSeparator();
    }

    public void setGraphInformationAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(GRAPH_STATISTICS), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(GRAPH_STATISTICS), GRAPH_STATISTICS.ordinal() );
    }

    public void setGraphFindAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(GRAPH_FIND), "Find By Name", GRAPH_FIND.ordinal() );
    }

    public void setRunMCLAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharactersForAllButLastName(RUN_MCL), splitCapitalizeFirstCharactersForAllButLastNameAndAddWhiteSpaceBetweenNames(RUN_MCL), RUN_MCL.ordinal() );
    }

    public void setRunSPNAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharactersForAllButLastName(RUN_SPN), splitCapitalizeFirstCharactersForAllButLastNameAndAddWhiteSpaceBetweenNames(RUN_SPN), RUN_SPN.ordinal() );
    }

    public void setClassViewerAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(CLASS_VIEWER), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(CLASS_VIEWER), CLASS_VIEWER.ordinal() );
    }

    public void setAnimationControlAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(ANIMATION_CONTROL), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(ANIMATION_CONTROL), ANIMATION_CONTROL.ordinal() );
    }

    public void setBurstLayoutIterationsAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(GeneralToolBarButtons.BURST_LAYOUT_ITERATIONS), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(GeneralToolBarButtons.BURST_LAYOUT_ITERATIONS), GeneralToolBarButtons.BURST_LAYOUT_ITERATIONS.ordinal() );
    }

    public String _2D3DActionName()
    {
        String[] actionNames = _2D_3D.toString().substring(1).split(ENUM_REGEX + "+");
        return actionNames[RENDERER_MODE_3D ? 0 : 1];
    }

    public void set2D3DSwitchAction(AbstractAction action)
    {
        allToolBarButtons[_2D_3D.ordinal()] = this.add(action);
        setToolBarButtonAction(allToolBarButtons[_2D_3D.ordinal()], _2D3DActionName(), "2D / 3D Switch");
        set2D3DButton();

        addSeparator();
    }

    public void setHomeAction(AbstractAction action)
    {
        setToolBarButtonAction( action, capitalizeFirstCharacter(HOME), HOME.ordinal() );
    }

    public void set2D3DButton()
    {
        setToolBarButtonImages(allToolBarButtons[_2D_3D.ordinal()], _2D3DActionName());
    }

    public void runSPNButtonResetRolloverState()
    {
        allToolBarButtons[RUN_SPN.ordinal()].getModel().setRollover(false);
    }

    public void animationControlButtonResetRolloverState()
    {
        allToolBarButtons[ANIMATION_CONTROL.ordinal()].getModel().setRollover(false);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        boolean isDataSetLoaded = !DATA_TYPE.equals(DataTypes.NONE);
        for (int i = 0; i < NUMBER_OF_GENERAL_TOOLBAR_BUTTONS; i++)
        {
            if ( ( i >= GRAPH_SAVE.ordinal() ) && ( i <= GeneralToolBarButtons.BURST_LAYOUT_ITERATIONS.ordinal() ) ) // for the Graph Save, Graph Information, Graph Find, Run MCL, Run SPN, Class Viewer, Burst Layout Iterations buttons
                allToolBarButtons[i].setEnabled(enabled && isDataSetLoaded);
            else // for the Graph Open, Graph Last Open, 2D/3D & Home buttons
                allToolBarButtons[i].setEnabled(enabled);
        }
    }

    @Override
    public boolean isEnabled()
    {
        for (int i = 0; i < NUMBER_OF_GENERAL_TOOLBAR_BUTTONS; i++)
            if ( constructorInitializationFinished && !allToolBarButtons[i].isEnabled() )
                return false;

        return true;
    }


}