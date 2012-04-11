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
package org.xwiki.component.mailarchive.internal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.mailarchive.MailArchive;
import org.xwiki.component.mailarchive.MailType;
import org.xwiki.component.mailarchive.internal.data.ConnectionErrors;
import org.xwiki.component.mailarchive.internal.data.MailArchiveFactory;
import org.xwiki.component.mailarchive.internal.data.MailItem;
import org.xwiki.component.mailarchive.internal.data.MailServer;
import org.xwiki.component.mailarchive.internal.data.MailShortItem;
import org.xwiki.component.mailarchive.internal.data.TopicShortItem;
import org.xwiki.component.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Implementation of a <tt>MailArchive</tt> component.
 */
@Component
@Singleton
public class DefaultMailArchive implements MailArchive, Initializable
{

    public static final String SPACE_HOME = "MailArchive";

    public static final String SPACE_CODE = "MailArchiveCode";

    public static final String SPACE_PREFS = "MailArchivePrefs";

    public static final String SPACE_ITEMS = "MailArchiveItems";

    /** Provides access to the request context. Injected by the Component Manager. */
    @Inject
    private Execution execution;

    private XWikiContext context;

    private XWiki xwiki;

    /** Provides access to documents. Injected by the Component Manager. */
    @Inject
    private DocumentAccessBridge dab;

    /**
     * Secure query manager that performs checks on rights depending on the query being executed.
     */
    // TODO : @Requirement("secure") ??
    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    private MailArchiveStore store;

    private MailArchiveFactory factory;

    private boolean isInitialized = false;

    private static boolean inProgress = false;

    private List<MailServer> servers;

    private List<MailType> mailTypes;

    // Key is
    private HashMap<String, String[]> mailingLists;

    private HashMap<String, TopicShortItem> existingTopics;

    // Key
    private HashMap<String, MailShortItem> existingMessages;

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
        this.factory = new MailArchiveFactory(dab);
        this.store = new MailArchiveStore(queryManager, logger, factory);

        this.isInitialized = true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchive#checkMails()
     */
    @Override
    public int checkMails(String serverPrefsDoc)
    {
        // Retrieve connection properties from prefs
        MailServer server = factory.createMailServer(serverPrefsDoc);
        if (server == null) {
            logger.warn("Could not retrieve server information from wiki page " + serverPrefsDoc);
            return ConnectionErrors.INVALID_PREFERENCES.getCode();
        }

        return checkMails(server);
    }

    /**
     * @param server
     * @return
     */
    public int checkMails(MailServer server)
    {
        logger.info("Checking server " + server);

        int nbMessages = -1;
        Store store = null;
        try {
            // Get a session. Use a blank Properties object.
            Properties props = new Properties();
            // necessary to work with Gmail
            props.put("mail.imap.partialfetch", "false");
            props.put("mail.imaps.partialfetch", "false");
            props.put("mail.store.protocol", server.getProtocol());

            Session session = Session.getDefaultInstance(props, null);
            // Get a Store object
            store = session.getStore(server.getProtocol());

            // Connect to the mail account
            store.connect(server.getHost(), server.getPort(), server.getUser(), server.getPassword());
            Folder fldr;
            // Specifically for GMAIL ...
            if (server.getHost().endsWith(".gmail.com")) {
                fldr = store.getDefaultFolder();
            }

            fldr = store.getFolder(server.getFolder());
            fldr.open(Folder.READ_ONLY);

            // Searches for mails not already read
            FlagTerm searchterms = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] messages = fldr.search(searchterms);
            nbMessages = messages.length;

        } catch (AuthenticationFailedException e) {
            logger.warn("checkMails : ", e);
            return ConnectionErrors.AUTHENTICATION_FAILED.getCode();
        } catch (FolderNotFoundException e) {
            logger.warn("checkMails : ", e);
            return ConnectionErrors.FOLDER_NOT_FOUND.getCode();
        } catch (MessagingException e) {
            logger.warn("checkMails : ", e);
            if (e.getCause() instanceof java.net.UnknownHostException) {
                return ConnectionErrors.UNKNOWN_HOST.getCode();
            } else {
                return ConnectionErrors.CONNECTION_ERROR.getCode();
            }
        } catch (IllegalStateException e) {
            return ConnectionErrors.ILLEGAL_STATE.getCode();
        } catch (Throwable t) {
            logger.warn("checkMails : ", t);
            return ConnectionErrors.UNEXPECTED_EXCEPTION.getCode();
        } finally {
            try {
                store.close();
            } catch (MessagingException e) {
                logger.debug("checkMails : Could not close connection", e);
            }
        }
        logger.debug("checkMails : " + nbMessages + " messages to be read from " + server);

