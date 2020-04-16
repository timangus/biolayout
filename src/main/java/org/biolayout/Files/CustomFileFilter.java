package org.biolayout.Files;

import java.io.*;
import static org.biolayout.Environment.GlobalEnvironment.*;

/**
*
* CustomFileFilter is the file filter class used to filter which files to show in the Open File dialog.
*
* @see org.biolayout.CoreUI.LayoutFrame
* @author Thanos Theo, 2008-2009
* @version 3.0.0.0
*/

public class CustomFileFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter
{

    /**
    *  Variable to be used for accepting or not directories for this file filter.
    */
    private boolean directoriesNotAccepted = false;

    /**
    *  Variable to be used for the supporting file extensions.
    */
    private String[] supportedExtensions = null;

    /**
    *  First constructor of the CustomFileFilter class.
    */
    public CustomFileFilter()
    {
        this(SupportedInputFileTypes.values(), false);
    }

    /**
    *  Second constructor of the CustomFileFilter class.
    */
    public CustomFileFilter(boolean directoriesNotAccepted)
    {
        this(SupportedInputFileTypes.values(), directoriesNotAccepted);
    }

    /**
    *  Third constructor of the CustomFileFilter class.
    */
    public CustomFileFilter(Object[] supportedExtensions)
    {
        this(supportedExtensions, false);
    }

    /**
    *  Fourth constructor of the CustomFileFilter class.
    */
    public CustomFileFilter(Object[] supportedExtensions, boolean directoriesNotAccepted)
    {
        this.directoriesNotAccepted = directoriesNotAccepted;
        this.supportedExtensions = new String[supportedExtensions.length];
        // populate the supportedExtensions string array
        for (int i = 0; i < supportedExtensions.length; i++)
            this.supportedExtensions[i] = supportedExtensions[i].toString().toLowerCase();
    }

    /**
    *  Accept all directories and all supported file types.
    */
    @Override
    public boolean accept(File file)
    {
        if (file != null)
        {
            if ( file.exists() )
            {
                if ( file.isDirectory() )
                    return !directoriesNotAccepted;

                String fileName = file.getAbsolutePath();
                String fileExtension = fileName.substring( fileName.lastIndexOf(".") + 1, fileName.length() ).toLowerCase(); // tolerance to upper/lowercase mix-ups
                for (String extension : supportedExtensions)
                    if ( fileExtension.equals(extension) && file.isFile() )
                        return true;
            }
        }

        return false;
    }

    /**
    *  The description of this filter.
    */
    @Override
    public String getDescription()
    {
        return "Supported file types: " + this.toString();
    }

    /**
    *  Shows detailed information about the supported file types.
    */
    @Override
    public String toString()
    {
        StringBuilder allSupportedFileTypes = new StringBuilder();
        for (String extension : supportedExtensions)
            allSupportedFileTypes.append("(*.").append(extension).append(") ");

        return allSupportedFileTypes.toString();
    }


}