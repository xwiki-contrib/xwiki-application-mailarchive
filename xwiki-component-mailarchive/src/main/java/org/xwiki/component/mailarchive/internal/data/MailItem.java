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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;

import org.xwiki.component.mailarchive.internal.Utils;

/**
 * POJO representing a Mail.
 * 
 * @version $Id$
 */
public class MailItem
{
    private static final int DEFAULT_MAX_TOPICID_SIZE = 255;

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

    public static MailItem fromMessage(Part mail)
    {
        return fromMessage(mail, DEFAULT_MAX_TOPICID_SIZE);
    }

    /**
     * parseMail Parse mail headers to create a MailItem. Decodes localization and date.
     */
    public static MailItem fromMessage(Part mail, int maxTopicIdSize)
    {

        MailItem m = new MailItem();

        String[] headers;

        try {
            String messageId = "";
            headers = mail.getHeader("Message-ID");
            if (headers != null) {
                messageId = MimeUtility.decodeText(headers[0]);
                messageId = cropId(messageId);
            }
            m.setMessageId(messageId);

            String replyToId = "";
            headers = mail.getHeader("In-Reply-To");
            if (headers != null) {
                replyToId = headers[0];
                replyToId = cropId(replyToId);
            }
            m.setReplyToId(replyToId);

            String refs = "";
            headers = mail.getHeader("References");
            if (headers != null) {
                refs = headers[0];
            }
            m.setRefs(refs);

            String subject = "[no subject]";
            headers = mail.getHeader("Subject");
            if (headers != null && !headers[0].isEmpty()) {
                subject =
                    MimeUtility.decodeText(headers[0]).replaceAll("\n", " ").replaceAll("\r", " ")
                        .replaceAll(">", "&gt;").replaceAll("<", "&lt;");
            }
            m.setSubject(subject);

            // If topic is not provided, we use message subject without the beginning junk
            // If topic header is provided but empty, we use a default subject
            // TODO : why did I cut IDs to 30 chars long ? It's usually longer than that and it would fit in a
            // StringProperty ????
            // CAHW2-9EBk1MUoqrC-duqXhth3k9nA-T+7BUQO-qnScNJHvWM4g@mail.gmail.com
            String topic = "[no subject]";
            headers = mail.getHeader("Thread-Topic");
            if (headers != null) {
                if (!headers[0].isEmpty()) {
                    topic = MimeUtility.decodeText(headers[0]).replaceAll("\n", " ").replaceAll("\r", " ");
                }
            } else {
                topic = subject.replaceAll("(?mi)([\\[\\(] *)?(RE|FWD?) *([-:;)\\]][ :;\\])-]*|$)|\\]+ *$", "");
            }
            m.setTopic(topic);

            // Topic Id : if none is provided, we generate a SHA-1 hash of the subject, if we can't we use the message
            // Id
            String topicId = "";
            headers = mail.getHeader("Thread-Index");
            if (headers != null) {
                topicId = MimeUtility.decodeText(headers[0]);
                topicId = cropId(topicId);
            } else {
                try {
                    topicId = Utils.SHA1(topic);
                } catch (NoSuchAlgorithmException e) {
                    topicId = messageId;
                }
            }
            // This is only for compatibility purpose with old version of this app
            // Most of the time, leaving this size to default is ok
            if (topicId.length() >= DEFAULT_MAX_TOPICID_SIZE) {
                topicId = topicId.substring(0, DEFAULT_MAX_TOPICID_SIZE - 1);
            }
            m.setTopicId(topicId);

            String from = "";
            headers = mail.getHeader("From");
            if (headers != null) {
                from = MimeUtility.decodeText(headers[0]);
            }
            from = from.replaceAll("\"", "");
            m.setFrom(from);

            String to = "";
            headers = mail.getHeader("To");
            if (headers != null) {
                to = MimeUtility.decodeText(headers[0]);
            }
            to = to.replaceAll("\"", "");
            m.setTo(to);

            String cc = "";
            headers = mail.getHeader("CC");
            if (headers != null) {
                cc = MimeUtility.decodeText(headers[0]);
            }
            cc = cc.replaceAll("\"", "");
            m.setCc(cc);

            // process the language
            String locLang = "en";
            String locCountry = "US";
            String language;
            headers = mail.getHeader("Content-Language");
            if (headers != null) {
                language = headers[0];
                if (language != null && !language.isEmpty()) {
                    int index = language.indexOf('.');
                    if (index != -1) {
                        locLang = language.substring(0, index - 1);
                        locCountry = language.substring(index);
                    }
                }
            }
            Locale locale = new Locale(locLang, locCountry);
            m.setLocale(locale);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ", locale);

            String date = "";
            Date decodedDate = null;
            headers = mail.getHeader("Date");
            if (headers != null) {
                date = headers[0];
            }
            // Decode the date
            try {
                decodedDate = dateFormatter.parse(date);
            } catch (ParseException pE) {
                decodedDate = new Date();
            }
            m.setDate(date);
            m.setDecodedDate(decodedDate);

            // // @TODO : not generic part
            // boolean isNewsletter = (subject.toUpperCase().contains("COMMUNITY NEWSLETTER"));
            // boolean isProductRelease =
            // ((from.toUpperCase().contains("DONOTREPLY@GEMALTO.COM") || from.toUpperCase().contains("DOWNLOADZONE"))
            // && (subject
            // .toUpperCase().startsWith("DELIVERY OF")));
            // // end of not generic part
            // String type = "Mail";
            // // @TODO : not generic part
            // if (isNewsletter) {
            // type = "Newsletter";
            // }
            // if (isProductRelease) {
            // type = "Product Release";
            // }
            // // end of not generic part
            // m.setType(type);

            boolean firstInTopic = ("".equals(replyToId));
            m.setFirstInTopic(firstInTopic);

            // // @TODO Try to retrieve wiki user
            // // @TODO : here, or after ? (link with ldap and xwiki profiles
            // // options to be checked ...)
            // /*
            // * String userwiki = parseUser(from); if (userwiki == null || userwiki == "") { userwiki = unknownUser; }
            // */
            // m.setWikiuser(null);

            m.setBodypart(mail.getContent());
            m.setContentType(mail.getContentType().toLowerCase());

            String sensitivity = "normal";
            headers = mail.getHeader("Sensitivity");
            if (headers != null && !headers[0].isEmpty()) {
                sensitivity = "normal";
            }
            m.setSensitivity(sensitivity);
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return m;
    }

    /**
     * Extract id from mail address header string.
     * 
     * @param id
     * @return
     */
    protected static String cropId(String id)
    {
        int start = id.indexOf('<');
        int end = id.indexOf('>');
        if (start != -1 && end != -1) {
            return id.substring(start + 1, end);
        } else {
            return id;
        }
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
