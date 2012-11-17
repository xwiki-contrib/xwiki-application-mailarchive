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

import java.io.IOException;

import org.xwiki.component.annotation.Role;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mail.ConnectionErrors;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.threads.ThreadableMessage;

import com.xpn.xwiki.XWikiException;

/**
 * Interface (aka Role) of the Component
 */
@Role
public interface IMailArchive
{
    /**
     * Checks connection to mail server, and count available (UNREAD) emails.
     * 
     * @return value >= 0 is the number of messages to read if connection was ok, negative value indicates connection
     *         error (see {@link ConnectionErrors}).
     */
    public int queryServerInfo(String serverPrefsDoc);

    /**
     * Fetch mails from a specific server (given page name holding its preferences), or from all configured servers. The
     * session object provides all needed parameters (Cf {@link LoadingSession}).
     * 
     * @param session
     * @return Number of emails loaded during this session.
     */
    public int loadMails(LoadingSession session);

    /**
     * Threads messages related to a topic, given its ID.<br/>
     * Result is the root of the threaded message hierarchy, descendants can be traversed using "child" and "next" to
     * get siblings recursively.<br/>
     * Use "flatten()" to flatten this recursive structure into an Array.
     * 
     * @param topicId
     * @return The root of the threaded messages hierarchy, or null if the threading failed, or if provided topic id
     *         does not exist.
     */
    public ThreadableMessage computeThreads(String topicId);

    /**
     * Parses an internet address and returns a wiki user.
     * 
     * @param internetAddress
     * @return a wiki user profile if one was found, along with a display name and an email address.
     */
    public IMAUser parseUser(String internetAddress);

    /**
     * Retrieves the html and/or plain text content from a persisted email page, and decodes it accordingly. Returns
     * html format in favor of plain text format if possible. Html content is expected to be stored in GZIP format,
     * converted to textual representation in hexadecimal form. It is up to the caller to detect if returned string
     * represents html or plain text.
     * 
     * @param mailPage The wiki page name.
     * @param cut Indicates wether to "cut" or not the reply history from returned text/html. <br/>
     *            Note that algorithm to cut this history is very simple, and merely relies on presence of "From:" text
     *            in mail content.
     * @return
     * @throws IOException
     * @throws XWikiException
     * @throws MailArchiveException
     * @throws InitializationException
     */
    public String getDecodedMailText(String mailPage, boolean cut) throws IOException, XWikiException,
        InitializationException, MailArchiveException;

    public IType getType(String name);

    public String computeTimeline() throws Exception;
}
