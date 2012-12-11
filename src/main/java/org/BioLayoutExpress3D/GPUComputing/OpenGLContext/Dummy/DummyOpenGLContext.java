package org.BioLayoutExpress3D.GPUComputing.OpenGLContext.Dummy;

import org.BioLayoutExpress3D.GPUComputing.OpenGLContext.*;

/**
*
* DummyOpenGLContext is the main OpenGL context component for a Dummy OpenGL context.
*
* @author Thanos Theo, 2008-2009-2010
* @version 3.0.0.0
*
*/

public class DummyOpenGLContext extends OpenGLContext
{

    /**
    *  Serial version UID variable for the OpenGLContext class.
    */
    public static final long serialVersionUID = 117222333444555669L;

    /**
    *  The first constructor of the DummyOpenGLContext class.
    */
    public DummyOpenGLContext(AllTextureParameters.TextureParameters textureParameters, int textureSize, int problemDomainX, int problemDomainY)
    {
        super(textureParameters, textureSize, problemDomainX, problemDomainY);
    }

    /**
    *  The second constructor of the DummyOpenGLContext class.
    */
    public DummyOpenGLContext(AllTextureParameters.TextureParameters textureParameters, int textureSize, int problemDomainX, int problemDomainY, boolean dialogErrorLog)
    {
        super(textureParameters, textureSize, problemDomainX, problemDomainY, dialogErrorLog);
    }

    /**
    *  The third constructor of the DummyOpenGLContext class.
    */
    public DummyOpenGLContext(AllTextureParameters.TextureParameters textureParameters, int textureSize, int problemDomainX, int problemDomainY, boolean dialogErrorLog, boolean openGLSupportAndExtensionsLog)
    {
        super(textureParameters, textureSize, problemDomainX, problemDomainY, dialogErrorLog, openGLSupportAndExtensionsLog);
    }

    /**
    *  Initializes CPU memory.
    */
    @Override
    protected void initializeCPUMemoryImplementation() throws OutOfMemoryError {}

    /**
    *  Initializes GPU memory.
    */
    @Override
    protected void initializeGPUMemoryImplementation() {}

    /**
    *  Performs the GPU Computing calculations.
    */
    @Override
    protected void performGPUComputingCalculationsImplementation() {}

    /**
    *  Retrieves GPU results.
    */
    @Override
    protected void retrieveGPUResultsImplementation() throws OutOfMemoryError {}

    /**
    *  Deletes the OpenGL context for GPU computing.
    */
    @Override
    protected void deleteOpenGLContextForGPUComputing() {}


}