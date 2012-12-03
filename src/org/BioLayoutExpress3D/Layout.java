package org.BioLayoutExpress3D;

import java.io.*;
import java.util.*;
import javax.swing.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.DebugConsole.*;
import org.BioLayoutExpress3D.StaticLibraries.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
* 
* BioLayoutExpress3D - A tool for visualisation
* and analysis of biological networks
*
* Copyright (c) 2006-2012 Genome Research Ltd.
* Authors: Thanos Theo, Anton Enright, Leon Goldovsky, Ildefonso Cases, Markus Brosch, Stijn van Dongen, Michael Kargas, Benjamin Boyer and Tom Freeman
* Contact: support@biolayout.org
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*
* @author void main, command-line & logging parameters refactoring by Thanos Theo, 2008-2009-2010-2011
* @author BioLayoutExpress3D Generics refactoring for Java 1.5/1.6 by Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
* 
*/

public final class Layout 
{

    /** 
    *  Constructor of the Layout class.
    */        
    private Layout(String fileName, boolean onlineConnect, String repository, String dataSets, boolean hasChosenUseNativeCodeCommandLine, boolean useNativeCode, boolean hasChosenUseShadersProcessCommandLine, boolean useShadersProcess)
    {
        if (hasChosenUseNativeCodeCommandLine)
        {
            if (USE_NATIVE_CODE) // only if native code on pass selection, so as to avoid JVM crashes
                USE_NATIVE_CODE = useNativeCode;
        }

        if (hasChosenUseShadersProcessCommandLine)
            USE_SHADERS_PROCESS = useShadersProcess;
        
        if (!onlineConnect)
        {
            if ( fileName.isEmpty() )
                new LayoutFrame().initializeFrame(false);
            else
                new LayoutFrame().initializeFrame(true).loadDataSet( new File(fileName) );
        }
        else
        {
            new LayoutFrame().initializeFrame(true).loadOnlineDataSet(repository, dataSets);
        }
    }  
    
    /** 
    *  Reports all relevant machine settings.
    */       
    private static void reportMachineSettings()
    {
        int ONE_MB = 1 << 20; // (1024 * 1024);

        println("OS Name: " + System.getProperties().getProperty("os.name"));
        println("OS Architecture: " + System.getProperties().getProperty("os.arch"));
        println("Is JVM 64bit: " + IS_64BIT);
        println();
        println("Java Runtime Name: " + System.getProperties().getProperty("java.runtime.name"));
        println("Java Runtime Version: " + System.getProperties().getProperty("java.runtime.version"));
        println("Java Version: " + System.getProperties().getProperty("java.version"));
        println("JVM Version: " + System.getProperties().getProperty("java.vm.version"));
        println("JVM Name: " + System.getProperties().getProperty("java.vm.name"));
        println("JVM Vendor: " + System.getProperties().getProperty("java.vm.vendor"));
        println("Available processor cores: " + RUNTIME.availableProcessors());
        println("Max JVM memory usage: " + (RUNTIME.maxMemory() / ONE_MB) + " MB");
        println("Total JVM memory usage: " + (RUNTIME.totalMemory() / ONE_MB) + " MB");
        println();
        println("User Name: " + System.getProperty("user.name"));
        println("Java Library Path: " + System.getProperty("java.library.path"));
        println("Java IO TmpDir: " + System.getProperty("java.io.tmpdir"));
        println();
        for ( Map.Entry entry : System.getenv().entrySet() )
            println( entry.getKey() + ": " + entry.getValue() );
        println();
        System.getProperties().list(System.out);
        println();
    }    

    /**
    *  Reports memory usage.
    */
    private static void reportMemoryUsage()
    {
        int ONE_MB = 1 << 20; // (1024 * 1024);
        int ONE_KB = 1 << 10; // (1024);

        println(VERSION);
        println("---------------------------------------------------------------------");
        println("Max Memory Set to: " + (RUNTIME.maxMemory() / ONE_MB) + " MB");
        println("Total memory: " + (RUNTIME.totalMemory() / ONE_KB) + " KB");
        println("Free memory: " + (RUNTIME.freeMemory() /ONE_KB) + " KB");
    }

    /** 
    *  Static method that is used from void main to set the logging parameters.
    */         
    private static void setLoggingParameters(boolean consoleOutput, boolean fileOutput)
    {
        setIsMultiCoreOn(USE_MULTICORE_PROCESS);
        setIsLoggingOn(consoleOutput);
        
        if (fileOutput)
            setFileOutput( new FileOutput("AllConsoleOutputMessages.log", true) );            
    }
    
