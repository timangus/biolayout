package org.Kajeka.Environment;

import java.io.File;
import org.Kajeka.BuildConfig;
import org.Kajeka.Utils.Path;
import org.Kajeka.StaticLibraries.LoadNativeLibrary;
import static org.Kajeka.Environment.GlobalEnvironment.*;

/**
 *
 * Retrieve a folder that is writable by the current user in which we store settings and/or extract data
 *
 * @author Tim Angus
 */
public class DataFolder
{
    private static final String ROOT_FOLDER_NAME = "BioLayout";
    private static final String ROOT_FOLDER_NAME_UNIX = "." + ROOT_FOLDER_NAME;

    public static String get()
    {
        String folder;

        if (GlobalEnvironment.IS_WIN)
        {
            folder = WindowsAppDataFolder();
        }
        else if (GlobalEnvironment.IS_MAC)
        {
            folder = MacApplicationSupportFolder();
        }
        else
        {
            folder = UnixHomeDirectory();
        }

        if (folder == null || folder.isEmpty())
        {
            folder = LoadNativeLibrary.findCurrentProgramDirectory();
        }

        File f = new File(folder);
        if (!f.exists())
        {
            f.mkdirs();
        }

        return folder;
    }

    private static String WindowsAppDataFolder()
    {
        String APPDATA = System.getenv("APPDATA");

        if (!APPDATA.isEmpty())
        {
            return Path.combine(APPDATA, ROOT_FOLDER_NAME,
                    PRODUCT_NAME, MajorVersionNumber());
        }

        return null;
    }

    private static String MacApplicationSupportFolder()
    {
        String userHome = System.getProperty("user.home");

        if (!userHome.isEmpty())
        {
            return Path.combine(userHome, "Library", "Application Support",
                    ROOT_FOLDER_NAME, PRODUCT_NAME, MajorVersionNumber());
        }

        return null;
    }

    private static String UnixHomeDirectory()
    {
        String userHome = System.getProperty("user.home");

        if (!userHome.isEmpty())
        {
            return Path.combine(userHome, ROOT_FOLDER_NAME_UNIX,
                    PRODUCT_NAME, MajorVersionNumber());
        }

        return null;
    }

    private static String MajorVersionNumber()
    {
        if (BuildConfig.VERSION.contains("."))
        {
            String[] tokens = BuildConfig.VERSION.split("\\.");

            return tokens[0];
        }

        return BuildConfig.VERSION;
    }
}
