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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mailarchive.timeline.TimeLineEvent;
import org.xwiki.contrib.mailarchive.timeline.TimeLineSubEvent;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.XMLWikiPrinter;

import edu.emory.mathcs.util.security.action.GetLongAction;

/**
 * Specialized Writer for TimeLine events.
 * 
 * @version $Id$
 */
@Component
@Named("simile")
public class TimeLineDataWriterSimile extends XMLWikiPrinter implements ITimeLineDataWriter
{

    private static final DateFormat eventsDateFormatter =
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    private static final DateFormat displayDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public TimeLineDataWriterSimile()
    {
        super(new DefaultWikiPrinter());
    }

    /**
     * @param printer
     */
    public TimeLineDataWriterSimile(WikiPrinter printer)
    {
        this();
        this.setWikiPrinter(printer);
    }

    /* (non-Javadoc)
	 * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#setWikiPrinter(org.xwiki.rendering.renderer.printer.WikiPrinter)
	 */
    @Override
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
        if (event.beginDate == null) {
            // skipping invalid event
            return;
        }

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
        // We use color of first mailing-list found for timeline band
        String classname = "";
        if (CollectionUtils.isNotEmpty(event.lists)) {
            classname = event.lists.get(0).replaceAll("\\s", "-");
        }

        String[][] attributes =
            new String[][] { {"start", eventsDateFormatter.format(event.beginDate)},
            {"end", eventsDateFormatter.format(event.endDate)}, {"title", event.title},
            {"icon", (event.icons != null && event.icons.size() > 0 ? event.icons.get(0) : "")},
            {"image", (event.icons != null && event.icons.size() > 0 ? event.icons.get(0) : "")},
            {"classname", classname}, {"durationEvent", "true"}, {"link", event.url}};

        printXMLStartElement("event", attributes);

        // FIXME HTML generation here
        if (CollectionUtils.isNotEmpty(event.lists)) {
            for (String tag : event.lists) {
                printXML("<span class=\"block-" + tag.replaceAll("\\s", "-") + "\">OO</span> " + tag + ' ');
            }
        }
        printXML("<br/><br/> " + event.action + " by " + event.author + "<br/> ");

        // Print extract
        printXML("<br/>");
        printXML("\"" + event.extract + "\"");
        printXML("<br/>");
        printXML("<br/>");
        if (MapUtils.isNotEmpty(event.messages)) {
            for (Entry<Long, TimeLineSubEvent> bubbleInfo : event.messages.entrySet()) {
                print(bubbleInfo.getValue());
            }
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
            new String[][] { {"start", eventsDateFormatter.format(event.beginDate)}, {"title", event.title},
            {"icon", (event.icons != null && event.icons.size() > 0 ? event.icons.get(0) : "")},
            {"image", (event.icons != null && event.icons.size() > 0 ? event.icons.get(0) : "")}, {"link", event.url}};

        printXMLStartElement("event", attributes);

        // FIXME HTML generation here
        if (CollectionUtils.isNotEmpty(event.lists)) {
            for (String tag : event.lists) {
                printXML("<span class=\"block-" + tag.replaceAll("\\s", "-") + "\">OO</span> " + tag + ' ');
            }
        }
        printXML("<br/><br/>" + event.action + " by " + event.author + "<br/>");
        printXML("<br/>");
        printXML("\"" + event.extract + "\"");
        printXML("<br/>");
        printXML("<br/>");

        printXMLEndElement("event");

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#print(org.xwiki.contrib.mailarchive.timeline.internal.TimeLineSubEvent)
     */
    @Override
    public void print(final TimeLineSubEvent bubbleInfo)
    {

        // FIXME HTML Generation ?
        printXML("<a href=\"javascript:centerTimeline('" + bubbleInfo.date + "');\">"
            + displayDateFormatter.format(bubbleInfo.date) + "</a>");
        printXML(" - ");

        printXML("<a href=\"" + bubbleInfo.url + "\">" + bubbleInfo.subject + "</a>");
        printXML(" - ");

        if (bubbleInfo.link != null) {
            printXML("<a href=\"" + bubbleInfo.link + "\">");
        }
        printXML(bubbleInfo.user);
        if (bubbleInfo.link != null) {
            printXML("</a>");
        }
        printXML("<br/>");

        /*
         * <a href="javascript:centerTimeline();"> formatter.format(maildate)</a> - <a href=\"" + +"\">" + subject +
         * "</a> - " + (link != null ? "<a href=\"" + link + "\">" : "") + user + (link != null ? "</a> " : "") +
         * "'<br/>";
         */
    }
}
