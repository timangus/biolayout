package org.biolayout.Models.UIComponents.ToolBars;

import javax.swing.*;
import org.biolayout.CoreUI.ToolBars.*;
import org.biolayout.Textures.*;
import static org.biolayout.Environment.GlobalEnvironment.*;

/**
*
* The ModelShapeNavigationToolBar is the Model Shape toolbar responsible for the Model Shape renderer toolbar Navigation control.
*
* @author Thanos Theo, 2011
* @version 3.0.0.0
*
*/

public final class ModelShapeNavigationToolBar extends LayoutNavigationToolBar
{

    private static final float MODEL_SAHPE_NAVIGATION_TOOLBAR_IMAGE_ICON_RESIZE_RATIO = 0.58823f;

    public ModelShapeNavigationToolBar()
    {
        this(JToolBar.VERTICAL);
    }

    public ModelShapeNavigationToolBar(int orientation)
    {
        super("Model Shape " + NAVIGATION_TOOLBAR_TITLE, orientation);

        texturesLoaderIcons = new TexturesLoader(NAVIGATION_DIR_NAME, NAVIGATION_FILE_NAME, false, false, true, MODEL_SAHPE_NAVIGATION_TOOLBAR_IMAGE_ICON_RESIZE_RATIO, false);
        allToolBarButtons = new JButton[NUMBER_OF_NAVIGATION_TOOLBAR_BUTTONS - 1]; // the Navigation Wizard button is excluded from the model shape navigation toolbar
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        // the Navigation Wizard button is excluded from the model shape navigation toolbar
        for (int i = 0; i < NUMBER_OF_NAVIGATION_TOOLBAR_BUTTONS - 1; i++)
            allToolBarButtons[i].setEnabled(enabled);
    }

    @Override
    public boolean isEnabled()
    {
        // the Navigation Wizard button is excluded from the model shape navigation toolbar
        for (int i = 0; i < NUMBER_OF_NAVIGATION_TOOLBAR_BUTTONS - 1; i++)
            if ( constructorInitializationFinished && !allToolBarButtons[i].isEnabled() )
                return false;

        return true;
    }


}
