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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.contrib.mail.MailContent;
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mail.Utils;

import com.xpn.xwiki.doc.XWikiAttachment;

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

    /**
     * Extracts mail content, and manage attachments.
     * 
     * @param part
     * @return
     * @throws MessagingException
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public MailContent extractMailContent(Part part) throws MessagingException, UnsupportedEncodingException,
        IOException
    {
        logger.debug("extractMailContent...");
        MailContent mailContent = new MailContent();

        if (part.isMimeType("application/pkcs7-mime") || part.isMimeType("multipart/encrypted")) {
            logger.debug("Mail content is ENCRYPTED");
            mailContent
                .setText("<<<This e-mail part is encrypted. Text Content and attachments of encrypted e-mails are not published in Mail Archiver to avoid disclosure of restricted or confidential information.>>>");
            mailContent
                .setHtml("<i>&lt;&lt;&lt;This e-mail is encrypted. Text Content and attachments of encrypted e-mails are not published in Mail Archiver to avoid disclosure of restricted or confidential information.&gt;&gt;&gt;</i>");
            mailContent.setEncrypted(true);

            return mailContent;
        } else {
            mailContent = extractPartsContent(part);
        }
        // TODO : filling attachment cids and creating xwiki attachments should be done in same method
        HashMap<String, String> attachmentsMap = fillAttachmentContentIds(mailContent.getAttachments());
        String fileName = "";
        for (MimeBodyPart currentbodypart : mailContent.getAttachments()) {
            try {
                String cid = currentbodypart.getContentID();
                fileName = currentbodypart.getFileName();

                // replace by correct name if filename was renamed (multiple attachments with same name)
                if (attachmentsMap.containsKey(cid)) {
                    fileName = attachmentsMap.get(cid);
                }
                logger.debug("Treating attachment: " + fileName + " with contentid " + cid);
                if (fileName == null) {
                    fileName = "file.ext";
                }
                if (fileName.equals("oledata.mso") || fileName.endsWith(".wmz") || fileName.endsWith(".emz")) {
                    logger.debug("Garbaging Microsoft crap !");
                } else {
                    String disposition = currentbodypart.getDisposition();
                    String attcontentType = currentbodypart.getContentType().toLowerCase();

                    logger.debug("Treating attachment of type: " + attcontentType);

                    XWikiAttachment wikiAttachment = new XWikiAttachment();
                    wikiAttachment.setFilename(fileName);
                    wikiAttachment.setContent(currentbodypart.getInputStream());
                    mailContent.addWikiAttachment(cid, wikiAttachment);

                } // end if
            } catch (Exception e) {
                logger.warn("Attachment " + fileName + " could not be treated", e);
            }
        }

        return mailContent;
    }

    /**
     * Recurssively extracts content of an email. Every Part that has a file name, or is neither multipart, plain text
     * or html, is considered an attachment.
     * 
     * @param part
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public MailContent extractPartsContent(Part part) throws MessagingException, UnsupportedEncodingException,
        IOException
    {
        MailContent mailContent = new MailContent();

        String contentType = part.getContentType().toLowerCase();

        if (!StringUtils.isBlank(part.getFileName())
            || (!contentType.startsWith("multipart/") && !part.isMimeType("text/plain") && !part
                .isMimeType("text/html"))) {
            mailContent.addAttachment((MimeBodyPart) part);
        } else if (part.isMimeType("text/plain")) {
            logger.debug("Adding PLAIN TEXT");
            mailContent.appendText(MimeUtility.decodeText((String) part.getContent()));
        } else if (part.isMimeType("text/html")) {
            logger.debug("Adding HTML");
            mailContent.appendHtml(MimeUtility.decodeText((String) part.getContent()));
        } else if (part.isMimeType("message/rfc822")) {
            Message innerMessage = (Message) part.getContent();
            mailContent.addAttachedMail(innerMessage);
            // FIXME attached mails should be loaded previously to their container
        } else if (contentType.startsWith("multipart/")) {
            logger.debug("Adding a MULTIPART");
            Multipart multipart = (Multipart) part;
            if (contentType.startsWith("multipart/signed")) {
                // Signed multiparts contain 2 parts: first is the content, second is the control information
                // We just ignore the control information
                logger.debug("Adding SIGNED MULTIPART CONTENT");
                mailContent.append(extractPartsContent(multipart.getBodyPart(0)));
            } else if (contentType.startsWith("multipart/related") || contentType.startsWith("multipart/mixed")
                || contentType.startsWith("multipart/alternative")) {
                // FIXME multipart/related should be treated differently than other parts, though the same treatment
                // should be ok most of the time
                // (multipart/related is usually one part text/plain and the alternative text/html, so as text and html
                // are always considered alternates by this algorithm, it's ok)
                int i = 0;
                int mcount = multipart.getCount();
                while (i < mcount) {
                    logger.debug("Adding MULTIPART #{}", i);
                    try {
                        mailContent.append(extractPartsContent(multipart.getBodyPart(i)));
                    } catch (Exception e) {
                        logger.warn("Could not add MULTIPART #{}", i, e);
                    }
                    i++;
                }
            } else {
                logger.info("Multipart subtype {} not managed", contentType.substring(0, contentType.indexOf(' ')));
            }
        } else {
            logger.info("Message Type {} not managed", contentType.substring(0, contentType.indexOf('/')));
        }

        return mailContent;

    }

    /*
     * Fills a map with key=contentId, value=filename of attachment
     */
    public HashMap<String, String> fillAttachmentContentIds(ArrayList<MimeBodyPart> bodyparts)
    {
        HashMap<String, String> attmap = new HashMap<String, String>();

        for (MimeBodyPart bodypart : bodyparts) {
            String fileName = null;
            String cid = null;
            try {
                fileName = bodypart.getFileName();
                cid = bodypart.getContentID();
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!StringUtils.isBlank(cid) && fileName != null) {
                logger.debug("fillAttachmentContentIds: Treating attachment: " + fileName + " with contentid " + cid);
                String name = getAttachmentValidName(fileName);
                int nb = 1;
                if (!name.contains(".")) {
                    name += ".ext";
                }
                String newName = name;
                while (attmap.containsValue(newName)) {
                    logger.debug("fillAttachmentContentIds: " + newName + " attachment already exists, renaming to "
                        + name.replaceAll("(.*)\\.([^.]*)", "$1-" + nb + ".$2"));
                    newName = name.replaceAll("(.*)\\.([^.]*)", "$1-" + nb + ".$2");
                    nb++;
                }
                attmap.put(cid, newName);
            } else {
                logger.debug("fillAttachmentContentIds: content ID is null, nothing to do");
            }
        }

        return attmap;
    }

    /*
     * Returns a valid name for an attachment from its original name
     */
    public String getAttachmentValidName(String afilename)
    {
        String fname = afilename;
        int i = fname.lastIndexOf("\\");
        if (i == -1) {
            i = fname.lastIndexOf("/");
        }
        String filename = fname.substring(i + 1);
        filename = filename.replaceAll("\\+", " ");
        return filename;
    }

}
