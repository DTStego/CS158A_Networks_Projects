package edu.sjsu.cs158a;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.util.ArrayList;

public class MyParser
{
    static class MyParserCallback extends HTMLEditorKit.ParserCallback
    {
        static boolean isInTitle = false;
        static boolean isInLink = false;

        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
        {
            if (t == HTML.Tag.TITLE)
            {
                isInTitle = true;
            }

            if (t == HTML.Tag.A)
            {
                if (a.getAttribute(HTML.Attribute.HREF) != null)
                    isInLink = true;
            }
        }
    }
}
