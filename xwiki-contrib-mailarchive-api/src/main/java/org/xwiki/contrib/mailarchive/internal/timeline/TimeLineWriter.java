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

import java.util.Map.Entry;
import java.util.TreeMap;

import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.XMLWikiPrinter;

/**
 * Specialized Writer for TimeLine events.
 * 
 * @version $Id$
 */
public class TimeLineWriter extends XMLWikiPrinter
{

    /**
     * @param printer
     */
    public TimeLineWriter(WikiPrinter printer)
    {
        super(printer);
    }

    public void printXML(TreeMap<Long, TimeLineEvent> sortedEvents)
    {
        printXMLStartElement("data");
        for (Entry<Long, TimeLineEvent> event : sortedEvents.entrySet()) {
            printXML(event.getValue());
        }
        printXMLEndElement("data");

    }

    public void printXML(TimeLineEvent event)
    {
        if (event.endDate != null) {
            // This is a topic
            printXMLTopicEvent(event);
        } else {
            // This is a special mail
            printXMLMailEvent(event);
        }
    }

    public void printXMLTopicEvent(TimeLineEvent event)
    {
        String[][] attributes =
            new String[][] { {"start", event.beginDate}, {"end", event.endDate}, {"title", event.title},
            {"icon", event.icon}, {"image", event.icon}, {"classname", event.tags}, {"durationEvent", "true"},
            {"link", event.url}};

        printXMLStartElement("event", attributes);

        // FIXME HTML generation here
        printXML((!event.tags.equals("") ? "<span class=\"tape-" + event.tags + "\">___</span> " + event.tags + "<br/>"
            : "") + "<br/> " + event.action + " by " + event.author + "<br/> " + event.extract);

        printXMLEndElement("event");
    }

    public void printXMLMailEvent(TimeLineEvent event)
    {
        String[][] attributes =
            new String[][] { {"start", event.beginDate}, {"title", event.title}, {"icon", event.icon},
            {"image", event.icon}, {"link", event.url}};

        printXMLStartElement("event", attributes);

        // FIXME HTML generation here
        printXML((!event.tags.equals("") ? "<span class=\"tape-" + event.tags + "\">___</span> " + event.tags + "<br/>"
            : "") + "<br/>" + event.action + " by " + event.author + "<br/> " + event.extract);

        printXMLEndElement("event");

    }
}
