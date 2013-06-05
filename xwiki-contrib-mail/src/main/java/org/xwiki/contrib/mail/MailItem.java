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
package org.xwiki.contrib.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.mail.Message;

import org.xwiki.text.XWikiToStringBuilder;

/**
 * POJO representing a Mail.<br/>
 * Most attributes relate easily to their corresponding mail header, except when specified.
 * Surrounding '<' and '>' are stripped from ID headers, except References that is "as is".
 * <ul>
 * <li>messageId : if not provided (that should never occur), it is generated</li>
 * <li>replyToId : is the "In-reply-to" header</li>
 * <li>sensitivity : is set to "normal" if not provided.</li>
 * <li>importance : is set to "normal" if not provided.</li>
 * </ul><br/>
 * The following fields are computed (and stored) but do not directly relate to a mail header :
 * <ul>
 * <li>decodedDate : is the "date" string parsed to a Date object</li>
 * <li>isFirstInTopic : is true if message is supposedly the first for this topic, meaning it has no value for in-reply-to</li>
 * <li>types</li>
 * <li>wikiUser</li>
 * </ul>
 * 
 * @version $Id$
 */
public class MailItem
{
    public static final int MAIL_HEADER_MAX_LENGTH = 255;

    // Date
    private Date date;

    // Subject
    private String subject;

    private String topic;

    private String from;

    private String to;

    private String cc;

    private String sender;

    private String topicId;

    private String messageId;

    private String replyToId;

    private String refs;

    private Locale locale;

    private Object bodypart;

    private String contentType;

    private String sensitivity;

    private String builtinType;

    private ArrayList<String> types = new ArrayList<String>();

    private String wikiuser;

    private boolean isFirstInTopic;

    private String importance;

    private boolean attached = false;

    private Message originalMessage;

    public MailItem()
    {
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
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

    public String getSender()
    {
        return sender;
    }

    public void setSender(String sender)
    {
        this.sender = sender;
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
     * @return the builtinType
     */
    public String getBuiltinType()
    {
        return builtinType;
    }

    /**
     * @param builtinType the builtinType to set
     */
    public void setBuiltinType(final String builtinType)
    {
        this.builtinType = builtinType;
    }

    /**
     * @return the types
     */
    public ArrayList<String> getTypes()
    {
        return types;
    }

    /**
     * @param types the types to set
     */
    public void addType(String type)
    {
        this.types.add(type);
    }

    /**
     * @param type
     */
    public void removeType(String type)
    {
        this.types.remove(type);
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

    public String getImportance()
    {
        return importance;
    }

    public void setImportance(String importance)
    {
        this.importance = importance;
    }

    /**
     * @return the attached
     */
    public boolean isAttached()
    {
        return attached;
    }

    /**
     * @param attached the attached to set
     */
    public void setAttached(boolean attached)
    {
        this.attached = attached;
    }

    /**
     * @param isFirstInTopic the isFirstInTopic to set
     */
    public void setFirstInTopic(boolean isFirstInTopic)
    {
        this.isFirstInTopic = isFirstInTopic;
    }

    public Message getOriginalMessage()
    {
        return originalMessage;
    }

    public void setOriginalMessage(final Message originalMessage)
    {
        this.originalMessage = originalMessage;
    }

    public HashMap<String, Object> asMap()
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("Date", this.date);
        map.put("From", this.from);
        map.put("To", this.to);
        map.put("Cc", this.cc);
        map.put("Message-ID", this.messageId);
        map.put("In-Reply-To", this.replyToId);
        map.put("References", this.refs);
        map.put("Subject", this.subject);
        map.put("Sender", this.sender);
        // TODO Keywords field

        return map;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        XWikiToStringBuilder builder = new XWikiToStringBuilder(this);
        builder.append("date", date);
        builder.append("subject", subject);
        builder.append("topic", topic);
        builder.append("from", from);
        builder.append("to", to);
        builder.append("cc", cc);
        builder.append("sender", sender);
        builder.append("topicId", topicId);
        builder.append("messageId", messageId);
        builder.append("replyToId", replyToId);
        builder.append("refs", refs);
        builder.append("locale", locale);
        builder.append("bodypart", bodypart != null ? bodypart.getClass() : "");
        builder.append("contentType", contentType);
        builder.append("sensitivity", sensitivity);
        builder.append("builtinType", builtinType);
        builder.append("types", types);
        builder.append("wikiuser", wikiuser);
        builder.append("isFirstInTopic", isFirstInTopic);
        builder.append("importance", importance);
        builder.append("attached", attached);
        builder.append("originalMessage", originalMessage != null ? originalMessage.getClass() : "");
        return builder.toString();
    }

}
