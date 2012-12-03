package org.BioLayoutExpress3D.GPUComputing.GL4;

import org.BioLayoutExpress3D.StaticLibraries.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/*
*
* GL4 is a final class containing only public static constants & native methods which extend the framework with OpenGL 4.0 support.
* In particular, the GL_ARB_tessellation_shader extension introduced in OpenGL 4.0 is supported. 
* GLEW is used on the native C/C++ side for this library extension.
*
* @author Thanos Theo, 2012
* @version 3.0.0.0
*
*/

public final class GL4
{
    
    /* ----------------------- GL_ARB_tessellation_shader related constants ---------------------- */
    public static final int GL_PATCHES  = 0xE;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_TESS_CONTROL_SHADER = 0x84F0;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_TESS_EVALUATION_SHADER = 0x84F1;
    public static final int GL_MAX_TESS_CONTROL_INPUT_COMPONENTS = 0x886C;
    public static final int GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS = 0x886D;
    public static final int GL_MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS = 0x8E1E;
    public static final int GL_MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS = 0x8E1F;
    public static final int GL_PATCH_VERTICES = 0x8E72;
    public static final int GL_PATCH_DEFAULT_INNER_LEVEL = 0x8E73;
    public static final int GL_PATCH_DEFAULT_OUTER_LEVEL = 0x8E74;
    public static final int GL_TESS_CONTROL_OUTPUT_VERTICES = 0x8E75;
    public static final int GL_TESS_GEN_MODE = 0x8E76;
    public static final int GL_TESS_GEN_SPACING = 0x8E77;
    public static final int GL_TESS_GEN_VERTEX_ORDER = 0x8E78;
    public static final int GL_TESS_GEN_POINT_MODE = 0x8E79;
    public static final int GL_ISOLINES = 0x8E7A;
    public static final int GL_FRACTIONAL_ODD = 0x8E7B;
    public static final int GL_FRACTIONAL_EVEN = 0x8E7C;
    public static final int GL_MAX_PATCH_VERTICES = 0x8E7D;
    public static final int GL_MAX_TESS_GEN_LEVEL = 0x8E7E;
    public static final int GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS = 0x8E7F;
    public static final int GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS = 0x8E80;
    public static final int GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS = 0x8E81;
    public static final int GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS = 0x8E82;
    public static final int GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS = 0x8E83;
    public static final int GL_MAX_TESS_PATCH_COMPONENTS = 0x8E84;
    public static final int GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS = 0x8E85;
    public static final int GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS = 0x8E86;
    public static final int GL_TESS_EVALUATION_SHADER = 0x8E87;
    public static final int GL_TESS_CONTROL_SHADER = 0x8E88;
    public static final int GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS = 0x8E89;
    public static final int GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS = 0x8E8A;   
    
    /** Entry point (through function pointer) to C language function: <br> <code> void {@native glPatchParameterfv}(GLenum  pname,  const GLfloat *values); </code>    */
    public static void glPatchParameterfv(int name, float[] values)
    {
        if (  ( (name == GL_PATCH_DEFAULT_OUTER_LEVEL) && (values.length == 4) )
           || ( (name == GL_PATCH_DEFAULT_INNER_LEVEL) && (values.length == 2) ) )
        {
            glPatchParameterfvNative(name, values);
        }
        else
        {
            if (DEBUG_BUILD) println("GL_PATCH_DEFAULT_OUTER_LEVEL or GL_PATCH_DEFAULT_INNER_LEVEL\nmust be specified with a float[4] or float[2] array respectively.");
        }
    }
    
    /** Entry point (through function pointer) to C language function: <br> <code> void {@native glPatchParameterfv}(GLenum  pname,  const GLfloat *values); </code>    */
    private static native void glPatchParameterfvNative(int name, float[] values);

    /** Entry point (through function pointer) to C language function: <br> <code> void {@native glPatchParameteri}(GLenum  pname,  GLint  value); </code>    */
    public static void glPatchParameteri(int name, int value)
    {
        glPatchParameteriNative(name, value);
    }

    /** Entry point (through function pointer) to C language function: <br> <code> void {@native glPatchParameteri}(GLenum  pname,  GLint  value); </code>    */
    private static native void glPatchParameteriNative(int name, int value);
    
    /**
    *  The glewInit() function call. Only once is needed to be made jus tafter loading the native library.
    */     
    private static native void glewInitNative();    
    
    /**
    *  Variable used for loading the native library only once (no use of re-loading the library).
    */
    private static boolean hasOnceLoadedNativeLibrary = false;
    
    /**
    *  Static initializer for the native library.
    */
    static
    {
        initNativeLibrary();
    }    
    
    /**
    *  Native library initializer to make sure to load all relevant native libraries (if being used).
    */
    private static void initNativeLibrary()
    {
        if (!hasOnceLoadedNativeLibrary && USE_NATIVE_CODE)
        {
            int index = NativeLibrariesTypes.GL4.ordinal();
            hasOnceLoadedNativeLibrary = LoadNativeLibrary.loadNativeLibrary(NAME_OF_BIOLAYOUT_EXPRESS_3D_NATIVE_LIBRARIES[index], FILE_SIZES_OF_BIOLAYOUT_EXPRESS_3D_NATIVE_LIBRARIES[index]);
            glewInitNative(); // warning: has to be initialized here or the JVM will crash with a machine-side error!
        }
    }
    
  
}