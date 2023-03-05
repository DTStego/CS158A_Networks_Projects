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

            if (tag == HTML.Tag.A)
            {
                String s = (String) a.getAttribute(HTML.Attribute.HREF);
                if (s != null && s.startsWith("/wiki/"))
                {
                    // Check if it's the stop condition (Found the "Geographic_coordinate_system" page).
                    if (s.equals("/wiki/".concat(Wiki.endPage)))
                    {
                        Wiki.isFound = true;
                        System.out.println("Found in: " + currentTitle);
                        System.exit(0);
                    }
                    // Add to the end of the child list.
                    else
                    {
                        // No easy way to invalidate a link. Every link takes time to check so this is a very
                        // archaic way of checking if it's a valid link based on output from wiki links.
                        if (!s.contains(":") && !s.contains("%") && !s.contains(".") && !s.contains("#"))
                        {
                            // Add the part of the link after "/wiki/" to the childList in Wiki.java.
                            Wiki.childList.add(s.substring(6));
                        }
                    }
                }
            }
        }

        @Override
        public void handleText(char[] data, int pos)
        {
            if (isInTitle)
            {
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
