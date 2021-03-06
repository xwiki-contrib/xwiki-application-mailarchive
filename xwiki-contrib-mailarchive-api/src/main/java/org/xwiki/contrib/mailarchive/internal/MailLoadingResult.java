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
package org.xwiki.contrib.mailarchive.internal;

/**
 * Results of an email loading.
 * 
 * @author jbousque
 * @version $Id$
 */
public class MailLoadingResult
{
    private STATUS status;

    private String createdTopicDocumentName;

    private String createdMailDocumentName;

    public MailLoadingResult(final STATUS status, final String createdTopicDocumentName,
        final String createdMailDocumentName)
    {
        super();
        this.status = status;
        this.createdTopicDocumentName = createdTopicDocumentName;
        this.createdMailDocumentName = createdMailDocumentName;
    }

    /**
     * If email was successfully loaded.
     * 
     * @return
     */
    public boolean isSuccess()
    {
        return STATUS.SUCCESS.equals(this.status) || STATUS.ALREADY_LOADED.equals(this.status);
    }
    
    public STATUS getStatus()
    {
        return this.status;
    }

    /**
     * Name of xwiki document created for topic.
     * 
     * @return
     */
    public String getCreatedTopicDocumentName()
    {
        return createdTopicDocumentName;
    }

    /**
     * Name of xwiki document created for mail.
     * 
     * @return
     */
    public String getCreatedMailDocumentName()
    {
        return createdMailDocumentName;
    }

    enum STATUS
    {
        SUCCESS,
        FAILED,
        ALREADY_LOADED,
        NOT_MATCHING_MAILING_LISTS
    }

}
