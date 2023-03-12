package edu.sjsu.cs158a;

import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;

public class Wiki
{
    // HashSet containing child links (only one layer) that are in the first page.
    public static final LinkedHashSet<String> childList = new LinkedHashSet<>();

    // If the specific page for "Geographic_coordinate_system" is found, end the program using this boolean.
    public static boolean isFound = false;
    public static boolean searchingChildren = false;
    public static final String wikipediaPrefix = "https://en.wikipedia.org/wiki/";
    public static String endPage = "Geographic_coordinate_system";


    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.err.println("Please input a string containing the portion of the Wikipedia link after /wiki/");
            System.exit(2);
        }

        if (args.length > 1)
        {
            System.err.println("Too Many Arguments!");
            System.exit(2);
        }

        // Case for if the argument matches the endPage.
        if (args[0].equals(endPage))
        {
            endPage = endPage.replaceAll("_", " ");
            System.out.println("Searching: " + endPage + " - Wikipedia");
            System.out.println("Found in: " + endPage + " - Wikipedia");
            System.exit(0);
        }

        // Scrape the first page.
        String webpageContent = webScrape(args[0]);

        System.out.println("Searching: " + args[0].replaceAll("_", " ") + " - Wikipedia");

        ParserDelegator delegator = new ParserDelegator();

        // Parse the information on the page, looking for content based on MyParser.java.
        // The variable "isFound" will change if the parser finds "endPage". Otherwise, the "childList" is filled.
        delegator.parse(new StringReader(webpageContent), new MyParser.MyParserCallback(), true);

        // If "endPage" was not found in the first page, check the pages in "childList"
        System.out.println("Checking children:");

        searchingChildren = true;

        for (String s : childList)
        {
            // Scrape the page and parse.
            webpageContent = webScrape(s);
            delegator.parse(new StringReader(webpageContent), new MyParser.MyParserCallback(), true);
        }

        System.out.println("Not found");
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

            // Reads 1024 bytes at a time from the page.
            byte[] webpageData = new byte[1024];

            int rc;

            // While there is website data available...
            while ((rc = inputStream.read(webpageData)) > 0)
            {
                byteArrayStream.write(webpageData, 0, rc);
            }

            return byteArrayStream.toString();
        }
        // Case for if the URL is invalid.
        catch (FileNotFoundException | UnknownHostException ex)
        {
            System.out.println("Could not search: " + wikipediaPrefix.concat(webpageName));
            System.exit(1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Unknown error where no article was able to be retrieved.
        return null;
    }
}
