package org.biolayout.CoreUI.ToolBars;

import javax.swing.*;
import org.biolayout.Textures.*;
import static org.biolayout.StaticLibraries.EnumUtils.*;
import static org.biolayout.CoreUI.ToolBars.LayoutAbstractToolBar.HomeToolBarButtons.*;
import static org.biolayout.Environment.GlobalEnvironment.*;

/**
*
* The LayoutNavigationToolBar is the toolbar responsible for Navigation control.
*
* @author Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public class LayoutHomeToolBar extends LayoutAbstractToolBar
{
    public LayoutHomeToolBar()
    {
        this(JToolBar.HORIZONTAL);
    }

    public LayoutHomeToolBar(int orientation)
    {
        super(HOME_TOOLBAR_TITLE, orientation);

        texturesLoaderIcons = new TexturesLoader(HOME_DIR_NAME, HOME_FILE_NAME, false, false, true, false);
        allToolBarButtons = new JButton[NUMBER_OF_HOME_TOOLBAR_BUTTONS];

        this.setFloatable(false);
    }

    @Override
    protected final String getFirstButtonName()
    {
        return capitalizeFirstCharacter(HOME);
    }

    public void setHomeAction(AbstractAction action)
    {
        setToolBarButtonAction( action, capitalizeFirstCharacter(HOME), HOME.ordinal() );
    }
}