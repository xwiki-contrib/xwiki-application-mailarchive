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
package org.xwiki.contrib.mailarchive.internal.persistence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge;

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
public class XWikiPersistence implements IPersistence, Initializable
{
    /**
     * XWiki profile name of a non-existing user.
     */
    public static final String UNKNOWN_USER = "XWiki.UserDoesNotExist";

    /**
     * Name of the space that contains end-user targeted pages.
     */
    public static final String SPACE_HOME = "MailArchive";

    /**
     * Name of the space that contains technical code.
     */
    public static final String SPACE_CODE = "MailArchiveCode";

    /**
     * Name of the space that contains configuration / preferences
     */
    public static final String SPACE_PREFS = "MailArchivePrefs";

    /**
     * Name of the space that contains created objects
     */
    public static String SPACE_ITEMS = "MailArchiveItems";

    public static final String CLASS_TOPICS = SPACE_CODE + ".MailTopicClass";

    public static final String CLASS_MAILS = SPACE_CODE + ".MailClass";

    public static final String CLASS_MAIL_TYPES = SPACE_CODE + ".TypesSettingsClass";

    public static final String CLASS_MAIL_MATCHERS = SPACE_CODE + ".MailMatcherClass";

    public static final String CLASS_MAIL_LISTS = SPACE_CODE + ".ListsSettingsClass";

    public static final String CLASS_MAIL_SERVERS = SPACE_CODE + ".ServerSettingsClass";

    public static final String CLASS_MAIL_STORES = SPACE_CODE + ".StoreClass";

    public static final String CLASS_LOADING_SESSION = SPACE_CODE + ".LoadingSessionClass";

    public static final String TEMPLATE_MAILS = SPACE_CODE + ".MailClassTemplate";

    public static final String PAGE_GLOBAL_PARAMETERS = SPACE_PREFS + ".GlobalParameters";

    @Inject
    private Logger logger;

    @Inject
    private Execution execution;

    @Inject
    @Named("extended")
    private IExtendedDocumentAccessBridge bridge;

    private XWiki xwiki;

    private XWikiContext context;

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
     * createTopicPage Creates a wiki page for a Topic.
     * 
     * @throws XWikiException
     */
    @Override
    public String createTopic(final String pagename, final MailItem m, final ArrayList<String> taglist,
        final String loadingUser, final boolean create) throws XWikiException
    {
        final String uniquePageName = bridge.getValidUniqueName(pagename, SPACE_ITEMS);
        final XWikiDocument topicDoc = xwiki.getDocument(SPACE_ITEMS + "." + uniquePageName, context);
        BaseObject topicObj = topicDoc.newObject(SPACE_CODE + ".MailTopicClass", context);

        topicObj.set("topicid", m.getTopicId(), context);
        topicObj.set("subject", m.getTopic(), context);
        // Note : we always add author and stardate at topic creation because anyway we will update this later if
        // needed, to avoid topics with "unknown" author
        topicObj.set("startdate", m.getDate(), context);
        topicObj.set("author", m.getFrom(), context);

        // when first created, we put the same date as start date
        topicObj.set("lastupdatedate", m.getDate(), context);
        topicDoc.setCreationDate(m.getDate());
        topicDoc.setDate(m.getDate());
        topicDoc.setContentUpdateDate(m.getDate());
        topicObj.set("sensitivity", m.getSensitivity(), context);
        topicObj.set("importance", m.getImportance(), context);

        String types = StringUtils.join(m.getTypes().toArray(new String[] {}), ',');
        topicObj.set("type", types, context);
        topicDoc.setParent(SPACE_HOME + ".WebHome");
        topicDoc.setTitle("Topic " + m.getTopic());
        topicDoc.setComment("Created topic from mail [" + m.getMessageId() + "]");

        // Materialize mailing-lists information and mail IType in Tags
        if (taglist.size() > 0) {
            BaseObject tagobj = topicDoc.newObject("XWiki.TagClass", context);
            String tags = StringUtils.join(taglist.toArray(new String[] {}), ',');
            tagobj.set("tags", tags.replaceAll(" ", "_"), context);
        }

        if (create) {
            saveAsUser(topicDoc, m.getWikiuser(), loadingUser, "Created topic from mail [" + m.getMessageId() + "]");
        }

        return topicDoc.getFullName();
    }

