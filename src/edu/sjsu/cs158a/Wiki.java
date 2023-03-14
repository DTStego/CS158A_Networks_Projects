package edu.sjsu.cs158a;

import javax.swing.text.html.parser.ParserDelegator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedHashSet;

public class Wiki
{
    // LinkedHashSet containing child links (only one layer) that are in the first page. Need to retain order.
    public static final LinkedHashSet<String> childList = new LinkedHashSet<>();

    public static boolean searchingChildren = false;
    public static final String wikipediaPrefix = "https://en.wikipedia.org/wiki/";
    public static String endPage = "Geographic_coordinate_system";

    /**
     * Program pulls a Wikipedia page's source code and scrapes the page to look for the page:
     * "Geographic_coordinate_system". It will be in a <a href></a> tag which MyParser can parse from
     * the HTML source code. If the link isn't found in the page (from args), loop through all the links
     * inside the first page and check if the exit condition is in one of those child pages. You do not
     * need to search the links in the child pages (only one level of search).
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.err.println("Please input a string containing the portion of the Wikipedia link after /wiki/");
            System.exit(0);
        }

        if (args.length > 1)
        {
            System.err.println("Too Many Arguments!");
            System.exit(0);
        }

        String webpageContent = webScrape(args[0]);

        ParserDelegator delegator = new ParserDelegator();

        // Parse the information on the page, looking for content based on MyParser.java.
        // If the page isn't found, fill the childList with links from the initial page.
        // parse() calls implemented methods in MyParser based on tags and program ends on exit condition.
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
     * @return String that contains the HTML source code from a Wikipedia page.
     * @param webpageName Contains the Wikipedia url portion after the "/wiki/" part. Spaces are underscores.
     */
    public static String webScrape(String webpageName)
    {
        // Should perhaps switch to an InputStreamReader() but the ByteArrayOutputStream still works.
        // String concatenation causes program slowdown due to the size of a page.
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

        try
        {
            URL wikipediaURL = new URL(wikipediaPrefix.concat(webpageName));
            InputStream inputStream = wikipediaURL.openStream();

            // Reads 1024 bytes at a time from the page.
            byte[] webpageData = new byte[1024];

            // InputStream.read() returns the size of the packet read.
            int packetSize;

            // While there is website data available...
            while ((packetSize = inputStream.read(webpageData)) > 0)
            {
                byteArrayStream.write(webpageData, 0, packetSize);
            }

            // Return the entire page's HTML source code as a string for parsing.
            return byteArrayStream.toString();
        }
        // Case for if the URL is invalid.
        catch (IOException ex)
        {
            System.out.println("Could not search: " + wikipediaPrefix.concat(webpageName));
            System.exit(1);
        }

        // Unknown error where no article was able to be retrieved.
        throw new UnknownError("Random Error Reached!");
    }
}
