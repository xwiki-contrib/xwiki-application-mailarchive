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
package org.xwiki.contrib.mailarchive.internal.timeline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.persistence.XWikiPersistence;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id$
 */
@Component
@Singleton
public class TimeLineGenerator implements Initializable, ITimeLineGenerator
{

    public static final int DEFAULT_MAX_ITEMS = 200;

    @Inject
    private IMailArchiveConfiguration config;

    @Inject
    private Logger logger;

    @Inject
    private QueryManager queryManager;

    /** Provides access to the request context. */
    @Inject
    public Execution execution;

    private XWiki xwiki;

    private XWikiContext context;

    @Inject
    private DocumentReferenceResolver<String> docResolver;

    @Inject
    @Named("chaplinks")
    ITimeLineDataWriter timelineWriter;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        ExecutionContext context = execution.getContext();
        this.context = (XWikiContext) context.getProperty("xwikicontext");
        this.xwiki = this.context.getWiki();
    }

    public String compute()
    {
        return compute(Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.timeline.ITimeLineGenerator#compute()
     */
    @Override
    public String compute(int maxItems)
    {
        try {
            config.reloadConfiguration();
        } catch (MailArchiveException e) {
            logger.error("Could not load mail archive configuration", e);
            return null;
        }
        Map<String, IMailingList> mailingLists = config.getMailingLists();
        Map<String, IType> types = config.getMailTypes();

        TreeMap<Long, TimeLineEvent> sortedEvents = new TreeMap<Long, TimeLineEvent>();

        // Set loading user in context (for rights)
        String loadingUser = config.getLoadingUser();
        context.setUserReference(docResolver.resolve(loadingUser));

        try {
            // Search topics
            String xwql =
                "select doc.fullName, topic.author, topic.subject, topic.topicid, topic.startdate, topic.lastupdatedate from Document doc, doc.object("
                    + XWikiPersistence.CLASS_TOPICS + ") as topic order by topic.lastupdatedate desc";
            List<Object[]> result = queryManager.createQuery(xwql, Query.XWQL).setLimit(maxItems).execute();

            for (Object[] item : result) {
                XWikiDocument doc = null;
                try {
                    String docurl = (String) item[0];
                    String author = (String) item[1];
                    String subject = (String) item[2];
                    String topicId = (String) item[3];
                    Date date = (Date) item[4];
                    Date end = (Date) item[5];

                    String action = "";

                    // Retrieve associated emails
                    TreeMap<Long, TopicEventBubble> emails = getTopicMails(topicId, subject);

                    if (emails == null || emails.isEmpty()) {
                        // Invalid topic, not emails attached, do not show it
                        logger.warn("Invalid topic, no emails attached " + doc);
                    } else {
                        if (date != null && end != null && date.equals(end)) {
                            // Add 10 min just to see the tape
                            end.setTime(end.getTime() + 600000);
                        }

                        doc = xwiki.getDocument(docResolver.resolve(docurl), context);
                        String tags = doc.getTags(context);
                        List<String> topicTypes = doc.getListValue(XWikiPersistence.CLASS_TOPICS, "type");

                        // Email type icon
                        List<String> icons = new ArrayList<String>();
                        // FIXME: an email/topic can have more than one type...
                        for (String topicType : topicTypes) {
                            IType type = types.get(topicType);
                            if (type != null && !StringUtils.isEmpty(type.getIcon())) {
                                icons.add(xwiki.getSkinFile("icons/silk/" + type.getIcon(), context));
                            }
                        }

                        // Author avatar
                        String authorAvatar = getAuthorAvatar(doc.getCreator());

                        TimeLineEvent timelineEvent = new TimeLineEvent();
                        timelineEvent.beginDate = date;
                        timelineEvent.title = subject;
                        timelineEvent.icons = icons;
                        timelineEvent.tags = tags;
                        timelineEvent.author = author;
                        timelineEvent.authorAvatar = authorAvatar;

                        if (emails.size() == 1) {
                            logger.debug("Adding email '" + subject + "'");
                            // Unique email, we show a punctual email event
                            timelineEvent.url = emails.firstEntry().getValue().link;
                            timelineEvent.action = "New Email ";
                        } else {
                            // Email thread, we show a topic event (a range)
                            logger.debug("Adding topic '" + subject + "'");
                            timelineEvent.endDate = end;
                            timelineEvent.url = doc.getURL("view", context);
                            timelineEvent.action = "New Topic ";
                            timelineEvent.messages = emails;
                        }

                        // Add the generated Event to the list
                        if (sortedEvents.containsKey(date.getTime())) {
                            date.setTime(date.getTime() + 1);
                        }
                        sortedEvents.put(date.getTime(), timelineEvent);
                    }

                } catch (Throwable t) {
                    logger.warn("Exception for " + doc, t);
                }

            }

        } catch (Throwable e) {
            logger.warn("could not compute timeline data", e);
        }

        return printEvents(sortedEvents);

    }

    public String toString(TreeMap<Long, TimeLineEvent> sortedEvents)
    {
        return printEvents(sortedEvents);
    }

    private String printEvents(TreeMap<Long, TimeLineEvent> sortedEvents)
    {

        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        timelineWriter.setWikiPrinter(printer);
        timelineWriter.print(sortedEvents);

        logger.debug("Loaded " + sortedEvents.size() + " into Timeline feed");
        return printer.toString();

    }

    /**
     * Formats a timeline bubble content, ie html presenting list of mails related to a given topic.
     * 
     * @param topicid
     * @param topicsubject
     * @return
     * @throws QueryException
     * @throws XWikiException
     */
    protected TreeMap<Long, TopicEventBubble> getTopicMails(String topicid, String topicsubject) throws QueryException,
        XWikiException
    {
        // TODO there should/could be an api to retrieve mails related to a topic somewhere else than in timeline
        // generator ...

        logger.debug("Retrieving emails linked to topic with id " + topicid);

        TreeMap<Long, TopicEventBubble> bubblesInfo = new TreeMap<Long, TopicEventBubble>();
        boolean first = true;
        String xwql_topic =
            "select doc.fullName, doc.author, mail.date, mail.messagesubject ,mail.from from Document doc, "
                + "doc.object(" + XWikiPersistence.CLASS_MAILS + ") as  mail where  mail.topicid='" + topicid
                + "' and doc.space='MailArchiveItems' order by mail.date asc";
        List<Object[]> msgs = queryManager.createQuery(xwql_topic, Query.XWQL).execute();
        for (Object[] msg : msgs) {
            String docfullname = (String) msg[0];
            String docauthor = (String) msg[1];
            Date maildate = (Date) msg[2];
            String mailmessagesubject = (String) msg[3];
            String mailfrom = (String) msg[4];
            // formatter for formatting dates for display
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            // FIXME this thing with doc author does work only if ldap link is activated and if user exists in xwiki
            XWikiDocument userdoc = xwiki.getDocument(docResolver.resolve(docauthor), context);
            String user = "";
            String link = null;
            if (!userdoc.isNew()) {
                BaseObject userobj = userdoc.getXObject(docResolver.resolve("XWiki.XWikiUsers"));
                if (userobj != null) {
                    user = userobj.getStringValue("first_name") + " " + userobj.getStringValue("last_name");
                    link = userdoc.getURL("view", context);
                }
            } else {
                // TODO replace with parse user method
                int start = mailfrom.indexOf("<");
                if (start != -1) {
                    user = mailfrom.substring(0, start);
                }
            }
            String subject = topicsubject;
            if (!first) {
                subject = mailmessagesubject.replace(topicsubject, "...");
            } else {
                subject = StringUtils.normalizeSpace(topicsubject);
                subject = StringUtils.abbreviate(subject, 23);
            }
            TopicEventBubble bubbleEvent = new TopicEventBubble();
            bubbleEvent.date = maildate;
            bubbleEvent.url = xwiki.getDocument(docResolver.resolve(docfullname), context).getURL("view", context);
            bubbleEvent.subject = subject;
            bubbleEvent.link = link;
            bubbleEvent.user = user;
            bubblesInfo.put(maildate.getTime(), bubbleEvent);

            first = false;
        }

        return bubblesInfo;

    }

    private String getAuthorAvatar(final String user)
    {
        String authorAvatar = null;
        String imgName = null;
        try {
            XWikiDocument userDoc = xwiki.getDocument(user, context);
            if (userDoc != null && !userDoc.isNew()) {
                BaseObject userObj = userDoc.getObject("XWiki.XWikiUsers");
                if (userObj != null) {
                    imgName = userObj.getStringValue("avatar");
                }
            }
            if (imgName == null) {
                authorAvatar =
                    xwiki.getDocument("XWiki.XWikiUserSheet", context).getURL("download", context) + "/noavatar.png";
            } else {
                authorAvatar = userDoc.getURL("download", context) + '/' + imgName;
            }
        } catch (XWikiException e) {
            logger.error("Failed to retrieve author avatar", e);
        }

        return authorAvatar;
    }

}
