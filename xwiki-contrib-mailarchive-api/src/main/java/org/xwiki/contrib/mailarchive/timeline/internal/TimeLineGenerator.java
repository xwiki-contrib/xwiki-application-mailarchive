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
package org.xwiki.contrib.mailarchive.timeline.internal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.mailarchive.IMAUser;
import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.timeline.ITimeLineGenerator;
import org.xwiki.contrib.mailarchive.timeline.TimeLineEvent;
import org.xwiki.contrib.mailarchive.timeline.TimeLineSubEvent;
import org.xwiki.contrib.mailarchive.utils.DecodedMailContent;
import org.xwiki.contrib.mailarchive.utils.IMailUtils;
import org.xwiki.contrib.mailarchive.utils.ITextUtils;
import org.xwiki.contrib.mailarchive.xwiki.internal.XWikiPersistence;
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
    @Named("simile")
    ITimeLineDataWriter timelineWriter;

    @Inject
    private IMailUtils mailUtils;

    @Inject
    private ITextUtils textUtils;

    private String userStatsUrl = "";

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
     * @see org.xwiki.contrib.mailarchive.timeline.ITimeLineGenerator#compute()
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

        try {
            this.userStatsUrl =
                xwiki.getDocument(docResolver.resolve("MailArchive.ViewUserMessages"), context).getURL("view", context);
        } catch (XWikiException e1) {
            logger.warn("Could not retrieve user stats url {}", ExceptionUtils.getRootCauseMessage(e1));
        }

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
                    TreeMap<Long, TimeLineSubEvent> emails = getTopicMails(topicId, subject);

                    if (emails == null || emails.isEmpty()) {
                        // Invalid topic, not emails attached, do not show it
                        logger.warn("Invalid topic, no emails attached " + doc);
                    } else {
                        if (date != null && end != null && date.equals(end)) {
                            // Add 10 min just to see the tape
                            end.setTime(end.getTime() + 600000);
                        }

                        doc = xwiki.getDocument(docResolver.resolve(docurl), context);
                        final List<String> tagsList = doc.getTagsList(context);                        
                        List<String> topicTypes = doc.getListValue(XWikiPersistence.CLASS_TOPICS, "type");

                        // Email type icon
                        List<String> icons = new ArrayList<String>();
                        for (String topicType : topicTypes) {
                            IType type = types.get(topicType);
                            if (type != null && !StringUtils.isEmpty(type.getIcon())) {
                                icons.add(xwiki.getSkinFile("icons/silk/" + type.getIcon() + ".png", context));
                                // http://localhost:8080/xwiki/skins/colibri/icons/silk/bell
                                // http://localhost:8080/xwiki/resources/icons/silk/bell.png

                            }
                        }

                        // Author and avatar
                        final IMAUser wikiUser = mailUtils.parseUser(author, config.isMatchLdap());
                        final String authorAvatar = getAuthorAvatar(wikiUser.getWikiProfile());

                        final TimeLineEvent timelineEvent = new TimeLineEvent();
                        TimeLineEvent additionalEvent = null;
                        timelineEvent.beginDate = date;
                        timelineEvent.title = subject;
                        timelineEvent.icons = icons;
                        timelineEvent.lists = doc.getListValue(XWikiPersistence.CLASS_TOPICS, "list");
                        timelineEvent.author = wikiUser.getDisplayName();
                        timelineEvent.authorAvatar = authorAvatar;
                        timelineEvent.extract = getExtract(topicId);

                        if (emails.size() == 1) {
                            logger.debug("Adding instant event for email '" + subject + "'");
                            // Unique email, we show a punctual email event
                            timelineEvent.url = emails.firstEntry().getValue().link;
                            timelineEvent.action = "New Email ";

                        } else {
                            // For email with specific type icon, and a duration, both a band and a point should be added (so 2 events)
                            // because no icon is displayed for duration events.
                            if (CollectionUtils.isNotEmpty(icons)) {
                                logger.debug("Adding additional instant event to display type icon for first email in topic");
                                additionalEvent = new TimeLineEvent(timelineEvent);
                                additionalEvent.url = emails.firstEntry().getValue().link;
                                additionalEvent.action = "New Email ";                                
                            }
                            
                            // Email thread, we show a topic event (a range)
                            logger.debug("Adding duration event for topic '" + subject + "'");
                            timelineEvent.endDate = end;
                            timelineEvent.url = doc.getURL("view", context);
                            timelineEvent.action = "New Topic ";
                            timelineEvent.messages = emails;
                        }

                        // Add the generated Event to the list
                        if (sortedEvents.containsKey(date.getTime())) {
                            // Avoid having more than 1 event at exactly the same time, because some timeline don't like it
                            date.setTime(date.getTime() + 1);
                        }
                        sortedEvents.put(date.getTime(), timelineEvent);
                        if (additionalEvent != null) {
                            sortedEvents.put(date.getTime() + 1, additionalEvent);
                        }
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
        logger.debug("Timeline data {}", printer.toString());
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
    protected TreeMap<Long, TimeLineSubEvent> getTopicMails(String topicid, String topicsubject) throws QueryException,
        XWikiException
    {
        // TODO there should/could be an api to retrieve mails related to a topic somewhere else than in timeline
        // generator ...

        logger.debug("Retrieving emails linked to topic with id " + topicid);

        final TreeMap<Long, TimeLineSubEvent> bubblesInfo = new TreeMap<Long, TimeLineSubEvent>();
        String xwql_topic =
            "select doc.fullName, mail.date, mail.messagesubject ,mail.from from Document doc, "
                + "doc.object(" + XWikiPersistence.CLASS_MAILS + ") as  mail where  mail.topicid='" + topicid
                + "' and doc.space='MailArchiveItems' order by mail.date asc";
        final List<Object[]> msgs = queryManager.createQuery(xwql_topic, Query.XWQL).execute();

        String previousSubject = StringUtils.normalizeSpace(topicsubject);

        for (Object[] msg : msgs) {
            final String docfullname = (String) msg[0];
            final Date maildate = (Date) msg[1];
            String mailmessagesubject = (String) msg[2];
            final String mailfrom = (String) msg[3];

            IMAUser parsedUser = mailUtils.parseUser(mailfrom, config.isMatchLdap());
            String user = parsedUser.getDisplayName();
            String link = this.userStatsUrl + "?user=" + mailfrom;
            if (StringUtils.isNotEmpty(parsedUser.getWikiProfile())) {
                link += "&wikiUser=" + parsedUser.getWikiProfile();
            }

            mailmessagesubject = StringUtils.normalizeSpace(mailmessagesubject);
            String subject = mailmessagesubject.replace(previousSubject, "...");
            previousSubject = mailmessagesubject;

            TimeLineSubEvent bubbleEvent = new TimeLineSubEvent();
            bubbleEvent.date = maildate;
            bubbleEvent.url = xwiki.getDocument(docResolver.resolve(docfullname), context).getURL("view", context);
            bubbleEvent.subject = subject;
            bubbleEvent.link = link;
            bubbleEvent.user = user;
            bubblesInfo.put(maildate.getTime(), bubbleEvent);
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

    /**
     * Formats a timeline bubble content, ie html presenting list of mails related to a given topic.
     * 
     * @param topicid
     * @param topicsubject
     * @return
     * @throws QueryException
     * @throws XWikiException
     */
    protected String getExtract(String topicid) throws QueryException, XWikiException
    {
        String extract = "";

        logger.debug("Retrieving first email linked to topic with id " + topicid);

        String xwql_topic =
            "select mail.body, mail.bodyhtml from Document doc, " + "doc.object(" + XWikiPersistence.CLASS_MAILS
                + ") as  mail where  mail.topicid='" + topicid
                + "' and doc.space='MailArchiveItems' order by mail.date asc";
        final List<Object[]> msgs = queryManager.createQuery(xwql_topic, Query.XWQL).setLimit(1).execute();

        if (CollectionUtils.isNotEmpty(msgs)) {

            final String body = (String) msgs.get(0)[0];
            final String bodyhtml = (String) msgs.get(0)[1];

            extract = body;

            try {
                DecodedMailContent decoded = mailUtils.decodeMailContent(bodyhtml, body, true);
                if (decoded != null) {
                    if (decoded.isHtml() && StringUtils.isEmpty(decoded.getText())) {
                        extract = textUtils.htmlToPlainText(bodyhtml);
                    } else {
                        extract = decoded.getText();
                    }
                }

            } catch (IOException e) {
                Log.warn("Could not decoded HTML content {}", e);
            }
            
            extract = StringUtils.normalizeSpace(extract);
            extract = StringUtils.abbreviate(extract, 200);

        }

        return extract;

    }

}
