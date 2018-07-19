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
package org.xwiki.contrib.mailarchive.timeline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    public List<String> lists = null;

    public String url = null;

    public String action = null;

    public String author = null;

    public String authorAvatar = null;

    public String content = null;

    public String extract = null;

    public Map<Long, TimeLineSubEvent> messages = null;

    public TimeLineEvent() {
        super();
    }
    
    public TimeLineEvent(final TimeLineEvent event)
    {       
        this();
        this.action = event.action;
        this.author = event.author;
        this.authorAvatar = event.authorAvatar;
        this.beginDate = event.beginDate;
        this.content = event.content;
        this.endDate = event.endDate;
        this.extract = event.extract;
        this.icons = event.icons;
        if (event.icons != null) {
            this.icons = new ArrayList<String>();
            this.icons.addAll(event.icons);
        }
        this.lists = event.lists;
        if (event.lists != null) {
            this.lists = new ArrayList<String>();
            this.lists.addAll(event.lists);
        }
        this.messages = event.messages;
        if (event.messages != null) {
            this.messages = new TreeMap<Long, TimeLineSubEvent>();
            this.messages.putAll(event.messages);
        }
        this.title = event.title;
        this.url = event.url;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((authorAvatar == null) ? 0 : authorAvatar.hashCode());
        result = prime * result + ((beginDate == null) ? 0 : beginDate.hashCode());
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + ((extract == null) ? 0 : extract.hashCode());
        result = prime * result + ((icons == null) ? 0 : icons.hashCode());
        result = prime * result + ((lists == null) ? 0 : lists.hashCode());
        result = prime * result + ((messages == null) ? 0 : messages.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeLineEvent other = (TimeLineEvent) obj;
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        if (author == null) {
            if (other.author != null)
                return false;
        } else if (!author.equals(other.author))
            return false;
        if (authorAvatar == null) {
            if (other.authorAvatar != null)
                return false;
        } else if (!authorAvatar.equals(other.authorAvatar))
            return false;
        if (beginDate == null) {
            if (other.beginDate != null)
                return false;
        } else if (!beginDate.equals(other.beginDate))
            return false;
        if (content == null) {
            if (other.content != null)
                return false;
        } else if (!content.equals(other.content))
            return false;
        if (endDate == null) {
            if (other.endDate != null)
                return false;
        } else if (!endDate.equals(other.endDate))
            return false;
        if (extract == null) {
            if (other.extract != null)
                return false;
        } else if (!extract.equals(other.extract))
            return false;
        if (icons == null) {
            if (other.icons != null)
                return false;
        } else if (!icons.equals(other.icons))
            return false;
        if (lists == null) {
            if (other.lists != null)
                return false;
        } else if (!lists.equals(other.lists))
            return false;
        if (messages == null) {
            if (other.messages != null)
                return false;
        } else if (!messages.equals(other.messages))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }
    

    

}
