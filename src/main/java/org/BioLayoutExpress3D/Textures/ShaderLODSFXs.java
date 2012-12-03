package org.BioLayoutExpress3D.Textures;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;
import org.BioLayoutExpress3D.StaticLibraries.*;
import static org.BioLayoutExpress3D.Graph.Graph.*;
import static org.BioLayoutExpress3D.Environment.AnimationEnvironment.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;

/**
*
*  Various GLSL shader LOD operations for nodes usage.
*  This class is responsible for producing LOD effects using the GLSL 400 specification (OpenGL 4.0 only).
*
*
* @author Thanos Theo, 2012
* @version 3.0.0.0
*
*/

public class ShaderLODSFXs extends ShaderLightingSFXs
{

    /**
    *  Available shader types.
    */
    public static enum ShaderTypes { POINT_SPHERES_LOD, POINT_NORMAL_TRIANGLES_LOD, PHONG_TESSELLATION_LOD }

    /**
    *  Available number of lighting shaders.
    */
    public static final int NUMBER_OF_AVAILABLE_LOD_SHADERS = ShaderTypes.values().length;

    /**
    *  Tesselation Control shader storage.
    *  3 sets of shader files per shader program.
    */
    private int[][] TESSELATION_CONTROL_SHADERS = null;

    /**
    *  Tesselation Evaluation shader storage.
    *  3 sets of shader files per shader program.
    */
    private int[][] TESSELATION_EVALUATION_SHADERS = null; 
    
    /**
    *  Shader program tessellation name.
    */
    private static final String SHADER_PROGRAM_TESSELLATION_NAME = "Tessellation";

