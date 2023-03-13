package edu.sjsu.cs158a;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class MyParser
{
    static class MyParserCallback extends HTMLEditorKit.ParserCallback
    {
        static boolean isInTitle = false;
        static String currentTitle;

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos)
        {
            if (tag == HTML.Tag.TITLE)
            {
                isInTitle = true;
            }

            if (tag != HTML.Tag.A)
            {
                return;
            }

            // Retrieve the text inside the <a href> tag.
            String s = (String) a.getAttribute(HTML.Attribute.HREF);

            if (s != null && s.startsWith("/wiki") && !s.contains(":"))
            {
                // Check if it's the stop condition (Found the "Geographic_coordinate_system" page).
                if (s.equals("/wiki/".concat(Wiki.endPage)))
                {
                    System.out.println("Found in: " + currentTitle);
                    System.exit(0);
                }
                // Add to the end of the child list.

                // Assignment says you only need to check for ':' for invalid links.
                // Adding other elements increases runtime dramatically.
                if (!Wiki.searchingChildren)
                {
                    // Add the part of the link after "/wiki/" to the childList in Wiki.java.
                    Wiki.childList.add(s.substring(6));
                }
            }
        }

        @Override
        public void handleText(char[] data, int pos)
        {
            if (isInTitle)
            {
                // Only print out Searching: [Title] for the parent!
                if (!Wiki.searchingChildren)
                    System.out.println("Searching: " + new String(data));

                currentTitle = new String(data);
            }
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int pos)
        {
            if (tag == HTML.Tag.TITLE)
            {
                isInTitle = false;
            }
        }
    }
}
