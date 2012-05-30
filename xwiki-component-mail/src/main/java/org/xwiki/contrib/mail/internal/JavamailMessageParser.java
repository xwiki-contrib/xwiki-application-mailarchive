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
package org.xwiki.contrib.mail.internal;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mail.Utils;

/**
 * @version $Id$
 */
public class JavamailMessageParser implements IMessageParser<Part>
{
    private Logger logger;

    public JavamailMessageParser(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.internal.IMessageParser#parseHeaders(java.lang.Object)
     */
    @Override
    public MailItem parseHeaders(Part mail)
    {
        MailItem m = new MailItem();

        String[] headers;

        try {
            String messageId = "";
            headers = mail.getHeader("Message-ID");
            if (headers != null && headers.length > 0) {
                messageId = MimeUtility.decodeText(headers[0]);
                messageId = Utils.cropId(messageId);
            }
            m.setMessageId(messageId);

            String replyToId = "";
            headers = mail.getHeader("In-Reply-To");
            if (headers != null) {
                replyToId = headers[0];
                replyToId = Utils.cropId(replyToId);
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
                    topic = Utils.removeCRLF(MimeUtility.decodeText(headers[0]));
                }
            } else {
                topic = subject.replaceAll("(?mi)([\\[\\(] *)?(RE|FWD?) *([-:;)\\]][ :;\\])-]*|$)|\\]+ *$", "");
            }
            m.setTopic(topic);

            // Topic Id : if none is provided, we use the message-id as topic id
            String topicId = "";
            headers = mail.getHeader("Thread-Index");
            if (headers != null && headers.length > 0) {
                topicId = MimeUtility.decodeText(headers[0]);
                topicId = Utils.cropId(topicId);
            } else {
                topicId = messageId;
            }
            // This is only for compatibility purpose with old version of this app
            // Most of the time, leaving this size to default is ok
            // TODO move this to the mail archive (after parsing)
            /*
             * if (topicId.length() >= MAIL_HEADER_MAX_LENGTH) { topicId = topicId.substring(0, MAIL_HEADER_MAX_LENGTH -
             * 1); }
             */
            m.setTopicId(topicId);

            String from = "";
            headers = mail.getHeader("From");
            if (headers != null) {
                from = MimeUtility.decodeText(headers[0]);
            }
            from = from.replaceAll("\"", "").replaceAll("[\n\r]", "");
            m.setFrom(from);

            String sender = "";
            headers = mail.getHeader("Sender");
            if (headers != null && headers.length > 0) {
                sender = MimeUtility.decodeText(headers[0]);
            }
            sender = sender.replaceAll("\"", "").replaceAll("[\n\r]", "");
            m.setSender(sender);

            String to = "";
            headers = mail.getHeader("To");
            if (headers != null) {
                to = MimeUtility.decodeText(headers[0]);
            }
            to = to.replaceAll("\"", "").replaceAll("[\n\r]", "");
            m.setTo(to);

            String cc = "";
            headers = mail.getHeader("CC");
            if (headers != null) {
                cc = MimeUtility.decodeText(headers[0]);
            }
            cc = cc.replaceAll("\"", "").replaceAll("[\n\r]", "");
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

            String date = "";
            Date decodedDate = null;
            headers = mail.getHeader("Date");
            if (headers != null) {
                date = headers[0];
            }
            // Decode the date
            try {
                logger.error("Parsing date [" + date + "] with Javamail MailDateFormat");
                decodedDate = new MailDateFormat().parse(date);
            } catch (ParseException e) {
                logger.error("Could not parse date header " + e.getLocalizedMessage());
                decodedDate = null;
            }
            if (decodedDate == null) {
                try {
                    logger.error("Parsing date [" + date + "] with GMail parser");
                    decodedDate = new GMailMailDateFormat().parse(date);
                } catch (ParseException e) {
                    logger.error("Could not parse date header with GMail parser " + e.getLocalizedMessage());
                    decodedDate = new Date();
                    logger.debug("Using 'now' as date");
                }
            }
            m.setDate(decodedDate);

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
            m.setSensitivity(sensitivity.toLowerCase());

            String importance = "normal";
            headers = mail.getHeader("Importance");
            if (importance == null || importance == "") {
                importance = "normal";
            }
            m.setImportance(importance.toLowerCase());
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return m;
    }

}
