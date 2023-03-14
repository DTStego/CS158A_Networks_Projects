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

        /**
         * Handle tags. Print out the title of a page by parsing the "title" tag.
         * A "a href" tag indicates links. Grab the link and check for the exit
         * condition ('endPage' in Wiki.java). Otherwise, store the page in the
         * LinkedHashSet in Wiki.java if there are no colons in the link.
         *
         * @param tag The HTML tag that is recognized by the parser.
         * @param a Set of attributes inside the tag, e.g., href, rel, or class.
         * @param pos Position where the tag is located. Can be used to insert new tags/attributes.
         */
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

            /*
             * Continue only if the getAttribute() method returned the text inside the "a href" tag,
             * the text starts with "wiki/", and it doesn't contain any colons (which would invalidate the link).
             *
             * Assignment says to only check for invalidation using colons.
             * Adding more checks increases program runtime dramatically!
             */
            if (s != null && s.startsWith("/wiki") && !s.contains(":"))
            {
                // Check if it's the stop condition (Found the "Geographic_coordinate_system" page).
                if (s.equals("/wiki/".concat(Wiki.endPage)))
                {
                    System.out.println("Found in: " + currentTitle);
                    System.exit(0);
                }

                // Otherwise, add to the end of the child list.
                if (!Wiki.searchingChildren)
                {
                    // Add the part of the link after "/wiki/" to the childList in Wiki.java.
                    Wiki.childList.add(s.substring(6));
                }
            }
        }

        /**
         * Links are handled in the handleStartTag() method. Only titles are handled in this method.
         * If the text that is called upon is in a "title" tag, print out the title
         * if the parser is not in a child link.
         *
         * @param data Text which is stored in a char[] array. Can be converted into a String.
         * @param pos Position where the tag is located. Can be used to insert new text in the page.
         */
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

        /**
         * Change the isInTitle boolean to note that the parser is no longer in a title.
         *
         * @param tag The HTML tag that is recognized by the parser.
         * @param pos Position where the tag is located.
         */
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
