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
package org.xwiki.component.mailarchive.internal.threads;

import java.util.ArrayList;
import java.util.Date;

/**
 * @version $Id$
 */
public class ThreadableMessage
{
    public String topicsubject;

    public String subject;

    public String id;

    public ArrayList<String> references;

    public String wikidoc;

    public boolean isReply;

    public Date date;

    public ThreadableMessage child;

    public ThreadableMessage next;

    public String getTopicsubject()
    {
        return topicsubject;
    }

    public void setTopicsubject(String topicsubject)
    {
        this.topicsubject = topicsubject;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public ArrayList<String> getReferences()
    {
        return references;
    }

    public void setReferences(ArrayList<String> references)
    {
        this.references = references;
    }

    public String getWikidoc()
    {
        return wikidoc;
    }

    public void setWikidoc(String wikidoc)
    {
        this.wikidoc = wikidoc;
    }

    public boolean isReply()
    {
        return isReply;
    }

    public void setReply(boolean isReply)
    {
        this.isReply = isReply;
    }

    public ThreadableMessage getChild()
    {
        return child;
    }

    public void setChild(ThreadableMessage child)
    {
        this.child = child;
    }

    public ThreadableMessage getNext()
    {
        return next;
    }

    public void setNext(ThreadableMessage next)
    {
        this.next = next;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public ArrayList<ThreadMessageBean> flatten()
    {
        return flatten(0);
    }

    public ArrayList<ThreadMessageBean> flatten(int i)
    {
        ArrayList<ThreadMessageBean> flatMap = new ArrayList<ThreadMessageBean>();

        ThreadMessageBean bean = new ThreadMessageBean();
        bean.setDate(date);
        bean.setSubject(subject);
        bean.setPage(wikidoc);
        bean.setUser(""); // TODO set user in thread
        bean.setLevel(i);
        flatMap.add(bean);

        if (child != null) {
            flatMap.addAll(child.flatten(i + 1));
        }

        if (next != null) {
            flatMap.addAll(next.flatten(i));
            if (i == 0) {
                System.out.println("flatten : root node should not have a next !");
            }
        }

        return flatMap;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        ThreadableMessage other = (ThreadableMessage) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ThreadableMessage [topicsubject=").append(topicsubject).append(", subject=").append(subject)
            .append(", id=").append(id).append(", references=").append(references).append(", wikidoc=").append(wikidoc)
            .append(", isReply=").append(isReply).append(", date=").append(date).append(", child=").append(child)
            .append(", next=").append(next).append("]");
        return builder.toString();
    }

}
