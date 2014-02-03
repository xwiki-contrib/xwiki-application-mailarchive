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
package org.xwiki.contrib.mailarchive.internal.threads;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.mailarchive.exceptions.MailArchiveException;
import org.xwiki.query.QueryException;

/**
 * @version $Id$
 */
@Role
public interface IMessagesThreader
{

    /**
     * Threads the whole archive.
     * 
     * @return The root of the threaded message tree.
     * @throws QueryException If there was a problem to retrieve the messages from db.
     * @throws MailArchiveException If there was a problem to retrieve the messages from db.
     * @throws QueryException If there was a problem to retrieve the messages from db.
     * @throws MailArchiveException If there was a problem during threading.
     */
    ThreadableMessage thread() throws QueryException, MailArchiveException;

    /**
     * Threads a specific topic.
     * 
     * @param topicId ID of the topic to be threaded.
     * @return The root of the threaded message tree.
     * @throws QueryException If there was a problem to retrieve the messages from db.
     * @throws MailArchiveException If there was a problem during threading.
     */
    ThreadableMessage thread(String topicId) throws QueryException, MailArchiveException;

    /**
     * Threads a list of messages.
     * 
     * @param messages The list of messages to thread, in no particular order.
     * @return The root of the threaded messages tree.
     * @throws MailArchiveException If a problem occurred while threading.
     */
    ThreadableMessage thread(List<ThreadableMessage> messages) throws MailArchiveException;

}