    /** 
    *  Static method that is used from void main to set the application's look and feel.
    *  Supports either the native or the new Nimbus L&F for Java 1.6 Update 10 or newer.
    */     
    private static void setLookAndFeel(boolean nimbusLAF)
    {
        try 
        {
            if (nimbusLAF)
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            else
                UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }
        catch (UnsupportedLookAndFeelException e1)
        {
            if (DEBUG_BUILD) println("UnsupportedLookAndFeelException with setLookAndFeel():\n" + e1.getMessage());
        }  
        catch (ClassNotFoundException e2)
        {
            if (DEBUG_BUILD) println("ClassNotFoundException with setLookAndFeel():\n" + e2.getMessage());
        } 
        catch (InstantiationException e3)
        {
            if (DEBUG_BUILD) println("InstantiationException with setLookAndFeel():\n" + e3.getMessage());
        } 
        catch (IllegalAccessException e4)
        {
            if (DEBUG_BUILD) println("IllegalAccessException with setLookAndFeel():\n" + e4.getMessage());
        }       
    }
    
    /** 
    *  Static method that is used from void main to show the command-line help page or
    *  is called if a wrong command-line option is inserted and shows again the help page.
    */    
    private static void usage(String msg) 
    {
        System.err.println();
        System.err.println(msg);
        System.err.println();
        System.err.println();
        System.err.println(" -parseFile fileName     : parseFile used for command-line file parsing");
        System.err.println();
        if (DEBUG_BUILD)
        {
            System.err.println(" -consoleOutput on|off   : consoleOutput used for console output (default off)");
            System.err.println();        
            System.err.println(" -fileOutput on|off      : fileOutput used for file logging (default off)");
            System.err.println();
            System.err.println(" -webStart on|off        : webStart used for the WebStart version (default off)");
            System.err.println();
        }
        System.err.println(" -loadFromRepository rep : loadFromRepository used to load datasets\n\t\t\t   from a web repository");
        System.err.println();        
        System.err.println(" -loadDataSets datasets  : loadDataSets used to load datasets\n\t\t\t   from a web repository");
        System.err.println();
        System.err.println(" -useNativeCode on|off   : use native code, default machine/OS autodetect");
        System.err.println();
        System.err.println(" -useShaders on|off      : use shaders support for the renderer (default on)");
        System.err.println();
        System.err.println(" -nimbusLAF on|off       : set the Nimbus look and feel (default off)");
        System.err.println();
        System.err.println(" -help                   : prints out this help page");
        System.err.println();
        
        System.exit(0);
    }    
    
    /** 
    *  Static method that is used from void main to initialize the needed JOGL libraries for OpenGL rendering.
    */     
    private static boolean initJOGLNativeLibraries()
    {
        for (int i = 0; i < NAME_OF_JOGL_NATIVE_LIBRARIES.length; i++)
            if( !LoadNativeLibrary.copyNativeLibrary(NAME_OF_JOGL_NATIVE_LIBRARIES[i], FILE_SIZES_OF_JOGL_NATIVE_LIBRARIES[i]) )
                return false;
        
        LoadNativeLibrary.setJavaLibraryPath();
        
        return true;
    }

    /**
    *  Static method that is used from void main to initialize the needed JOCL libraries for OpenCL GPU Computing.
    */
    private static boolean initJOCLNativeLibraries()
    {
        if ( !LoadNativeLibrary.loadNativeLibrary(NAME_OF_JOCL_NATIVE_LIBRARY, FILE_SIZES_OF_JOCL_NATIVE_LIBRARIES) )
            return false;

        LoadNativeLibrary.setJavaLibraryPath();

        return true;
    }

