package org.Kajeka.Utils;

import java.security.MessageDigest;
import static org.Kajeka.DebugConsole.ConsoleOutput.println;
import static org.Kajeka.Environment.GlobalEnvironment.DEBUG_BUILD;

public class LicenseKeyValidator
{
    private final String emailAddress;

    public LicenseKeyValidator(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public boolean valid(String key)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] b = emailAddress.getBytes("UTF-8");
            byte[] d = md.digest(b);

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < d.length; i += 2)
            {
                int vi = (int)(d[i] & 0xFF);
                int viPlus1 = (int)((d[i] + 1) & 0xFF);
                int value = 65 + ((vi + viPlus1) % 26);
                sb.append(Character.toChars(value));
            }

            String computedKey = sb.toString();

            return key.compareTo(computedKey) == 0;
        }
        catch (Exception e)
        {
            if (DEBUG_BUILD)
            {
                println("Exception occurred when checking key\n" + e.getMessage());
            }
        }

        return false;
    }
}
