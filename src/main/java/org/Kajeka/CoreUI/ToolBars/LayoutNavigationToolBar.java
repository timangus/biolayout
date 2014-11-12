package org.Kajeka.CoreUI.ToolBars;

import javax.swing.*;
import org.Kajeka.Textures.*;
import static org.Kajeka.StaticLibraries.EnumUtils.*;
import static org.Kajeka.CoreUI.ToolBars.LayoutAbstractToolBar.NavigationToolBarButtons.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;

/**
*
* The LayoutNavigationToolBar is the toolbar responsible for Navigation control.
*
* @author Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public class LayoutNavigationToolBar extends LayoutAbstractToolBar
{

    public LayoutNavigationToolBar()
    {
        this(JToolBar.HORIZONTAL);
    }

    public LayoutNavigationToolBar(int orientation)
    {
        super(NAVIGATION_TOOLBAR_TITLE, orientation);

        texturesLoaderIcons = new TexturesLoader(NAVIGATION_DIR_NAME, NAVIGATION_FILE_NAME, false, false, true, false);
        allToolBarButtons = new JButton[NUMBER_OF_NAVIGATION_TOOLBAR_BUTTONS];
    }

    public LayoutNavigationToolBar(String name, int orientation) // to be used from ModelShapeNavigationToolBar subclass
    {
        super(name, orientation);
    }

    @Override
    protected final String getFirstButtonName()
    {
        return capitalizeFirstCharacter(UP);
    }

    public void setUpAction(AbstractAction action)
    {
        setToolBarButtonAction(action, capitalizeFirstCharacter(UP), UP.ordinal() );
    }

    public void setDownAction(AbstractAction action)
    {
        setToolBarButtonAction(action, capitalizeFirstCharacter(DOWN), DOWN.ordinal() );
    }

    public void setLeftAction(AbstractAction action)
    {
        setToolBarButtonAction(action, capitalizeFirstCharacter(NavigationToolBarButtons.LEFT), NavigationToolBarButtons.LEFT.ordinal() );
    }

    public void setRightAction(AbstractAction action)
    {
        setToolBarButtonAction(action, capitalizeFirstCharacter(NavigationToolBarButtons.RIGHT), NavigationToolBarButtons.RIGHT.ordinal() );
        addSeparator();
    }

    public void setRotateUpAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(ROTATE_UP), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(ROTATE_UP), ROTATE_UP.ordinal() );
    }

    public void setRotateDownAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(ROTATE_DOWN), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(ROTATE_DOWN), ROTATE_DOWN.ordinal() );
    }

    public void setRotateLeftAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(ROTATE_LEFT), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(ROTATE_LEFT), ROTATE_LEFT.ordinal() );
    }

    public void setRotateRightAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(ROTATE_RIGHT), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(ROTATE_RIGHT), ROTATE_RIGHT.ordinal() );
        addSeparator();
    }

    public void setZoomInAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(ZOOM_IN), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(ZOOM_IN), ZOOM_IN.ordinal() );
    }

    public void setZoomOutAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(ZOOM_OUT), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(ZOOM_OUT), ZOOM_OUT.ordinal() );
        addSeparator();
    }

    public void setResetViewAction(AbstractAction action)
    {
        setResetViewAction(action, true);
    }

    public void setResetViewAction(AbstractAction action, boolean useSeparator)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(RESET_VIEW), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(RESET_VIEW), RESET_VIEW.ordinal() );
        if (useSeparator) addSeparator();
    }

    public void setNavigationWizardAction(AbstractAction action)
    {
        setToolBarButtonAction(action, splitAndCapitalizeFirstCharacters(NAVIGATION_WIZARD), splitCapitalizeFirstCharactersAndAddWhiteSpaceBetweenNames(NAVIGATION_WIZARD), NAVIGATION_WIZARD.ordinal() );
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        boolean isDataSetLoaded = !DATA_TYPE.equals(DataTypes.NONE);
        for (int i = 0; i < NUMBER_OF_NAVIGATION_TOOLBAR_BUTTONS; i++)
        {
            if ( (i == 4) || (i == 5) ) // for the RotateUp & RotateDown buttons
                allToolBarButtons[i].setEnabled(enabled && isDataSetLoaded && RENDERER_MODE_3D);
            else if (i == 11) // for the Navigation Wizard button
                allToolBarButtons[i].setEnabled(enabled);
            else
                allToolBarButtons[i].setEnabled(enabled && isDataSetLoaded);
        }
    }

    @Override
    public boolean isEnabled()
    {
        for (int i = 0; i < NUMBER_OF_NAVIGATION_TOOLBAR_BUTTONS; i++)
        {
            if ( ( i == ROTATE_UP.ordinal()) || (i == ROTATE_DOWN.ordinal() ) ) // for the RotateUp & RotateDown buttons
            {
                if ( RENDERER_MODE_3D && constructorInitializationFinished && !allToolBarButtons[i].isEnabled() )
                    return false;
                else
                    continue;
            }
            else
            {
                if ( constructorInitializationFinished && !allToolBarButtons[i].isEnabled() )
                    return false;
            }
        }

        return true;
    }


}