        // Persist state in db

        try {
            logger.debug("Updating server state in " + server.getWikiDoc());
            logger.warn("Context.getWiki " + context.getWiki());
            logger.warn("getWikiDoc " + server.getWikiDoc());
            XWikiDocument serverDoc = context.getWiki().getDocument(server.getWikiDoc(), context);
            BaseObject serverObj = serverDoc.getObject(SPACE_CODE + ".ServerSettingsClass");
            serverObj.set("status", nbMessages, context);
            serverObj.setDateValue("lasttest", new Date());
        } catch (Exception e) {
            logger.info("Failed to persist server connection state", e);
        }

        return nbMessages;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchive#loadMails(int, boolean)
     */
    @Override
    public boolean loadMails(int maxMailsNb)
    {
        logger.info("Starting new MAIL loading session...");
        if (!inProgress) {
            inProgress = true;
            try {
                // Init
                if (!this.isInitialized) {
                    initialize();
                }

                servers = store.loadServersDefinitions();
                mailTypes = store.loadMailTypesDefinitions();
                mailingLists = store.loadMailingListsDefinitions();
                existingTopics = store.loadStoredTopics();
                existingMessages = store.loadStoredMessages();

                for (MailServer server : servers) {
                    logger.info("Loading mails from server " + server);
                    try {
                        Message[] messages = loadMailsFromServer(server);
                        logger.warn("Returned number of messages to treat : " + messages.length);
                        int currentMsg = 0;
                        while (currentMsg < maxMailsNb && currentMsg < messages.length) {
                            try {
                                MailItem mail = MailItem.fromMessage(messages[currentMsg]);
                                logger.warn("SERVER " + server + " PARSED MAIL  " + currentMsg + " : " + mail);
                                loadMail(mail, true, false, null);
                                currentMsg++;
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Could not load emails from server " + server);
                    }
                }

            } catch (MailArchiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (InitializationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

            return true;
        } else {
            logger.info("Loading process already in progress ...");
            return false;
        }

    }

    /**
     * @param existingTopics
     * @param existingMessages
     * @param message
     * @param confirm
     * @param isAttachedMail
     * @param parentMail
     * @throws XWikiException
     */
    public MailLoadingResult loadMail(MailItem m, boolean confirm, boolean isAttachedMail, String parentMail)
        throws XWikiException
    {
        XWikiDocument msgDoc = null;
        XWikiDocument topicDoc = null;

        logger.debug("Loading mail content into wiki objects");

        // set loading user for rights - loading user must have edit rights on MailArchive and MailArchiveCode spaces
        context.setUser(getLoadingUser());
        logger.debug("Loading user " + getLoadingUser() + " set in context");

        // Retrieve information for mailing-list from headers
        /*
         * RULES : - first message of topic : 1- "Thread-Topic" = "Subject" OR 2- min(Date) OR 3- not exist(In-Reply-To)
         * - 1 : subject or topic can be null or '' ? --> NO - 3 : seems to be the best - Topic Id =
         * Thread-Index.substring(0,30) - Tree structure : - find first message of topic = firstMsg - for each msg in
         * (In-Reply-To(msg)=Message-ID(firstMsg)) order by Date(msg) - increase level - display msg - for each msg2 in
         * (In-Reply-To(msg2)=Message-ID(msg)) --> Recursivity - decrease level
         */

        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ", m.getLocale());

        // Create a new topic if needed
        String existingTopicId = "";
        // we don't create new topics for attached emails
        if (!isAttachedMail) {
            existingTopicId = existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId());
            if (existingTopicId == null) {
                logger.debug("  did not find existing topic, creating a new one");
                if (existingTopics.containsKey(m.getTopicId())) {
                    logger.debug("  new topic but topicId already loaded, using messageId as new topicId");
                    m.setTopicId(m.getMessageId());
                }
                existingTopicId = m.getTopicId();
                topicDoc = createTopicPage(m, dateFormatter, confirm);

                logger.debug("  loaded new topic " + topicDoc);
            } else if (similarSubjects(m.getTopic(), existingTopics.get(existingTopicId).getSubject())) {
                logger.debug("  topic already loaded " + m.getTopicId() + " : " + existingTopics.get(existingTopicId));
                topicDoc = updateTopicPage(m, existingTopicId, dateFormatter, confirm);
            } else {
                logger.debug("  found existing topic but subjects are too different, using new messageid as topicid ["
                    + m.getMessageId() + "]");
                m.setTopicId(m.getMessageId());
                m.setReplyToId("");
                existingTopicId = existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId());
                logger.debug("  creating new topic");
                topicDoc = createTopicPage(m, dateFormatter, confirm);
            }
        } // if not attached email

        // Create a new message if needed
        if (!existingMessages.containsKey(m.getMessageId())) {
            logger.debug("creating new message " + m.getMessageId() + " ...");
            /*
             * Note : use already existing topic id if any, instead of the one from the message, to keep an easy to
             * parse link between thread messages
             */
            if ("".equals(existingTopicId)) {
                existingTopicId = m.getTopicId();
            }
            // Note : correction bug of messages linked to same topic but with different topicIds
            m.setTopicId(existingTopicId);
            msgDoc = createMailPage(m, existingTopicId, isAttachedMail, parentMail, confirm);

            return new MailLoadingResult(true, topicDoc != null ? topicDoc.getFullName() : null, msgDoc != null
                ? msgDoc.getFullName() : null);
        } else {
            // message already loaded
            logger.debug("Mail already loaded - checking for updates ...");

            MailShortItem msg = existingMessages.get(m.getMessageId());
            logger.debug("TopicId of existing message " + msg.getTopicId() + " and of topic " + existingTopicId
                + " are different ?" + (!msg.getTopicId().equals(existingTopicId)));
            if (!msg.getTopicId().equals(existingTopicId)) {
                msgDoc = xwiki.getDocument(existingMessages.get(m.getMessageId()).getFullName(), context);
                BaseObject msgObj = msgDoc.getObject("MailArchiveCode.MailClass");
                msgObj.set("topicid", existingTopicId, context);
                if (confirm) {
                    logger.debug("saving message " + m.getSubject());
                    saveAsUser(msgDoc, null, getLoadingUser(), "Updated mail with existing topic id found");
                }
            }

            return new MailLoadingResult(true, topicDoc != null ? topicDoc.getFullName() : null, msgDoc != null
                ? msgDoc.getFullName() : null);
        }
    }

    /**
     * @return
     */
    public String getLoadingUser()
    {
        // TODO : retrieve loading user from prefs
        return "XWiki.Admin";
    }

    /**
     * createTopicPage Creates a wiki page for a Topic.
     * 
     * @throws XWikiException
     */
    protected XWikiDocument createTopicPage(MailItem m, SimpleDateFormat dateFormatter, boolean create)
        throws XWikiException
    {

        XWikiDocument topicDoc;

        String topicwikiname = context.getWiki().clearName("T" + m.getTopic().replaceAll(" ", ""), context);
        if (topicwikiname.length() >= 30) {
            topicwikiname = topicwikiname.substring(0, 30);
        }
        String pagename = context.getWiki().getUniquePageName("MailArchive", topicwikiname, context);
        topicDoc = xwiki.getDocument("MailArchive." + pagename, context);
        BaseObject topicObj = topicDoc.newObject("MailArchiveCode.MailTopicClass", context);

        topicObj.set("topicid", m.getTopicId(), context);
        topicObj.set("subject", m.getTopic(), context);
        // Note : we always add author and stardate at topic creation because anyway we will update this later if
        // needed,
        // to avoid topics with "unknown" author
        logger.debug("adding startdate and author to topic");
        topicObj.set("startdate", dateFormatter.format(m.getDecodedDate()), context);
        topicObj.set("author", m.getFrom(), context);

        // when first created, we put the same date as start date
        topicObj.set("lastupdatedate", dateFormatter.format(m.getDecodedDate()), context);
        topicDoc.setCreationDate(m.getDecodedDate());
        topicDoc.setDate(m.getDecodedDate());
        topicDoc.setContentUpdateDate(m.getDecodedDate());

        topicObj.set("type", m.getType(), context);
        topicDoc.setParent("MailArchive.WebHome");
        topicDoc.setTitle("Topic " + m.getTopic());
        topicDoc.setComment("Created topic from mail [" + m.getMessageId() + "]");

        // Materialize mailing-lists information and mail Type in Tags
        String taglist = parseTags(m);
        if (!"".equals(taglist) && !"".equals(m.getType())) {
            taglist += ",";
        }
        taglist += m.getType();

        if (!"".equals(taglist)) {
            BaseObject tagobj = topicDoc.newObject("XWiki.TagClass", context);
            tagobj.set("tags", taglist.replaceAll(" ", "_"), context);
        }

        if (create) {
            saveAsUser(topicDoc, m.getWikiuser(), null /* TODO getLoadingUser() */,
                "Created topic from mail [" + m.getMessageId() + "]");
        }
        // add the existing topic created to the map
        existingTopics.put(m.getTopicId(), new TopicShortItem(topicDoc.getFullName(), m.getTopic()));

        return topicDoc;
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
     */
    protected XWikiDocument updateTopicPage(MailItem m, String existingTopicId, SimpleDateFormat dateFormatter,
        boolean create)
    {
        // addDebug("updateTopicPage(${existingTopicId})")
        //
        // def newuser = null
        // def topicDoc = xwiki.getDocument(existingTopics[existingTopicId][0])
        // addDebug("Existing topic ${topicDoc}")
        // def topicObj = topicDoc.getObject("MailArchiveCode.MailTopicClass")
        // def lastupdatedate = topicObj.lastupdatedate
        // def startdate = topicObj.startdate
        // def originalAuthor = topicObj.author
        // if (lastupdatedate == null || lastupdatedate == "") { lastupdatedate = m.date } // note : this should never
        // occur
        // if (startdate == null || startdate == "") { startdate = m.date }
        // def decodedlastupdatedate = dateFormatter.parse(lastupdatedate)
        // def decodedstartdate = dateFormatter.parse(startdate)
        //
        // def isMoreRecent = (m.decodedDate.getTime() > decodedlastupdatedate.getTime())
        // def isMoreAncient = (m.decodedDate.getTime() < decodedstartdate.getTime())
        // addDebug("decodedDate = ${m.decodedDate.getTime()}, lastupdatedate = ${decodedlastupdatedate.getTime()}, is more recent = ${isMoreRecent}, first in topic = ${m.isFirstInTopic}")
        // addDebug("lastupdatedate $decodedlastupdatedate")
        // addDebug("current mail date $m.decodedDate")
        //
        // // If the first one, we add the startdate to existing topic
        // if (m.isFirstInTopic || isMoreRecent)
        // {
        // def dirty = false
        // addDebug("Checking if existing topic has to be updated ...")
        // def comment = ""
        // // if (m.isFirstInTopic) {
        // if ((originalAuthor != m.from && isMoreAncient) || originalAuthor == "")
        // {
        // addDebug("     updating author from ${originalAuthor} to ${m.from}")
        // topicDoc.set("author", m.from)
        // comment += " Updated author "
        // newuser = parseUser(m.from)
        // if (newuser == null || newuser == "") {
        // newuser = unknownUser
        // }
        // dirty = true
        // }
        // addDebug("     existing startdate $topicObj.startdate")
        // if ((topicObj.startdate == null || topicObj.startdate == "") || isMoreAncient)
        // {
        // addDebug("     checked startdate not already added to topic")
        // topicDoc.set("startdate", dateFormatter.format(m.decodedDate), topicObj)
        // topicDoc.document.setCreationDate(m.decodedDate)
        // comment += " Updated start date "
        // dirty = true
        // }
        // // }
        // if (isMoreRecent) {
        // addDebug("     updating lastupdatedate from $topicObj.lastupdatedate to " +
        // dateFormatter.format(m.decodedDate))
        // topicDoc.set("lastupdatedate", dateFormatter.format(m.decodedDate), topicObj)
        // topicDoc.document.setDate(m.decodedDate)
        // topicDoc.document.setContentUpdateDate(m.decodedDate)
        // newuser = parseUser(m.from)
        // comment += " Updated last update date "
        // dirty = true
        // }
        // topicDoc.setComment(comment)
        //
        // if (create && dirty) {
        // addDebug("     Updated existing topic")
        // saveAsUser(topicDoc, newuser, getLoadingUser(), comment)
        // }
        // existingTopics[m.topicId] = [topicDoc.fullName, topicObj.subject]
        // } else
        // {
        // addDebug("     Nothing to update in topic")
        // }

        // return topicDoc

        return null;
    }

    /**
     * createMailPage Creates a wiki page for a Mail.
     */
    protected XWikiDocument createMailPage(MailItem m, String existingTopicId, boolean isAttachedMail,
        String parentMail, boolean create)
    {
        // def msgDoc
        // def content = ""
        // def htmlcontent = ""
        // def zippedhtmlcontent = ""
        // def attachedMails = [:]
        // // a map to store attachment filename = contentId for replacements in HTML retrieved from mails
        // def attachmentsMap = [:]
        // def attbodyparts = []
        //
        // def prefix = 'M'
        // if (isAttachedMail) {
        // prefix = 'A'
        // }
        // def msgwikiname = xwiki.clearName(prefix + m.topic.replaceAll(" ",""))
        // if (msgwikiname.length() >= 30) {
        // msgwikiname = msgwikiname.substring(0,30)
        // }
        // def pagename = xwiki.getUniquePageName("MailArchive", msgwikiname )
        // msgDoc = xwiki.getDocument("MailArchive.${pagename}")
        // addDebug("NEW MSG msgwikiname=$msgwikiname pagename=$pagename")
        //
        // def bodypart = m.bodypart
        // addDebug("bodypart class " + bodypart.class)
        // // addDebug("mail content type " + m.contentType)
        // // Retrieve mail body(ies)
        // if (m.contentType.contains("pkcs7-mime")
        // || m.contentType.contains("multipart/encrypted")) {
        // content =
        // "<<<This e-mail was encrypted. Text Content and attachments of encrypted e-mails are not publshed in Mail Archiver to avoid disclosure of restricted or confidential information.>>>"
        // htmlcontent =
        // "<i>&lt;&lt;&lt;This e-mail was encrypted. Text Content and attachments of encrypted e-mails are not publshed in Mail Archiver to avoid disclosure of restricted or confidential information.&gt;&gt;&gt;</i>"
        // attachedMails = []
        // m.sensitivity = "encrypted"
        // } else if (bodypart instanceof String)
        // {
        // content = MimeUtility.decodeText(bodypart)
        //
        // } else
        // {
        // addDebug("Fetching plain text content ...")
        // content = getMailContent(bodypart)
        // addDebug("Fetching HTML content ...")
        // htmlcontent = getMailContentHtml(bodypart, 0)
        // addDebug("Fetching attached mails ...")
        // attachedMails = getMailContentAttachedMails(bodypart, msgDoc.fullName)
        //
        // addDebug("Fetching attachments from mail")
        // def nbatts = getMailAttachments(bodypart, attbodyparts)
        // addDebug("FOUND $nbatts attachments to add")
        // addDebug("Retrieving contentIds ...")
        // fillAttachmentContentIds (attbodyparts, attachmentsMap)
        // }
        //
        // // Truncate body
        // content = truncateStringForBytes(content, 65500, 65500)
        //
        // /* Treat HTML parts ... */
        // zippedhtmlcontent = treatHtml(msgDoc, htmlcontent, attachmentsMap)
        //
        // // Treat lengths
        // if (m.messageId.length()>255) { m.messageId = m.messageId[0..254] }
        // if (m.subject.length()>255) { m.subject= m.subject[0..254] }
        // if (existingTopicId.length()>255) { existingTopicId= existingTopicId[0..254] }
        // if (m.topicId.length()>255) { m.topicId= m.topicId[0..254] }
        // if (m.topic.length()>255) { m.topic= m.topic[0..254] }
        // // largestrings : normally 65535, but we don't know the size of the largestring itself
        // if (m.replyToId.length()>65500) { m.replyToId= m.replyToId[0..65499] }
        // if (m.refs.length()>65500) { m.refs= m.refs[0..65499] }
        // if (m.from.length()>65500) { m.from= m.from[0..65499] }
        // if (m.to.length()>65500) { m.to= m.to[0..65499] }
        // if (m.cc.length()>65500) { m.cc= m.cc[0..65499] }
        //
        // if ((content == null || content == "") && (htmlcontent != null && htmlcontent != "")) {
        // def converted = null
        // try {
        // def xdom = services.rendering.parse(htmlcontent, "html/4.01")
        // converted = services.rendering.render(xdom, "plain/1.0")
        // } catch (Throwable t) {
        // addDebug("Conversion from HTML to plain text thrown exception ${t}")
        // t.printStackTrace()
        // converted = null
        // }
        // if (converted != null && converted != "") {
        // // replace content with value (remove excessive whitespace also)
        // content = converted.replaceAll(~/[\s]{2,}/, "\n")
        // addDebug("Text body now contains converted html content")
        // } else {
        // addDebug("Conversion from HTML to Plain Text returned empty or null string")
        // }
        // }
        //
        // // Fill all new object's fields
        // def msgObj = msgDoc.newObject("MailArchiveCode.MailClass")
        // msgObj.set("messageid", m.messageId)
        // msgObj.set("messagesubject", m.subject)
        //
        // msgObj.set("topicid", existingTopicId)
        // msgObj.set("topicsubject", m.topic)
        // msgObj.set("inreplyto", m.replyToId)
        // msgObj.set("references", m.refs)
        // msgObj.set("date", m.decodedDate)
        // msgDoc.document.setCreationDate(m.decodedDate)
        // msgDoc.document.setDate(m.decodedDate)
        // msgDoc.document.setContentUpdateDate(m.decodedDate)
        // msgObj.set("from", m.from)
        // msgObj.set("to", m.to)
        // msgObj.set("cc", m.cc)
        // msgObj.set("body", content)
        // msgObj.set("bodyhtml", zippedhtmlcontent)
        // msgObj.set("sensitivity", m.sensitivity)
        // if (attachedMails.size() != 0) {
        // msgObj.set("attachedMails", attachedMails.grep(~/^MailArchive\..*$/).join(','))
        // }
        // if (!isAttachedMail) {
        // if (m.isFirstInTopic)
        // {
        // msgObj.set("type", m.type)
        // } else
        // {
        // msgObj.set("type", "Mail")
        // }
        // } else {
        // msgObj.set("type", "Attached Mail")
        // }
        // if (parentMail != null) {
        // msgDoc.setParent(parentMail)
        // } else if (existingTopics[m.topicId] != null) {
        // msgDoc.setParent(existingTopics[m.topicId][0])
        // }
        // //msgDoc.setContent(xwiki.getDocument("MailArchiveCode.MailClassTemplate").getContent())
        // msgDoc.setTitle("Message ${m.subject}")
        // if (!isAttachedMail) {
        // msgDoc.setComment("Created message from mailing-list from folder ${mailingListFolder}")
        // } else {
        // msgDoc.setComment("Attached mail created")
        // }
        // def tag = parseTags(m, threadsMap)
        // if (tag != "")
        // {
        // msgDoc.use(msgDoc.newObject("XWiki.TagClass"))
        // msgDoc.set("tags", tag.replaceAll(" ", "_") )
        // }
        //
        // if (create && !checkMsgIdExistence(m.messageId)) {
        // addDebug("saving message ${m.subject}")
        // saveAsUser(msgDoc, m.wikiuser, getLoadingUser(),
        // "Created message from mailing-list from folder ${mailingListFolder}")
        // }
        // existingMessages[m.messageId] = [m.subject, existingTopicId, msgDoc.fullName]
        // addDebug("  mail loaded and saved with id $m.messageId, subject=$m.subject topicid=$m.topicId topicsubject=$m.topic replyto=$m.replyToId references=$m.refs date=$m.decodedDate from=$m.from")
        //
        // addDebug("adding attachments to document")
        // addAttachmentsFromMail(msgDoc, attbodyparts, attachmentsMap, context)
        //
        // return msgDoc
        return null;
    }

    /**
     * @param m
     * @return
     */
    protected String parseTags(MailItem m)
    {
        String taglist = "";

        for (Entry<String, String[]> list : mailingLists.entrySet()) {
            if (m.getFrom().contains(list.getKey()) || m.getTo().contains(list.getKey())
                || m.getCc().contains(list.getKey())) {
                if (!"".equals(taglist)) {
                    taglist += ",";
                }
                // Add tag of this mailing-list to the list of tags
                taglist += list.getValue()[1];
            }
        }

        return taglist;
    }

    /**
     * @param doc
     * @param user
     * @param contentUser
     * @param comment
     * @throws XWikiException
     */
    protected void saveAsUser(final XWikiDocument doc, final String user, final String contentUser, final String comment)
        throws XWikiException
    {
        String luser = user;
        // If user is not provided we leave existing one
        if (user == null) {
            luser = doc.getCreator();
        }
        // We set creator only at document creation
        if (doc.getCreator() == null || "".equals(doc.getCreator())) {
            doc.setCreator(luser);
        }
        doc.setAuthor(luser);
        doc.setContentAuthor(contentUser);
        doc.setContentDirty(false);
        doc.setMetaDataDirty(false);
        xwiki.getXWiki(context).saveDocument(doc, comment, context);
    }

    /**
     * Returns the topicId of already existing topic for this topic id or subject. If no topic with this id or subject
     * is found, try to search for a message for wich msgid = replyid of new msg, then attach this new msg to the same
     * topic. If there is no existing topic, returns null. Search topic with same subject only if inreplyto is not
     * empty, meaning it's not supposed to be the first message of another topic.
     * 
     * @param topicId
     * @param topicSubject
     * @param inreplyto
     * @return
     */
    public String existsTopic(String topicId, String topicSubject, String inreplyto)
    {
        String foundTopicId = null;
        String replyId = inreplyto;
        String previous = "";
        String previousSubject = topicSubject;
        boolean quit = false;

        // Search in existing messages for existing msg id = new reply id, and grab topic id
        // search replies until root message
        while (existingMessages.containsKey(replyId) && existingMessages.get(replyId) != null && !quit) {
            XWikiDocument msgDoc = null;
            try {
                msgDoc = context.getWiki().getDocument(existingMessages.get(replyId).getFullName(), context);
            } catch (XWikiException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (msgDoc != null) {
                BaseObject msgObj = msgDoc.getObject(SPACE_CODE + ".MailClass");
                if (msgObj != null) {
                    logger
                        .debug("existsTopic : message " + replyId + " is a reply to " + existingMessages.get(replyId));
                    if (similarSubjects(previousSubject, msgObj.getStringValue("topicsubject"))) {
                        previous = replyId;
                        replyId = msgObj.getStringValue("inreplyto");
                        previousSubject = msgObj.getStringValue("topicSubject");
                    } else {
                        logger.debug("existsTopic : existing message subject is too different, exiting loop");
                        quit = true;
                    }
                } else {
                    replyId = null;
                }
            } else {
                replyId = null;
            }
        }
        if (replyId != inreplyto && replyId != null) {
            logger
                .debug("existsTopic : found existing message that current message is a reply to, to attach to same topic id");
            foundTopicId = existingMessages.get(previous).getTopicId();
            logger.debug("existsTopic : Found topic id " + foundTopicId);
        } else {
            // Search in existing topics with id
            if (existingTopics.containsKey(topicId)) {
                logger.debug("existsTopic : found topic id in loaded topics");
                if (similarSubjects(topicSubject, existingTopics.get(topicId).getSubject())) {
                    foundTopicId = topicId;
                } else {
                    logger.debug("... but subjects are too different");
                }
            }
            if (foundTopicId == null) {
                // Search in existing topics with exactly same subject
                for (String currentTopicId : existingTopics.keySet()) {
                    TopicShortItem currentTopic = existingTopics.get(currentTopicId);
                    if (currentTopic.getSubject().trim().equalsIgnoreCase(topicSubject.trim())) {
                        logger.debug("existsTopic : found subject in loaded topics");
                        if (!"".equals(inreplyto)) {
                            foundTopicId = currentTopicId;
                        } else {
                            logger.debug("existsTopic : found a topic but it's first message in topic");
                            // Note : desperate tentative to attach this message to an existing topic
                            // instead of creating a new one ... Sometimes replyId and refs can be
                            // empty even if this is a reply to something already loaded, in this
                            // case we just check if topicId was already loaded once, even if not
                            // the same topic ...
                            if (existingTopics.containsKey(topicId)) {
                                logger
                                    .debug("existsTopic : ... but we 'saw' this topicId before, so attach to found topicId "
                                        + currentTopicId + " with same subject");
                                foundTopicId = currentTopicId;
                            }
                        }

                    }
                }
            }
        }

        return foundTopicId;
    }

    /**
     * Compare 2 strings for similarity Returns true if strings can be considered similar enough<br/>
     * - s1 and s2 have a levenshtein distance < 25% <br/>
     * - s1 or s2 begins with s2 or s1 respectively
     * 
     * @param s1
     * @param s2
     * @return
     */
    private boolean similarSubjects(String s1, String s2)
    {
        logger.debug("similarSubjects : comparing [" + s1 + "] and [" + s2 + "]");
        s1 = s1.replaceAll("^([Rr][Ee]:|[Ff][Ww]:)(.*)$", "$2");
        s2 = s2.replaceAll("^([Rr][Ee]:|[Ff][Ww]:)(.*)$", "$2");
        if (s1 == s2) {
            logger.debug("similarSubjects : subjects are equal");
            return true;
        }
        try {
            double d = getLevenshteinDistance(s1, s2);
            logger.debug("similarSubjects : Levenshtein distance d=" + d);
            if (d <= 0.25) {
                logger.debug("similarSubjects : subjects are considered similar because d <= 0.25");
                return true;
            }
        } catch (IllegalArgumentException iaE) {
            return false;
        }
        if (s1.startsWith(s2) || s2.startsWith(s1)) {
            logger.debug("similarSubjects : subjects are considered similar because one start with the other");
            return true;
        }
        return false;
    }

    /**
     * @param s
     * @param t
     * @return
     */
    protected double getLevenshteinDistance(String s, String t)
    {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        /*
         * The difference between this impl. and the previous is that, rather than creating and retaining a matrix of
         * size s.length()+1 by t.length()+1, we maintain two single-dimensional arrays of length s.length()+1. The
         * first, d, is the 'current working' distance array that maintains the newest distance cost counts as we
         * iterate through the characters of String s. Each time we increment the index of String t we are comparing, d
         * is copied to p, the second int[]. Doing so allows us to retain the previous cost counts as required by the
         * algorithm (taking the minimum of the cost count to the left, up one, and diagonally up and to the left of the
         * current cost count being calculated). (Note that the arrays aren't really copied anymore, just
         * switched...this is clearly much better than cloning an array or doing a System.arraycopy() each time through
         * the outer loop.) Effectively, the difference between the two implementations is this one does not cause an
         * out of memory condition when calculating the LD over two very large strings.
         */

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int[] p = new int[n + 1]; // 'previous' cost array, horizontally
        int[] d = new int[n + 1]; // cost array, horizontally
        int[] _d; // placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        System.out.println("" + p[n] + " max " + Math.max(s.length(), t.length()));

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return (double) (p[n]) / ((double) Math.max(s.length(), t.length()));
    }

    /**
     * @param server
     * @return
     * @throws MailArchiveException
     */
    public Message[] loadMailsFromServer(MailServer server) throws MailArchiveException
    {
        assert (server != null);

        Message[] messages = new Message[] {};

        if (checkMails(server) >= 0) {

            try {
                logger.info("Trying to retrieve mails from server " + server.toString());
                // Get a session. Use a blank Properties object.
                Properties props = new Properties();
                // necessary to work with Gmail
                props.put("mail.imap.partialfetch", "false");
                props.put("mail.imaps.partialfetch", "false");
                Session session = Session.getInstance(props);
                // Get a Store object
                Store store = session.getStore(server.getProtocol());

                // Connect to the mail account
                store.connect(server.getHost(), server.getPort(), server.getUser(), server.getPassword());
                Folder fldr;
                // Specifically for GMAIL ...
                if (server.getHost().endsWith(".gmail.com")) {
                    fldr = store.getDefaultFolder();
                }
                fldr = store.getFolder(server.getFolder());

                fldr.open(Folder.READ_WRITE);

                // Searches for mails not already read
                FlagTerm searchterms = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                messages = fldr.search(searchterms);
            } catch (Exception e) {
                throw new MailArchiveException("Could not connect to server " + server, e);
            }
        } else {
            throw new MailArchiveException("Connection to server checked as failed, not trying to load mails");
        }

        logger.info("Found " + messages.length + " messages");

        return messages;

    }

}
