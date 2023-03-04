package edu.sjsu.cs158a;

import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class Wiki
{
    public static final ArrayList<String> childList = new ArrayList<>();
    public static final String wikipediaPrefix = "https://www.wikipedia.com/wiki/";


    public static void main(String[] args) throws IOException
    {
        String webpageContent = webScrape(args[0]);

        ParserDelegator delegator = new ParserDelegator();
        delegator.parse(new StringReader(webpageContent), new MyParser.MyParserCallback(), true);
    }

    /**
     * Attempts to connect to a Wikipedia page and pull
     * all information from the page using Java's URL class.
     * @return String that contains all data from a Wikipedia page.
     * @param webpageName Contains the Wikipedia url portion after the "/wiki/" part. Spaces are underscores.
     */
    public static String webScrape(String webpageName)
    {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

        try
        {
            URL wikipediaURL = new URL(wikipediaPrefix.concat(webpageName));
            InputStream inputStream = wikipediaURL.openStream();

            // Reads 5120 bytes at a time from the page.
            byte[] webpageData = new byte[5120];

            int rc = 0;

            // While there is website data available...
            while ((rc = inputStream.read(webpageData)) > 0)
            {
                byteArrayStream.write(webpageData, 0, rc);
            }

            return byteArrayStream.toString();
        }
        // Case for if the URL is invalid.
        catch (FileNotFoundException e)
        {
            System.out.println("Could not search: " + wikipediaPrefix.concat(webpageName));
            System.exit(2);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Unknown error where no article was able to be retrieved.
        return null;
    }

}
