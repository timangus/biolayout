package org.BioLayoutExpress3D.Textures;

import java.io.*;
import java.nio.*;
import javax.media.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.GL3.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
*
*  This class includes shader related static methods.
*
* @author Thanos Theo, 2008-2009
* @version 3.0.0.0
*/

public final class ShaderUtils
{

    /**
    *  The file path of the shader file to be copied from.
    */
    private static final String SHADERS_FILE_PATH_1 = "/Resources";

    /**
    *  The file path of the shader file to be copied from.
    */
    private static final String SHADERS_FILE_PATH_2 = "Shaders/";

    /**
    *  The vertex shader file name.
    */
    private static final String VERTEX_SHADER_FILE_NAME = "vertexShader";

    /**
    *  The tesselation control shader file name.
    */
    private static final String TESSELATION_CONTROL_SHADER_FILE_NAME = "tesselationControlShader";

    /**
    *  The tesselation evaluation shader file name.
    */
    private static final String TESSELATION_EVALUATION_SHADER_FILE_NAME = "tesselationEvaluationShader";

    /**
    *  The geometry shader file name.
    */
    private static final String GEOMETRY_SHADER_FILE_NAME = "geometryShader";

    /**
    *  The fragment shader file name.
    */
    private static final String FRAGMENT_SHADER_FILE_NAME = "fragmentShader";

    /**
    *  The vertex shaders file name extension.
    */
    private static final String VERTEX_SHADERS_FILE_NAME_EXTENSION = ".vert";

    /**
    *  The tesselation control shaders file name extension.
    */
    private static final String TESSELATION_CONTROL_SHADERS_FILE_NAME_EXTENSION = ".tcs";

    /**
    *  The tesselation evaluation shaders file name extension.
    */
    private static final String TESSELATION_EVALUATION_SHADERS_FILE_NAME_EXTENSION = ".tes";

    /**
    *  The geometry shaders file name extension.
    */
    private static final String GEOMETRY_SHADERS_FILE_NAME_EXTENSION = ".geom";

    /**
    *  The fragment shaders file name extension.
    */
    private static final String FRAGMENT_SHADERS_FILE_NAME_EXTENSION = ".frag";

    /**
    *  The vertex source buffer.
    */
    private static final IntBuffer VERTEX_SOURCE_BUFFER = Buffers.newDirectIntBuffer(1);

    /**
    *  The tesselation control source buffer.
    */
    private static final IntBuffer TESSELATION_CONTROL_SOURCE_BUFFER = Buffers.newDirectIntBuffer(1);

    /**
    *  The tesselation evaluation source buffer.
    */
    private static final IntBuffer TESSELATION_EVALUATION_SOURCE_BUFFER = Buffers.newDirectIntBuffer(1);

    /**
    *  The geometry source buffer.
    */
    private static final IntBuffer GEOMETRY_SOURCE_BUFFER = Buffers.newDirectIntBuffer(1);

    /**
    *  The fragment source buffer.
    */
    private static final IntBuffer FRAGMENT_SOURCE_BUFFER = Buffers.newDirectIntBuffer(1);

    /**
    *  Checks if the given OpenGL extension is supported with the current configuration.
    */
    public static boolean checkOpenGLExtensionSupport(String extensionString)
    {
        for (String currentString : GL_EXTENSIONS_STRINGS)
            if ( currentString.contains(extensionString) )
                return true;

        return false;
    }