    /** 
    *  The void main entry point of the BioLayoutExpress3D framework.
    */     
    public static void main(String[] args) 
    {
        if (DEBUG_BUILD)
            if (args.length > 0)
                System.out.println("Command-line command detected:\n");
        
        String fileName = "";        
        boolean consoleOutput = false;
        boolean fileOutput = false;
        String repository = "";
        String dataSets = "";
        // use this complex way for parsing the USE_NATIVE_CODE/USE_SHADERS_PROCESS value so as to avoid a weird OpenGL flicker effect in Windows OSs
        // which only happens when the USE_NATIVE_CODE/USE_SHADERS_PROCESS variables are being accessed within void main!!!
        boolean hasChosenUseNativeCodeCommandLine = false;
        boolean useNativeCode = false;
        boolean hasChosenUseShadersProcessCommandLine = false;
        boolean useShadersProcess = false;
        boolean nimbusLAF = false;

        int i = 0;
        try
        {
            for (; i < args.length; i++) 
            {
                if ("-parseFile".equals(args[i])) 
                {
                    fileName = args[++i];
                    if (DEBUG_BUILD) System.out.println("Now starting with parsing fileName:\n\"" + fileName + "\"");
                }
                else if ("-consoleOutput".equals(args[i])) 
                {
                    consoleOutput = "on".equals( args[++i].toLowerCase() );
                    if (DEBUG_BUILD) System.out.println("Now starting with console output: " + Boolean.toString(consoleOutput));
                }                 
                else if ("-fileOutput".equals(args[i])) 
                {
                    fileOutput = "on".equals( args[++i].toLowerCase() );
                    if (DEBUG_BUILD) System.out.println("Now starting with file log output enabled: " + Boolean.toString(fileOutput));
                } 
                else if ("-webStart".equals(args[i])) 
                {
                    WEBSTART = "on".equals( args[++i].toLowerCase() );
                    if (DEBUG_BUILD) System.out.println("Now starting with WebStart option enabled: " + Boolean.toString(WEBSTART));
                }
                else if ("-loadFromRepository".equals(args[i])) 
                {
                    repository = args[++i];
                    if (DEBUG_BUILD) System.out.println("Now starting with a web repository:\n\"" + repository + "\"");
                }                        
                else if ("-loadDataSets".equals(args[i])) 
                {
                    dataSets = args[++i];
                    if (DEBUG_BUILD) System.out.println("Now starting with loading dataSets:\n\"" + dataSets + "\"");
                }
                else if ("-useNativeCode".equals(args[i]))
                {
                    hasChosenUseNativeCodeCommandLine = true;
                    useNativeCode = "on".equals( args[++i].toLowerCase() );
                    if (DEBUG_BUILD) System.out.println("Now starting with using native code: " + Boolean.toString(useNativeCode));
                }
                else if ("-useShaders".equals(args[i]))
                {
                    hasChosenUseShadersProcessCommandLine = true;
                    useShadersProcess = "on".equals( args[++i].toLowerCase() );
                    if (DEBUG_BUILD) System.out.println("Now starting with using shaders process: " + Boolean.toString(useShadersProcess));
                }
                else if ("-nimbusLAF".equals(args[i]))
                {
                    nimbusLAF = "on".equals( args[++i].toLowerCase() );
                    if (DEBUG_BUILD) System.out.println("Now starting with Nimbus L&F: " + Boolean.toString(nimbusLAF));
                }
                else if ("-help".equals(args[i])) 
                {
                    usage("Help page requested.");
                } 
                else 
                {
                    usage("Invalid argument: " + args[i]);
                }
            }

            if (DEBUG_BUILD) System.out.println();
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            usage("Argument at position " + i + " missing:\n" + e.getMessage());
        }        

        // to solve a  flicker bug with some hardware configurations and JOGL in Windows platforms
        // caution, it has to be enabled through command-line for the webstart version!
        if ( LoadNativeLibrary.isWin() )
            System.setProperty("sun.java2d.noddraw", "true");

        if (DEBUG_BUILD)
            setLoggingParameters(consoleOutput, fileOutput);
        setLookAndFeel(nimbusLAF);

        if ( initJOGLNativeLibraries() )
        {
            if (DEBUG_BUILD) println("\nJOGL Libraries working!\n");
        } 
        else 
        {
            if (DEBUG_BUILD) println("Error: JOGL Libraries not installed or found!\n");
            JOptionPane.showMessageDialog(null, "Error: JOGL Libraries not installed or found!", "JOGL error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }  

        if ( initJOCLNativeLibraries() )
        {
            OPENCL_GPU_COMPUTING_ENABLED = true;
            if (DEBUG_BUILD) println("JOCL Libraries working!\n");
        }
        else
        {
            if (DEBUG_BUILD) println("Error: JOCL Library not installed or found!\n");
        }

        if (DEBUG_BUILD)
        {
            reportMachineSettings();
            reportMemoryUsage();
        }       
        
        new Layout( ( !fileName.isEmpty() ) ? fileName : "", ( WEBSTART || !dataSets.isEmpty() ), repository, dataSets, hasChosenUseNativeCodeCommandLine, useNativeCode, hasChosenUseShadersProcessCommandLine, useShadersProcess );
    }

    
}