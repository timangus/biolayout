package org.BioLayoutExpress3D.StaticLibraries;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
*
* Utils is a final class containing only static utility methods.
*
* @author Thanos Theo, 2008-2009
* @version 3.0.0.0
*/

public final class Utils
{

    /**
    *  Constant variable used from the Utils final class.
    */
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    /**
    *  Returns the compile timestamp by checking the MANIFEST.MF file timestamp.
    */
    public static String compileTimeStamp()
    {
        ZipInputStream zipInputStream = null;
        String compileTimeStamp = "";

        try
        {
            // make sure to navigate to the MANIFEST.MF file to get its timestamp
            URL url = Utils.class.getProtectionDomain().getCodeSource().getLocation();
            zipInputStream = new ZipInputStream( url.openStream() );
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while ( zipEntry != null && !zipEntry.getName().endsWith("MANIFEST.MF") )
                zipEntry = zipInputStream.getNextEntry();

            if (zipEntry != null)
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis( zipEntry.getTime() );
                compileTimeStamp = calendar.getTime().toString();
            }
        }
        catch (IOException ioExc)
        {
            if (DEBUG_BUILD) println("Problem in compileTimeStamp():\n" + ioExc.getMessage());
        }
        finally
        {
            try
            {
                if (zipInputStream != null) zipInputStream.close();
            }
            catch (IOException ioExc)
            {
                if (DEBUG_BUILD) println("Problem with 'finally' clause in compileTimeStamp():\n" + ioExc.getMessage());
            }
        }

        return compileTimeStamp;
    }

    /**
    *  Prints all methods of a given class using reflection.
    */
    public static void printMethods(Class cl)
    {
        Method[] allMethods = cl.getMethods();
        for (Method method : allMethods)
        {
            println("Name: " + method.getName());
            println("   Return Type: " + method.getReturnType().getName());
            print("   Parameter Types:");
            Class[] parameterTypes = method.getParameterTypes();
            for (Class parameterType : parameterTypes)
                print(" " + parameterType.getName());
            println();
        }
    }

    /**
    *  Prints all fields of a given class using reflection.
    */
    public static void printFields(Class cl)
    {
        Field[] fields = cl.getDeclaredFields();
        for (Field field : fields)
        {
            print( Modifier.toString( field.getModifiers() ) );
            println(" " + field.getType().getName() + " " + field.getName() + ";");
        }
    }

    /**
    *  Merges any count of Array objects (generic arrays method).
    *
    *  @param arrays many arrays
    *  @return merged array
    */
    @SuppressWarnings("unchecked") // suppresses the "unchecked" warning inside the method's (T[]) cast
    public static <T> T[] mergeArrays(T[]... arrays)
    {
        int count = 0;
        for (T[] array : arrays)
            count += array.length;

        /*
           create new array

           needed unchecked convertion, since for compatibility reasons with Generics/Templates,
           the newIstance() method is defined as:

           public static Object newInstance(Class<?> componentType, int length)

           instead of:

           public static<T> T[] newInstance(Class<T> componentType, int length
         */
        T[] mergedArray = (T[])Array.newInstance(arrays[0][0].getClass(), count);
        int start = 0;
        for (T[] array : arrays)
        {
            System.arraycopy(array, 0, mergedArray, start, array.length);
            start += array.length;
        }

        return mergedArray;
    }

    /**
    *  Checks if two arrays are equal.
    */
    public static <T> boolean areArraysEqual(T[] array1, T[] array2)
    {
        if (array1 == null || array2 == null)
            return false;
        else if (array1.length != array2.length)
            return false;
        else
        {
            for (int i = 0; i < array1.length; i++)
                if ( !array1[i].equals(array2[i]) )
                    return false;

            return true;
        }
    }

    /**
    *  Creates a String representing the hex format of a byte[] array.
    */
    public static String byteArrayHexFormat(byte[] data)
    {
        StringBuilder result = new StringBuilder();
        int n = 0;

        for (byte b : data)
        {
            if ( (n % 16) == 0 )
                result.append( String.format("%05X: ", n) );
            result.append( String.format("%02X: ", b) );
            n++;
            if ( (n % 16) == 0 ) result.append("\n");
        }

        result.append("\n");

        return result.toString();
    }

    /**
    *  Creates a String representing the Collection data in a more readable line-by-line format than the standard toString() method in Collection classes.
    */
    public static String collectionFormat(Collection<?> collection)
    {
        if ( collection.isEmpty() ) return "[]";

        StringBuilder result = new StringBuilder("[");

        for (Object elem : collection)
        {
            if (collection.size() != 1)
                result.append("\n");
            result.append(elem);
        }

        if (collection.size() != 1)
            result.append("\n");
        result.append("]");

        return result.toString();
    }

    /**
    *  Creates a String representing the T object data in a more readable line-by-line format than the standard toString() method.
    */
    public static <T> String objectArrayFormat(T... t)
    {
        return collectionFormat( Arrays.asList(t) );
    }

    /**
    *  Creates a String representing the Double number with a set number of fraction digits.
    */
    public static String numberFormatting(Double result, int numberOfDigits)
    {
        NUMBER_FORMAT.setMaximumFractionDigits(numberOfDigits);

        return NUMBER_FORMAT.format(result);
    }

    /**
    *  Creates a Hex String representing the Color object.
    */
    public static String getHexColor(Color color)
    {
        String s = Integer.toHexString(color.getRGB() & 0xffffff);
        if (s.length() < 6)
        {
            // pad on left with zeros
            s = "000000".substring( 0, 6 - s.length() ) + s;
        }

        return '#' + s;
    }

    /**
    *  Converts "one_two_three" to "One Two Three"
    */
    public static String titleCaseOf(String s, String delimiters, char delimiter)
    {
        StringBuilder out = new StringBuilder();
        boolean nextTitleCase = true;

        s = s.toLowerCase();

        for (char c : s.toCharArray())
        {
            if (delimiters.indexOf(c) >= 0)
            {
                nextTitleCase = true;
                c = delimiter;
            }
            else if (nextTitleCase)
            {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            out.append(c);
        }

        return out.toString();
    }

    public static String titleCaseOf(String s)
    {
        return titleCaseOf(s, " _", ' ');
    }
}