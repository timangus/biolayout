package org.BioLayoutExpress3D.StaticLibraries;

import java.io.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
*
* CopyMCLExecutable is a final class containing only static methods for copying the MCL executable (and depending library) depending on running OS.
*
* @see org.BioLayoutExpress3D.Clustering.MCL.LayoutClusterMCL
* @author Thanos Theo, 2008-2009
* @version 3.0.0.0
*/

public final class CopyMCLExecutable
{

    /**
    *  The file path of the MCL executable to be copied from.
    */
    private static final String EXTRACT_FROM_MCL_FILE_PATH = "/Resources/MCL/";

    /**
    *  The file path of the MCL executable to be copied to.
    */
    public static final String EXTRACT_TO_MCL_FILE_PATH = "MCL" + ( ( LoadNativeLibrary.is64bit() ) ? "64" : "32" ) + "/";

    /**
    *  The file path of the MCL executable to be copied from the OS-based specific path.
    */
    private static final String[] EXTRACT_FROM_MCL_OS_SPECIFIC_PATH = { "Win32/", "Win64/", "Linux32/", "Linux64/", "Mac/" };

    /**
    *  The MCL relevant files to copy.
    */
    private static final String[] MCL_FILES_TO_COPY = { "mcl", "cygwin1.dll" };

    /**
    *  Variable used for the different sizes of the MCL executable for OSs:
    *  Win32, Win64, Linux32, Linux64, MacOS X
    */
    private static final long[] SIZES_OF_MCL_SPECIFIC_EXECUTABLES = { 688194, 688194, 212438, 288775, 314264 };

    /**
    *  Variable used for the different sizes of the Cygwin library for OSs:
    *  Win32, Win64
    */
    private static final long[] SIZE_OF_CYGWIN_LIBRARIES = { 1872884, 1872884 };

    /**
    *  Copies the MCL executable (and depending library) depending on running OS.
    */
    public static boolean copyMCLExecutable()
    {
        try
        {
            boolean[] OSSpecificType = LoadNativeLibrary.checkRunningOSAndReturnOSSpecificType();
            String[] OSSpecificMCLExecutableNames = returnOSSpecificMCLExecutableName(OSSpecificType);
            String currentProgramDirectory = LoadNativeLibrary.findCurrentProgramDirectory();
            File MCLDir = new File(currentProgramDirectory + EXTRACT_TO_MCL_FILE_PATH);
            if ( !MCLDir.isDirectory() ) MCLDir.mkdir();

            int OSSPecificPathIndex = 0;
            for (boolean position : OSSpecificType)
            {
                if (position) break;
                OSSPecificPathIndex++;
            }

            File MCLExecutableFile = new File(currentProgramDirectory + EXTRACT_TO_MCL_FILE_PATH + OSSpecificMCLExecutableNames[0]);
            BufferedInputStream in = new BufferedInputStream( LoadNativeLibrary.class.getResourceAsStream(EXTRACT_FROM_MCL_FILE_PATH + EXTRACT_FROM_MCL_OS_SPECIFIC_PATH[OSSPecificPathIndex] + OSSpecificMCLExecutableNames[0]) );
            if ( !MCLExecutableFile.isFile() || ( !LoadNativeLibrary.checkLibraryFileSize(MCLExecutableFile.length(), SIZES_OF_MCL_SPECIFIC_EXECUTABLES) ) )
            {
                MCLExecutableFile.createNewFile();

                // necessary to set the unix-based executable permission, no use for windows platforms
                if (OSSpecificType[2] || OSSpecificType[3] || OSSpecificType[4])
                    MCLExecutableFile.setExecutable(true);

                IOUtils.streamAndClose( in, new BufferedOutputStream( new FileOutputStream(MCLExecutableFile) ) );

                if (DEBUG_BUILD)
                {
                    println();
                    println("MCL executable " + OSSpecificMCLExecutableNames[0] + " copied!");
                }
            }

            if (OSSpecificMCLExecutableNames.length > 1)
            {
                File CygwinLibraryFile = new File(currentProgramDirectory + EXTRACT_TO_MCL_FILE_PATH + OSSpecificMCLExecutableNames[1]);
                in = new BufferedInputStream( LoadNativeLibrary.class.getResourceAsStream(EXTRACT_FROM_MCL_FILE_PATH + EXTRACT_FROM_MCL_OS_SPECIFIC_PATH[OSSPecificPathIndex] + OSSpecificMCLExecutableNames[1]) );
                if ( !CygwinLibraryFile.isFile() || ( !LoadNativeLibrary.checkLibraryFileSize(CygwinLibraryFile.length(), SIZE_OF_CYGWIN_LIBRARIES) ) )
                {
                    CygwinLibraryFile.createNewFile();

                    IOUtils.streamAndClose( in, new BufferedOutputStream( new FileOutputStream(CygwinLibraryFile) ) );

                    if (DEBUG_BUILD)
                    {
                        println("Cygwin library " + OSSpecificMCLExecutableNames[1] + " copied!");
                        println();
                    }
                }
            }

            return true;
        }
        catch (FileNotFoundException ex)
        {
            if (DEBUG_BUILD) println("Problem with not finding the MCL executable (or Cygwin library) file:\n" + ex.getMessage());
        }
        catch (IOException ex)
        {
            if (DEBUG_BUILD) println("IO exception with:\n" + ex.getMessage());
        }
        catch (RuntimeException ex)
        {
            if (DEBUG_BUILD) println("Runtime exception with:\n" + ex.getMessage());
        }

        return false;
    }

    /**
    *  Returns the OS specific MCL executable (and depending library).
    */
    private static String[] returnOSSpecificMCLExecutableName(boolean[] OSSpecificType)
    {
        // if Windows OS type, put a .exe at end of executable and add the Cygwin library as well
        String[] OSSpecificMCLExecutableName = (OSSpecificType[0] || OSSpecificType[1]) ? new String[] { MCL_FILES_TO_COPY[0] + ".exe", MCL_FILES_TO_COPY[1] } : new String[] { MCL_FILES_TO_COPY[0] };

        if (DEBUG_BUILD) println("OS specific MCL executable name: " + OSSpecificMCLExecutableName[0]);

        return OSSpecificMCLExecutableName;
    }


}