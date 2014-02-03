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
package org.xwiki.contrib.mailarchive.timeline.internal;

import java.text.DateFormat;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.XMLWikiPrinter;

/**
 * Specialized Writer for TimeLine events.
 * 
 * @version $Id$
 */
@Component
@Named("simile")
public class TimeLineDataWriterSimile extends XMLWikiPrinter implements ITimeLineDataWriter
{

    private static final DateFormat dateFormatter = DateFormat.getDateInstance();

    public TimeLineDataWriterSimile()
    {
        super(null);
    }

    /**
     * @param printer
     */
    public TimeLineDataWriterSimile(WikiPrinter printer)
    {
        super(printer);
    }

    public void setWikiPrinter(final WikiPrinter printer)
    {
        super.setWikiPrinter(printer);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#print(java.util.TreeMap)
     */
    @Override
    public void print(TreeMap<Long, TimeLineEvent> sortedEvents)
    {
        printXMLStartElement("data");
        for (Entry<Long, TimeLineEvent> event : sortedEvents.entrySet()) {
            print(event.getValue());
        }
        printXMLEndElement("data");

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#print(org.xwiki.contrib.mailarchive.timeline.internal.TimeLineEvent)
     */
    @Override
    public void print(TimeLineEvent event)
    {
        if (event.endDate != null) {
            // This is a topic
            printTopic(event);
        } else {
            // This is a special mail
            printMail(event);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#printTopic(org.xwiki.contrib.mailarchive.timeline.internal.TimeLineEvent)
     */
    protected void printTopic(TimeLineEvent event)
    {
        String[][] attributes =
            new String[][] { {"start", dateFormatter.format(event.beginDate)},
            {"end", dateFormatter.format(event.endDate)}, {"title", event.title}, {"icon", event.icons.get(0)},
            {"image", event.icons.get(0)}, {"classname", event.tags}, {"durationEvent", "true"}, {"link", event.url}};

        printXMLStartElement("event", attributes);

        // FIXME HTML generation here
        printXML((!event.tags.equals("") ? "<span class=\"tape-" + event.tags + "\">___</span> " + event.tags + "<br/>"
            : "") + "<br/> " + event.action + " by " + event.author + "<br/> ");

        // Print extract
        for (Entry<Long, TopicEventBubble> bubbleInfo : event.messages.entrySet()) {
            print(bubbleInfo.getValue());
        }

        printXMLEndElement("event");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#printMail(org.xwiki.contrib.mailarchive.timeline.internal.TimeLineEvent)
     */
    protected void printMail(TimeLineEvent event)
    {
        String[][] attributes =
            new String[][] { {"start", dateFormatter.format(event.beginDate)}, {"title", event.title},
            {"icon", event.icons.get(0)}, {"image", event.icons.get(0)}, {"link", event.url}};

        printXMLStartElement("event", attributes);

        // FIXME HTML generation here
        printXML((!event.tags.equals("") ? "<span class=\"tape-" + event.tags + "\">___</span> " + event.tags + "<br/>"
            : "") + "<br/>" + event.action + " by " + event.author + "<br/> " + event.extract);

        printXMLEndElement("event");

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#print(org.xwiki.contrib.mailarchive.timeline.internal.TopicEventBubble)
     */
    @Override
    public void print(final TopicEventBubble bubbleInfo)
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
