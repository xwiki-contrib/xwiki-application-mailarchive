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
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mail.Utils;

/**
 * @version $Id$
 */
public class JavamailMessageParser implements IMessageParser<Part>
{
    public static final String DEFAULT_SUBJECT = "[no subject]";

    private Logger logger;

    public JavamailMessageParser(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @throws IOException
     * @see org.xwiki.contrib.mail.internal.IMessageParser#parseHeaders(java.lang.Object)
     */
    @Override
    public MailItem parseHeaders(Part mail) throws MessagingException, IOException
    {
        MailItem m = new MailItem();

        String[] headers;

        String value = null;

        value = extractSingleHeader(mail, "Message-ID");
        value = Utils.cropId(value);
        m.setMessageId(value);

        value = extractSingleHeader(mail, "In-Reply-To");
        value = Utils.cropId(value);
        m.setReplyToId(value);

        value = extractSingleHeader(mail, "References");
        m.setRefs(value);

        value = extractSingleHeader(mail, "Subject");
        if (StringUtils.isBlank(value)) {
            value = DEFAULT_SUBJECT;
        }
        value = value.replaceAll("[\n\r]", "").replaceAll(">", "&gt;").replaceAll("<", "&lt;");
        m.setSubject(value);

        // If topic is not provided, we use message subject without the beginning junk
        value = extractSingleHeader(mail, "Thread-Topic");
        if (StringUtils.isBlank(value)) {
            value = m.getSubject().replaceAll("(?mi)([\\[\\(] *)?(RE|FWD?) *([-:;)\\]][ :;\\])-]*|$)|\\]+ *$", "");
        } else {
            value = Utils.removeCRLF(value);
        }
        m.setTopic(value);

        // Topic Id : if none is provided, we use the message-id as topic id
        value = extractSingleHeader(mail, "Thread-Index");
        if (!StringUtils.isBlank(value)) {
            value = Utils.cropId(value);
        }
        m.setTopicId(value);

        value = extractSingleHeader(mail, "From");
        value = value.replaceAll("\"", "").replaceAll("[\n\r]", "");
        m.setFrom(value);

        value = extractSingleHeader(mail, "Sender");
        value = value.replaceAll("\"", "").replaceAll("[\n\r]", "");
        m.setSender(value);

        value = extractSingleHeader(mail, "To");
        value = value.replaceAll("\"", "").replaceAll("[\n\r]", "");
        m.setTo(value);

        value = extractSingleHeader(mail, "CC");
        value = value.replaceAll("\"", "").replaceAll("[\n\r]", "");
        m.setCc(value);

        // process the locale, if any provided
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

        boolean firstInTopic = ("".equals(m.getReplyToId()));
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

        return m;
    }

    /**
     * Gets a unique value from a mail header. If header is present more than once, only first value is returned. Value
     * is Mime decoded, if needed. If header is not found, an empty string is returned.
     * 
     * @param part
     * @param name Header identifier.
     * @return First header value, or empty string if not found.
     * @throws MessagingException
     */
    public static String extractSingleHeader(final Part part, final String name) throws MessagingException
    {
        String[] values = part.getHeader(name);
        if (values != null && values.length > 0) {
            try {
                return MimeUtility.decodeText(values[0]);
            } catch (UnsupportedEncodingException e) {
                return values[0];
            }
        }
        return "";
    }

}
