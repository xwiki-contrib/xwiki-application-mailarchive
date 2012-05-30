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

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.contrib.mail.ConnectionErrors;
import org.xwiki.contrib.mailarchive.internal.threads.ThreadableMessage;

/**
 * Interface (aka Role) of the Component
 */
@ComponentRole
public interface MailArchive
{
    /**
     * Checks connection to mail server, and count available (UNREAD) emails.
     * 
     * @return value >= 0 is the number of messages to read if connection was ok, negative value indicates connection
     *         error (see {@link ConnectionErrors}).
     */
    public int queryServerInfo(String serverPrefsDoc);

    /**
     * Fetch mails from a mail server folder, considering only UNREAD emails (not having Flag.SEEN), parses them, and
     * persists new Topic and Mail objects (or update existing ones) into XWiki appropriately.
     * 
     * @param maxMailsNb The maximum number of mails to read
     * @return false if no mails at all could be read
     */
    public int loadMails(int maxMailsNb);

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
     * @return a wiki user profile if one was found.
     */
    public String parseUser(String internetAddress);
}
