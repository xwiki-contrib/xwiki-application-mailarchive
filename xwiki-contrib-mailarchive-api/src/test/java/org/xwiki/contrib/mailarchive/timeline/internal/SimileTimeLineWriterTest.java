package org.xwiki.contrib.mailarchive.timeline.internal;
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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter;
import org.xwiki.contrib.mailarchive.timeline.internal.TimeLineDataWriterSimile;
import org.xwiki.contrib.mailarchive.timeline.internal.TimeLineEvent;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * @version $Id$
 */
public class SimileTimeLineWriterTest
{

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testPrint()
    {
        ITimeLineDataWriter writer = new TimeLineDataWriterSimile();
        WikiPrinter wikiPrinter = new DefaultWikiPrinter();
        writer.setWikiPrinter(wikiPrinter);

        TreeMap<Long, TimeLineEvent> events = new TreeMap<Long, TimeLineEvent>();

        TimeLineEvent event = new TimeLineEvent();
        event.beginDate = new Date();
        event.title = "Testing the Simile library";
        event.author = "Michel Alki";
        event.lists = new ArrayList<String>();
        event.lists.add("XWiki-Devs");
        event.action = "posted";
        events.put(event.beginDate.getTime(), event);

        event = new TimeLineEvent();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime() + 100000000);
        event.beginDate = new Date();
        event.endDate = calendar.getTime();
        event.title = "Testing the Simile long event";
        event.author = "Toto The Hero";
        event.lists = new ArrayList<String>();
        event.lists.add("XWiki-Users");
        event.action = "posted";
        events.put(event.beginDate.getTime()-10000, event);

        event = new TimeLineEvent();
        calendar.setTimeInMillis(new Date().getTime() + 10000000);
        event.beginDate = calendar.getTime();
        calendar.setTimeInMillis(new Date().getTime() + 70000000);
        event.endDate = calendar.getTime();
        event.title = "Another long event";
        event.author = "Michel";
        event.lists = new ArrayList<String>();
        event.lists.add("XWiki-Devs");
        event.action = "posted";
        events.put(event.beginDate.getTime(), event);        
        
        writer.print(events);

        // FIXME assertions
        System.out.println(wikiPrinter.toString());
    }
    
    @Test
    public void testPrintWithEmptyEvent() {
        ITimeLineDataWriter writer = new TimeLineDataWriterSimile();
        WikiPrinter wikiPrinter = new DefaultWikiPrinter();
        writer.setWikiPrinter(wikiPrinter);

        TreeMap<Long, TimeLineEvent> events = new TreeMap<Long, TimeLineEvent>();

        TimeLineEvent event = new TimeLineEvent();
        events.put(new Date().getTime(), event);
        
        event = new TimeLineEvent();
        event.beginDate = new Date();
        events.put(new Date().getTime(), event);

        
        writer.print(events);

        // FIXME assertions
        System.out.println(wikiPrinter.toString());
    }
}
