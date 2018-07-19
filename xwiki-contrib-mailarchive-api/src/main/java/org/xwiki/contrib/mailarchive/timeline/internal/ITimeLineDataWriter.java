package org.xwiki.contrib.mailarchive.timeline.internal;

import org.xwiki.rendering.renderer.printer.WikiPrinter;

import java.util.TreeMap;

import org.xwiki.contrib.mailarchive.timeline.TimeLineEvent;
import org.xwiki.contrib.mailarchive.timeline.TimeLineSubEvent;

public interface ITimeLineDataWriter {

	void setWikiPrinter(WikiPrinter printer);

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#print(java.util.TreeMap)
	 */
	void print(TreeMap<Long, TimeLineEvent> sortedEvents);

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#print(org.xwiki.contrib.mailarchive.timeline.internal.TimeLineEvent)
	 */
	void print(TimeLineEvent event);

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.xwiki.contrib.mailarchive.timeline.internal.ITimeLineDataWriter#print(org.xwiki.contrib.mailarchive.timeline.internal.TimeLineSubEvent)
	 */
	void print(TimeLineSubEvent bubbleInfo);

}