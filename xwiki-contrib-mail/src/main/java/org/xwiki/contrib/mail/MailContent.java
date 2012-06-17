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
import java.util.HashMap;
import java.util.List;

import javax.mail.Message;
import javax.mail.internet.MimeBodyPart;

import com.xpn.xwiki.doc.XWikiAttachment;

/**
 * Email content. TODO javadoc
 * 
 * @version $Id$
 */
public class MailContent
{
    private StringBuilder text;

    private StringBuilder html;

    /**
     * Extracted attachments as a map, key being content-ID.
     */
    private ArrayList<MimeBodyPart> rawAttachments = new ArrayList<MimeBodyPart>();

    /**
     * relates a Content-ID to an attachment filename as it would/should be added to a wiki page. In fact if several
     * joined files in a mail have the same filenames, renaming occurs in order to have only unique names. This map
     * provides the new name to be used instead of original attachment name.
     */
    private HashMap<String, XWikiAttachment> wikiAttachments = new HashMap<String, XWikiAttachment>();

    private List<Message> attachedMails = new ArrayList<Message>();

    private boolean encrypted = false;

    private boolean signed = false;

    public boolean isEncrypted()
    {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted)
    {
        this.encrypted = encrypted;
    }

    public boolean isSigned()
    {
        return signed;
    }

    public void setSigned(boolean signed)
    {
        this.signed = signed;
    }

    public String getText()
    {
        return text.toString();
    }

    public void setText(String text)
    {
        this.text = new StringBuilder(text);
    }

    public void appendText(String text)
    {
        this.text.append(text);
    }

    public String getHtml()
    {
        return html.toString();
    }

    public void setHtml(String html)
    {
        this.html = new StringBuilder(html);
    }

    public void appendHtml(String html)
    {
        this.html.append(html);
    }

    public ArrayList<MimeBodyPart> getAttachments()
    {
        return rawAttachments;
    }

    public void setAttachments(ArrayList<MimeBodyPart> attachments)
    {
        this.rawAttachments = attachments;
    }

    public void addAttachment(MimeBodyPart attachment)
    {
        if (this.rawAttachments == null) {
            this.rawAttachments = new ArrayList<MimeBodyPart>();
        }
        this.rawAttachments.add(attachment);
    }

    public HashMap<String, XWikiAttachment> getWikiAttachments()
    {
        return wikiAttachments;
    }

    public void setWikiAttachments(HashMap<String, XWikiAttachment> wikiAttachments)
    {
        this.wikiAttachments = wikiAttachments;
    }

    public void addWikiAttachment(String contentId, XWikiAttachment attachment)
    {
        if (this.wikiAttachments == null) {
            this.wikiAttachments = new HashMap<String, XWikiAttachment>();
        }
        this.wikiAttachments.put(contentId, attachment);
    }

    public void append(MailContent mailContent)
    {
        appendText(mailContent.getText());
        appendHtml(mailContent.getHtml());
        this.rawAttachments.addAll(mailContent.getAttachments());
        this.wikiAttachments.putAll(mailContent.getWikiAttachments());
    }

    public List<Message> getAttachedMails()
    {
        return attachedMails;
    }

    public void setAttachedMails(List<Message> attachedMails)
    {
        this.attachedMails = attachedMails;
    }

    public void addAttachedMail(Message message)
    {
        if (this.attachedMails == null) {
            this.attachedMails = new ArrayList<Message>();
        }
        this.attachedMails.add(message);
    }

}
