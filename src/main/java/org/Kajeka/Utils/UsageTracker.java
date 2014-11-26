package org.Kajeka.Utils;

import org.Kajeka.BuildConfig;
import static org.Kajeka.DebugConsole.ConsoleOutput.println;
import static org.Kajeka.Environment.GlobalEnvironment.DEBUG_BUILD;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;

public class UsageTracker
{
    private final static String UPLOAD_URL = "http://localhost/uploadlog.php";
    private String log;

    public UsageTracker()
    {
        log = BuildConfig.NAME + " log format version 1";
    }

    public void log(String text)
    {
        String build = BuildConfig.VERSION + "(" + BuildConfig.BUILD_TIME + ")";
        long unixTime = System.currentTimeMillis() / 1000L;

        String line = build + "," + unixTime + "," + text;

        log += "\n" + line;
    }

    public void upload()
    {
        try
        {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("log", new StringBody(log, ContentType.TEXT_PLAIN));

            HttpPost post = new HttpPost(UPLOAD_URL);
            post.setEntity(builder.build());

            HttpClient client = HttpClientBuilder.create().build();
            client.execute(post);
        }
        catch (Exception e)
        {
            if (DEBUG_BUILD)
                println("Exception occurred when uploading tracking data\n" + e.getMessage() );
        }
    }
}

/*

<?php
if (!empty($_SERVER['HTTP_CLIENT_IP']))
{
  $ip = $_SERVER['HTTP_CLIENT_IP'];
}
elseif (!empty($_SERVER['HTTP_X_FORWARDED_FOR']))
{
  $ip = $_SERVER['HTTP_X_FORWARDED_FOR'];
}
else
{
  $ip = $_SERVER['REMOTE_ADDR'];
}

$outputFilename = "/var/www/html/uploads/usageLog.csv";

$outputFile = fopen($outputFilename, "a");
flock($outputFile, LOCK_EX);

if($_POST != null)
{
  $csvData = $_POST["log"];

  // Trivial data validity check
  if(strpos($csvData, " log format version ") !== FALSE)
  {
    $lines = explode(PHP_EOL, $csvData);
    $logEntries = array();
    foreach($lines as $line)
    {
      $logEntries[] = str_getcsv($line);
    }

    foreach($logEntries as $logEntry)
    {
      if(count($logEntry) == 0)
        continue;

      array_unshift($logEntry, $ip);
      $firstColumn = true;
      foreach($logEntry as $column)
      {
        if(!$firstColumn)
          fwrite($outputFile, ",");

        fwrite($outputFile, $column);
        $firstColumn = false;
      }

      fwrite($outputFile, "\n");
    }
  }
}

fclose($outputFile)
?>

*/