    /**
    *  Loads the shader file and compiles the shader program from the internal jar.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, new boolean[]{ true, true }, false, vertexShaders, fragmentShaders, shaderPrograms, index, "", validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program from the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, new boolean[]{ true, true }, false, vertexShaders, fragmentShaders, shaderPrograms, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the shaders to a program either from an external file or the internal jar.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, new boolean[]{ true, true }, loadFromFileOrFromJar, vertexShaders, fragmentShaders, shaderPrograms, index, "", validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, new boolean[]{ true, true }, loadFromFileOrFromJar, vertexShaders, fragmentShaders, shaderPrograms, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean[] loadVertexFragmentPair, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, loadVertexFragmentPair, false, vertexShaders, fragmentShaders, shaderPrograms, index, "", validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean[] loadVertexFragmentPair, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, loadVertexFragmentPair, false, vertexShaders, fragmentShaders, shaderPrograms, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program either from an external file or the internal jar.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean[] loadVertexFragmentPair, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, loadVertexFragmentPair, loadFromFileOrFromJar, vertexShaders, fragmentShaders, shaderPrograms, index, "", validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean[] loadVertexFragmentPair, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        if (loadVertexFragmentPair[0])
            vertexShaders[index] = gl.glCreateShader(GL_VERTEX_SHADER);
        if (loadVertexFragmentPair[1])
            fragmentShaders[index] = gl.glCreateShader(GL_FRAGMENT_SHADER);
        shaderPrograms[index] = gl.glCreateProgram();

        if (loadVertexFragmentPair[0])
        {
            String vertexSource = GLSLPreprocessorCommands + readShaderFile(pathName, VERTEX_SHADER_FILE_NAME + shaderName + VERTEX_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !vertexSource.isEmpty() )
            {
                gl.glShaderSource( vertexShaders[index], 1, new String[]{ vertexSource }, (IntBuffer)VERTEX_SOURCE_BUFFER.put( vertexSource.length() ).rewind() );
                gl.glCompileShader(vertexShaders[index]);
                checkInfoLog(gl, "Vertex File '" + VERTEX_SHADER_FILE_NAME + shaderName + VERTEX_SHADERS_FILE_NAME_EXTENSION + "'", vertexShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], vertexShaders[index]);
            }
        }

        if (loadVertexFragmentPair[1])
        {
            String fragmentSource = GLSLPreprocessorCommands + readShaderFile(pathName, FRAGMENT_SHADER_FILE_NAME + shaderName + FRAGMENT_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !fragmentSource.isEmpty() )
            {
                gl.glShaderSource( fragmentShaders[index], 1, new String[]{ fragmentSource }, (IntBuffer)FRAGMENT_SOURCE_BUFFER.put( fragmentSource.length() ).rewind() );
                gl.glCompileShader(fragmentShaders[index]);
                checkInfoLog(gl, "Fragment File '" + FRAGMENT_SHADER_FILE_NAME + shaderName + FRAGMENT_SHADERS_FILE_NAME_EXTENSION + "'", fragmentShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], fragmentShaders[index]);
            }
        }

        gl.glLinkProgram(shaderPrograms[index]);
        if (!validateShader && !GL_IS_AMD_ATI) gl.glValidateProgram(shaderPrograms[index]); // warning, the AMD/ATI driver produces unnecessary OpenGL errors with GLSL validation
        checkInfoLog(gl, "Shader Program '" + shaderName + "'", shaderPrograms[index], false);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/geometry/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] geometryShaders, int[] fragmentShaders, int[] shaderPrograms, int inputTopology, int outputTopology, int maxVerticesOut, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, new boolean[]{ true, true, true }, loadFromFileOrFromJar, vertexShaders, geometryShaders, fragmentShaders, shaderPrograms, inputTopology, outputTopology, maxVerticesOut, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/geometry/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean[] loadVertexGeometryFragmentPair, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] geometryShaders, int[] fragmentShaders, int[] shaderPrograms, int inputTopology, int outputTopology, int maxVerticesOut, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        boolean setGeometryShaderTopologySettingsBeforeLinkingProgram = false;
        if (loadVertexGeometryFragmentPair[0])
            vertexShaders[index] = gl.glCreateShader(GL_VERTEX_SHADER);
        if (loadVertexGeometryFragmentPair[1])
        {
            if( (inputTopology != GL_POINTS)  && (inputTopology != GL_LINES)  &&  (inputTopology != GL_LINES_ADJACENCY_EXT)  &&  (inputTopology != GL_TRIANGLES)  &&  (inputTopology != GL_TRIANGLES_ADJACENCY_EXT) )
            {
                if (DEBUG_BUILD) println("Error: You have not specified a supported Geometry Shader Input Topology.\nShader loading process now aborted.");
                return;
            }
            if( (outputTopology != GL_POINTS)  && (outputTopology != GL_LINE_STRIP)  &&  (outputTopology != GL_TRIANGLE_STRIP) )
            {
                if (DEBUG_BUILD) println("Error: You have not specified a supported Geometry Shader Output Topology.\nShader loading process now aborted.");
                return;
            }
            if (maxVerticesOut > GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB_INTEGER)
            {
                if (DEBUG_BUILD) println("Error: You have specified a larger number of Geometry Shader output vertices than this GPU can handle.\nGL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB: " + GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB_INTEGER + "\nShader loading process now aborted.");
                return;
            }
            geometryShaders[index] = gl.glCreateShader(GL_GEOMETRY_SHADER);
            setGeometryShaderTopologySettingsBeforeLinkingProgram = true;
        }
        if (loadVertexGeometryFragmentPair[2])
            fragmentShaders[index] = gl.glCreateShader(GL_FRAGMENT_SHADER);
        shaderPrograms[index] = gl.glCreateProgram();

        if (loadVertexGeometryFragmentPair[0])
        {
            String vertexSource = GLSLPreprocessorCommands + readShaderFile(pathName, VERTEX_SHADER_FILE_NAME + shaderName + VERTEX_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !vertexSource.isEmpty() )
            {
                gl.glShaderSource( vertexShaders[index], 1, new String[]{ vertexSource }, (IntBuffer)VERTEX_SOURCE_BUFFER.put( vertexSource.length() ).rewind() );
                gl.glCompileShader(vertexShaders[index]);
                checkInfoLog(gl, "Vertex File '" + VERTEX_SHADER_FILE_NAME + shaderName + VERTEX_SHADERS_FILE_NAME_EXTENSION + "'", vertexShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], vertexShaders[index]);
            }
        }

        if (loadVertexGeometryFragmentPair[1])
        {
            String geometrySource = GLSLPreprocessorCommands + readShaderFile(pathName, GEOMETRY_SHADER_FILE_NAME + shaderName + GEOMETRY_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !geometrySource.isEmpty() )
            {
                gl.glShaderSource( geometryShaders[index], 1, new String[]{ geometrySource }, (IntBuffer)GEOMETRY_SOURCE_BUFFER.put( geometrySource.length() ).rewind() );
                gl.glCompileShader(geometryShaders[index]);
                checkInfoLog(gl, "Geometry File '" + GEOMETRY_SHADER_FILE_NAME + shaderName + GEOMETRY_SHADERS_FILE_NAME_EXTENSION + "'", geometryShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], geometryShaders[index]);
            }
        }

        if (loadVertexGeometryFragmentPair[2])
        {
            String fragmentSource = GLSLPreprocessorCommands + readShaderFile(pathName, FRAGMENT_SHADER_FILE_NAME + shaderName + FRAGMENT_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !fragmentSource.isEmpty() )
            {
                gl.glShaderSource( fragmentShaders[index], 1, new String[]{ fragmentSource }, (IntBuffer)FRAGMENT_SOURCE_BUFFER.put( fragmentSource.length() ).rewind() );
                gl.glCompileShader(fragmentShaders[index]);
                checkInfoLog(gl, "Fragment File '" + FRAGMENT_SHADER_FILE_NAME + shaderName + FRAGMENT_SHADERS_FILE_NAME_EXTENSION + "'", fragmentShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], fragmentShaders[index]);
            }
        }

        if (setGeometryShaderTopologySettingsBeforeLinkingProgram)
        {
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_INPUT_TYPE_EXT, inputTopology);
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_OUTPUT_TYPE_EXT, outputTopology);
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_VERTICES_OUT_EXT, maxVerticesOut);
        }

        gl.glLinkProgram(shaderPrograms[index]);
        if (!validateShader && !GL_IS_AMD_ATI) gl.glValidateProgram(shaderPrograms[index]); // warning, the AMD/ATI driver produces unnecessary OpenGL errors with GLSL validation
        checkInfoLog(gl, "Shader Program '" + shaderName + "'", shaderPrograms[index], false);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/tesselation control & evaluation/geometry/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] tesselationControlShaders, int[] tesselationEvaluationShaders, int[] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, new boolean[]{ true, true, true, false, true }, loadFromFileOrFromJar, vertexShaders, tesselationControlShaders, tesselationEvaluationShaders, null, fragmentShaders, shaderPrograms, 0, 0, 0, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/tesselation control & evaluation/geometry/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean[] loadVertexTesselationGeometryFragmentPair, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] tesselationControlShaders, int[] tesselationEvaluationShaders, int[] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadVertexTesselationGeometryFragmentPair[3] = false; // make sure the geometry shader will be disabled
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, loadVertexTesselationGeometryFragmentPair, loadFromFileOrFromJar, vertexShaders, tesselationControlShaders, tesselationEvaluationShaders, null, fragmentShaders, shaderPrograms, 0, 0, 0, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/tesselation control & evaluation/geometry/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] tesselationControlShaders, int[] tesselationEvaluationShaders, int[] geometryShaders, int[] fragmentShaders, int[] shaderPrograms, int inputTopology, int outputTopology, int maxVerticesOut, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathName, shaderName, new boolean[]{ true, true, true, true, true }, loadFromFileOrFromJar, vertexShaders, tesselationControlShaders, tesselationEvaluationShaders, geometryShaders, fragmentShaders, shaderPrograms, inputTopology, outputTopology, maxVerticesOut, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/tesselation control & evaluation/geometry/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String pathName, String shaderName, boolean[] loadVertexTesselationGeometryFragmentPair, boolean loadFromFileOrFromJar, int[] vertexShaders, int[] tesselationControlShaders, int[] tesselationEvaluationShaders, int[] geometryShaders, int[] fragmentShaders, int[] shaderPrograms, int inputTopology, int outputTopology, int maxVerticesOut, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        boolean setGeometryShaderTopologySettingsBeforeLinkingProgram = false;
        if (loadVertexTesselationGeometryFragmentPair[0])
            vertexShaders[index] = gl.glCreateShader(GL_VERTEX_SHADER);
        if (loadVertexTesselationGeometryFragmentPair[1])
            tesselationControlShaders[index] = gl.glCreateShader(GL3.GL_TESS_CONTROL_SHADER);
        if (loadVertexTesselationGeometryFragmentPair[2])
            tesselationEvaluationShaders[index] = gl.glCreateShader(GL3.GL_TESS_EVALUATION_SHADER);
        if (loadVertexTesselationGeometryFragmentPair[3])
        {
            if( (inputTopology != GL_POINTS)  && (inputTopology != GL_LINES)  &&  (inputTopology != GL_LINES_ADJACENCY_EXT)  &&  (inputTopology != GL_TRIANGLES)  &&  (inputTopology != GL_TRIANGLES_ADJACENCY_EXT) )
            {
                if (DEBUG_BUILD) println("Error: You have not specified a supported Geometry Shader Input Topology.\nShader loading process now aborted.");
                return;
            }
            if( (outputTopology != GL_POINTS)  && (outputTopology != GL_LINE_STRIP)  &&  (outputTopology != GL_TRIANGLE_STRIP) )
            {
                if (DEBUG_BUILD) println("Error: You have not specified a supported Geometry Shader Output Topology.\nShader loading process now aborted.");
                return;
            }
            if (maxVerticesOut > GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB_INTEGER)
            {
                if (DEBUG_BUILD) println("Error: You have specified a larger number of Geometry Shader output vertices than this GPU can handle.\nGL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB: " + GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB_INTEGER + "\nShader loading process now aborted.");
                return;
            }
            geometryShaders[index] = gl.glCreateShader(GL_GEOMETRY_SHADER);
            setGeometryShaderTopologySettingsBeforeLinkingProgram = true;
        }
        if (loadVertexTesselationGeometryFragmentPair[4])
            fragmentShaders[index] = gl.glCreateShader(GL_FRAGMENT_SHADER);
        shaderPrograms[index] = gl.glCreateProgram();

        if (loadVertexTesselationGeometryFragmentPair[0])
        {
            String vertexSource = GLSLPreprocessorCommands + readShaderFile(pathName, VERTEX_SHADER_FILE_NAME + shaderName + VERTEX_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !vertexSource.isEmpty() )
            {
                gl.glShaderSource( vertexShaders[index], 1, new String[]{ vertexSource }, (IntBuffer)VERTEX_SOURCE_BUFFER.put( vertexSource.length() ).rewind() );
                gl.glCompileShader(vertexShaders[index]);
                checkInfoLog(gl, "Vertex File '" + VERTEX_SHADER_FILE_NAME + shaderName + VERTEX_SHADERS_FILE_NAME_EXTENSION + "'", vertexShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], vertexShaders[index]);
            }
        }

        if (loadVertexTesselationGeometryFragmentPair[1])
        {
            String tesselationControlSource = GLSLPreprocessorCommands + readShaderFile(pathName, TESSELATION_CONTROL_SHADER_FILE_NAME + shaderName + TESSELATION_CONTROL_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !tesselationControlSource.isEmpty() )
            {
                gl.glShaderSource( tesselationControlShaders[index], 1, new String[]{ tesselationControlSource }, (IntBuffer)TESSELATION_CONTROL_SOURCE_BUFFER.put( tesselationControlSource.length() ).rewind() );
                gl.glCompileShader(tesselationControlShaders[index]);
                checkInfoLog(gl, "Tesselation Control File '" + TESSELATION_CONTROL_SHADER_FILE_NAME + shaderName + TESSELATION_CONTROL_SHADERS_FILE_NAME_EXTENSION + "'", tesselationControlShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], tesselationControlShaders[index]);
            }
        }

        if (loadVertexTesselationGeometryFragmentPair[2])
        {
            String tesselationEvaluationSource = GLSLPreprocessorCommands + readShaderFile(pathName, TESSELATION_EVALUATION_SHADER_FILE_NAME + shaderName + TESSELATION_EVALUATION_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !tesselationEvaluationSource.isEmpty() )
            {
                gl.glShaderSource( tesselationEvaluationShaders[index], 1, new String[]{ tesselationEvaluationSource }, (IntBuffer)TESSELATION_EVALUATION_SOURCE_BUFFER.put( tesselationEvaluationSource.length() ).rewind() );
                gl.glCompileShader(tesselationEvaluationShaders[index]);
                checkInfoLog(gl, "Tesselation Evaluation File '" + TESSELATION_EVALUATION_SHADER_FILE_NAME + shaderName + TESSELATION_EVALUATION_SHADERS_FILE_NAME_EXTENSION + "'", tesselationEvaluationShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], tesselationEvaluationShaders[index]);
            }
        }

        if (loadVertexTesselationGeometryFragmentPair[3])
        {
            String geometrySource = GLSLPreprocessorCommands + readShaderFile(pathName, GEOMETRY_SHADER_FILE_NAME + shaderName + GEOMETRY_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !geometrySource.isEmpty() )
            {
                gl.glShaderSource( geometryShaders[index], 1, new String[]{ geometrySource }, (IntBuffer)GEOMETRY_SOURCE_BUFFER.put( geometrySource.length() ).rewind() );
                gl.glCompileShader(geometryShaders[index]);
                checkInfoLog(gl, "Geometry File '" + GEOMETRY_SHADER_FILE_NAME + shaderName + GEOMETRY_SHADERS_FILE_NAME_EXTENSION + "'", geometryShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], geometryShaders[index]);
            }
        }

        if (loadVertexTesselationGeometryFragmentPair[4])
        {
            String fragmentSource = GLSLPreprocessorCommands + readShaderFile(pathName, FRAGMENT_SHADER_FILE_NAME + shaderName + FRAGMENT_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
            if ( !fragmentSource.isEmpty() )
            {
                gl.glShaderSource( fragmentShaders[index], 1, new String[]{ fragmentSource }, (IntBuffer)FRAGMENT_SOURCE_BUFFER.put( fragmentSource.length() ).rewind() );
                gl.glCompileShader(fragmentShaders[index]);
                checkInfoLog(gl, "Fragment File '" + FRAGMENT_SHADER_FILE_NAME + shaderName + FRAGMENT_SHADERS_FILE_NAME_EXTENSION + "'", fragmentShaders[index], true);
                gl.glAttachShader(shaderPrograms[index], fragmentShaders[index]);
            }
        }

        if (setGeometryShaderTopologySettingsBeforeLinkingProgram)
        {
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_INPUT_TYPE_EXT, inputTopology);
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_OUTPUT_TYPE_EXT, outputTopology);
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_VERTICES_OUT_EXT, maxVerticesOut);
        }

        gl.glLinkProgram(shaderPrograms[index]);
        if (!validateShader && !GL_IS_AMD_ATI) gl.glValidateProgram(shaderPrograms[index]); // warning, the AMD/ATI driver produces unnecessary OpenGL errors with GLSL validation
        checkInfoLog(gl, "Shader Program '" + shaderName + "'", shaderPrograms[index], false);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathShaderNames, shaderNames, returnDummyLoadVertexFragmentPairArray(shaderNames.length), false, vertexShaders, fragmentShaders, shaderPrograms, index, "", validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathShaderNames, shaderNames, returnDummyLoadVertexFragmentPairArray(shaderNames.length), false, vertexShaders, fragmentShaders, shaderPrograms, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the shaders to a program either from an external file or the internal jar.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathShaderNames, shaderNames, returnDummyLoadVertexFragmentPairArray(shaderNames.length), loadFromFileOrFromJar, vertexShaders, fragmentShaders, shaderPrograms, index, "", validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathShaderNames, shaderNames, returnDummyLoadVertexFragmentPairArray(shaderNames.length), loadFromFileOrFromJar, vertexShaders, fragmentShaders, shaderPrograms, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean[][] loadVertexFragmentPair, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathShaderNames, shaderNames, loadVertexFragmentPair, false, vertexShaders, fragmentShaders, shaderPrograms, index, "", validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean[][] loadVertexFragmentPair, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathShaderNames, shaderNames, loadVertexFragmentPair, false, vertexShaders, fragmentShaders, shaderPrograms, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program either from an external file or the internal jar.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean[][] loadVertexFragmentPair, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathShaderNames, shaderNames, loadVertexFragmentPair, loadFromFileOrFromJar, vertexShaders, fragmentShaders, shaderPrograms, index, "", validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean[][] loadVertexFragmentPair, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        for (int i = 0; i < shaderNames.length; i++)
        {
            if (loadVertexFragmentPair[i][0])
                vertexShaders[index][i] = gl.glCreateShader(GL_VERTEX_SHADER);
            if (loadVertexFragmentPair[i][1])
                fragmentShaders[index][i] = gl.glCreateShader(GL_FRAGMENT_SHADER);
        }
        shaderPrograms[index] = gl.glCreateProgram();

        String vertexSource = null;
        String fragmentSource = null;
        for (int i = 0; i < shaderNames.length; i++)
        {
            if (loadVertexFragmentPair[i][0])
            {
                vertexSource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], VERTEX_SHADER_FILE_NAME + shaderNames[i] + VERTEX_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !vertexSource.isEmpty() )
                {
                    gl.glShaderSource( vertexShaders[index][i], 1, new String[]{ vertexSource }, (IntBuffer)VERTEX_SOURCE_BUFFER.put( vertexSource.length() ).rewind() );
                    gl.glCompileShader(vertexShaders[index][i]);
                    checkInfoLog(gl, "Modular Vertex File '" + VERTEX_SHADER_FILE_NAME + shaderNames[i] + VERTEX_SHADERS_FILE_NAME_EXTENSION + "'", vertexShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], vertexShaders[index][i]);
                }
            }

            if (loadVertexFragmentPair[i][1])
            {
                fragmentSource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], FRAGMENT_SHADER_FILE_NAME + shaderNames[i] + FRAGMENT_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !fragmentSource.isEmpty() )
                {
                    gl.glShaderSource( fragmentShaders[index][i], 1, new String[]{ fragmentSource }, (IntBuffer)FRAGMENT_SOURCE_BUFFER.put( fragmentSource.length() ).rewind() );
                    gl.glCompileShader(fragmentShaders[index][i]);
                    checkInfoLog(gl, "Modular Fragment File '" + FRAGMENT_SHADER_FILE_NAME + shaderNames[i] + FRAGMENT_SHADERS_FILE_NAME_EXTENSION + "'", fragmentShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], fragmentShaders[index][i]);
                }
            }
        }

        gl.glLinkProgram(shaderPrograms[index]);
        if (!validateShader && !GL_IS_AMD_ATI) gl.glValidateProgram(shaderPrograms[index]); // warning, the AMD/ATI driver produces unnecessary OpenGL errors with GLSL validation

        String mergedShaderName = shaderNames[0];
        for (int i = 1; i < shaderNames.length; i++)
            mergedShaderName += " + " + shaderNames[i];

        checkInfoLog(gl, "Modular Shader Program '" + mergedShaderName + "'", shaderPrograms[index], false);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] geometryShaders, int[][] fragmentShaders, int[] shaderPrograms, int inputTopology, int outputTopology, int maxVerticesOut, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathShaderNames, shaderNames, returnDummyLoadVertexGeometryFragmentPairArray(shaderNames.length), loadFromFileOrFromJar, vertexShaders, geometryShaders, fragmentShaders, shaderPrograms, inputTopology, outputTopology, maxVerticesOut, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean[][] loadVertexGeometryFragmentPair, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] geometryShaders, int[][] fragmentShaders, int[] shaderPrograms, int inputTopology, int outputTopology, int maxVerticesOut, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        boolean setGeometryShaderTopologySettingsBeforeLinkingProgram = false;
        for (int i = 0; i < shaderNames.length; i++)
        {
            if (loadVertexGeometryFragmentPair[i][0])
                vertexShaders[index][i] = gl.glCreateShader(GL_VERTEX_SHADER);
            if (loadVertexGeometryFragmentPair[i][1])
            {
                if( (inputTopology != GL_POINTS)  && (inputTopology != GL_LINES)  &&  (inputTopology != GL_LINES_ADJACENCY_EXT)  &&  (inputTopology != GL_TRIANGLES)  &&  (inputTopology != GL_TRIANGLES_ADJACENCY_EXT) )
                {
                    if (DEBUG_BUILD) println("Error: You have not specified a supported Geometry Shader Input Topology.\nShader loading process now aborted.");
                    return;
                }
                if( (outputTopology != GL_POINTS)  && (outputTopology != GL_LINE_STRIP)  &&  (outputTopology != GL_TRIANGLE_STRIP) )
                {
                    if (DEBUG_BUILD) println("Error: You have not specified a supported Geometry Shader Output Topology.\nShader loading process now aborted.");
                    return;
                }
                if (maxVerticesOut > GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB_INTEGER)
                {
                    if (DEBUG_BUILD) println("Error: You have specified a larger number of Geometry Shader output vertices than this GPU can handle.\nGL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB: " + GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB_INTEGER + "\nShader loading process now aborted.");
                    return;
                }
                geometryShaders[index][i] = gl.glCreateShader(GL_GEOMETRY_SHADER);
                setGeometryShaderTopologySettingsBeforeLinkingProgram = true;
            }
            if (loadVertexGeometryFragmentPair[i][2])
                fragmentShaders[index][i] = gl.glCreateShader(GL_FRAGMENT_SHADER);
        }
        shaderPrograms[index] = gl.glCreateProgram();

        String vertexSource = null;
        String geometrySource = null;
        String fragmentSource = null;
        for (int i = 0; i < shaderNames.length; i++)
        {
            if (loadVertexGeometryFragmentPair[i][0])
            {
                vertexSource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], VERTEX_SHADER_FILE_NAME + shaderNames[i] + VERTEX_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !vertexSource.isEmpty() )
                {
                    gl.glShaderSource( vertexShaders[index][i], 1, new String[]{ vertexSource }, (IntBuffer)VERTEX_SOURCE_BUFFER.put( vertexSource.length() ).rewind() );
                    gl.glCompileShader(vertexShaders[index][i]);
                    checkInfoLog(gl, "Modular Vertex File '" + VERTEX_SHADER_FILE_NAME + shaderNames[i] + VERTEX_SHADERS_FILE_NAME_EXTENSION + "'", vertexShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], vertexShaders[index][i]);
                }
            }

            if (loadVertexGeometryFragmentPair[i][1])
            {
                geometrySource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], GEOMETRY_SHADER_FILE_NAME + shaderNames[i] + GEOMETRY_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !vertexSource.isEmpty() )
                {
                    gl.glShaderSource( geometryShaders[index][i], 1, new String[]{ geometrySource }, (IntBuffer)GEOMETRY_SOURCE_BUFFER.put( geometrySource.length() ).rewind() );
                    gl.glCompileShader(geometryShaders[index][i]);
                    checkInfoLog(gl, "Modular Geometry File '" + GEOMETRY_SHADER_FILE_NAME + shaderNames[i] + GEOMETRY_SHADERS_FILE_NAME_EXTENSION + "'", geometryShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], geometryShaders[index][i]);
                }
            }

            if (loadVertexGeometryFragmentPair[i][2])
            {
                fragmentSource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], FRAGMENT_SHADER_FILE_NAME + shaderNames[i] + FRAGMENT_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !fragmentSource.isEmpty() )
                {
                    gl.glShaderSource( fragmentShaders[index][i], 1, new String[]{ fragmentSource }, (IntBuffer)FRAGMENT_SOURCE_BUFFER.put( fragmentSource.length() ).rewind() );
                    gl.glCompileShader(fragmentShaders[index][i]);
                    checkInfoLog(gl, "Modular Fragment File '" + FRAGMENT_SHADER_FILE_NAME + shaderNames[i] + FRAGMENT_SHADERS_FILE_NAME_EXTENSION + "'", fragmentShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], fragmentShaders[index][i]);
                }
            }
        }

        if (setGeometryShaderTopologySettingsBeforeLinkingProgram)
        {
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_INPUT_TYPE_EXT, inputTopology);
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_OUTPUT_TYPE_EXT, outputTopology);
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_VERTICES_OUT_EXT, maxVerticesOut);
        }

        gl.glLinkProgram(shaderPrograms[index]);
        if (!validateShader && !GL_IS_AMD_ATI) gl.glValidateProgram(shaderPrograms[index]); // warning, the AMD/ATI driver produces unnecessary OpenGL errors with GLSL validation

        String mergedShaderName = shaderNames[0];
        for (int i = 1; i < shaderNames.length; i++)
            mergedShaderName += " + " + shaderNames[i];

        checkInfoLog(gl, "Modular Shader Program '" + mergedShaderName + "'", shaderPrograms[index], false);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/tesselation control & evaluation/geometry/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathNames, String[] shaderNames, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] tesselationControlShaders, int[][] tesselationEvaluationShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathNames, shaderNames, returnDummyLoadVertexTesselationGeometryFragmentPairArray(shaderNames.length, false), loadFromFileOrFromJar, vertexShaders, tesselationControlShaders, tesselationEvaluationShaders, null, fragmentShaders, shaderPrograms, 0, 0, 0, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads the shader file and compiles the shader program.
    *  Overloaded version so as to load the separate vertex/tesselation control & evaluation/geometry/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathNames, String[] shaderNames, boolean[][] loadVertexTesselationGeometryFragmentPair, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] tesselationControlShaders, int[][] tesselationEvaluationShaders, int[][] fragmentShaders, int[] shaderPrograms, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        for (int i = 0 ; i < shaderNames.length; i++)
            loadVertexTesselationGeometryFragmentPair[i][3] = false; // make sure the geometry shaders will be disabled
        loadShaderFileCompileAndLinkProgram(gl, pathNames, shaderNames, loadVertexTesselationGeometryFragmentPair, loadFromFileOrFromJar, vertexShaders, tesselationControlShaders, tesselationEvaluationShaders, null, fragmentShaders, shaderPrograms, 0, 0, 0, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] tesselationControlShaders, int[][] tesselationEvaluationShaders, int[][] geometryShaders, int[][] fragmentShaders, int[] shaderPrograms, int inputTopology, int outputTopology, int maxVerticesOut, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        loadShaderFileCompileAndLinkProgram(gl, pathShaderNames, shaderNames, returnDummyLoadVertexTesselationGeometryFragmentPairArray(shaderNames.length, true), loadFromFileOrFromJar, vertexShaders, tesselationControlShaders, tesselationEvaluationShaders, geometryShaders, fragmentShaders, shaderPrograms, inputTopology, outputTopology, maxVerticesOut, index, GLSLPreprocessorCommands, validateShader);
    }

    /**
    *  Loads multiple shader files and compiles the shader program from the internal jar.
    *  Overloaded version so as to load the separate vertex/fragment shaders to a program either from an external file or the internal jar.
    *  Overloaded version that also supports GLSL preprocessor commands.
    */
    public static void loadShaderFileCompileAndLinkProgram(GL2 gl, String[] pathShaderNames, String[] shaderNames, boolean[][] loadVertexTesselationGeometryFragmentPair, boolean loadFromFileOrFromJar, int[][] vertexShaders, int[][] tesselationControlShaders, int[][] tesselationEvaluationShaders, int[][] geometryShaders, int[][] fragmentShaders, int[] shaderPrograms, int inputTopology, int outputTopology, int maxVerticesOut, int index, String GLSLPreprocessorCommands, boolean validateShader)
    {
        boolean setGeometryShaderTopologySettingsBeforeLinkingProgram = false;
        for (int i = 0; i < shaderNames.length; i++)
        {
            if (loadVertexTesselationGeometryFragmentPair[i][0])
                vertexShaders[index][i] = gl.glCreateShader(GL_VERTEX_SHADER);
            if (loadVertexTesselationGeometryFragmentPair[i][1])
                tesselationControlShaders[index][i] = gl.glCreateShader(GL3.GL_TESS_CONTROL_SHADER);
            if (loadVertexTesselationGeometryFragmentPair[i][2])
                tesselationEvaluationShaders[index][i] = gl.glCreateShader(GL3.GL_TESS_EVALUATION_SHADER);
            if (loadVertexTesselationGeometryFragmentPair[i][3])
            {
                if( (inputTopology != GL_POINTS)  && (inputTopology != GL_LINES)  &&  (inputTopology != GL_LINES_ADJACENCY_EXT)  &&  (inputTopology != GL_TRIANGLES)  &&  (inputTopology != GL_TRIANGLES_ADJACENCY_EXT) )
                {
                    if (DEBUG_BUILD) println("Error: You have not specified a supported Geometry Shader Input Topology.\nShader loading process now aborted.");
                    return;
                }
                if( (outputTopology != GL_POINTS)  && (outputTopology != GL_LINE_STRIP)  &&  (outputTopology != GL_TRIANGLE_STRIP) )
                {
                    if (DEBUG_BUILD) println("Error: You have not specified a supported Geometry Shader Output Topology.\nShader loading process now aborted.");
                    return;
                }
                if (maxVerticesOut > GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB_INTEGER)
                {
                    if (DEBUG_BUILD) println("Error: You have specified a larger number of Geometry Shader output vertices than this GPU can handle.\nGL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB: " + GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB_INTEGER + "\nShader loading process now aborted.");
                    return;
                }
                geometryShaders[index][i] = gl.glCreateShader(GL_GEOMETRY_SHADER);
                setGeometryShaderTopologySettingsBeforeLinkingProgram = true;
            }
            if (loadVertexTesselationGeometryFragmentPair[i][4])
                fragmentShaders[index][i] = gl.glCreateShader(GL_FRAGMENT_SHADER);
        }
        shaderPrograms[index] = gl.glCreateProgram();

        String vertexSource = null;
        String geometrySource = null;
        String fragmentSource = null;
        for (int i = 0; i < shaderNames.length; i++)
        {
            if (loadVertexTesselationGeometryFragmentPair[i][0])
            {
                vertexSource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], VERTEX_SHADER_FILE_NAME + shaderNames[i] + VERTEX_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !vertexSource.isEmpty() )
                {
                    gl.glShaderSource( vertexShaders[index][i], 1, new String[]{ vertexSource }, (IntBuffer)VERTEX_SOURCE_BUFFER.put( vertexSource.length() ).rewind() );
                    gl.glCompileShader(vertexShaders[index][i]);
                    checkInfoLog(gl, "Modular Vertex File '" + VERTEX_SHADER_FILE_NAME + shaderNames[i] + VERTEX_SHADERS_FILE_NAME_EXTENSION + "'", vertexShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], vertexShaders[index][i]);
                }
            }

            if (loadVertexTesselationGeometryFragmentPair[i][1])
            {
                String tesselationControlSource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], TESSELATION_CONTROL_SHADER_FILE_NAME + shaderNames[i] + TESSELATION_CONTROL_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !tesselationControlSource.isEmpty() )
                {
                    gl.glShaderSource( tesselationControlShaders[index][i], 1, new String[]{ tesselationControlSource }, (IntBuffer)TESSELATION_CONTROL_SOURCE_BUFFER.put( tesselationControlSource.length() ).rewind() );
                    gl.glCompileShader(tesselationControlShaders[index][i]);
                    checkInfoLog(gl, "Tesselation Control File '" + TESSELATION_CONTROL_SHADER_FILE_NAME + shaderNames[i] + TESSELATION_CONTROL_SHADERS_FILE_NAME_EXTENSION + "'", tesselationControlShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], tesselationControlShaders[index][i]);
                }
            }

            if (loadVertexTesselationGeometryFragmentPair[i][2])
            {
                String tesselationEvaluationSource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], TESSELATION_EVALUATION_SHADER_FILE_NAME + shaderNames[i] + TESSELATION_EVALUATION_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !tesselationEvaluationSource.isEmpty() )
                {
                    gl.glShaderSource( tesselationEvaluationShaders[index][i], 1, new String[]{ tesselationEvaluationSource }, (IntBuffer)TESSELATION_EVALUATION_SOURCE_BUFFER.put( tesselationEvaluationSource.length() ).rewind() );
                    gl.glCompileShader(tesselationEvaluationShaders[index][i]);
                    checkInfoLog(gl, "Tesselation Evaluation File '" + TESSELATION_EVALUATION_SHADER_FILE_NAME + shaderNames[i] + TESSELATION_EVALUATION_SHADERS_FILE_NAME_EXTENSION + "'", tesselationEvaluationShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], tesselationEvaluationShaders[index][i]);
                }
            }

            if (loadVertexTesselationGeometryFragmentPair[i][3])
            {
                geometrySource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], GEOMETRY_SHADER_FILE_NAME + shaderNames[i] + GEOMETRY_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !vertexSource.isEmpty() )
                {
                    gl.glShaderSource( geometryShaders[index][i], 1, new String[]{ geometrySource }, (IntBuffer)GEOMETRY_SOURCE_BUFFER.put( geometrySource.length() ).rewind() );
                    gl.glCompileShader(geometryShaders[index][i]);
                    checkInfoLog(gl, "Modular Geometry File '" + GEOMETRY_SHADER_FILE_NAME + shaderNames[i] + GEOMETRY_SHADERS_FILE_NAME_EXTENSION + "'", geometryShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], geometryShaders[index][i]);
                }
            }

            if (loadVertexTesselationGeometryFragmentPair[i][4])
            {
                fragmentSource = GLSLPreprocessorCommands + readShaderFile(pathShaderNames[i], FRAGMENT_SHADER_FILE_NAME + shaderNames[i] + FRAGMENT_SHADERS_FILE_NAME_EXTENSION, loadFromFileOrFromJar);
                if ( !fragmentSource.isEmpty() )
                {
                    gl.glShaderSource( fragmentShaders[index][i], 1, new String[]{ fragmentSource }, (IntBuffer)FRAGMENT_SOURCE_BUFFER.put( fragmentSource.length() ).rewind() );
                    gl.glCompileShader(fragmentShaders[index][i]);
                    checkInfoLog(gl, "Modular Fragment File '" + FRAGMENT_SHADER_FILE_NAME + shaderNames[i] + FRAGMENT_SHADERS_FILE_NAME_EXTENSION + "'", fragmentShaders[index][i], true);
                    gl.glAttachShader(shaderPrograms[index], fragmentShaders[index][i]);
                }
            }
        }

        if (setGeometryShaderTopologySettingsBeforeLinkingProgram)
        {
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_INPUT_TYPE_EXT, inputTopology);
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_OUTPUT_TYPE_EXT, outputTopology);
            gl.glProgramParameteri(shaderPrograms[index], GL_GEOMETRY_VERTICES_OUT_EXT, maxVerticesOut);
        }

        gl.glLinkProgram(shaderPrograms[index]);
        if (!validateShader && !GL_IS_AMD_ATI) gl.glValidateProgram(shaderPrograms[index]); // warning, the AMD/ATI driver produces unnecessary OpenGL errors with GLSL validation

        String mergedShaderName = shaderNames[0];
        for (int i = 1; i < shaderNames.length; i++)
            mergedShaderName += " + " + shaderNames[i];

        checkInfoLog(gl, "Modular Shader Program '" + mergedShaderName + "'", shaderPrograms[index], false);
    }

    /**
    *  Returns a dummy loadVertexFragmentPair array.
    */
    private static boolean[][] returnDummyLoadVertexFragmentPairArray(int length)
    {
        boolean[][] loadVertexFragmentPair = new boolean[length][2];
        for (int i = 0 ; i < length; i++)
        {
            loadVertexFragmentPair[i][0] = true;
            loadVertexFragmentPair[i][1] = true;
        }

        return loadVertexFragmentPair;
    }

    /**
    *  Returns a dummy loadVertexGeometryFragmentPair array.
    */
    private static boolean[][] returnDummyLoadVertexGeometryFragmentPairArray(int length)
    {
        boolean[][] loadVertexGeometryFragmentPair = new boolean[length][3];
        for (int i = 0 ; i < length; i++)
        {
            loadVertexGeometryFragmentPair[i][0] = true;
            loadVertexGeometryFragmentPair[i][1] = true;
            loadVertexGeometryFragmentPair[i][2] = true;
        }

        return loadVertexGeometryFragmentPair;
    }

    /**
    *  Returns a dummy loadVertexTesselationGeometryFragmentPair array.
    */
    private static boolean[][] returnDummyLoadVertexTesselationGeometryFragmentPairArray(int length, boolean useGeometryShaders)
    {
        boolean[][] loadVertexTesselationGeometryFragmentPair = new boolean[length][5];
        for (int i = 0 ; i < length; i++)
        {
            loadVertexTesselationGeometryFragmentPair[i][0] = true;
            loadVertexTesselationGeometryFragmentPair[i][1] = true;
            loadVertexTesselationGeometryFragmentPair[i][2] = true;
            loadVertexTesselationGeometryFragmentPair[i][3] = useGeometryShaders;
            loadVertexTesselationGeometryFragmentPair[i][4] = true;
        }

        return loadVertexTesselationGeometryFragmentPair;
    }

    /**
    *  Reads the shader file.
    */
    private static String readShaderFile(String pathName, String fileName, boolean loadFromFileOrFromJar)
    {
        BufferedReader br = null;
        StringBuilder returnShaderFileString = new StringBuilder();

        try
        {
            String shaderFilePath = SHADERS_FILE_PATH_2 + pathName + "/" + fileName;
            String shaderJarFilePath = SHADERS_FILE_PATH_1 + "/" + SHADERS_FILE_PATH_2 + pathName + "/" + fileName;

            br = new BufferedReader( (loadFromFileOrFromJar) ?
                    new FileReader(shaderFilePath) :
                    new InputStreamReader( ShaderUtils.class.getResourceAsStream(shaderJarFilePath) ) );

            String line = "";
            while ( ( line = br.readLine() ) != null )
              returnShaderFileString.append(line).append("\n");
        }
        catch (IOException ioExc)
        {
            if (DEBUG_BUILD) println("IOException caught in readShaderFile():\n" + ioExc.getMessage());
        }
        finally
        {
            try
            {
                if (br != null) br.close();
            }
            catch (IOException ioExc)
            {
                if (DEBUG_BUILD) println("Not managed to close the shader file with 'finally' clause in readShaderFile() method:\n" + ioExc.getMessage());
            }
        }

        return returnShaderFileString.toString();
    }

    /**
    *  Checks the OpenGL info log of the shader loading process.
    */
    private static void checkInfoLog(GL2 gl, String shaderName, int obj, boolean shaderInfo)
    {
        IntBuffer value = Buffers.newDirectIntBuffer(1);
        if (shaderInfo)
            gl.glGetShaderiv(obj, GL_INFO_LOG_LENGTH, value);
        else
            gl.glGetProgramiv(obj, GL_INFO_LOG_LENGTH, value);

        int length = value.get();
        if (length <= 1)
            return;

        ByteBuffer infoLog = Buffers.newDirectByteBuffer(length);
        value.flip();

        if (shaderInfo)
            gl.glGetShaderInfoLog(obj, length, value, infoLog);
        else
            gl.glGetProgramInfoLog(obj, length, value, infoLog);

        byte[] infoBytes = new byte[length];
        infoLog.get(infoBytes);

        if (DEBUG_BUILD) println( "\nGLSL Validation Report for the Shader " + shaderName + ":\n" + new String(infoBytes) );
    }

    /**
    *  Detaches and deletes the shader program.
    */
    public static void detachAndDeleteShader(GL2 gl, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index)
    {
        detachAndDeleteShader(gl, new boolean[]{ true, true }, vertexShaders, fragmentShaders, shaderPrograms, index);
    }

    /**
    *  Detaches and deletes the shader program.
    *  Overloaded version so as to detach and delete the separate vertex/fragment shaders.
    */
    public static void detachAndDeleteShader(GL2 gl, boolean[] loadVertexFragmentPair, int[] vertexShaders, int[] fragmentShaders, int[] shaderPrograms, int index)
    {
        if (loadVertexFragmentPair[0])
        {
            if ( gl.glIsShader(vertexShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], vertexShaders[index]);
                gl.glDeleteShader(vertexShaders[index]);
            }
        }

        if (loadVertexFragmentPair[1])
        {
            if ( gl.glIsShader(fragmentShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], fragmentShaders[index]);
                gl.glDeleteShader(fragmentShaders[index]);
            }
        }

        if ( gl.glIsProgram(shaderPrograms[index]) )
            gl.glDeleteProgram(shaderPrograms[index]);
    }

    /**
    *  Detaches and deletes the shader program.
    */
    public static void detachAndDeleteShader(GL2 gl, int[] vertexShaders, int[] geometryShaders, int[] fragmentShaders, int[] shaderPrograms, int index)
    {
        detachAndDeleteShader(gl, new boolean[]{ true, true, true }, vertexShaders, fragmentShaders, shaderPrograms, index);
    }

    /**
    *  Detaches and deletes the shader program.
    *  Overloaded version so as to detach and delete the separate vertex/geometry/fragment shaders.
    */
    public static void detachAndDeleteShader(GL2 gl, boolean[] loadVertexGeometryFragmentPair, int[] vertexShaders, int[] geometryShaders, int[] fragmentShaders, int[] shaderPrograms, int index)
    {
        if (loadVertexGeometryFragmentPair[0])
        {
            if ( gl.glIsShader(vertexShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], vertexShaders[index]);
                gl.glDeleteShader(vertexShaders[index]);
            }
        }

        if (loadVertexGeometryFragmentPair[1])
        {
            if ( gl.glIsShader(geometryShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], geometryShaders[index]);
                gl.glDeleteShader(geometryShaders[index]);
            }
        }

        if (loadVertexGeometryFragmentPair[2])
        {
            if ( gl.glIsShader(fragmentShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], fragmentShaders[index]);
                gl.glDeleteShader(fragmentShaders[index]);
            }
        }

        if ( gl.glIsProgram(shaderPrograms[index]) )
            gl.glDeleteProgram(shaderPrograms[index]);
    }

    /**
    *  Detaches and deletes the shader program.
    */
    public static void detachAndDeleteShader(GL2 gl, int[] vertexShaders, int[] tesselationControlShaders, int[] tesselationEvaluationShaders, int[] geometryShaders, int[] fragmentShaders, int[] shaderPrograms, int index)
    {
        detachAndDeleteShader(gl, new boolean[]{ true, true, true, true, true }, vertexShaders, tesselationControlShaders, tesselationEvaluationShaders, geometryShaders, fragmentShaders, shaderPrograms, index);
    }

    /**
    *  Detaches and deletes the shader program.
    *  Overloaded version so as to detach and delete the separate vertex/geometry/fragment shaders.
    */
    public static void detachAndDeleteShader(GL2 gl, boolean[] loadVertexTesselationGeometryFragmentPair, int[] vertexShaders, int[] tesselationControlShaders, int[] tesselationEvaluationShaders, int[] geometryShaders, int[] fragmentShaders, int[] shaderPrograms, int index)
    {
        if (loadVertexTesselationGeometryFragmentPair[0])
        {
            if ( gl.glIsShader(vertexShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], vertexShaders[index]);
                gl.glDeleteShader(vertexShaders[index]);
            }
        }

        if (loadVertexTesselationGeometryFragmentPair[1])
        {
            if ( gl.glIsShader(tesselationControlShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], tesselationControlShaders[index]);
                gl.glDeleteShader(tesselationControlShaders[index]);
            }
        }

        if (loadVertexTesselationGeometryFragmentPair[2])
        {
            if ( gl.glIsShader(tesselationEvaluationShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], tesselationEvaluationShaders[index]);
                gl.glDeleteShader(tesselationEvaluationShaders[index]);
            }
        }

        if (loadVertexTesselationGeometryFragmentPair[3])
        {
            if ( gl.glIsShader(geometryShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], geometryShaders[index]);
                gl.glDeleteShader(geometryShaders[index]);
            }
        }

        if (loadVertexTesselationGeometryFragmentPair[4])
        {
            if ( gl.glIsShader(fragmentShaders[index]) )
            {
                gl.glDetachShader(shaderPrograms[index], fragmentShaders[index]);
                gl.glDeleteShader(fragmentShaders[index]);
            }
        }

        if ( gl.glIsProgram(shaderPrograms[index]) )
            gl.glDeleteProgram(shaderPrograms[index]);
    }

    /**
    *  Detaches and deletes multiple shader programs.
    */
    public static void detachAndDeleteShader(GL2 gl, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index)
    {
        detachAndDeleteShader(gl, returnDummyLoadVertexFragmentPairArray(vertexShaders[0].length), vertexShaders, fragmentShaders, shaderPrograms, index);
    }

    /**
    *  Detaches and deletes multiple shader programs.
    *  Overloaded version so as to detach and delete the separate vertex/fragment shaders.
    */
    public static void detachAndDeleteShader(GL2 gl, boolean[][] loadVertexFragmentPair, int[][] vertexShaders, int[][] fragmentShaders, int[] shaderPrograms, int index)
    {
        for (int i = 0; i < vertexShaders[index].length; i++)
        {
            if (loadVertexFragmentPair[i][0])
            {
                if ( gl.glIsShader(vertexShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], vertexShaders[index][i]);
                    gl.glDeleteShader(vertexShaders[index][i]);
                }
            }
        }

        for (int i = 0; i < fragmentShaders[index].length; i++)
        {
            if (loadVertexFragmentPair[i][1])
            {
                if ( gl.glIsShader(fragmentShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], fragmentShaders[index][i]);
                    gl.glDeleteShader(fragmentShaders[index][i]);
                }
            }
        }

        if ( gl.glIsProgram(shaderPrograms[index]) )
            gl.glDeleteProgram(shaderPrograms[index]);
    }

    /**
    *  Detaches and deletes multiple shader programs.
    */
    public static void detachAndDeleteShader(GL2 gl, int[][] vertexShaders, int[][] geometryShaders, int[][] fragmentShaders, int[] shaderPrograms, int index)
    {
        detachAndDeleteShader(gl, returnDummyLoadVertexGeometryFragmentPairArray(vertexShaders[0].length), vertexShaders, fragmentShaders, shaderPrograms, index);
    }

    /**
    *  Detaches and deletes multiple shader programs.
    *  Overloaded version so as to detach and delete the separate vertex/geometry/fragment shaders.
    */
    public static void detachAndDeleteShader(GL2 gl, boolean[][] loadVertexGeometryFragmentPair, int[][] vertexShaders, int[][] geometryShaders, int[][] fragmentShaders, int[] shaderPrograms, int index)
    {
        for (int i = 0; i < vertexShaders[index].length; i++)
        {
            if (loadVertexGeometryFragmentPair[i][0])
            {
                if ( gl.glIsShader(vertexShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], vertexShaders[index][i]);
                    gl.glDeleteShader(vertexShaders[index][i]);
                }
            }
        }

        for (int i = 0; i < geometryShaders[index].length; i++)
        {
            if (loadVertexGeometryFragmentPair[i][1])
            {
                if ( gl.glIsShader(geometryShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], geometryShaders[index][i]);
                    gl.glDeleteShader(geometryShaders[index][i]);
                }
            }
        }

        for (int i = 0; i < fragmentShaders[index].length; i++)
        {
            if (loadVertexGeometryFragmentPair[i][2])
            {
                if ( gl.glIsShader(fragmentShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], fragmentShaders[index][i]);
                    gl.glDeleteShader(fragmentShaders[index][i]);
                }
            }
        }

        if ( gl.glIsProgram(shaderPrograms[index]) )
            gl.glDeleteProgram(shaderPrograms[index]);
    }

    /**
    *  Detaches and deletes multiple shader programs.
    */
    public static void detachAndDeleteShader(GL2 gl, int[][] vertexShaders, int[][] tesselationControlShaders, int[][] tesselationEvaluationShaders, int[][] geometryShaders, int[][] fragmentShaders, int[] shaderPrograms, int index)
    {
        detachAndDeleteShader(gl, returnDummyLoadVertexTesselationGeometryFragmentPairArray(vertexShaders[0].length, true), vertexShaders, tesselationControlShaders, tesselationEvaluationShaders, geometryShaders, fragmentShaders, shaderPrograms, index);
    }

    /**
    *  Detaches and deletes multiple shader programs.
    *  Overloaded version so as to detach and delete the separate vertex/geometry/fragment shaders.
    */
    public static void detachAndDeleteShader(GL2 gl, boolean[][] loadVertexTesselationGeometryFragmentPair, int[][] vertexShaders, int[][] tesselationControlShaders, int[][] tesselationEvaluationShaders, int[][] fragmentShaders, int[] shaderPrograms, int index)
    {
        for (int i = 0 ; i < vertexShaders.length; i++)
            loadVertexTesselationGeometryFragmentPair[i][3] = false; // make sure the geometry shaders will be disabled
        detachAndDeleteShader(gl, loadVertexTesselationGeometryFragmentPair, vertexShaders, tesselationControlShaders, tesselationEvaluationShaders, null, fragmentShaders, shaderPrograms, index);
    }

    /**
    *  Detaches and deletes multiple shader programs.
    *  Overloaded version so as to detach and delete the separate vertex/geometry/fragment shaders.
    */
    public static void detachAndDeleteShader(GL2 gl, boolean[][] loadVertexTesselationGeometryFragmentPair, int[][] vertexShaders, int[][] tesselationControlShaders, int[][] tesselationEvaluationShaders, int[][] geometryShaders, int[][] fragmentShaders, int[] shaderPrograms, int index)
    {
        for (int i = 0; i < vertexShaders[index].length; i++)
        {
            if (loadVertexTesselationGeometryFragmentPair[i][0])
            {
                if ( gl.glIsShader(vertexShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], vertexShaders[index][i]);
                    gl.glDeleteShader(vertexShaders[index][i]);
                }
            }
        }

        for (int i = 0; i < tesselationControlShaders[index].length; i++)
        {
            if (loadVertexTesselationGeometryFragmentPair[i][1])
            {
                if ( gl.glIsShader(tesselationControlShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], tesselationControlShaders[index][i]);
                    gl.glDeleteShader(tesselationControlShaders[index][i]);
                }
            }
        }

        for (int i = 0; i < tesselationEvaluationShaders[index].length; i++)
        {
            if (loadVertexTesselationGeometryFragmentPair[i][2])
            {
                if ( gl.glIsShader(tesselationEvaluationShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], tesselationEvaluationShaders[index][i]);
                    gl.glDeleteShader(tesselationEvaluationShaders[index][i]);
                }
            }
        }

        for (int i = 0; i < geometryShaders[index].length; i++)
        {
            if (loadVertexTesselationGeometryFragmentPair[i][3])
            {
                if ( gl.glIsShader(geometryShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], geometryShaders[index][i]);
                    gl.glDeleteShader(geometryShaders[index][i]);
                }
            }
        }

        for (int i = 0; i < fragmentShaders[index].length; i++)
        {
            if (loadVertexTesselationGeometryFragmentPair[i][4])
            {
                if ( gl.glIsShader(fragmentShaders[index][i]) )
                {
                    gl.glDetachShader(shaderPrograms[index], fragmentShaders[index][i]);
                    gl.glDeleteShader(fragmentShaders[index][i]);
                }
            }
        }

        if ( gl.glIsProgram(shaderPrograms[index]) )
            gl.glDeleteProgram(shaderPrograms[index]);
    }


}