    /**
    *  Shader program tessellation storage.
    */
    private final int[] SHADER_PROGRAM_TESSELLATIONS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];    
    
    /**
    *  The first constructor of the ShaderLODSFXs class.
    */
    public ShaderLODSFXs(GL2 gl)
    {
        this(gl, 0.01f, false, false);
    }

    /**
    *  The second constructor of the ShaderLODSFXs class.
    */
    public ShaderLODSFXs(GL2 gl, float timerUpdateStep)
    {
        this(gl, timerUpdateStep, false, false);
    }    
    
    /**
    *  The third constructor of the ShaderLODSFXs class.
    */
    public ShaderLODSFXs(GL2 gl, boolean useGeometryShaders)
    {
        this(gl, 0.01f, useGeometryShaders, false);
    }       
    
    /**
    *  The fourth constructor of the ShaderLODSFXs class.
    */
    public ShaderLODSFXs(GL2 gl, boolean useGeometryShaders, boolean applyNormalsGeometry)
    {
        this(gl, 0.01f, useGeometryShaders, applyNormalsGeometry);
    }     
    
    /**
    *  The fifth constructor of the ShaderLODSFXs class.
    */
    public ShaderLODSFXs(GL2 gl, float timerUpdateStep, boolean useGeometryShaders, boolean applyNormalsGeometry)
    {
        super(gl, timerUpdateStep, useGeometryShaders, applyNormalsGeometry, false);

        initAllVariables();
        loadAndCompileAllShaderPrograms(gl);
    }
    
    /**
    *  Initializes all relevant variables.
    */    
    private void initAllVariables()
    {
        SHADER_FILES_DIRECTORY_1 = "LOD";
        VERTEX_SHADERS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS][3];
        TESSELATION_CONTROL_SHADERS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS][3];
        TESSELATION_EVALUATION_SHADERS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS][3];
        GEOMETRY_SHADERS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS][3];    
        FRAGMENT_SHADERS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS][3];
        SHADER_PROGRAMS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];    
        SHADER_PROGRAM_2D_TEXTURES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];        
        SHADER_PROGRAM_ANIMATION_2D_TEXTURES = new int[NUMBER_OF_AVAILABLE_SHADERS];
        SHADER_PROGRAM_FOGS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_TEXTURINGS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_SPHERICAL_MAPPINGS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_EMBOSS_NODE_TEXTURES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_PXS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_PYS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_TIMERS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANTIALIASES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_STATES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_OLD_LCD_STYLE_TRANSPARENCIES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_SHRINK_TRIANGLES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_SOLID_WIREFRAMES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];      
        SHADER_PROGRAM_NORMALS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];  
        SHADER_PROGRAM_ANIMATION_GPU_COMPUTING_MODES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_NODE_VALUES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_PROCESS_NEXT_NODE_VALUES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_NEXT_NODE_VALUES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_FRAME_COUNTS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_FRAMERATE_PER_SECOND_FOR_ANIMATIONS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_FLUID_LINEAR_TRANSITIONS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_FLUID_POLYNOMIAL_TRANSITIONS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_TICKS_PER_SECONDS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_MAX_NODE_SIZES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_RESULTS_MAX_VALUES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_RESULTS_REAL_MAX_VALUES = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITIONS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_USE_REAL_MAX_VALUE_FOR_COLOR_TRANSITIONS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_MAX_SPECTRUM_COLORS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];
        SHADER_PROGRAM_ANIMATION_USE_IMAGE_AS_SPECTRUMS = new int[NUMBER_OF_AVAILABLE_LOD_SHADERS];        
    }    

    /**
    *  Loads and compiles all the shader programs.
    */
    private void loadAndCompileAllShaderPrograms(GL2 gl)
    {        
        int NUMBER_OF_OUTPUT_GS_VERTICES = GL_MAX_GEOMETRY_OUTPUT_VERTICES_EXT_INTEGER;
        if (useGeometryShaders)
            NUMBER_OF_OUTPUT_GS_VERTICES = (applyNormalsGeometry) ? 4 * 3 : 3; // up to 32 output vertices, for sphere subdivision for up to level 2, then even NVidia Fermi-based cards fail to viz properly
        loadShadersPairs = new boolean[VERTEX_SHADERS[0].length][5];
        for (int i = 0 ; i < VERTEX_SHADERS[0].length; i++)
        {
                loadShadersPairs[i][0] = true;
                loadShadersPairs[i][1] = (i == 0) || (i == 1); // load Geometry Shaders for core shaders & Effects library but not for GPU COmputing Animation library
                loadShadersPairs[i][2] = (i == 0) || (i == 1); // load Geometry Shaders for core shaders & Effects library but not for GPU COmputing Animation library
                loadShadersPairs[i][3] = (i == 0) && useGeometryShaders; // load Geometry Shaders for core shaders  but not for Effects library (re-implemented for TS case in their GS parts) & GPU Computing Animation library
                loadShadersPairs[i][4] = true;
        }
            
        String versionString =  MINIMUM_GLSL_VERSION_FOR_400_SHADERS + " " + GLSL_LANGUAGE_MODE;
        String GLSLPreprocessorCommands = "#version " + versionString + "\n" +
                                          "#define GPU_SHADER_FP64_COMPATIBILITY_CONDITION "      + ( (USE_GL_ARB_GPU_SHADER_FP64 && GL_IS_NVIDIA) ? 1 : 0 )                                             + "\n" +
                                          "#define GPU_SHADER4_COMPATIBILITY_CONDITION "          + ( USE_GL_EXT_GPU_SHADER4 ? 1 : 0 )                                                                   + "\n" + 
                                          "#define GPU_GEOMETRY_SHADER4_COMPATIBILITY_CONDITION " + ( (USE_GL_ARB_GEOMETRY_SHADER4 && useGeometryShaders) ? 1 : 0 )                                      + "\n" +
                                          "#define APPLY_NORMALS_GEOMETRY_CONDITION "             + ( (USE_GL_ARB_GEOMETRY_SHADER4 && useGeometryShaders && applyNormalsGeometry) ? 1 : 0 )              + "\n" +
                                          "#define ANIMATION_COMPATIBILITY_CONDITION "            + ( (GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_INTEGER <= MINIMUM_GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS) ? 1 : 0 ) + "\n" +
                                          "#define FS_VARYING in"                                 + "\n" +
                                          "#define TS_GS_POSITION "    + ( (USE_GL_ARB_GEOMETRY_SHADER4 && useGeometryShaders) ? "tsGs" : "fs" ) + "Position"   + "\n" + 
                                          "#define TS_GS_NORMAL "      + ( (USE_GL_ARB_GEOMETRY_SHADER4 && useGeometryShaders) ? "tsGs" : "fs" ) + "Normal"     + "\n" +
                                          "#define TS_GS_SCENE_COLOR " + ( (USE_GL_ARB_GEOMETRY_SHADER4 && useGeometryShaders) ? "tsGs" : "fs" ) + "SceneColor" + "\n" + 
                                          "#define TS_GS_TEX_COORDS  " + ( (USE_GL_ARB_GEOMETRY_SHADER4 && useGeometryShaders) ? "tsGs" : "fs" ) + "TexCoords"  + "\n"
                                          ;
        ShaderTypes[] allShaderTypes = ShaderTypes.values();
        String shaderEffectName = "";
        String shaderEffectFileName = "";
        for (int i = 0; i < NUMBER_OF_AVAILABLE_LOD_SHADERS; i++)
        {
            shaderEffectFileName = EnumUtils.splitAndCapitalizeFirstCharacters(allShaderTypes[i]);
            shaderEffectName = Character.toLowerCase( shaderEffectFileName.charAt(0) ) + shaderEffectFileName.substring(1);
            if (useGeometryShaders)
                ShaderUtils.loadShaderFileCompileAndLinkProgram(gl, new String[] { SHADER_FILES_DIRECTORY_1, SHADER_FILES_DIRECTORY_2, GPU_COMPUTING_DIRECTORY + SHADER_FILES_DIRECTORY_3 }, new String[]{ shaderEffectFileName, SHADER_FILE_NAME_2, SHADER_FILE_NAME_3 }, loadShadersPairs,
                                                                LOAD_SHADER_PROGRAMS_FROM_EXTERNAL_SOURCE, VERTEX_SHADERS, TESSELATION_CONTROL_SHADERS, TESSELATION_EVALUATION_SHADERS, FRAGMENT_SHADERS, GEOMETRY_SHADERS, SHADER_PROGRAMS, GL_TRIANGLES, GL_TRIANGLE_STRIP, NUMBER_OF_OUTPUT_GS_VERTICES, i, GLSLPreprocessorCommands, DEBUG_BUILD);
            else
                ShaderUtils.loadShaderFileCompileAndLinkProgram(gl, new String[] { SHADER_FILES_DIRECTORY_1, SHADER_FILES_DIRECTORY_2, GPU_COMPUTING_DIRECTORY + SHADER_FILES_DIRECTORY_3 }, new String[]{ shaderEffectFileName, SHADER_FILE_NAME_2, SHADER_FILE_NAME_3 }, loadShadersPairs,
                                                                LOAD_SHADER_PROGRAMS_FROM_EXTERNAL_SOURCE, VERTEX_SHADERS, TESSELATION_CONTROL_SHADERS, TESSELATION_EVALUATION_SHADERS, FRAGMENT_SHADERS, SHADER_PROGRAMS, i, GLSLPreprocessorCommands, DEBUG_BUILD);                

            // common effects uniform variables
            SHADER_PROGRAM_2D_TEXTURES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_2D_TEXTURE_NAME);
            SHADER_PROGRAM_EMBOSS_NODE_TEXTURES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_EMBOSS_NODE_TEXTURE_NAME);
            SHADER_PROGRAM_PXS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_PX_NAME);
            SHADER_PROGRAM_PYS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_PY_NAME);

            // per shader naming uniform variables
            SHADER_PROGRAM_FOGS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_FOG_NAME);
            SHADER_PROGRAM_TEXTURINGS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_TEXTURING_NAME);
            SHADER_PROGRAM_SPHERICAL_MAPPINGS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_SPHERICAL_MAPPING_NAME);
            SHADER_PROGRAM_TIMERS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_TIMER_NAME);
            SHADER_PROGRAM_MORPHINGS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_MORPHING_NAME);
            SHADER_PROGRAM_USER_CLIPPINGS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_USER_CLIPPING_NAME);
            SHADER_PROGRAM_ANTIALIASES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_ANTIALIAS_NAME);
            SHADER_PROGRAM_STATES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_STATE_NAME);
            SHADER_PROGRAM_OLD_LCD_STYLE_TRANSPARENCIES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_OLD_LCD_STYLE_TRANSPARENCY_NAME);
            SHADER_PROGRAM_SHRINK_TRIANGLES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_SHRINK_TRIANGLES_NAME);
            SHADER_PROGRAM_SOLID_WIREFRAMES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_SOLID_WIREFRAME_NAME);
            SHADER_PROGRAM_NORMALS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_NORMALS_NAME);
            SHADER_PROGRAM_TESSELLATIONS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], shaderEffectName + SHADER_PROGRAM_TESSELLATION_NAME);
    
            // animation related GPU Computing uniform variables
            SHADER_PROGRAM_ANIMATION_2D_TEXTURES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_2D_TEXTURE_NAME);
            SHADER_PROGRAM_ANIMATION_GPU_COMPUTING_MODES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_GPU_COMPUTING_MODE_NAME);
            SHADER_PROGRAM_NODE_VALUES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_NODE_VALUE_NAME);
            SHADER_PROGRAM_PROCESS_NEXT_NODE_VALUES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_PPROCESS_NEXT_NODE_VALUE_NAME);
            SHADER_PROGRAM_NEXT_NODE_VALUES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_NEXT_NODE_VALUE_NAME);
            SHADER_PROGRAM_ANIMATION_FRAME_COUNTS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_FRAME_COUNT_NAME);
            SHADER_PROGRAM_FRAMERATE_PER_SECOND_FOR_ANIMATIONS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_FRAMERATE_PER_SECOND_FOR_ANIMATION_NAME);
            SHADER_PROGRAM_ANIMATION_FLUID_LINEAR_TRANSITIONS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_FLUID_LINEAR_TRANSITION_NAME);
            SHADER_PROGRAM_ANIMATION_FLUID_POLYNOMIAL_TRANSITIONS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_FLUID_POLYNOMIAL_TRANSITION_NAME);
            SHADER_PROGRAM_ANIMATION_TICKS_PER_SECONDS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_TICKS_PER_SECOND_NAME);
            SHADER_PROGRAM_ANIMATION_MAX_NODE_SIZES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_MAX_NODE_SIZE_NAME);
            SHADER_PROGRAM_ANIMATION_RESULTS_MAX_VALUES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_RESULTS_MAX_VALUE_NAME);
            SHADER_PROGRAM_ANIMATION_RESULTS_REAL_MAX_VALUES[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_RESULTS_REAL_MAX_VALUE_NAME);
            SHADER_PROGRAM_ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITIONS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITION_NAME);
            SHADER_PROGRAM_ANIMATION_USE_REAL_MAX_VALUE_FOR_COLOR_TRANSITIONS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_USE_REAL_MAX_VALUE_FOR_COLOR_TRANSITION_NAME);
            SHADER_PROGRAM_ANIMATION_MAX_SPECTRUM_COLORS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_MAX_SPECTRUM_COLOR_NAME);
            SHADER_PROGRAM_ANIMATION_USE_IMAGE_AS_SPECTRUMS[i] = gl.glGetUniformLocation(SHADER_PROGRAMS[i], SHADER_PROGRAM_ANIMATION_USE_IMAGE_AS_SPECTRUM_NAME);
        }
    }

    /**
    *  Uses a particular shader program with given texturing & fog variables.
    */
    private void useProgramAndUniforms(GL2 gl, int effectIndex, boolean useTexturing, boolean useSphericalMapping, boolean useEmbossBioLayoutLogoName, boolean useFog, float morphingValue, boolean useUserClipping, boolean useAntiAlias, boolean state, boolean oldLCDStyleTransparency, boolean shrinkTriangles, boolean solidWireFrame, boolean normals, float px, float py, float tessellation)
    {
        gl.glUseProgram(SHADER_PROGRAMS[effectIndex]);
        gl.glUniform1i(SHADER_PROGRAM_2D_TEXTURES[effectIndex], ACTIVE_TEXTURE_UNIT_FOR_2D_TEXTURE);
        gl.glUniform1i(SHADER_PROGRAM_FOGS[effectIndex], (useFog) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_TEXTURINGS[effectIndex], (useTexturing) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_SPHERICAL_MAPPINGS[effectIndex], (useSphericalMapping) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_EMBOSS_NODE_TEXTURES[effectIndex], (useEmbossBioLayoutLogoName) ? 1 : 0);
        gl.glUniform1f(SHADER_PROGRAM_PXS[effectIndex], px);
        gl.glUniform1f(SHADER_PROGRAM_PYS[effectIndex], py);        
        gl.glUniform1f(SHADER_PROGRAM_TIMERS[effectIndex], timerUpdate);
        gl.glUniform1f(SHADER_PROGRAM_MORPHINGS[effectIndex], morphingValue);
        gl.glUniform1i(SHADER_PROGRAM_USER_CLIPPINGS[effectIndex], (useUserClipping) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_ANTIALIASES[effectIndex], (useAntiAlias) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_STATES[effectIndex], (state) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_OLD_LCD_STYLE_TRANSPARENCIES[effectIndex], (oldLCDStyleTransparency) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_SHRINK_TRIANGLES[effectIndex], (shrinkTriangles) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_SOLID_WIREFRAMES[effectIndex], (solidWireFrame) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_NORMALS[effectIndex], (normals) ? 1 : 0);
        gl.glUniform1f(SHADER_PROGRAM_TESSELLATIONS[effectIndex], tessellation);

        // animation is off
        gl.glUniform1i(SHADER_PROGRAM_ANIMATION_GPU_COMPUTING_MODES[effectIndex], 0);
    }

    /**
    *  Uses a particular shader program with given texturing & fog variables.
    *  Overloaded version for also passing all the animation uniform values.
    */
    private void useProgramAndUniforms(GL2 gl, int effectIndex, boolean useTexturing, boolean useSphericalMapping, boolean useEmbossBioLayoutLogoName, boolean useFog, float morphingValue, boolean useUserClipping, boolean useAntiAlias, boolean state, boolean oldLCDStyleTransparency, boolean shrinkTriangles, boolean solidWireFrame, boolean normals, float px, float py, float tessellation,
                                              float nodeValue, boolean processNextNodeValue, float nextNodeValue, int animationFrameCount)
    {
        useProgramAndUniforms(gl, effectIndex, useTexturing, useSphericalMapping, useEmbossBioLayoutLogoName, useFog, morphingValue, useUserClipping, useAntiAlias, state, oldLCDStyleTransparency, shrinkTriangles, solidWireFrame, normals, px, py, tessellation);

        // animation is on, pass all needed uniform values
        gl.glUniform1i(SHADER_PROGRAM_ANIMATION_GPU_COMPUTING_MODES[effectIndex], 1);
        gl.glUniform1f(SHADER_PROGRAM_NODE_VALUES[effectIndex], nodeValue);
        gl.glUniform1i(SHADER_PROGRAM_PROCESS_NEXT_NODE_VALUES[effectIndex], (processNextNodeValue) ? 1 : 0);
        gl.glUniform1f(SHADER_PROGRAM_NEXT_NODE_VALUES[effectIndex], nextNodeValue);
        gl.glUniform1i(SHADER_PROGRAM_ANIMATION_FRAME_COUNTS[effectIndex], animationFrameCount);
        gl.glUniform1i(SHADER_PROGRAM_FRAMERATE_PER_SECOND_FOR_ANIMATIONS[effectIndex], FRAMERATE_PER_SECOND_FOR_ANIMATION);
        gl.glUniform1i(SHADER_PROGRAM_ANIMATION_FLUID_LINEAR_TRANSITIONS[effectIndex], (ANIMATION_FLUID_LINEAR_TRANSITION) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_ANIMATION_FLUID_POLYNOMIAL_TRANSITIONS[effectIndex], (ANIMATION_FLUID_POLYNOMIAL_TRANSITION) ? 1 : 0);
        gl.glUniform1f(SHADER_PROGRAM_ANIMATION_TICKS_PER_SECONDS[effectIndex], ANIMATION_TICKS_PER_SECOND);
        gl.glUniform1i(SHADER_PROGRAM_ANIMATION_MAX_NODE_SIZES[effectIndex], ANIMATION_MAX_NODE_SIZE);
        gl.glUniform1f(SHADER_PROGRAM_ANIMATION_RESULTS_MAX_VALUES[effectIndex], ANIMATION_RESULTS_MAX_VALUE);
        gl.glUniform1i(SHADER_PROGRAM_ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITIONS[effectIndex], (ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITION) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_ANIMATION_USE_REAL_MAX_VALUE_FOR_COLOR_TRANSITIONS[effectIndex], (ANIMATION_USE_REAL_MAX_VALUE_FOR_COLOR_TRANSITION) ? 1 : 0);
        gl.glUniform1i(SHADER_PROGRAM_ANIMATION_USE_IMAGE_AS_SPECTRUMS[effectIndex], (ANIMATION_USE_IMAGE_AS_SPECTRUM) ? 1 : 0);
        if (ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITION)
        {
            if (ANIMATION_USE_REAL_MAX_VALUE_FOR_COLOR_TRANSITION)
                gl.glUniform1f(SHADER_PROGRAM_ANIMATION_RESULTS_REAL_MAX_VALUES[effectIndex], ANIMATION_RESULTS_REAL_MAX_VALUE);

            if (!ANIMATION_USE_IMAGE_AS_SPECTRUM)
            {
                ANIMATION_MAX_SPECTRUM_COLOR.getRGBComponents(ANIMATION_MAX_SPECTRUM_COLOR_ARRAY);
                gl.glUniform3fv(SHADER_PROGRAM_ANIMATION_MAX_SPECTRUM_COLORS[effectIndex], 1, ANIMATION_MAX_SPECTRUM_COLOR_ARRAY, 0);
            }
            else
            {                
                gl.glUniform1i(SHADER_PROGRAM_ANIMATION_2D_TEXTURES[effectIndex], ACTIVE_TEXTURE_UNIT_FOR_ANIMATION_SPECTRUM_2D_TEXTURE);
            }
        }
    }

    /**
    *  Uses the given shader SFX lighting program.
    */
    public void useShaderLODSFX(GL2 gl, ShaderTypes shaderType, boolean useTexturing, boolean useSphericalMapping, boolean useEmbossBioLayoutLogoName, boolean useFog, float morphingValue, boolean useUserClipping, boolean useAntiAlias, boolean state, boolean oldLCDStyleTransparency, boolean shrinkTriangles, boolean solidWireFrame, boolean normals, float px, float py, float tessellation)
    {
        useProgramAndUniforms(gl, shaderType.ordinal(), useTexturing, useSphericalMapping, useEmbossBioLayoutLogoName, useFog, morphingValue, useUserClipping, useAntiAlias, state, oldLCDStyleTransparency, shrinkTriangles, solidWireFrame, normals, px, py, tessellation);
    }

    /**
    *  Uses the given shader SFX lighting program.
    *  Overloaded version for also passing all the animation uniform values.
    */
    public void useShaderLODSFX(GL2 gl, ShaderTypes shaderType, boolean useTexturing, boolean useSphericalMapping, boolean useEmbossBioLayoutLogoName, boolean useFog, float morphingValue, boolean useUserClipping, boolean useAntiAlias, boolean state, boolean oldLCDStyleTransparency, boolean shrinkTriangles, boolean solidWireFrame, boolean normals, float px, float py, float tessellation,
                                            float nodeValue, boolean processNextNodeValue, float nextNodeValue, int animationFrameCount)
    {
        useProgramAndUniforms(gl, shaderType.ordinal(), useTexturing, useSphericalMapping, useEmbossBioLayoutLogoName, useFog, morphingValue, useUserClipping, useAntiAlias, state, oldLCDStyleTransparency, shrinkTriangles, solidWireFrame, normals, px, py, tessellation,
                                  nodeValue, processNextNodeValue, nextNodeValue, animationFrameCount);
    }

    /**
    *  Destroys (de-initializes) all the effect resources.
    */
    @Override
    public void destructor(GL2 gl)
    {
        if (useGeometryShaders)
        {
            for (int i = 0; i < NUMBER_OF_AVAILABLE_LOD_SHADERS; i++)
                ShaderUtils.detachAndDeleteShader(gl, loadShadersPairs, VERTEX_SHADERS, TESSELATION_CONTROL_SHADERS, TESSELATION_EVALUATION_SHADERS, GEOMETRY_SHADERS, FRAGMENT_SHADERS, SHADER_PROGRAMS, i);
        }
        else
        {
            for (int i = 0; i < NUMBER_OF_AVAILABLE_LOD_SHADERS; i++)
                ShaderUtils.detachAndDeleteShader(gl, loadShadersPairs, VERTEX_SHADERS, TESSELATION_CONTROL_SHADERS, TESSELATION_EVALUATION_SHADERS, FRAGMENT_SHADERS, SHADER_PROGRAMS, i);            
        }
    }


}