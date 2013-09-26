/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.mailarchive.internal.timeline;

import java.text.DateFormat;

import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;

/**
 * @version $Id$
 */
public class TimeLineBubbleWriter extends XHTMLWikiPrinter
{
    private static final DateFormat dateFormatter = DateFormat.getDateInstance();

    /**
     * @param printer
     */
    public TimeLineBubbleWriter(WikiPrinter printer)
    {
        super(printer);
    }

    public void printXML(final TopicEventBubble bubbleInfo)
    {

        String[][] attributes = new String[][] {{"href", "javascript:centerTimeline(" + bubbleInfo.date + ");\">"}};

        printXMLStartElement("a", attributes);
        printXML(dateFormatter.format(bubbleInfo.date));
        printXMLEndElement("a");

        printXML(" - ");

        attributes = new String[][] {{"href", bubbleInfo.url}};
        printXMLStartElement("a", attributes);
        printXML(bubbleInfo.subject);
        printXMLEndElement("a");

        printXML(" - ");
        if (bubbleInfo.link != null) {
            attributes = new String[][] {{"href", bubbleInfo.link}};
            printXMLStartElement("a", attributes);
        }
        printXML(bubbleInfo.user);
        if (bubbleInfo.link != null) {
            printXMLEndElement("a");
        }

        printXMLElement("br");

        /*
         * <a href="javascript:centerTimeline();"> formatter.format(maildate)</a> - <a href=\"" + +"\">" + subject +
         * "</a> - " + (link != null ? "<a href=\"" + link + "\">" : "") + user + (link != null ? "</a> " : "") +
         * "'<br/>";
         */
    }

}
