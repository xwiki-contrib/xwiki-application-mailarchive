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
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * Specialized Writer for TimeLine events for CHAP Links library.
 * 
 * @version $Id$
 */
@Component
@Named("chaplinks")
public class TimeLineDataWriterChapLinks implements ITimeLineDataWriter
{
    @Inject
    private Logger logger;

    private static final DateFormat dateFormatter = DateFormat.getDateInstance();

    private WikiPrinter wikiPrinter;

    private JSONArray jsonArray;

    public TimeLineDataWriterChapLinks()
    {
    }

    /**
     * @param printer
     */
    public TimeLineDataWriterChapLinks(WikiPrinter printer)
    {
        this();
        this.wikiPrinter = printer;
    }

    @Override
    public void setWikiPrinter(final WikiPrinter printer)
    {
        this.wikiPrinter = printer;
    }

    @Override
    public void print(TreeMap<Long, TimeLineEvent> sortedEvents)
    {
        jsonArray = new JSONArray();
        for (TimeLineEvent event : sortedEvents.values()) {
            print(event);
        }

        wikiPrinter.print(jsonArray.toString(3));
    }

    @Override
    public void print(TimeLineEvent event)
    {
        if (event != null && event.beginDate != null) {

            if (event.endDate != null) {
                // This is a topic
                printTopic(event);
            } else {
                // This is a special mail
                printMail(event);
            }

        } else {
            logger.warn("Skip invalid event, miss begin date for '" + event.title);
        }
    }

    protected void printMail(TimeLineEvent event)
    {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("start", event.beginDate.getTime());
        // FIXME dummy content
        jsonObject.put("content", event.title + " " + event.lists + " " + event.author);

        jsonArray.add(jsonObject);
    }

    protected void printTopic(TimeLineEvent event)
    {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("start", event.beginDate.getTime());
        jsonObject.put("end", event.endDate.getTime());
        // FIXME dummy content
        jsonObject.put("content", event.title + " " + event.lists + " " + event.author);

        jsonArray.add(jsonObject);

    }

    @Override
    // FIXME: move to a 3rd component, in charge of generating topic extract (same HTML generated whatever timeline
    // feed)
    public void print(final TopicEventBubble bubbleInfo)
    {
        // FIXME dummy content
    }
}
