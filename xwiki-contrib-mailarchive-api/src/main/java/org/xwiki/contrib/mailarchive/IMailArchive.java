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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import org.xwiki.component.annotation.Role;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mail.SourceConnectionErrors;
import org.xwiki.contrib.mail.internal.FolderItem;
import org.xwiki.contrib.mail.source.IMailSource;
import org.xwiki.contrib.mailarchive.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.MailLoadingResult;
import org.xwiki.contrib.mailarchive.internal.data.MailDescriptor;
import org.xwiki.contrib.mailarchive.internal.data.TopicDescriptor;
import org.xwiki.contrib.mailarchive.internal.threads.ThreadableMessage;
import org.xwiki.contrib.mailarchive.utils.DecodedMailContent;

import com.xpn.xwiki.XWikiException;

/**
 * A component to manage a Mail Archive, ie mainly:<br/>
 * - load a mail into the archive<br/>
 * - retrieve mails and topics from the archive <br/>
 * - thread emails<br/>
 */
@Role
public interface IMailArchive
{
    /**
     * Checks connection to mail server, and count available (UNREAD) emails.
     * 
     * @return value >= 0 is the number of messages to read if connection was ok, negative value indicates connection
     *         error (see {@link SourceConnectionErrors}).
     */
    public int checkSource(String serverPrefsDoc);

    /**
     * Checks connections defined by a loading session, and count available emails.
     * 
     * @param session
     * @return
     */
    public Map<String, Integer> checkSource(LoadingSession session);

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
    public DecodedMailContent getDecodedMailText(String mailPage, boolean cut) throws IOException, XWikiException,
        InitializationException, MailArchiveException;

    public IType getType(String name);

    public String computeTimeline() throws Exception;

    /**
     * The mail archive configuration.
     * 
     * @return
     * @throws InitializationException
     * @throws MailArchiveException
     */
    public IMailArchiveConfiguration getConfiguration() throws InitializationException, MailArchiveException;

    /**
     * Returns a view on mails already loaded into the mail archive.
     * 
     * @return
     * @throws MailArchiveException
     */
    public Map<String, MailDescriptor> getMails() throws MailArchiveException;

    /**
     * Returns a view on topics already loaded into the mail archive.
     * 
     * @return
     * @throws MailArchiveException
     */
    public Map<String, TopicDescriptor> getTopics() throws MailArchiveException;

    public List<IMASource> getSourcesList(final LoadingSession session);

    /**
     * Loads a mail in the archive. Creates a new Topic page if needed, or link new mail to an existing topic if
     * possible. Creates a new Mail page only if the Message-Id has not already been loaded.
     * 
     * @param mail The javamail message Part.
     * @param confirm If false, no page will be created in the wiki.
     * @param isAttachedMail True if the mail is in fact an attachment of another mail.
     * @param parentMail
     * @return
     * @throws XWikiException
     * @throws ParseException
     * @throws MessagingException
     * @throws IOException
     */
    MailLoadingResult loadMail(Part mail, boolean confirm, boolean isAttachedMail, String parentMail)
        throws XWikiException, ParseException, MessagingException, IOException;

    /**
     * 
     * @param message
     */
    void dumpEmail(Message message);

    /**
     * 
     */
    void unlock();

    /**
     * 
     * @return
     */
    boolean lock();
    
    /**
     * 
     * @param serverId
     * @param source
     * @param message
     */
    void saveToInternalStore(final String serverId, final IMailSource source, final Message message);

    /**
     * Retrieves a message from the internal store.
     * 
     * @param serverId The server id, ie the folder into the store.
     * @param messageId The value of the Message-Id header of the message to find.
     * @return A javamail Message, or null if it was not found for any reason.
     */
    Message getFromStore(final String serverId, final String messageId);
    
    /**
     * 
     * @param sourcePrefsDoc
     * @return
     */
    ArrayList<FolderItem> getFolderTree(final String sourcePrefsDoc);

    public abstract boolean isLocked();

}
