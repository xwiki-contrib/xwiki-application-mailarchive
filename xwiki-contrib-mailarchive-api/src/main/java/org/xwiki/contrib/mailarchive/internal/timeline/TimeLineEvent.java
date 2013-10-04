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

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Minimalistic POJO to store a Topic event to show in the timeline.
 * 
 * @version $Id$
 */
public class TimeLineEvent
{
    public Date beginDate = null;

    public Date endDate = null;

    public String title = null;

    public List<String> icons = null;

    public String tags = null;

    public String url = null;

    public String action = null;

    public String author = null;

    public String authorAvatar = null;

    public String content = null;

    public String extract = null;

    public Map<Long, TopicEventBubble> messages = null;

}