    /**
     * updateTopicPage Update topic against new mail taking part to existing topic.
     */
    /**
     * @param m
     * @param existingTopicId
     * @param dateFormatter
     * @param create
     * @return
     * @throws XWikiException
     * @throws ParseException
     */
    @Override
    public String updateTopicPage(MailItem m, String existingTopicPage, SimpleDateFormat dateFormatter,
        final String loadingUser, boolean create) throws XWikiException
    {
        logger.debug("updateTopicPage(" + existingTopicPage + ")");

        XWikiDocument topicDoc = xwiki.getDocument(existingTopicPage, context);
        logger.debug("Existing topic " + topicDoc);
        BaseObject topicObj = topicDoc.getObject(XWikiPersistence.SPACE_CODE + ".MailTopicClass");
        Date lastupdatedate = topicObj.getDateValue("lastupdatedate");
        Date startdate = topicObj.getDateValue("startdate");
        String originalAuthor = topicObj.getStringValue("author");
        if (lastupdatedate == null || "".equals(lastupdatedate)) {
            lastupdatedate = m.getDate();
        } // note : this should never occur
        if (startdate == null || "".equals(startdate)) {
            startdate = m.getDate();
        }

        boolean isMoreRecent = (m.getDate().getTime() > lastupdatedate.getTime());
        boolean isMoreAncient = (m.getDate().getTime() < startdate.getTime());
        logger.debug("mail date = " + m.getDate().getTime() + ", last update date = " + lastupdatedate.getTime()
            + ", is more recent = " + isMoreRecent + ", is more ancient = " + isMoreAncient + ", first in topic = "
            + m.isFirstInTopic());

        // If the first one, we add the startdate to existing topic
        if (m.isFirstInTopic() || isMoreRecent) {
            boolean dirty = false;
            logger.debug("Checking if existing topic has to be updated ...");
            String comment = "";
            // if (m.isFirstInTopic) {
            if ((!originalAuthor.equals(m.getFrom()) && isMoreAncient) || "".equals(originalAuthor)) {
                logger.debug("     updating author from " + originalAuthor + " to " + m.getFrom());
                topicObj.set("author", m.getFrom(), context);
                comment += " Updated author ";
                dirty = true;
            }
            logger.debug("     existing startdate " + topicObj.getDateValue("startdate"));
            if ((topicObj.getStringValue("startdate") == null || "".equals(topicObj.getStringValue("startdate")))
                || isMoreAncient) {
                logger.debug("     checked startdate not already added to topic");
                topicObj.set("startdate", m.getDate(), context);
                topicDoc.setCreationDate(m.getDate());
                comment += " Updated start date ";
                dirty = true;
            }
            // }
            if (isMoreRecent) {
                logger.debug("     updating lastupdatedate from " + lastupdatedate + " to "
                    + dateFormatter.format(m.getDate()));
                topicObj.set("lastupdatedate", m.getDate(), context);
                topicDoc.setDate(m.getDate());
                topicDoc.setContentUpdateDate(m.getDate());

                comment += " Updated last update date ";
                dirty = true;
            }
            topicDoc.setComment(comment);

            if (create && dirty) {
                logger.debug("     Updated existing topic");
                saveAsUser(topicDoc, m.getWikiuser(), loadingUser, comment);
            }
        } else {
            logger.debug("     Nothing to update in topic");
        }

        // return topicDoc

        return topicDoc.getFullName();
    }

    @Override
    public void updateMailServerState(String serverPrefsDoc, int status) throws XWikiException
    {
        logger.debug("Updating server state in " + serverPrefsDoc);
        XWikiDocument serverDoc = context.getWiki().getDocument(serverPrefsDoc, context);
        BaseObject serverObj = serverDoc.getObject(CLASS_MAIL_SERVERS);
        serverObj.set("status", status, context);
        serverObj.setDateValue("lasttest", new Date());
        xwiki.saveDocument(serverDoc, context);
    }

    @Override
    public void updateMailStoreState(String storePrefsDoc, int status) throws XWikiException
    {
        logger.debug("Updating store state in " + storePrefsDoc);
        XWikiDocument serverDoc = context.getWiki().getDocument(storePrefsDoc, context);
        BaseObject serverObj = serverDoc.getObject(CLASS_MAIL_STORES);
        serverObj.set("status", status, context);
        serverObj.setDateValue("lasttest", new Date());
        xwiki.saveDocument(serverDoc, context);
    }

    /**
     * @param doc
     * @param user
     * @param contentUser
     * @param comment
     * @throws XWikiException
     */
    @Override
    public void saveAsUser(final XWikiDocument doc, final String user, final String contentUser, final String comment)
        throws XWikiException
    {
        String luser = user;
        // If user is not provided we leave existing one
        if (luser == null) {
            if (xwiki.exists(doc.getFullName(), context)) {
                luser = doc.getCreator();
            } else {
                luser = UNKNOWN_USER;
            }
        }
        // We set creator only at document creation
        if (!xwiki.exists(doc.getFullName(), context)) {
            doc.setCreator(luser);
        }
        doc.setAuthor(luser);
        doc.setContentAuthor(contentUser);
        // avoid automatic set of update date to current date
        doc.setContentDirty(false);
        doc.setMetaDataDirty(false);
        xwiki.getXWiki(context).saveDocument(doc, comment, context);
    }

}
