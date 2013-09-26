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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mailarchive.internal.data.MailDescriptor;
import org.xwiki.contrib.mailarchive.internal.data.TopicDescriptor;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.persistence.XWikiPersistence;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

/**
 * Methods accessing stored information useful for the Mail Archive, either from storage (database) or from wiki
 * documents.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class ItemsManager implements IItemsManager
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.IItemsManager#loadStoredTopics()
     */
    @Override
    public HashMap<String, TopicDescriptor> loadStoredTopics() throws MailArchiveException
    {

        final HashMap<String, TopicDescriptor> existingTopics = new HashMap<String, TopicDescriptor>();
        List<Object[]> topics;

        String xwql =
            "select doc.fullName, topic.topicid, topic.subject " + "from Document doc, doc.object("
                + XWikiPersistence.SPACE_CODE + ".TopicClass) as  topic " + "where doc.space='"
                + XWikiPersistence.SPACE_ITEMS + "'";

        try {
            topics = queryManager.createQuery(xwql, Query.XWQL).execute();

            for (Object[] topic : topics) {
                // map[topicId] = [fullname, subject]
                TopicDescriptor shorttopic = new TopicDescriptor((String) topic[0], (String) topic[2]);
                existingTopics.put((String) topic[1], shorttopic);
                logger.debug("Loaded topic " + topic[0] + " : " + shorttopic);
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load existing topics", e);
        }
        return existingTopics;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.IItemsManager#loadStoredMessages()
     */
    @Override
    public HashMap<String, MailDescriptor> loadStoredMessages() throws MailArchiveException
    {

        final HashMap<String, MailDescriptor> existingMessages = new HashMap<String, MailDescriptor>();
        List<Object[]> messages;

        try {
            String xwql =
                "select mail.messageid, mail.messagesubject, mail.topicid, doc.fullName "
                    + "from Document doc, doc.object(" + XWikiPersistence.SPACE_CODE + ".MailClass) as  mail "
                    + "where doc.space='" + XWikiPersistence.SPACE_ITEMS + "'";

            messages = queryManager.createQuery(xwql, Query.XWQL).execute();

            if (messages != null) {
                for (Object[] message : messages) {
                    if (message[0] != null && message[0] != "") {
                        // map[messageid] = [subject, topicid, fullName]
                        MailDescriptor shortmail =
                            new MailDescriptor((String) message[1], (String) message[2], (String) message[3]);
                        existingMessages.put((String) message[0], shortmail);
                        logger.debug("Loaded mail " + message[1] + " : " + shortmail);
                    } else {
                        logger.warn("Incorrect message object found in db for " + message[3]);
                    }
                }

            }

        } catch (Exception e) {
            throw new MailArchiveException("Failed to load existing messages", e);
        }

        return existingMessages;

    }

}
