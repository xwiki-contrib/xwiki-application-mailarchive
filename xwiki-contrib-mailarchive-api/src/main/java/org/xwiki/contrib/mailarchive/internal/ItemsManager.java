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

import org.slf4j.Logger;
import org.xwiki.contrib.mailarchive.internal.data.MailArchiveFactory;
import org.xwiki.contrib.mailarchive.internal.data.MailShortItem;
import org.xwiki.contrib.mailarchive.internal.data.TopicShortItem;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

/**
 * Methods accessing stored information useful for the Mail Archive, either from storage (database) or from wiki
 * documents.
 * 
 * @version $Id$
 */
public class ItemsManager
{
    private QueryManager queryManager;

    private Logger logger;

    private MailArchiveFactory factory;

    public ItemsManager(final QueryManager queryManager, final Logger logger, final MailArchiveFactory factory)
    {
        this.queryManager = queryManager;
        this.logger = logger;
        this.factory = factory;
    }

    /**
     * Loads existing topics minimal information from database.
     * 
     * @return a map of existing topics, with key = topicId
     * @throws QueryException
     */
    public HashMap<String, TopicShortItem> loadStoredTopics() throws MailArchiveException
    {

        final HashMap<String, TopicShortItem> existingTopics = new HashMap<String, TopicShortItem>();
        List<Object[]> topics;

        String xwql =
            "select doc.fullName, topic.topicid, topic.subject " + "from Document doc, doc.object("
                + DefaultMailArchive.SPACE_CODE + ".MailTopicClass) as  topic " + "where doc.space='"
                + DefaultMailArchive.SPACE_ITEMS + "'";

        try {
            topics = queryManager.createQuery(xwql, Query.XWQL).execute();

            for (Object[] topic : topics) {
                // map[topicId] = [fullname, subject]
                TopicShortItem shorttopic = new TopicShortItem((String) topic[0], (String) topic[2]);
                existingTopics.put((String) topic[1], shorttopic);
                logger.debug("Loaded topic " + topic[0] + " : " + shorttopic);
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load existing topics", e);
        }
        return existingTopics;
    }

    /**
     * Loads existing mails minimal information from database.
     * 
     * @return a map of existing mails, with key = messageId
     * @throws MailArchiveException
     */
    public HashMap<String, MailShortItem> loadStoredMessages() throws MailArchiveException
    {

        final HashMap<String, MailShortItem> existingMessages = new HashMap<String, MailShortItem>();
        List<Object[]> messages;

        try {
            String xwql =
                "select mail.messageid, mail.messagesubject, mail.topicid, doc.fullName "
                    + "from Document doc, doc.object(" + DefaultMailArchive.SPACE_CODE + ".MailClass) as  mail "
                    + "where doc.space='" + DefaultMailArchive.SPACE_ITEMS + "'";

            messages = queryManager.createQuery(xwql, Query.XWQL).execute();

            if (messages != null) {
                for (Object[] message : messages) {
                    if (message[0] != null && message[0] != "") {
                        // map[messageid] = [subject, topicid, fullName]
                        MailShortItem shortmail =
                            new MailShortItem((String) message[1], (String) message[2], (String) message[3]);
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
