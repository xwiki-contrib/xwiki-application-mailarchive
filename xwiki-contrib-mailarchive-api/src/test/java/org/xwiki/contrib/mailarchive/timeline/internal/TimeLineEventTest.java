package org.xwiki.contrib.mailarchive.timeline.internal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.contrib.mailarchive.timeline.internal.TimeLineEvent;

public class TimeLineEventTest
{

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testConstructorClone() throws CloneNotSupportedException
    {
        TimeLineEvent event = new TimeLineEvent();
        event.action = "action";
        event.author= "author";
        event.authorAvatar = "authorAvatar";
        event.beginDate = new Date();
        event.content = "content";
        event.endDate = new Date();
        event.extract = "extract";
        event.icons = new ArrayList<String>();
        event.icons.add("icon1");
        event.lists = new ArrayList<String>();
        event.lists.add("list1");
        event.lists.add("list2");
        event.messages= new TreeMap<Long, TopicEventBubble>();
        event.messages.put(10L, new TopicEventBubble());
        TimeLineEvent clone = new TimeLineEvent(event);
        assertEquals(event, clone);
    }    
    
    @Test
    public void testConstructorCloneWithEmptyEvent() throws CloneNotSupportedException
    {
        TimeLineEvent event = new TimeLineEvent();
        TimeLineEvent clone = new TimeLineEvent(event);
        assertEquals(event, clone);
    }

}
