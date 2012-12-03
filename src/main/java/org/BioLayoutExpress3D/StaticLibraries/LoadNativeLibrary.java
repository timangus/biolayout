package org.BioLayoutExpress3D.StaticLibraries;

import java.io.*;
import java.lang.reflect.*;
import com.jogamp.gluegen.runtime.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/** 
*   
* LoadNativeLibrary is a final class containing only static methods for loading native libraries depending on running OS.
*
* @see org.BioLayoutExpress3D.Layout
* @author Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*/

public final class LoadNativeLibrary
{
 
    /** 
    *  The file path of the library file to be copied from.
    */     
    private static final String EXTRACT_FROM_LIBRARIES_FILE_PATH = "/Resources/Libraries/";
    
    /** 
    *  The file path of the library file to be copied to.
    */     
    private static final String EXTRACT_TO_LIBRARIES_FILE_PATH = "Libraries" + ( ( is64bit() ) ? "64" : "32" ) + "/";

    /**
    *  The file path of the library file to be copied from the OS-based specific path.
    */
    private static final String[] EXTRACT_FROM_LIBRARIES_OS_SPECIFIC_PATH = { "Win32/", "Win64/", "Linux32/", "Linux64/", "Mac/" };

    /**
    *  The number version of MacOSX Lion.
    */    
    private static final float MACOSX_LION_NUMBER_VERSION = 10.7f;
    
    /** 
    *  Unpacks the native library to a selected folder and loads it.
    *  Note, that loading order matters here, load those libraries that are required by other libraries first.
    *  Inter-dependent libraries may cause problems.
    */     
    public static boolean loadNativeLibrary(String libraryName)
    {
        return loadNativeLibrary(libraryName, null);
    }
    
