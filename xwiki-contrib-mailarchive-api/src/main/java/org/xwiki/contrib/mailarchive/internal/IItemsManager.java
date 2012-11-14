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

import java.util.HashMap;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.mailarchive.internal.data.MailDescriptor;
import org.xwiki.contrib.mailarchive.internal.data.TopicDescriptor;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.query.QueryException;

/**
 * @version $Id$
 */
@Role
public interface IItemsManager
{

    /**
     * Loads existing topics minimal information from database.
     * 
     * @return a map of existing topics, with key = topicId
     * @throws QueryException
     */
    HashMap<String, TopicDescriptor> loadStoredTopics() throws MailArchiveException;

    /**
     * Loads existing mails minimal information from database.
     * 
     * @return a map of existing mails, with key = messageId
     * @throws MailArchiveException
     */
    HashMap<String, MailDescriptor> loadStoredMessages() throws MailArchiveException;

}
