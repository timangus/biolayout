package org.Kajeka.Licensing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.JOptionPane;
import static org.Kajeka.DebugConsole.ConsoleOutput.println;
import static org.Kajeka.Environment.GlobalEnvironment.DEBUG_BUILD;
import static org.Kajeka.Environment.GlobalEnvironment.PRODUCT_NAME;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class RevocationChecker
{
    private final static String LICENSING_URL = "http://licensing.kajeka.com/";

    class PingLicenseServerTimerTask extends TimerTask
    {
        @Override
        public void run()
        {
            if (revoked())
            {
                JOptionPane.showMessageDialog(null, "Your license has expired. " +
                        PRODUCT_NAME + " will now exit.",
                        "License expired", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
        }
    }

    private final String licensee;
    private final Timer timer;

    public RevocationChecker(String licensee)
    {
        this.licensee = licensee;
        timer = new Timer("License Revocation Timer");
    }

    public boolean revoked()
    {
        try
        {
            HttpPost post = new HttpPost(LICENSING_URL);

            List<NameValuePair> params = new ArrayList<>(1);
            params.add(new BasicNameValuePair("email", licensee));
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            final int TIMEOUT = 5000;
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT).build();
            SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(TIMEOUT).build();
            HttpClient client = HttpClientBuilder.create().
                    setDefaultRequestConfig(requestConfig).
                    setDefaultSocketConfig(socketConfig).build();

            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                InputStream stream = entity.getContent();
                try
                {
                    String s = IOUtils.toString(stream, "UTF-8");
                    return s.startsWith("revoked");
                }
                finally
                {
                    stream.close();
                }
            }
        }
        catch (Exception e)
        {
            if (DEBUG_BUILD)
            {
                println("Exception occurred when checking license\n" + e.getMessage());
            }
        }

        return false;
    }

    public boolean start()
    {
        PingLicenseServerTimerTask pingLicenseServerTimerTask = new PingLicenseServerTimerTask();

        if(!revoked())
        {
            timer.scheduleAtFixedRate(pingLicenseServerTimerTask, 30000, 30000);
            return false;
        }

        return true;
    }

    public void stop()
    {
        timer.cancel();
    }
}
