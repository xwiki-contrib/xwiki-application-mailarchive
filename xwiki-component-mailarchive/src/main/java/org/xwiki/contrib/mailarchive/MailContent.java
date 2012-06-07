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
package org.xwiki.contrib.mailarchive;

import java.util.ArrayList;
import java.util.HashMap;

import javax.mail.internet.MimeBodyPart;

import com.xpn.xwiki.doc.XWikiAttachment;

/**
 * Email content. TODO javadoc
 * 
 * @version $Id$
 */
public class MailContent
{
    private String text;

    private String html;

    /**
     * Extracted attachments as a map, key being content-ID.
     */
    private ArrayList<MimeBodyPart> rawAttachments;

    /**
     * relates a Content-ID to an attachment filename as it would/should be added to a wiki page. In fact if several
     * rawAttachments in a mail have the same filenames, renaming occurs in order to have only unique names. This map
     * provides the new name to be used instead of original attachment name.
     */
    private HashMap<String, XWikiAttachment> wikiAttachments;

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;
    }

    public ArrayList<MimeBodyPart> getAttachments()
    {
        return rawAttachments;
    }

    public void setAttachments(ArrayList<MimeBodyPart> attachments)
    {
        this.rawAttachments = attachments;
    }

    public void addAttachmnet(MimeBodyPart attachment)
    {
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
        this.wikiAttachments.put(contentId, attachment);
    }

}