    /** 
    *  Unpacks the native library to a selected folder and loads it.
    *  Note, that loading order matters here, load those libraries that are required by other libraries first.
    *  Inter-dependent libraries may cause problems.
    *  Overloaded version by checking the library's file size as well.
    */
    public static boolean loadNativeLibrary(String libraryName, long... libraryFileSizes)
    {
        try 
        {
            boolean[] OSSpecificType = checkRunningOSAndReturnOSSpecificType();
            String OSSpecificLibraryName = checkRunningOSAndReturnOSSpecificLibraryName(libraryName);
            String currentProgramDirectory = findCurrentProgramDirectory();
            File librariesDir = new File(currentProgramDirectory + EXTRACT_TO_LIBRARIES_FILE_PATH);
            if ( !librariesDir.isDirectory() ) librariesDir.mkdir();

            int OSSPecificPathIndex = 0;
            for (boolean position : OSSpecificType)
            {
                if (position) break;
                OSSPecificPathIndex++;
            }

            File libraryFile = new File(currentProgramDirectory + EXTRACT_TO_LIBRARIES_FILE_PATH + OSSpecificLibraryName);
            BufferedInputStream in = new BufferedInputStream( LoadNativeLibrary.class.getResourceAsStream(EXTRACT_FROM_LIBRARIES_FILE_PATH + EXTRACT_FROM_LIBRARIES_OS_SPECIFIC_PATH[OSSPecificPathIndex] + OSSpecificLibraryName) );
            if ( !libraryFile.isFile() || ( !checkLibraryFileSize(libraryFile.length(), libraryFileSizes) ) )
            {
                libraryFile.createNewFile();
                IOUtils.streamAndClose( in, new BufferedOutputStream( new FileOutputStream(libraryFile) ) );
            }
            
            String libraryAbsolutePath = libraryFile.getAbsolutePath();
            System.load(libraryAbsolutePath);
                        
            if (DEBUG_BUILD)
            {
                println("Native " + ( ( !is64bit() ) ? "32 bit" : "64 bit" ) + " library " + OSSpecificLibraryName + " loaded!");
                println();  
            }
            
            return true;
        } 
        catch (FileNotFoundException ex) 
        {
            if (DEBUG_BUILD) println("Problem with not finding the native library file:\n" + ex.getMessage());
        } 
        catch (UnsatisfiedLinkError ex)
        {
            if (DEBUG_BUILD) println("Problem with loading the native library:\n" + ex.getMessage());
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
    *  Unpacks the native library to a selected folder with a pre-selected name and loads it.
    *  Note, that loading order matters here, load those libraries that are required by other libraries first.
    *  Inter-dependent libraries may cause problems.
    */     
    public static boolean loadNativeLibraryWithFullName(String[] libraryNames)
    {
        return loadNativeLibraryWithFullName(libraryNames, false, false, null);
    }
    
    /** 
    *  Unpacks the native library to a selected folder with a pre-selected name and loads it.
    *  Note, that loading order matters here, load those libraries that are required by other libraries first.
    *  Inter-dependent libraries may cause problems.
    *  Overloaded version with optional selection of putting the native library to the current program directory or in Java's home directory.
    */     
    public static boolean loadNativeLibraryWithFullName(String[] libraryNames, boolean useCurrentProgramDirectory, boolean storeInJavaHomeDir)
    {
        return loadNativeLibraryWithFullName(libraryNames, useCurrentProgramDirectory, storeInJavaHomeDir, null);
    }

    /** 
    *  Unpacks the native library to a selected folder with a pre-selected name and loads it.
    *  Note, that loading order matters here, load those libraries that are required by other libraries first.
    *  Inter-dependent libraries may cause problems.
    *  Overloaded version by checking the library's file size as well.
    */     
    public static boolean loadNativeLibraryWithFullName(String[] libraryNames, long... libraryFileSizes)
    {
        return loadNativeLibraryWithFullName(libraryNames, false, false, libraryFileSizes);
    }    
    
    /** 
    *  Unpacks the native library to a selected folder with a pre-selected name and loads it.
    *  Note, that loading order matters here, load those libraries that are required by other libraries first.
    *  Inter-dependent libraries may cause problems.
    *  Overloaded version with optional selection of putting the native library to the current program directory or in Java's home directory & checking the library's file size as well.
    */
    public static boolean loadNativeLibraryWithFullName(String[] libraryNames, boolean useCurrentProgramDirectory, boolean storeInJavaHomeDir, long... libraryFileSizes)
    {
        try 
        {
            boolean[] OSSpecificType = checkRunningOSAndReturnOSSpecificType();
            String OSSpecificLibraryName = checkRunningOSAndReturnOSSpecificLibraryName("dummyLib");
            int libraryNameIndexToUse = 0;
            if ( OSSpecificLibraryName.endsWith(".dll") )
            {
                libraryNameIndexToUse = !( is64bit() && !isMac() ) ? 0 : 1;
            }
            else if ( OSSpecificLibraryName.endsWith(".so") )
            {
                libraryNameIndexToUse = !( is64bit() && !isMac() ) ? 2 : 3;
            }
            else if ( OSSpecificLibraryName.endsWith(".jnilib") )
                libraryNameIndexToUse = 4;
                        
            String currentProgramDirectory = "";
            File libraryFile = null;
            
            if (useCurrentProgramDirectory && !storeInJavaHomeDir)
            {
                currentProgramDirectory = findCurrentProgramDirectory();
                libraryFile = new File(currentProgramDirectory + libraryNames[libraryNameIndexToUse]);
            }
            else if (storeInJavaHomeDir)
            {
                currentProgramDirectory = findJavaHomeBinDirectory();
                
                File librariesDir = new File(currentProgramDirectory + "bin/");
                if ( !librariesDir.isDirectory() ) librariesDir.mkdir();                
                
                libraryFile = new File(currentProgramDirectory + "bin/" + libraryNames[libraryNameIndexToUse]);
            }
            else
            {
                currentProgramDirectory = findCurrentProgramDirectory();
                
                File librariesDir = new File(currentProgramDirectory + EXTRACT_TO_LIBRARIES_FILE_PATH);
                if ( !librariesDir.isDirectory() ) librariesDir.mkdir();
            
                libraryFile = new File(currentProgramDirectory + EXTRACT_TO_LIBRARIES_FILE_PATH + libraryNames[libraryNameIndexToUse]);
            }
            
            int OSSPecificPathIndex = 0;
            for (boolean position : OSSpecificType)
            {
                if (position) break;
                OSSPecificPathIndex++;
            }

            BufferedInputStream in = new BufferedInputStream( LoadNativeLibrary.class.getResourceAsStream(EXTRACT_FROM_LIBRARIES_FILE_PATH + EXTRACT_FROM_LIBRARIES_OS_SPECIFIC_PATH[OSSPecificPathIndex] + libraryNames[libraryNameIndexToUse]) );
            if ( !libraryFile.isFile() || ( !checkLibraryFileSize(libraryFile.length(), libraryFileSizes) ) )
            {
                libraryFile.createNewFile();
                IOUtils.streamAndClose( in, new BufferedOutputStream( new FileOutputStream(libraryFile) ) );
            }
            
            String libraryAbsolutePath = libraryFile.getAbsolutePath();
            System.load(libraryAbsolutePath);
                        
            if (DEBUG_BUILD)
            {
                println("Native " + ( ( !is64bit() ) ? "32 bit" : "64 bit" ) + " library " + libraryNames[libraryNameIndexToUse] + " loaded!");
                println();  
            }
            
            return true;
        } 
        catch (FileNotFoundException ex) 
        {
            if (DEBUG_BUILD) println("Problem with not finding the native library file:\n" + ex.getMessage());
        } 
        catch (UnsatisfiedLinkError ex)
        {
            if (DEBUG_BUILD) println("Problem with loading the native library:\n" + ex.getMessage());
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
    *  Unpacks the native library to a selected folder.
    */     
    public static boolean copyNativeLibrary(String libraryName)
    {
        return copyNativeLibrary(libraryName, null);
    }
    
    /** 
    *  Unpacks the native library to a selected folder.     
    *  Overloaded version by checking the library's file size as well.
    */     
    public static boolean copyNativeLibrary(String libraryName, long... libraryFileSizes)
    {
        try 
        {
            boolean[] OSSpecificType = checkRunningOSAndReturnOSSpecificType();
            String OSSpecificLibraryName = checkRunningOSAndReturnOSSpecificLibraryName(libraryName);
            String currentProgramDirectory = findCurrentProgramDirectory();
            File librariesDir = new File(currentProgramDirectory + EXTRACT_TO_LIBRARIES_FILE_PATH);
            if ( !librariesDir.isDirectory() ) librariesDir.mkdir();
            File libraryFile = new File(currentProgramDirectory + EXTRACT_TO_LIBRARIES_FILE_PATH + OSSpecificLibraryName);

            int OSSPecificPathIndex = 0;
            for (boolean position : OSSpecificType)
            {
                if (position) break;
                OSSPecificPathIndex++;
            }

            BufferedInputStream in = new BufferedInputStream( LoadNativeLibrary.class.getResourceAsStream(EXTRACT_FROM_LIBRARIES_FILE_PATH + EXTRACT_FROM_LIBRARIES_OS_SPECIFIC_PATH[OSSPecificPathIndex] + OSSpecificLibraryName) );
            if ( !libraryFile.isFile() || ( !checkLibraryFileSize(libraryFile.length(), libraryFileSizes) ) )
            {
                libraryFile.createNewFile();
                IOUtils.streamAndClose( in, new BufferedOutputStream( new FileOutputStream(libraryFile) ) );
                
                if (DEBUG_BUILD)
                {
                    println("Native " + ( ( !is64bit() ) ? "32 bit" : "64 bit" ) + " library " + OSSpecificLibraryName + " copied!");
                    println();                                
                }
            }
            
            return true;
        } 
        catch (FileNotFoundException ex) 
        {
            if (DEBUG_BUILD) println("Problem with not finding the native library file:\n" + ex.getMessage());
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
    *  Unpacks the native library to a selected folder with a pre-selected name.
    */     
    public static boolean copyNativeLibraryWithFullName(String[] libraryNames)
    {
        return copyNativeLibraryWithFullName(libraryNames, false, false, null);
    }    
    
    /** 
    *  Unpacks the native library to a selected folder with a pre-selected name. 
    *  Overloaded version with optional selection of putting the native library to the current program directory or in Java's home directory.
    */     
    public static boolean copyNativeLibraryWithFullName(String[] libraryNames, boolean useCurrentProgramDirectory, boolean storeInJavaHomeDir)
    {
        return copyNativeLibraryWithFullName(libraryNames, useCurrentProgramDirectory, storeInJavaHomeDir, null);
    }   
    
    /** 
    *  Unpacks the native library to a selected folder with a pre-selected name.     
    *  Overloaded version by checking the library's file size as well.
    */     
    public static boolean copyNativeLibraryWithFullName(String[] libraryNames, long... libraryFileSizes)
    {
        return copyNativeLibraryWithFullName(libraryNames, false, false, libraryFileSizes);
    }
    
    /** 
    *  Unpacks the native library to a selected folder with a pre-selected name.     
    *  Overloaded version with optional selection of putting the native library to the current program directory or in Java's home directory & checking the library's file size as well.
    */     
    public static boolean copyNativeLibraryWithFullName(String[] libraryNames, boolean useCurrentProgramDirectory, boolean storeInJavaHomeDir, long... libraryFileSizes)
    {
        try 
        {
            boolean[] OSSpecificType = checkRunningOSAndReturnOSSpecificType();
            String OSSpecificLibraryName = checkRunningOSAndReturnOSSpecificLibraryName("dummyLib");
            int libraryNameIndexToUse = 0;
            if ( OSSpecificLibraryName.endsWith(".dll") )
            {                
                libraryNameIndexToUse = !( is64bit() && !isMac() ) ? 0 : 1;
            }
            else if ( OSSpecificLibraryName.endsWith(".so") )
            {
                libraryNameIndexToUse = !( is64bit() && !isMac() ) ? 2 : 3;
            }
            else if ( OSSpecificLibraryName.endsWith(".jnilib") )
                libraryNameIndexToUse = 4;
                        
            String currentProgramDirectory = "";
            File libraryFile = null;
            
            if (useCurrentProgramDirectory && !storeInJavaHomeDir)
            {
                currentProgramDirectory = findCurrentProgramDirectory();
                libraryFile = new File(currentProgramDirectory + libraryNames[libraryNameIndexToUse]);
            }
            else if (storeInJavaHomeDir)
            {
                currentProgramDirectory = findJavaHomeBinDirectory();
                
                File librariesDir = new File(currentProgramDirectory + "bin/");
                if ( !librariesDir.isDirectory() ) librariesDir.mkdir();                
                
                libraryFile = new File(currentProgramDirectory + "bin/" + libraryNames[libraryNameIndexToUse]);
            }
            else
            {
                currentProgramDirectory = findCurrentProgramDirectory();
                
                File librariesDir = new File(currentProgramDirectory + EXTRACT_TO_LIBRARIES_FILE_PATH);
                if ( !librariesDir.isDirectory() ) librariesDir.mkdir();
            
                libraryFile = new File(currentProgramDirectory + EXTRACT_TO_LIBRARIES_FILE_PATH + libraryNames[libraryNameIndexToUse]);
            }
            
            int OSSPecificPathIndex = 0;
            for (boolean position : OSSpecificType)
            {
                if (position) break;
                OSSPecificPathIndex++;
            }

            BufferedInputStream in = new BufferedInputStream( LoadNativeLibrary.class.getResourceAsStream(EXTRACT_FROM_LIBRARIES_FILE_PATH +  EXTRACT_FROM_LIBRARIES_OS_SPECIFIC_PATH[OSSPecificPathIndex] + libraryNames[libraryNameIndexToUse]) );
            if ( !libraryFile.isFile() || ( !checkLibraryFileSize(libraryFile.length(), libraryFileSizes) ) )
            {
                libraryFile.createNewFile();
                IOUtils.streamAndClose( in, new BufferedOutputStream( new FileOutputStream(libraryFile) ) );
                
                if (DEBUG_BUILD)
                {
                    println("Native " + ( ( !is64bit() ) ? "32 bit" : "64 bit" ) + " library " + libraryNames[libraryNameIndexToUse] + " copied!");
                    println();                                
                }
            }
            
            return true;
        } 
        catch (FileNotFoundException ex) 
        {
            if (DEBUG_BUILD) println("Problem with not finding the native library file:\n" + ex.getMessage());
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
    *  Sets the java library path to be the current residing program directory.
    */     
    public static void setJavaLibraryPath()
    {
        Field field = null;
        try 
        {
            field = ClassLoader.class.getDeclaredField("sys_paths");
        } 
        catch (NoSuchFieldException ex) 
        {
            if (DEBUG_BUILD) println("NoSuchFieldException in setJavaLibraryPath():\n" + ex.getMessage());
        } 
        catch (SecurityException ex) 
        {
            if (DEBUG_BUILD) println("SecurityException in setJavaLibraryPath():\n" + ex.getMessage());
        }

        if ( (field != null) && !field.isAccessible() )
            field.setAccessible(true);

        if (field != null)
        {
            try
            {
                // Reset it to null so that whenever "System.loadLibrary()" is called, it will be reconstructed with the changed value
                field.set(ClassLoader.class, null);
            }
            catch (IllegalAccessException ex)
            {
                if (DEBUG_BUILD) println("IllegalAccessException in setJavaLibraryPath():\n" + ex.getMessage());
            }
        }

        System.setProperty("java.library.path", findJavaLibraryPath());        
    }        
     
    /** 
    *  Finds the java library path according to the current residing program directory.
    */     
    public static String findJavaLibraryPath()
    {
        String fileSeparator = System.getProperty("file.separator");            
        String javaLibraryPath = findCurrentProgramDirectory() + EXTRACT_TO_LIBRARIES_FILE_PATH;
        String[] allSeparatedPaths = javaLibraryPath.split("/");
        javaLibraryPath = "";
        for (String path : allSeparatedPaths)
            javaLibraryPath += (path + fileSeparator);
        
        return javaLibraryPath;
    } 
    
    /** 
    *  Finds the current residing program directory.
    *  All '%20' spaces have to be replaced with a whitespace.
    */     
    public static String findCurrentProgramDirectory()
    {
        if (WEBSTART)
        {
            return ("/" + System.getProperty("java.io.tmpdir") + "/").replace("\\", "/");
        }
        else
        {
            String JARPath = LoadNativeLibrary.class.getProtectionDomain().getCodeSource().getLocation().toString();
            int firstIndex = JARPath.indexOf('/');
            int lastIndex = JARPath.lastIndexOf('/');

            return "/" + JARPath.substring(firstIndex + 1, lastIndex + 1).replaceAll("%20", " ");
        }
    }      
    
    /** 
    *  Finds the Java Home binary program directory.  
    */     
    private static String findJavaHomeBinDirectory()
    {
        return ("/" + System.getProperty("java.home") + "/").replace("\\", "/");
    }    

    /**
    *  Checks the running OS and returns the OS specific type. Package access.
    */
    static boolean[] checkRunningOSAndReturnOSSpecificType()
    {
        String osName = System.getProperty("os.name");

        boolean isWin = osName.startsWith("Windows");
        boolean isXP = isWin && osName.contains("XP");
        boolean isVista = isWin && osName.contains("Vista");
        boolean isWin7 = isWin && osName.contains("7");
        boolean isLinux = osName.startsWith("Linux");
        boolean isMac = !isWin && ( osName.startsWith("Mac") || osName.startsWith("Darwin") );
        boolean is64 = is64bit();

        if (DEBUG_BUILD)
        {
            println();
            println("OS non-specific " + osName + " library file...");
        }

        return new boolean[] { (isWin || isXP || isVista || isWin7) && !is64, (isWin || isXP || isVista || isWin7) && is64, isLinux && !is64, isLinux && is64, isMac };
    }

    /** 
    *  Checks the running OS and returns the OS specific library name.
    */         
    private static String checkRunningOSAndReturnOSSpecificLibraryName(String libraryName)
    {
        String osName = System.getProperty("os.name");
        String OSSpecificLibraryName = "";        
        
        boolean isWin = osName.startsWith("Windows");
        boolean isXP = isWin && osName.contains("XP");
        boolean isVista = isWin && osName.contains("Vista");
        boolean isWin7 = isWin && osName.contains("7");
        boolean isLinux = osName.startsWith("Linux");        
        boolean isMac = !isWin && ( osName.startsWith("Mac") || osName.startsWith("Darwin") );
        
        if (DEBUG_BUILD)
        {
            println();
            println("OS non-specific " + osName + " native library...");        
        }
        
        if (isWin || isXP || isVista || isWin7)
            OSSpecificLibraryName = libraryName + ".dll";
        else if (isLinux)
            OSSpecificLibraryName = "lib" + libraryName + ".so";
        else if (isMac)
            OSSpecificLibraryName = "lib" + libraryName + ".jnilib";
        
        if (DEBUG_BUILD) println("OS specific library name: " + OSSpecificLibraryName);
        
        return OSSpecificLibraryName;        
    }

    /**
    *  Checks if the running OS is the Windows platform.
    */
    public static boolean isWin()
    {
        String osName = System.getProperty("os.name");
        boolean isWin = osName.startsWith("Windows");
        boolean isXP = isWin && osName.contains("XP");
        boolean isVista = isWin && osName.contains("Vista");
        boolean isWin7 = isWin && osName.contains("7");

        return (isWin || isXP || isVista || isWin7);
    }

    /**
    *  Checks if the running OS is the Windows XP platform.
    */
    public static boolean isWinXP()
    {
        String osName = System.getProperty("os.name");
        boolean isWin = osName.startsWith("Windows");
        boolean isXP = isWin && osName.contains("XP");

        return (isWin && isXP);
    }     
    
    /**
    *  Checks if the running OS is the Windows Vista platform.
    */
    public static boolean isWinVista()
    {
        String osName = System.getProperty("os.name");
        boolean isWin = osName.startsWith("Windows");
        boolean isVista = isWin && osName.contains("Vista");

        return (isWin && isVista);
    }     
    
    /**
    *  Checks if the running OS is the Windows 7 platform.
    */
    public static boolean isWin7()
    {
        String osName = System.getProperty("os.name");
        boolean isWin = osName.startsWith("Windows");
        boolean isWin7 = isWin && osName.contains("7");

        return (isWin && isWin7);
    }    
    
    /**
    *  Checks if the running OS is the Linux platform.
    */
    public static boolean isLinux()
    {
        return System.getProperty("os.name").startsWith("Linux");
    }

    /** 
    *  Checks if the running OS is the MacOSX platform.
    */     
    public static boolean isMac()
    {
        String osName = System.getProperty("os.name");
        
        return ( !isWin() && ( osName.startsWith("Mac") || osName.startsWith("Darwin") ) );
    }     
    
    /** 
    *  Checks if the running OS is the MacOSX platform Lion edition (Version 10.7) and above.
    */     
    public static boolean isMacLionAndAbove()
    {
        String osVersionString = System.getProperty("os.version");
        int firstIndexOfDot = osVersionString.indexOf(".");
        float osVersion = Float.parseFloat( osVersionString.substring(0, firstIndexOfDot + 2) );

        return ( isMac() && (osVersion >= MACOSX_LION_NUMBER_VERSION) );
    }

    /**
    *  Checks if the running OS is 64 bit.
    */
    public static boolean is64bit()
    {
        return System.getProperty("sun.arch.data.model") == "64";
    }

    /** 
    *  Checks if the actual library size is the one expected. Package access.
    */    
    static boolean checkLibraryFileSize(long actualLibraryFileSize, long... libraryFileSizes)
    {
        if (libraryFileSizes == null) return false;
        
        for (long libraryFileSize : libraryFileSizes)
            if (libraryFileSize == actualLibraryFileSize) 
                return true;        
        
        return false;
    }      
    
    
}
