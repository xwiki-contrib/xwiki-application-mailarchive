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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;

import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.mailarchive.MailArchive;
import org.xwiki.component.mailarchive.MailArchiveConfiguration;
import org.xwiki.component.mailarchive.MailType;
import org.xwiki.component.mailarchive.internal.data.ConnectionErrors;
import org.xwiki.component.mailarchive.internal.data.MailArchiveConfigurationImpl;
import org.xwiki.component.mailarchive.internal.data.MailArchiveFactory;
import org.xwiki.component.mailarchive.internal.data.MailItem;
import org.xwiki.component.mailarchive.internal.data.MailServer;
import org.xwiki.component.mailarchive.internal.data.MailShortItem;
import org.xwiki.component.mailarchive.internal.data.TopicShortItem;
import org.xwiki.component.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.component.mailarchive.internal.threads.NewMessagesThreading;
import org.xwiki.component.mailarchive.internal.threads.ThreadableMessage;
import org.xwiki.component.mailarchive.internal.timeline.TimeLine;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
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

    public static String SPACE_ITEMS = "MailArchiveItems";

    public static final String unknownUser = "XWiki.UserDoesNotExist";

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

    /**
     * The component used to parse the XHTML obtained after cleaning, when transformations are not executed.
     */
    @Inject
    @Named("html/4.01")
    private StreamParser htmlStreamParser;

    /**
     * The component manager. We need it because we have to access some components dynamically based on the input
     * syntax.
     */
    @Inject
    private ComponentManager componentManager;

    private MailArchiveStore store;

    private MailArchiveFactory factory;

    private MailArchiveConfiguration config;

    private MailUtils mailutils;

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
        try {
            ExecutionContext context = execution.getContext();
            this.context = (XWikiContext) context.getProperty("xwikicontext");
            this.xwiki = this.context.getWiki();
            this.factory = new MailArchiveFactory(dab);
            this.store = new MailArchiveStore(queryManager, logger, factory);
            logger.error("Mail archive initiliazed !");
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            logger.error("Could not initiliaze mailarchive ", e);
            e.printStackTrace();
        }

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

    public ThreadableMessage computeThreads(String topicId)
    {
        NewMessagesThreading threads = new NewMessagesThreading(context, xwiki, queryManager, logger, mailutils);

        try {
            return threads.thread(topicId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchive#loadMails(int, boolean)
     */
    @Override
    public int loadMails(int maxMailsNb)
    {
        logger.info("Starting new MAIL loading session...");
        int nbMessages = 0;
        int currentMsg = 0;
        if (!inProgress) {
            inProgress = true;
            try {
                init();

                for (MailServer server : servers) {
                    logger.info("Loading mails from server " + server);
                    try {
                        Message[] messages = loadMailsFromServer(server);
                        logger.warn("Returned number of messages to treat : " + messages.length);
                        currentMsg = 0;
                        while ((currentMsg < maxMailsNb || maxMailsNb < 0) && currentMsg < messages.length) {
                            try {

                                loadMail(messages[currentMsg], true, false, null);
                                currentMsg++;
                            } catch (Exception e) {
                                logger.warn("Failed to load mail", e);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Could not load emails from server " + server);
                    }
                    nbMessages += currentMsg;
                }

                try {
                    // Compute timeline
                    if (config.isManageTimeline() && nbMessages > 0) {
                        TimeLine timeline = new TimeLine(config, xwiki, context, queryManager, logger);
                        timeline.compute();
                    }
                } catch (XWikiException e) {
                    logger.warn("Could not compute timeline date", e);
                }

            } catch (MailArchiveException e) {
                logger.warn("EXCEPTION ", e);
                return -1;
            } catch (InitializationException e) {
                logger.warn("EXCEPTION ", e);
                return -1;
            }

            inProgress = false;
            return nbMessages;
        } else {
            logger.warn("Loading process already in progress ...");
            return -1;
        }

    }

    /**
     * @throws InitializationException
     * @throws MailArchiveException
     */
    protected void init() throws InitializationException, MailArchiveException
    {
        // Init
        if (!this.isInitialized) {
            initialize();
        }

        config = new MailArchiveConfigurationImpl(SPACE_PREFS + ".GlobalParameters", this.context);

        if (config.getItemsSpaceName() != null && !"".equals(config.getItemsSpaceName())) {
            SPACE_ITEMS = config.getItemsSpaceName();
        }

        mailutils = new MailUtils(xwiki, context, logger, queryManager);
        servers = store.loadServersDefinitions();
        mailTypes = store.loadMailTypesDefinitions();
        mailingLists = store.loadMailingListsDefinitions();
        existingTopics = store.loadStoredTopics();
        existingMessages = store.loadStoredMessages();
    }

    /**
     * @param m
     */
    public void setMailSpecificParts(final MailItem m)
    {
        // set Type
        // TODO : severely bugged, logs to be added ...
        MailType foundType = null;
        for (MailType type : mailTypes) {
            logger.info("Treating mailType " + type);
            boolean matched = true;
            for (Entry<List<String>, String> entry : type.getPatterns().entrySet()) {
                logger.info("Treating entry " + entry);
                List<String> fields = entry.getKey();
                String regexp = entry.getValue();
                Pattern pattern = null;
                try {
                    pattern = Pattern.compile(regexp);
                } catch (Exception e) {
                    logger.warn("Invalid Pattern " + regexp + "can't be compiled, skipping this mail type");
                    break;
                }
                Matcher matcher = null;
                boolean fieldMatch = false;
                for (String field : fields) {
                    logger.info("Treating field " + field);
                    String fieldValue = "";
                    if ("from".equals(field)) {
                        fieldValue = m.getFrom();
                    } else if ("to".equals(field)) {
                        fieldValue = m.getTo();
                    } else if ("cc".equals(field)) {
                        fieldValue = m.getCc();
                    } else if ("subject".equals(field)) {
                        fieldValue = m.getSubject();
                    }
                    matcher = pattern.matcher(fieldValue);
                    if (matcher != null) {
                        fieldMatch = matcher.find();
                    }
                    if (fieldMatch) {
                        logger.info("Field " + field + " value [" + fieldValue + "] matches pattern [" + regexp + "]");
                        break;
                    }
                }
                matched = matched && fieldMatch;
            }
            if (matched && !MailType.TYPE_MAIL.equals(type.getName())) {
                logger.info("Matched type " + type);
                foundType = type;
                break;
            }
        }
        if (foundType != null) {
            m.setType(foundType.getName());
        } else {
            m.setType(MailType.TYPE_MAIL);
        }

        // set wiki user
        // // @TODO Try to retrieve wiki user
        // // @TODO : here, or after ? (link with ldap and xwiki profiles
        // // options to be checked ...)
        // /*
        // * String userwiki = parseUser(from); if (userwiki == null || userwiki == "") { userwiki = unknownUser; }
        // */
        m.setWikiuser(null);
    }

    /**
     * @param mail
     * @param confirm
     * @param isAttachedMail
     * @param parentMail
     * @return
     * @throws XWikiException
     * @throws ParseException
     */
    public MailLoadingResult loadMail(Part mail, boolean confirm, boolean isAttachedMail, String parentMail)
        throws XWikiException, ParseException
    {
        MailItem m = MailItem.fromMessage(mail);
        setMailSpecificParts(m);
        // Compatibility option with old version of the mail archive
        if (config.isCropTopicIds() && m.getTopicId().length() > 30) {
            m.setTopicId(m.getTopicId().substring(0, 29));
        }
        logger.warn("PARSED MAIL  " + m);

        return loadMail(m, true, false, null);
    }

    /**
     * @param existingTopics
     * @param existingMessages
     * @param message
     * @param confirm
     * @param isAttachedMail
     * @param parentMail
     * @throws XWikiException
     * @throws ParseException
     */
    public MailLoadingResult loadMail(MailItem m, boolean confirm, boolean isAttachedMail, String parentMail)
        throws XWikiException, ParseException
    {
        XWikiDocument msgDoc = null;
        XWikiDocument topicDoc = null;

        logger.debug("Loading mail content into wiki objects");

        // set loading user for rights - loading user must have edit rights on MailArchive and MailArchiveCode spaces
        context.setUser(config.getLoadingUser());
        logger.debug("Loading user " + config.getLoadingUser() + " set in context");

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
            existingTopicId = existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId());
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
                // We consider this was a topic hack : someone replied to an existing thread, but to start on another
                // subject.
                // In this case, we split, use messageId as a new topic Id, and set replyToId to empty string in order
                // to treat this as a new topic to create.
                // In order for this new thread to be correctly threaded, we search for existing topic with this new
                // topicId,
                // so now all new mails in this case will be attached to this new topic.
                logger.debug("  found existing topic but subjects are too different, using new messageid as topicid ["
                    + m.getMessageId() + "]");
                m.setTopicId(m.getMessageId());
                m.setReplyToId("");
                existingTopicId = existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId());
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
            try {
                msgDoc = createMailPage(m, existingTopicId, isAttachedMail, parentMail, confirm);
            } catch (Exception e) {
                logger.error("Could not create mail page for " + m.getMessageId(), e);
                return new MailLoadingResult(false, topicDoc != null ? topicDoc.getFullName() : null, null);
            }

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
                BaseObject msgObj = msgDoc.getObject(SPACE_CODE + ".MailClass");
                msgObj.set("topicid", existingTopicId, context);
                if (confirm) {
                    logger.debug("saving message " + m.getSubject());
                    saveAsUser(msgDoc, null, config.getLoadingUser(), "Updated mail with existing topic id found");
                }
            }

            return new MailLoadingResult(true, topicDoc != null ? topicDoc.getFullName() : null, msgDoc != null
                ? msgDoc.getFullName() : null);
        }
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
        String pagename = context.getWiki().getUniquePageName(SPACE_ITEMS, topicwikiname, context);
        topicDoc = xwiki.getDocument(SPACE_ITEMS + "." + pagename, context);
        BaseObject topicObj = topicDoc.newObject(SPACE_CODE + ".MailTopicClass", context);

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
        topicObj.set("sensitivity", m.getSensitivity(), context);
        topicObj.set("importance", m.getImportance(), context);

        topicObj.set("type", m.getType(), context);
        topicDoc.setParent(SPACE_HOME + ".WebHome");
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
            saveAsUser(topicDoc, m.getWikiuser(), config.getLoadingUser(),
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
     * @throws XWikiException
     * @throws ParseException
     */
    protected XWikiDocument updateTopicPage(MailItem m, String existingTopicId, SimpleDateFormat dateFormatter,
        boolean create) throws XWikiException, ParseException
    {
        logger.debug("updateTopicPage(" + existingTopicId + ")");

        String newuser = null;
        XWikiDocument topicDoc = xwiki.getDocument(existingTopics.get(existingTopicId).getFullName(), context);
        logger.debug("Existing topic " + topicDoc);
        BaseObject topicObj = topicDoc.getObject(SPACE_CODE + ".MailTopicClass");
        String lastupdatedate = topicObj.getStringValue("lastupdatedate");
        String startdate = topicObj.getStringValue("startdate");
        String originalAuthor = topicObj.getStringValue("author");
        if (lastupdatedate == null || "".equals(lastupdatedate)) {
            lastupdatedate = m.getDate();
        } // note : this should never occur
        if (startdate == null || "".equals(startdate)) {
            startdate = m.getDate();
        }
        Date decodedlastupdatedate = dateFormatter.parse(lastupdatedate);
        Date decodedstartdate = dateFormatter.parse(startdate);

        boolean isMoreRecent = (m.getDecodedDate().getTime() > decodedlastupdatedate.getTime());
        boolean isMoreAncient = (m.getDecodedDate().getTime() < decodedstartdate.getTime());
        logger.debug("decodedDate = " + m.getDecodedDate().getTime() + ", lastupdatedate = "
            + decodedlastupdatedate.getTime() + ", is more recent = " + isMoreRecent + ", first in topic = "
            + m.isFirstInTopic());
        logger.debug("lastupdatedate " + decodedlastupdatedate);
        logger.debug("current mail date " + m.getDecodedDate());

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
                newuser = mailutils.parseUser(m.getFrom(), config.isMatchProfiles(), config.getLoadingUser());
                if (newuser == null || "".equals(newuser)) {
                    newuser = unknownUser;
                }
                dirty = true;
            }
            logger.debug("     existing startdate $topicObj.startdate");
            if ((topicObj.getStringValue("startdate") == null || "".equals(topicObj.getStringValue("startdate")))
                || isMoreAncient) {
                logger.debug("     checked startdate not already added to topic");
                topicObj.set("startdate", dateFormatter.format(m.getDecodedDate()), context);
                topicDoc.setCreationDate(m.getDecodedDate());
                comment += " Updated start date ";
                dirty = true;
            }
            // }
            if (isMoreRecent) {
                logger.debug("     updating lastupdatedate from " + lastupdatedate + " to "
                    + dateFormatter.format(m.getDecodedDate()));
                topicObj.set("lastupdatedate", dateFormatter.format(m.getDecodedDate()), context);
                topicDoc.setDate(m.getDecodedDate());
                topicDoc.setContentUpdateDate(m.getDecodedDate());
                newuser = mailutils.parseUser(m.getFrom(), config.isMatchProfiles(), config.getLoadingUser());
                comment += " Updated last update date ";
                dirty = true;
            }
            topicDoc.setComment(comment);

            if (create && dirty) {
                logger.debug("     Updated existing topic");
                saveAsUser(topicDoc, newuser, config.getLoadingUser(), comment);
            }
            // TODO if already added, update the map
            existingTopics.put(m.getTopicId(),
                new TopicShortItem(topicDoc.getFullName(), topicObj.getStringValue("subject")));
        } else {
            logger.debug("     Nothing to update in topic");
        }

        // return topicDoc

        return null;
    }

    /**
     * createMailPage Creates a wiki page for a Mail.
     * 
     * @throws XWikiException
     * @throws IOException
     * @throws MessagingException
     */
    protected XWikiDocument createMailPage(MailItem m, String existingTopicId, boolean isAttachedMail,
        String parentMail, boolean create) throws XWikiException, MessagingException, IOException
    {
        XWikiDocument msgDoc;
        String content = "";
        String htmlcontent = "";
        String zippedhtmlcontent = "";
        ArrayList<String> attachedMails = new ArrayList<String>();
        // a map to store attachment filename = contentId for replacements in HTML retrieved from mails
        HashMap<String, String> attachmentsMap = new HashMap<String, String>();
        ArrayList<MimeBodyPart> attbodyparts = new ArrayList<MimeBodyPart>();

        char prefix = 'M';
        if (isAttachedMail) {
            prefix = 'A';
        }
        String msgwikiname = xwiki.clearName(prefix + m.getTopic().replaceAll(" ", ""), context);
        if (msgwikiname.length() >= 30) {
            msgwikiname = msgwikiname.substring(0, 30);
        }
        String pagename = xwiki.getUniquePageName(SPACE_ITEMS, msgwikiname, context);
        msgDoc = xwiki.getDocument(SPACE_ITEMS + '.' + pagename, context);
        logger.debug("NEW MSG msgwikiname=" + msgwikiname + " pagename=" + pagename);

        Object bodypart = m.getBodypart();
        logger.debug("bodypart class " + bodypart.getClass());
        // addDebug("mail content type " + m.contentType)
        // Retrieve mail body(ies)
        if (m.getContentType().contains("pkcs7-mime") || m.getContentType().contains("multipart/encrypted")) {
            content =
                "<<<This e-mail was encrypted. Text Content and attachments of encrypted e-mails are not publshed in Mail Archiver to avoid disclosure of restricted or confidential information.>>>";
            htmlcontent =
                "<i>&lt;&lt;&lt;This e-mail was encrypted. Text Content and attachments of encrypted e-mails are not publshed in Mail Archiver to avoid disclosure of restricted or confidential information.&gt;&gt;&gt;</i>";

            m.setSensitivity("encrypted");
        } else if (bodypart instanceof String) {
            content = MimeUtility.decodeText((String) bodypart);

        } else {
            logger.debug("Fetching plain text content ...");
            content = getMailContent((Multipart) bodypart);
            logger.debug("Fetching HTML content ...");
            htmlcontent = getMailContentHtml((Multipart) bodypart, 0);
            logger.debug("Fetching attached mails ...");
            attachedMails = getMailContentAttachedMails((Multipart) bodypart, msgDoc.getFullName());

            logger.debug("Fetching attachments from mail");
            int nbatts = getMailAttachments((Multipart) bodypart, attbodyparts);
            logger.debug("FOUND " + nbatts + " attachments to add");
            logger.debug("Retrieving contentIds ...");
            fillAttachmentContentIds(attbodyparts, attachmentsMap);
        }

        // Truncate body
        content = truncateStringForBytes(content, 65500, 65500);

        /* Treat HTML parts ... */
        zippedhtmlcontent = treatHtml(msgDoc, htmlcontent, attachmentsMap);

        // Treat lengths
        if (m.getMessageId().length() > 255) {
            m.setMessageId(m.getMessageId().substring(0, 254));
        }
        if (m.getSubject().length() > 255) {
            m.setSubject(m.getSubject().substring(0, 254));
        }
        if (existingTopicId.length() > 255) {
            existingTopicId = existingTopicId.substring(0, 254);
        }
        if (m.getTopicId().length() > 255) {
            m.setTopicId(m.getTopicId().substring(0, 254));
        }
        if (m.getTopic().length() > 255) {
            m.setTopic(m.getTopic().substring(0, 254));
        }
        // largestrings : normally 65535, but we don't know the size of the largestring itself
        if (m.getReplyToId().length() > 65500) {
            m.setReplyToId(m.getReplyToId().substring(0, 65499));
        }
        if (m.getRefs().length() > 65500) {
            m.setRefs(m.getRefs().substring(0, 65499));
        }
        if (m.getFrom().length() > 65500) {
            m.setFrom(m.getFrom().substring(0, 65499));
        }
        if (m.getTo().length() > 65500) {
            m.setTo(m.getTo().substring(0, 65499));
        }
        if (m.getCc().length() > 65500) {
            m.setCc(m.getCc().substring(0, 65499));
        }

        // Assign text body converted from html content if there is no pure-text content
        if ((content == null || "".equals(content)) && (htmlcontent != null && !"".equals(htmlcontent))) {
            String converted = null;
            try {

                WikiPrinter printer = new DefaultWikiPrinter();
                PrintRendererFactory printRendererFactory =
                    componentManager.lookup(PrintRendererFactory.class, Syntax.PLAIN_1_0.toIdString());
                htmlStreamParser.parse(new StringReader(htmlcontent), printRendererFactory.createRenderer(printer));

                converted = printer.toString();

            } catch (Throwable t) {
                logger.warn("Conversion from HTML to plain text thrown exception", t);
                converted = null;
            }
            if (converted != null && !"".equals(converted)) {
                // replace content with value (remove excessive whitespace also)
                content = converted.replaceAll("[\\s]{2,}", "\n");
                logger.debug("Text body now contains converted html content");
            } else {
                logger.debug("Conversion from HTML to Plain Text returned empty or null string");
            }
        }

        // Fill all new object's fields
        BaseObject msgObj = msgDoc.newObject(SPACE_CODE + ".MailClass", context);
        msgObj.set("messageid", m.getMessageId(), context);
        msgObj.set("messagesubject", m.getSubject(), context);

        msgObj.set("topicid", existingTopicId, context);
        msgObj.set("topicsubject", m.getTopic(), context);
        msgObj.set("inreplyto", m.getReplyToId(), context);
        msgObj.set("references", m.getRefs(), context);
        msgObj.set("date", m.getDecodedDate(), context);
        msgDoc.setCreationDate(m.getDecodedDate());
        msgDoc.setDate(m.getDecodedDate());
        msgDoc.setContentUpdateDate(m.getDecodedDate());
        msgObj.set("from", m.getFrom(), context);
        msgObj.set("to", m.getTo(), context);
        msgObj.set("cc", m.getCc(), context);
        msgObj.set("body", content, context);
        msgObj.set("bodyhtml", zippedhtmlcontent, context);
        msgObj.set("sensitivity", m.getSensitivity(), context);
        if (attachedMails.size() != 0) {
            msgObj.set("attachedMails", attachedMails/* TODO .grep("^MailArchive\\..*$").join(',') */, context);
        }
        if (!isAttachedMail) {
            if (m.isFirstInTopic()) {
                msgObj.set("type", m.getType(), context);
            } else {
                msgObj.set("type", MailType.TYPE_MAIL, context);
            }
        } else {
            msgObj.set("type", "Attached Mail", context);
        }
        if (parentMail != null) {
            msgDoc.setParent(parentMail);
        } else if (existingTopics.get(m.getTopicId()) != null) {
            msgDoc.setParent(existingTopics.get(m.getTopicId()).getFullName());
        }
        // msgDoc.setContent(xwiki.getDocument("MailArchiveCode.MailClassTemplate").getContent())
        msgDoc.setTitle("Message ${m.subject}");
        if (!isAttachedMail) {
            msgDoc.setComment("Created message from mailing-list from folder ${mailingListFolder}");
        } else {
            msgDoc.setComment("Attached mail created");
        }
        String tag = parseTags(m);
        if (tag != "") {
            BaseObject tagobj = msgDoc.newObject("XWiki.TagClass", context);
            tagobj.set("tags", tag.replaceAll(" ", "_"), context);
        }

        if (create && !checkMsgIdExistence(m.getMessageId())) {
            logger.debug("saving message " + m.getSubject());
            saveAsUser(msgDoc, m.getWikiuser(), config.getLoadingUser(), "Created message from mailing-list");
        }
        existingMessages
            .put(m.getMessageId(), new MailShortItem(m.getSubject(), existingTopicId, msgDoc.getFullName()));
        logger.debug("  mail loaded and saved :" + msgDoc.getFullName());
        logger.debug("adding attachments to document");
        addAttachmentsFromMail(msgDoc, attbodyparts, attachmentsMap);

        return msgDoc;
    }

    /*
     * Cleans up HTML content and treat it to replace cid tags with correct image urls (targeting attachments)
     */
    String treatHtml(XWikiDocument msgdoc, String htmlcontent, HashMap<String, String> attachmentsMap)
        throws IOException
    {
        if (htmlcontent != null && !"".equals(htmlcontent) && htmlcontent.length() != 0) {
            // Try to clean HTML with JTidy to decrease its length and complexity
            logger.debug("Original HTML length " + htmlcontent.length());

            // Replace "&nbsp;" to avoid issue of "Â" characters displayed (???)
            htmlcontent = htmlcontent.replaceAll("&Acirc;", " ");

            // Replace attachment URLs in HTML content for images to be shown
            for (Entry<String, String> att : attachmentsMap.entrySet()) {
                // remove starting "<" and finishing ">"
                String pattern = att.getKey().substring(1, att.getKey().length() - 2);
                pattern = "cid:" + pattern;

                logger.debug("Testing for pattern " + context.getUtil().encodeURI(pattern, context) + " " + pattern);
                String replacement = msgdoc.getAttachmentURL(att.getValue(), context);
                logger.debug("To be replaced by " + replacement);
                htmlcontent = htmlcontent.replaceAll(pattern, replacement);
            }

            logger.debug("Zipping HTML part ...");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(bos);
            byte[] bytes = htmlcontent.getBytes("UTF8");
            zos.write(bytes, 0, bytes.length);
            zos.finish();
            zos.close();

            byte[] compbytes = bos.toByteArray();
            htmlcontent = Utils.byte2hex(compbytes);
            bos.close();

            if (htmlcontent.length() > 65534) {
                logger.debug("Failed to have HTML fit in target field");
                htmlcontent = "";
            }

        } else {
            logger.debug("No HTML to treat");
        }

        logger.debug("Html Zipped length : " + htmlcontent.length());
        return htmlcontent;
    }

    // Truncate a string "s" to obtain less than a certain number of bytes "maxBytes", starting with "maxChars"
    // characters.
    public String truncateStringForBytes(String s, int maxChars, int maxBytes)
    {

        String substring = s;
        if (s.length() > maxChars) {
            substring = s.substring(0, maxChars);
        }

        byte[] bytes = new byte[] {};
        try {
            bytes = substring.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (bytes.length > maxBytes) {

            logger.debug("Truncate string to " + substring.length() + " characters, result in " + bytes.length
                + " bytes array");
            return truncateStringForBytes(s, maxChars - (bytes.length - maxChars) / 4, maxBytes);

        } else {

            logger.debug("String truncated to " + substring.length() + " characters, resulting in " + bytes.length
                + " bytes array");
            return substring;
        }

    }

    // ****** Check existence of wiki object with same value as 'messageid', from database
    public boolean checkMsgIdExistence(String msgid)
    {
        boolean exists = false;
        String hql =
            "select count(*) from StringProperty as prop where prop.name='messageid' and prop.value='" + msgid + "')";

        try {
            List<Object> result = queryManager.createQuery(hql, Query.HQL).execute();
            logger.debug("CheckMsgIdExistence result " + result);
            exists = (Long) result.get(0) != 0;
        } catch (QueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!exists) {
            logger.debug("Message with id " + msgid + " does not exist in database");
            return false;
        } else {
            logger.debug("Message with id " + msgid + " already loaded in database");
            return true;
        }

    }

    /*
     * Add map of attachments (bodyparts) to a document (doc1)
     */
    public int addAttachmentsFromMail(XWikiDocument doc1, ArrayList<MimeBodyPart> bodyparts,
        HashMap<String, String> attachmentsMap) throws MessagingException, IOException, XWikiException
    {
        int nb = 0;
        for (MimeBodyPart bodypart : bodyparts) {
            String fileName = bodypart.getFileName();
            String cid = bodypart.getContentID();

            try {
                // replace by correct name if filename was renamed (multiple attachments with same name)
                if (attachmentsMap.containsKey(cid)) {
                    fileName = attachmentsMap.get(cid);
                }
                logger.debug("Treating attachment: " + fileName + " with contentid " + cid);
                if (fileName == null) {
                    fileName = "fichier.doc";
                }
                if (fileName.equals("oledata.mso") || fileName.endsWith(".wmz") || fileName.endsWith(".emz")) {
                    logger.debug("Not treating Microsoft crap !");
                } else {
                    String disposition = bodypart.getDisposition();
                    String contentType = bodypart.getContentType().toLowerCase();

                    logger.debug("Treating attachment of type: " + bodypart.getContentType());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    OutputStream out = new BufferedOutputStream(baos);
                    // We can't just use p.writeTo() here because it doesn't
                    // decode the attachment. Instead we copy the input stream
                    // onto the output stream which does automatically decode
                    // Base-64, quoted printable, and a variety of other formats.
                    InputStream ins = new BufferedInputStream(bodypart.getInputStream());
                    int b = ins.read();
                    while (b != -1) {
                        out.write(b);
                        b = ins.read();
                    }
                    out.flush();
                    out.close();
                    ins.close();

                    logger.debug("Treating attachment step 3: " + fileName);

                    byte[] data = baos.toByteArray();
                    logger.debug("Ready to attach attachment: " + fileName);
                    nb += addAttachmentFromMail(doc1, fileName, data);
                } // end if
            } catch (Exception e) {
                logger.warn("Attachment " + fileName + " could not be treated", e);
            }
        } // end for all attachments
        return nb;
    }

    /*
     * Add to document (doc1) an attached file (afilename) with its content (adata), and fills a map (adata) with
     * relation between contentId (cid) and (afilename)
     */
    public int addAttachmentFromMail(XWikiDocument doc, String afilename, byte[] adata) throws XWikiException
    {
        String filename = getAttachmentValidName(afilename);
        logger.debug("adding attachment: " + filename);

        XWikiAttachment attachment = new XWikiAttachment();
        doc.getAttachmentList().add(attachment);
        attachment.setContent(adata);
        attachment.setFilename(filename);
        // TODO: handle Author
        attachment.setAuthor(context.getUser());
        // Add the attachment to the document
        attachment.setDoc(doc);
        logger.debug("saving attachment: " + filename);
        doc.setComment("Added attachment " + filename);
        doc.saveAttachmentContent(attachment, context);

        return 1;
    }

    /*
     * Retrieves body parts for content from mail, and returns them as a String
     */
    public String getMailContent(Multipart bodypart)
    {
        StringBuilder content = new StringBuilder();
        String is;
        String str;
        try {
            int mcount = bodypart.getCount();
            int i = 0;
            while (i < mcount) {
                BodyPart newbodypart = bodypart.getBodyPart(i);
                logger.debug("BODYPART CONTENTTYPE = " + newbodypart.getContentType().toLowerCase() + " FILENAME = "
                    + newbodypart.getFileName());
                // We don't treat attachments here
                if (newbodypart.getFileName() != null) {
                    if (newbodypart.getContentType().toLowerCase().contains("vcard")) {
                        logger.debug("Adding vcard to content");
                        if (!content.toString().toLowerCase().contains("xwiki")) {
                            str = (String) newbodypart.getContent();
                            content.append(" ").append(str);
                        }
                    }
                    // Note : we treat HTML or XML appart
                    else if (newbodypart.getContentType().toLowerCase().startsWith("text/")
                        && !(newbodypart.getContentType().toLowerCase().startsWith("text/html"))
                        && !(newbodypart.getContentType().toLowerCase().startsWith("text/xml"))) {
                        logger.debug("Adding text to content");
                        str = (String) newbodypart.getContent();
                        content.append(" ").append(str);
                    }

                    if (newbodypart.getContentType().toLowerCase().startsWith("multipart/")) {
                        logger.debug("Adding multipart to content");
                        String ncontent = getMailContent((Multipart) newbodypart.getContent());
                        if (!"".equals(ncontent)) {
                            content.append(" ").append(ncontent);
                        }
                    }

                    if (newbodypart.getContentType().toLowerCase().startsWith("message/rfc822")) {
                        logger.debug("Adding rfc822 to content");
                        String ncontent =
                            getMailContent((Multipart) ((BodyPart) newbodypart.getContent()).getContent());
                        if (!"".equals(ncontent)) {
                            content.append(" ").append(ncontent);
                        }
                    }
                } // not an attachment

                i++;
            }

            return content.toString();

        } catch (Exception e) {
            logger.warn("Failed to get Mail Content", e);
            return "Failed to get Mail Content";
        }
    }

    /*
     * Retrieves HTML parts only from a mail and returns them as String
     */
    public String getMailContentHtml(Multipart bodypart, int level)
    {
        StringBuffer content = new StringBuffer();
        try {
            int mcount = bodypart.getCount();
            int i = 0;
            while (i < mcount) {
                BodyPart newbodypart = bodypart.getBodyPart(i);
                logger.debug("BODYPART CONTENTTYPE = " + newbodypart.getContentType().toLowerCase() + " FILENAME = "
                    + newbodypart.getFileName());
                // we don't treat attachments here
                if (newbodypart.getFileName() == null) {
                    // Note : we treat HTML appart
                    if (newbodypart.getContentType().toLowerCase().startsWith("text/html")) {
                        String htmlcontent = (String) newbodypart.getContent();
                        logger.debug("Adding HTML text of length " + htmlcontent.length() + " to content");
                        content.append(" ").append(htmlcontent);
                    }

                    if (newbodypart.getContentType().toLowerCase().startsWith("multipart/")) {
                        logger.debug("Adding multipart to content");
                        String ncontent = getMailContentHtml((Multipart) newbodypart.getContent(), level + 1);
                        if (!"".equals(ncontent)) {
                            content.append(" ").append(ncontent);
                        }
                    }

                    if (newbodypart.getContentType().toLowerCase().startsWith("message/rfc822")) {
                        logger.debug("Adding rfc822 to content");
                        BodyPart rfcbodypart = (BodyPart) newbodypart.getContent();
                        logger.debug("BODYPART CONTENTTYPE for RFC822 = " + rfcbodypart.getContentType().toLowerCase()
                            + " FILENAME = " + rfcbodypart.getFileName());
                        String from = rfcbodypart.getHeader("From")[0];
                        // If header is set, it's likely to be an attached mail to the original mail, so we load it
                        // first
                        if (from != null && !"".equals(from)) {
                            logger.debug("Not Html, but attached email");
                        } else {
                            String ncontent = getMailContentHtml((Multipart) rfcbodypart.getContent(), level + 1);
                            if (!"".equals(ncontent)) {
                                content.append(" ").append(ncontent);
                            }
                        }
                    }
                } else { // not an attachment
                    logger.debug("bodypart has a filename " + newbodypart.getFileName() + ", no html to fetch");
                }

                i++;
            }

            return content.toString();

        } catch (Exception e) {
            logger.warn("Failed to get Html Mail Content", e);
            return "Failed to get Html Mail Content " + e.getClass() + " " + e.getMessage();
        }
    }

    /*
     * Retrieves attached mails parts only from a mail, loads them and returns a String with list of created pages
     */
    public ArrayList<String> getMailContentAttachedMails(Multipart bodypart, String parentMail)
    {
        try {
            ArrayList<String> attachedMailsList = new ArrayList<String>();
            int mcount = bodypart.getCount();
            int i = 0;
            ArrayList<String> result;
            while (i < mcount) {
                BodyPart newbodypart = bodypart.getBodyPart(i);

                if (newbodypart.getContentType().toLowerCase().startsWith("multipart/")) {
                    logger.debug("Adding multipart to attached mails");
                    result = getMailContentAttachedMails((Multipart) newbodypart.getContent(), parentMail);
                    attachedMailsList.addAll(result);
                }

                if (newbodypart.getContentType().toLowerCase().startsWith("message/rfc822")) {
                    logger.debug("Adding rfc822 to content");
                    BodyPart rfcbodypart = (BodyPart) newbodypart.getContent();
                    logger.debug("BODYPART CONTENTTYPE for RFC822 = " + rfcbodypart.getContentType().toLowerCase()
                        + " FILENAME = " + rfcbodypart.getFileName());
                    String from = rfcbodypart.getHeader("From")[0];
                    // If header is set, it's likely to be an attached mail to the original mail, so we load it first
                    if (from != null && !"".equals(from)) {
                        // Load attached email into the wiki
                        MailLoadingResult loadresult = loadMail(rfcbodypart, true, true, parentMail);
                        if (loadresult.isSuccess() && loadresult.getCreatedMailDocumentName() != null) {
                            attachedMailsList.add(loadresult.getCreatedMailDocumentName()); // the name of created page
                                                                                            // for this attached mail
                        }
                    } else {
                        result = getMailContentAttachedMails((Multipart) rfcbodypart.getContent(), parentMail);
                        attachedMailsList.addAll(result);
                    }
                }

                i++;
            }

            return attachedMailsList;

        } catch (Exception e) {
            logger.debug("Failed to get Attached Mails Content", e);
            ArrayList<String> returnval = new ArrayList<String>();
            returnval.add("Failed to get Attached Mails Content");
            return returnval;
        }
    }

    /*
     * Fills a map (bodyparts) with attachments found recursively in mail (bodypart)
     */
    public int getMailAttachments(Multipart bodypart, ArrayList<MimeBodyPart> bodyparts)
    {
        int nb = 0;
        try {
            int mcount = bodypart.getCount();
            int i = 0;
            while (i < mcount) {
                BodyPart newbodypart = bodypart.getBodyPart(i);

                if (newbodypart.getFileName() != null) {
                    nb++;
                    bodyparts.add((MimeBodyPart) newbodypart);
                    logger.debug("getMailAttachments: found an attachment " + newbodypart.getFileName());
                } else if (newbodypart.getContentType().toLowerCase().startsWith("application/")
                    || newbodypart.getContentType().toLowerCase().startsWith("image/")) {
                    nb++;
                    bodyparts.add((MimeBodyPart) newbodypart);
                    logger.debug("getMailAttachments: found an attachment " + newbodypart.getClass());
                }

                if (newbodypart.getContentType().toLowerCase().startsWith("multipart/")) {
                    nb += getMailAttachments((Multipart) newbodypart.getContent(), bodyparts);
                }

                if (newbodypart.getContentType().toLowerCase().startsWith("message/rfc822")) {
                    nb += getMailAttachments((Multipart) ((BodyPart) newbodypart.getContent()).getContent(), bodyparts);
                }

                i++;
            }

        } catch (Exception e) {
            logger.debug("Failed to retrieve mail attachments", e);
            nb = 0;
        }

        return nb;
    }

    /*
     * Fills a map with key=contentId, value=filename of attachment
     */
    public void fillAttachmentContentIds(ArrayList<MimeBodyPart> bodyparts, HashMap<String, String> attmap)
    {

        for (MimeBodyPart bodypart : bodyparts) {
            String fileName = null;
            String cid = null;
            try {
                fileName = bodypart.getFileName();
                cid = bodypart.getContentID();
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (cid != null && !"".equals(cid) && fileName != null) {
                logger.debug("fillAttachmentContentIds: Treating attachment: " + fileName + " with contentid " + cid);
                String name = getAttachmentValidName(fileName);
                int nb = 1;
                if (!name.contains(".")) {
                    name += ".ext";
                }
                String newName = name;
                while (attmap.containsValue(newName)) {
                    logger.debug("fillAttachmentContentIds: " + newName + " attachment already exists, renaming to "
                        + name.replaceAll("(.*)\\.([^.]*)", "$1-" + nb + ".$2"));
                    newName = name.replaceAll("(.*)\\.([^.]*)", "$1-" + nb + ".$2");
                    nb++;
                }
                attmap.put(cid, newName);
            } else {
                logger.debug("fillAttachmentContentIds: content ID is null, nothing to do");
            }
        }
    }

    /*
     * Returns a valid name for an attachment from its original name
     */
    public String getAttachmentValidName(String afilename)
    {
        String fname = afilename;
        int i = fname.lastIndexOf("\\");
        if (i == -1) {
            i = fname.lastIndexOf("/");
        }
        String filename = fname.substring(i + 1);
        filename = filename.replaceAll("\\+", " ");
        return filename;
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
    public String existsTopic(String topicId, String topicSubject, String inreplyto, String messageid)
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
        }
        // Search in existing topics with id
        if (foundTopicId == null) {
            if (!"".equals(topicId) && existingTopics.containsKey(topicId)) {
                logger.debug("existsTopic : found topic id in loaded topics");
                if (similarSubjects(topicSubject, existingTopics.get(topicId).getSubject())) {
                    foundTopicId = topicId;
                } else {
                    logger.debug("... but subjects are too different");
                }
            }
        }
        // Search with references
        if (foundTopicId == null) {
            String xwql =
                "select distinct mail.topicid from Document doc, doc.object(" + SPACE_CODE
                    + ".MailClass) as mail where mail.references like '%" + messageid + "%'";
            try {
                List<String> topicIds = queryManager.createQuery(xwql, Query.XWQL).execute();
                // We're not supposed to find several topics related to messages having this id in references ...
                if (topicIds.size() == 1) {
                    foundTopicId = topicIds.get(0);
                }
                logger.warn("We should have found only one topicId instead of this list : " + topicIds
                    + ", using the first found");
            } catch (QueryException e) {
                logger.warn("Issue while searching for references", e);
            }
        }
        // Search in existing topics with exactly same subject
        if (foundTopicId == null) {
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

        return foundTopicId;
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
        logger.debug("similarSubjects : comparing [" + s1 + "] and [" + s2 + "]");
        if (s1 == s2) {
            logger.debug("similarSubjects : subjects are equal");
            return true;
        }
        if (s1 != null && s1.equals(s2)) {
            logger.debug("similarSubjects : subjects are the equal");
            return true;
        }
        if (s1.length() == 0 || s2.length() == 0) {
            logger.debug("similarSubjects : one subject is empty, we consider them different");
            return false;
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
        if ((s1.startsWith(s2) || s2.startsWith(s1))) {
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
