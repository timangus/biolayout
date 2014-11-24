package org.Kajeka.CoreUI.Services;

import org.Kajeka.Connections.*;
import org.Kajeka.CoreUI.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;

/**
*
* LayoutOnlineServices is the class used to check for updates of the application.
*
* @author Thanos Theo, 2008-2009
* @version 3.0.0.0
*/

public final class LayoutOnlineServices extends HttpConnection
{

    /**
    *  Constant variables used for tracking program usage.
    */
    private static final String PHP_LINK_BUILD_PART = "?build=";
    private static final String PHP_LINK_VERSION_PART = "&version=";

    /**
    *  LayoutFrame reference.
    */
    private LayoutFrame layoutFrame = null;

    /**
    *  The first constructor of the LayoutOnlineServices class (without proxy related settings).
    */
    public LayoutOnlineServices(LayoutFrame layoutFrame)
    {
        super();
        this.layoutFrame = layoutFrame;
    }

    /**
    *  The second constructor of the LayoutOnlineServices class (with proxy related settings).
    */
    public LayoutOnlineServices(Proxy proxy, LayoutFrame layoutFrame)
    {
        super(proxy);
        this.layoutFrame = layoutFrame;
    }

    /**
    *  This method is the main method for checking application usage.
    *  Runs inside its own lightweight thread so as to not stall main program code execution.
    */
    public void checkApplicationUsage()
    {
        Thread runLightWeightThread = new Thread( new Runnable()
        {

            @Override
            public void run()
            {
                sendLinkWithHttpConnection( APPLICATION_USAGE_URL + PHP_LINK_BUILD_PART +
                        ( (DEBUG_BUILD) ? "Debug" : "Release" ) + PHP_LINK_VERSION_PART +
                        (TITLE_VERSION + TITLE_VERSION_NUMBER).replace(" ", ""));
            }


        }, "checkApplicationUsage" );

        runLightWeightThread.setPriority(Thread.NORM_PRIORITY);
        runLightWeightThread.start();
    }
}