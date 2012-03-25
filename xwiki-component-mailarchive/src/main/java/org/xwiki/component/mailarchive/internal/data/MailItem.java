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
package org.xwiki.component.mailarchive.internal.data;

import java.util.Date;
import java.util.Locale;

/**
 * POJO representing a Mail.
 * 
 * @version $Id$
 */
public class MailItem
{
    private String date;

    private String subject;

    private String topic;

    private String from;

    private String to;

    private String cc;

    private String topicId;

    private String messageId;

    private String replyToId;

    private String refs;

    private Locale locale;

    private Object bodypart;

    private String contentType;

    private String sensitivity;

    private Date decodedDate;

    private String type;

    private String wikiuser;

    private boolean isFirstInTopic;

    public MailItem()
    {
    }

    /**
     * @return the date
     */
    public String getDate()
    {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date)
    {
        this.date = date;
    }

    /**
     * @return the subject
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    /**
     * @return the topic
     */
    public String getTopic()
    {
        return topic;
    }

    /**
     * @param topic the topic to set
     */
    public void setTopic(String topic)
    {
        this.topic = topic;
    }

    /**
     * @return the from
     */
    public String getFrom()
    {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(String from)
    {
        this.from = from;
    }

    /**
     * @return the to
     */
    public String getTo()
    {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(String to)
    {
        this.to = to;
    }

    /**
     * @return the cc
     */
    public String getCc()
    {
        return cc;
    }

    /**
     * @param cc the cc to set
     */
    public void setCc(String cc)
    {
        this.cc = cc;
    }

    /**
     * @return the topicId
     */
    public String getTopicId()
    {
        return topicId;
    }

    /**
     * @param topicId the topicId to set
     */
    public void setTopicId(String topicId)
    {
        this.topicId = topicId;
    }

    /**
     * @return the messageId
     */
    public String getMessageId()
    {
        return messageId;
    }

    /**
     * @param messageId the messageId to set
     */
    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    /**
     * @return the replyToId
     */
    public String getReplyToId()
    {
        return replyToId;
    }

    /**
     * @param replyToId the replyToId to set
     */
    public void setReplyToId(String replyToId)
    {
        this.replyToId = replyToId;
    }

    /**
     * @return the refs
     */
    public String getRefs()
    {
        return refs;
    }

    /**
     * @param refs the refs to set
     */
    public void setRefs(String refs)
    {
        this.refs = refs;
    }

    /**
     * @return the locale
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * @return the bodypart
     */
    public Object getBodypart()
    {
        return bodypart;
    }

    /**
     * @param bodypart the bodypart to set
     */
    public void setBodypart(Object bodypart)
    {
        this.bodypart = bodypart;
    }

    /**
     * @return the contentType
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    /**
     * @return the sensitivity
     */
    public String getSensitivity()
    {
        return sensitivity;
    }

    /**
     * @param sensitivity the sensitivity to set
     */
    public void setSensitivity(String sensitivity)
    {
        this.sensitivity = sensitivity;
    }

    /**
     * @return the decodedDate
     */
    public Date getDecodedDate()
    {
        return decodedDate;
    }

    /**
     * @param decodedDate the decodedDate to set
     */
    public void setDecodedDate(Date decodedDate)
    {
        this.decodedDate = decodedDate;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the wikiuser
     */
    public String getWikiuser()
    {
        return wikiuser;
    }

    /**
     * @param wikiuser the wikiuser to set
     */
    public void setWikiuser(String wikiuser)
    {
        this.wikiuser = wikiuser;
    }

    /**
     * @return the isFirstInTopic
     */
    public boolean isFirstInTopic()
    {
        return isFirstInTopic;
    }

    /**
     * @param isFirstInTopic the isFirstInTopic to set
     */
    public void setFirstInTopic(boolean isFirstInTopic)
    {
        this.isFirstInTopic = isFirstInTopic;
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();

        result.append("MailItem [\n").append("\t  date:'").append(date).append("'\n").append("\t, subject:'")
            .append(subject).append("'\n").append("\t, topic:'").append(topic).append("'\n").append("\t, from:'")
            .append(from).append("'\n").append("\t, to:'").append(to).append("'\n").append("\t, cc:'").append(cc)
            .append("'\n").append("\t, topicId:'").append(topicId).append("'\n").append("\t, messageId:'")
            .append(messageId).append("'\n").append("\t, replyToId:'").append(replyToId).append("'\n")
            .append("\t, refs:'").append(refs).append("'\n").append("\t, contentType:'").append(contentType)
            .append("'\n").append("\t, sensitivity:'").append(sensitivity).append("'\n").append("\t, locale:'")
            .append(locale).append("'\n").append("\t, type:'").append(type).append("'\n").append("\t, wikiuser:'")
            .append(wikiuser).append("'\n").append("\t, isFirstInTopic:'").append(isFirstInTopic).append("'\n")
            .append("]");

        return result.toString();
    }

}
