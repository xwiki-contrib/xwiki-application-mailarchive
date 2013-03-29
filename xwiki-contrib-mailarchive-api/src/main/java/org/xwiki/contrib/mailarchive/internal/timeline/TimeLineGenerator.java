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
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.internal.persistence.XWikiPersistence;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

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

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.timeline.ITimeLineGenerator#compute()
     */
    @Override
    public String compute()
    {
        // FIXME This compute() should be split in 2 parts, one part for generating a structure
        // representing timeline data into memory, another part in charge of generating XML/HTML from this
        // structure
        int count = 200;
        // XWikiDocument curdoc = xwiki.getDocument("", context);
        TreeMap<Long, String> sortedEvents = new TreeMap<Long, String>();

        // Utility class for mail archive
        // def tools = xwiki.parseGroovyFromPage('MailArchiveCode.ToolsGroovyClass');

        // Set loading user in context (for rights)
        String loadingUser = config.getLoadingUser();
        context.setUserReference(docResolver.resolve(loadingUser));

        try {
            // Add Topics durations
            String xwql =
                "select doc.fullName, topic.author, topic.subject, topic.topicid, topic.startdate, topic.lastupdatedate, topic.type from Document doc, doc.object("
                    + XWikiPersistence.CLASS_TOPICS + ") as topic order by topic.lastupdatedate desc";
            List<Object[]> result = queryManager.createQuery(xwql, Query.XWQL).setLimit(count).execute();

            for (Object[] item : result) {
                XWikiDocument doc = null;
                try {
                    String author = (String) item[1];
                    String subject = (String) item[2];
                    String docurl = (String) item[0];
                    String topicId = (String) item[3];
                    String icon = "";
                    String action = "";
                    String link = "";

                    Date date = (Date) item[4];
                    Date end = (Date) item[5];
                    if (date != null && date.equals(end)) {
                        // Add 10 min just to see the tape
                        end.setTime(end.getTime() + 600000);
                    }
                    String extract = getTopicMails(topicId, subject);
                    String type = (String) item[6];
                    String tags = "";

                    doc = xwiki.getDocument(docResolver.resolve(docurl), context);
                    // FIXME not generic
                    if (doc.getTags(context).contains("GGS_WW_")) {
                        tags = "GGS_WW";
                    }
                    for (String tag : doc.getTagsList(context))/* TODO .grep("^Community.*$").each() */{
                        Pattern pattern = Pattern.compile("^Community.*$");
                        Matcher matcher = pattern.matcher(tag);
                        if (matcher.matches()) {
                            if (tags == "GGS_WW") {
                                tags = "";
                            }

                            tags += tag + ' ';
                        }
                    }

                    if (type == "Mail") {
                        if (!xwiki.getDocument(doc.getCreatorReference(), context).isNew()) {
                            icon =
                                xwiki.getDocument(doc.getCreatorReference(), context).getURL("download", context)
                                    + "/ldapavatar.jpg";
                        } else {
                            icon =
                                xwiki.getDocument(docResolver.resolve("XWiki.XWikiUserSheet"), context).getURL(
                                    "download", context)
                                    + "/noavatar.png";

                        }
                        action = "Topic created";
                        sortedEvents.put(
                            date.getTime(),
                            new StringBuilder("<event start=\"")
                                .append(date.toLocaleString())
                                .append("\" end=\"")
                                .append(end.toLocaleString())
                                .append("\" title=\"")
                                .append(StringEscapeUtils.escapeXml(subject))
                                .append("\" icon=\"")
                                .append(icon)
                                .append("\" image=\"")
                                .append(icon)
                                .append("\" classname=\"")
                                .append(tags)
                                .append("\" durationEvent=\"true\" link=\"")
                                .append(doc.getURL("view", context))
                                .append("\">")
                                .append(
                                    StringEscapeUtils.escapeXml((!tags.equals("") ? "<span class=\"tape-" + tags
                                        + "\">___</span> " + tags + "<br/>" : "")
                                        + "<br/> " + action + " by " + author + "<br/> " + extract)).append("</event>")
                                .toString());

                    } else {
                        // FIXME use new types and not non-generic types...
                        if (type == "Newsletter") {
                            action = "Newsletter posted";
                            // FIXME XWiki.GSESkin is not generic ...
                            icon =
                                xwiki.getDocument(docResolver.resolve("XWiki.GSESkin"), context).getURL("download",
                                    context)
                                    + "/667%2D2_pupils_speech.png";
                        }
                        if (type == "Product Release") {
                            action = "Product Release published";
                            // FIXME direct url to resources is horrible
                            icon = "http://r-wikiggs.gemalto.com/xwiki/resources/icons/silk/cd.gif";
                        }
                        link =
                            xwiki.getDocument(
                                docResolver.resolve("IMailArchive.M"
                                    + doc.getName().substring(1, doc.getName().length())), context).getURL("view",
                                context);
                        sortedEvents.put(
                            date.getTime(),
                            "<event start=\""
                                + date.toLocaleString()
                                + "\" title=\""
                                + StringEscapeUtils.escapeXml(subject)
                                + "\" icon=\""
                                + icon
                                + "\" image=\""
                                + icon
                                + "\" link=\""
                                + link
                                + "\" >"
                                + StringEscapeUtils.escapeXml((!tags.equals("") ? "<span class=\"tape-" + tags
                                    + "\">___</span> " + tags + "<br/>" : "")
                                    + "<br/>" + action + " by " + author + "<br/> " + extract) + "</event>");
                    }
                } catch (Throwable t) {
                    logger.warn("Exception for " + doc, t);
                }

            }

        } catch (Throwable e) {
            logger.warn("could not compute timeline data", e);
        }

        String data = null;
        try {
            data = toString(sortedEvents);
        } catch (Exception e) {
            logger.warn("Could not save timeline date", e);
        }

        return data;

    }

    private String toString(TreeMap<Long, String> sortedEvents)
    {
        StringBuilder content = new StringBuilder();
        content.append("<data>");
        for (Entry<Long, String> event : sortedEvents.entrySet()) {
            content.append(event.getValue() + '\n');
        }
        content.append("</data>");
        logger.debug("Loaded " + sortedEvents.size() + " into Timeline feed");
        return content.toString();

        /*
         * curdoc.addAttachment("TimeLineFeed-MailArchiver.xml", content.toString().getBytes(), context);
         * curdoc.saveAllAttachments(context); // TODO path to timeline file FileWriter fw = new
         * FileWriter("TimeLineFeed-MailArchiver.xml", false); fw.write(content.toString()); fw.close();
         */

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
    protected String getTopicMails(String topicid, String topicsubject) throws QueryException, XWikiException
    {
        // TODO there should/could be an api to retrieve mails related to a topic somewhere ...

        String returnVal = "";
        boolean first = true;
        String xwql_topic =
            "select doc.fullName, doc.author, mail.date, mail.messagesubject ,mail.from from Document doc, "
                + "doc.object(" + XWikiPersistence.CLASS_MAILS + ") as  mail where  mail.topicid='" + topicid
                + "' and doc.fullName<>'" + XWikiPersistence.TEMPLATE_MAILS + "' order by mail.date asc";
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
            returnVal +=
                "<a href=\"javascript:centerTimeline(" + maildate.getTime() + ");\">" + formatter.format(maildate)
                    + "</a> - <a href=\""
                    + xwiki.getDocument(docResolver.resolve(docfullname), context).getURL("view", context) + "\">"
                    + subject + "</a> - " + (link != null ? "<a href=\"" + link + "\">" : "") + user
                    + (link != null ? "</a> " : "") + "'<br/>";
            first = false;
        }

        return returnVal;

    }

}
