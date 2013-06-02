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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.mail.IMailComponent;
import org.xwiki.contrib.mail.IMailReader;
import org.xwiki.contrib.mail.IStoreManager;
import org.xwiki.contrib.mail.MailContent;
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mail.SourceConnectionErrors;
import org.xwiki.contrib.mail.internal.JavamailMessageParser;
import org.xwiki.contrib.mail.internal.MailAttachment;
import org.xwiki.contrib.mail.internal.util.Utils;
import org.xwiki.contrib.mail.source.SourceType;
import org.xwiki.contrib.mailarchive.IMASource;
import org.xwiki.contrib.mailarchive.IMAUser;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.contrib.mailarchive.internal.bridge.ExtendedDocumentAccessBridge;
import org.xwiki.contrib.mailarchive.internal.data.IFactory;
import org.xwiki.contrib.mailarchive.internal.data.MailDescriptor;
import org.xwiki.contrib.mailarchive.internal.data.MailStore;
import org.xwiki.contrib.mailarchive.internal.data.Server;
import org.xwiki.contrib.mailarchive.internal.data.TopicDescriptor;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.persistence.IPersistence;
import org.xwiki.contrib.mailarchive.internal.persistence.XWikiPersistence;
import org.xwiki.contrib.mailarchive.internal.threads.IMessagesThreader;
import org.xwiki.contrib.mailarchive.internal.threads.ThreadableMessage;
import org.xwiki.contrib.mailarchive.internal.timeline.ITimeLineGenerator;
import org.xwiki.contrib.mailarchive.internal.utils.DecodedMailContent;
import org.xwiki.contrib.mailarchive.internal.utils.IMailUtils;
import org.xwiki.contrib.mailarchive.internal.utils.TextUtils;
import org.xwiki.environment.Environment;
import org.xwiki.logging.LogLevel;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.util.Util;

/**
 * Implementation of a <tt>IMailArchive</tt> component.
 */
@Component
@Singleton
public class DefaultMailArchive implements IMailArchive, Initializable
{

    /**
     * XWiki profile name of a non-existing user.
     */
    public static final String UNKNOWN_USER = "XWiki.UserDoesNotExist";

    public boolean isConfigured = false;

    // Components injected by the Component Manager

    /** Provides access to documents. */
    @Inject
    private DocumentAccessBridge dab;

    /** Provides access to the request context. */
    @Inject
    public Execution execution;

    /** Provides access to execution environment and from it to context and old core */
    @Inject
    private Environment environment;

    /**
     * Secure query manager that performs checks on rights depending on the query being executed.
     */
    // TODO : @Requirement("secure") ??
    @Inject
    private QueryManager queryManager;

    /** Provides access to log facility */
    @Inject
    Logger logger;

    @Inject
    IAggregatedLoggerManager aggregatedLoggerManager;

    /**
     * The component used to parse XHTML obtained after cleaning, when transformations are not executed.
     */
    @Inject
    @Named("xhtml/1.0")
    private StreamParser htmlStreamParser;

    @Inject
    @Named("plain/1.0")
    private PrintRendererFactory printRendererFactory;

    /**
     * The component manager. We need it because we have to access some components dynamically based on the input
     * syntax.
     */
    @Inject
    private ComponentManager componentManager;

    /** Provides access to low-level mail api component */
    @Inject
    private IMailComponent mailManager;

    // Other global objects

    /** The XWiki context */
    private XWikiContext context;

    // TODO remove dependency to old core
    /** The XWiki old core */
    private XWiki xwiki;

    /** Provides access to Mail archive configuration items */
    @Inject
    private IItemsManager store;

    @Inject
    @Named("mbox")
    private IStoreManager builtinStore;

    /** Factory to convert raw conf to POJO */
    @Inject
    private IFactory factory;

    /** Provides access to the Mail archive configuration */
    @Inject
    private IMailArchiveConfiguration config;

    @Inject
    private IMessagesThreader threads;

    @Inject
    private ITimeLineGenerator timelineGenerator;

    /** Used to persist pages for mails and topics */
    @Inject
    private IPersistence persistence;

    /** Some utilities */
    @Inject
    public IMailUtils mailutils;

    /** Already archived topics, loaded from database */
    private HashMap<String, TopicDescriptor> existingTopics;

    /** Already archived messages, loaded from database */
    private HashMap<String, MailDescriptor> existingMessages;

    /** Is the component initialized ? */
    private boolean isInitialized = false;

    /** Are we currently in a loading session ? */
    private boolean inProgress = false;

    /** Current progress, ie index of mail currently loading, if any */
    private int progressMails = 0;

    /** Current progress, ie index of mail source currently loading, if any */
    private int progressSources = 0;

    private int totalMails = 0;

    private int totalSources = 0;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        try {
            logger.debug("initialize()");
            ExecutionContext context = execution.getContext();
            this.context = (XWikiContext) context.getProperty("xwikicontext");
            this.xwiki = this.context.getWiki();
            // Initialize switchable logging for main components useful for the mail archive
            aggregatedLoggerManager.addComponentLogger(IMailArchive.class);
            aggregatedLoggerManager.addComponentLogger(IMailComponent.class);
            aggregatedLoggerManager.addComponentLogger(StreamParser.class);
            logger.info("Mail archive initialized !");
            logger.debug("PERMANENT DATA DIR : " + this.environment.getPermanentDirectory());
        } catch (Throwable e) {
            logger.error("Could not initiliaze mailarchive ", e);
            e.printStackTrace();
        }

        this.isInitialized = true;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MailArchiveException
     * @throws InitializationException
     * @see org.xwiki.contrib.mailarchive.IMailArchive#getConfiguration()
     */
    @Override
    public IMailArchiveConfiguration getConfiguration() throws InitializationException, MailArchiveException
    {
        configure();
        return this.config;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailArchive#checkSource(String)
     */
    @Override
    public int checkSource(final String sourcePrefsDoc)
    {
        XWikiDocument serverDoc = null;
        try {
            serverDoc = xwiki.getDocument(sourcePrefsDoc, context);
        } catch (XWikiException e) {
            serverDoc = null;
        }
        if (serverDoc == null || !dab.exists(sourcePrefsDoc)) {
            logger.error("Page " + sourcePrefsDoc + " does not exist");
            return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
        }
        if (serverDoc.getObject(XWikiPersistence.CLASS_MAIL_SERVERS) != null) {
            // Retrieve connection properties from prefs
            Server server = factory.createMailServer(sourcePrefsDoc);
            if (server == null) {
                logger.warn("Could not retrieve server information from wiki page " + sourcePrefsDoc);
                return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
            }

            return checkSource(server);
        } else if (serverDoc.getObject(XWikiPersistence.CLASS_MAIL_STORES) != null) {
            // Retrieve connection properties from prefs
            MailStore store = factory.createMailStore(sourcePrefsDoc);
            if (store == null) {
                logger.warn("Could not retrieve store information from wiki page " + sourcePrefsDoc);
                return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
            }

            return checkSource(store);

        } else {
            logger.error("Could not retrieve valid configuration object from page");
            return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailArchive#checkSource(org.xwiki.contrib.mailarchive.LoadingSession)
     */
    @Override
    public Map<String, Integer> checkSource(final LoadingSession session)
    {
        Map<String, Integer> results = new HashMap<String, Integer>();
        List<IMASource> sources = getSourcesList(session);
        for (IMASource source : sources) {
            if ("SERVER".equals(source.getType())) {
                results.put(source.getWikiDoc(), checkSource((Server) source));
            } else if ("STORE".equals(source.getType())) {
                results.put(source.getWikiDoc(), checkSource((MailStore) source));
            } else {
                logger.error("Unknown type of source " + source.getType() + " for " + source.getId());
                results.put(source.getWikiDoc(), SourceConnectionErrors.UNKNOWN_SOURCE_TYPE.getCode());
            }
        }
        return results;
    }

    /**
     * @param server
     * @return
     */
    public int checkSource(final Server server)
    {
        logger.info("Checking server " + server);

        IMailReader mailReader = null;
        try {
            mailReader =
                mailManager.getMailReader(server.getHostname(), server.getPort(), server.getProtocol(),
                    server.getUsername(), server.getPassword(), server.getAdditionalProperties());
        } catch (ComponentLookupException e) {
            logger.error("Could not find appropriate mail reader for server " + server.getId(), e);
            return -1;
        }

        int nbMessages = mailReader.check(server.getFolder(), true);
        logger.debug("check of server " + server.getId() + " returned " + nbMessages);

        // Persist connection state

        try {
            persistence.updateMailServerState(server.getWikiDoc(), nbMessages);
        } catch (Exception e) {
            logger.info("Failed to persist server connection state", e);
        }

        server.setState(nbMessages);

        return nbMessages;
    }

    /**
     * @param store
     * @return
     */
    public int checkSource(final MailStore store)
    {
        logger.info("Checking store " + store);

        IMailReader mailReader = null;
        try {
            mailReader = mailManager.getStoreManager(store.getFormat(), store.getLocation());
        } catch (ComponentLookupException e) {
            logger.error("Could not find appropriate mail reader for store " + store.getId(), e);
            return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
        }

        int nbMessages = mailReader.check(store.getFolder(), true);
        logger.debug("check of server " + store.getId() + " returned " + nbMessages);

        // Persist connection state

        try {
            persistence.updateMailStoreState(store.getWikiDoc(), nbMessages);
        } catch (Exception e) {
            logger.info("Failed to persist server connection state", e);
        }

        store.setState(nbMessages);

        return nbMessages;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailArchive#computeThreads(java.lang.String)
     */
    public ThreadableMessage computeThreads(String topicId)
    {
        try {
            if (topicId == null) {
                return threads.thread();
            } else {
                return threads.thread(topicId);
            }
        } catch (Exception e) {
            logger.error("Could not compute threads", e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailArchive#loadMails(org.xwiki.contrib.mailarchive.LoadingSession)
     */
    @Override
    public int loadMails(LoadingSession session)
    {
        int nbSuccess = 0;

        logger.info("Starting new MAIL loading session...");

        if (inProgress) {
            logger.warn("Loading process already in progress ...");
            return -1;
        }

        if (session.isDebugMode()) {
            enterDebugMode();
        }

        inProgress = true;
        setProgressSources(0);
        setProgressMails(0);
        try {
            configure();
            loadTopicsAndMails();

            final List<IMASource> servers = getSourcesList(session);
            setTotalMails(servers.size());

            // Loop on all servers
            for (IMASource server : servers) {
                if (!server.isEnabled()) {
                    logger.info("[{}] Server not enabled, skipping it", server.getId());
                    setProgressSources(getProgressSources() + 1);
                    break;
                }
                logger.info("[{}] Loading mails from server", server.getId());

                IMailReader mailReader = null;
                try {
                    if (server instanceof Server) {
                        Server s = (Server) server;
                        mailReader =
                            mailManager.getMailReader(s.getHostname(), s.getPort(), s.getProtocol(), s.getUsername(),
                                s.getPassword(), server.getAdditionalProperties());

                    } else if (server instanceof MailStore) {
                        MailStore s = (MailStore) server;
                        mailReader = mailManager.getStoreManager(s.getFormat(), s.getLocation());
                    }
                } catch (Exception e1) {
                    logger.warn("[{}] Can't retrieve a mail reader", server.getId(), e1);
                }

                nbSuccess += loadMails(mailReader, server.getFolder(), session, server.getId());
                setProgressSources(getProgressSources() + 1);
                setProgressMails(0);
            }

            try {
                // Compute timeline feed
                if (config.isManageTimeline() && nbSuccess > 0) {
                    String timelineFeed = timelineGenerator.compute();
                    if (!StringUtils.isBlank(timelineFeed)) {
                        File timelineFeedLocation =
                            new File(environment.getPermanentDirectory(), "mailarchive/timeline");
                        FileWriter fw =
                            new FileWriter(timelineFeedLocation.getAbsolutePath() + "/TimeLineFeed.xml", false);
                        fw.write(timelineFeed);
                        fw.close();
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not compute timeline data", e);
            }

        } catch (MailArchiveException e) {
            logger.warn("EXCEPTION ", e);
            return -1;
        } catch (InitializationException e) {
            logger.warn("EXCEPTION ", e);
            return -1;
        } finally {
            inProgress = false;
            this.existingMessages = null;
            this.existingTopics = null;
            if (session.isDebugMode()) {
                quitDebugMode();
            }
        }

        return nbSuccess;

    }

    private List<IMASource> getSourcesList(final LoadingSession session)
    {
        List<IMASource> servers = null;
        final Map<org.xwiki.contrib.mail.source.SourceType, String> sources = session.getSources();
        servers = new ArrayList<IMASource>();
        boolean hasServers = false;
        for (Entry<SourceType, String> source : sources.entrySet()) {
            if (SourceType.SERVER.equals(source.getKey())) {
                Server server = factory.createMailServer(source.getValue());
                if (server != null) {
                    hasServers = true;
                    servers.add(server);
                }
            } else if (SourceType.STORE.equals(source.getKey())) {
                MailStore store = factory.createMailStore(source.getValue());
                if (store != null) {
                    servers.add(store);
                }
            } else {
                logger.warn("Unknown type of source connection: " + source.getKey());
            }
        }
        if (!hasServers) {
            // Empty server config means all servers
            // Empty store config means no store
            servers.addAll(config.getServers());
        }
        return servers;
    }

    public int loadMails(IMailReader mailReader, final String folder, final LoadingSession session,
        final String serverId)
    {

        int currentMsg = 1;
        int nbSuccess = 0;

        List<Message> messages = null;

        if (mailReader != null) {
            try {
                messages = mailReader.read(folder, !session.isLoadAll(), session.getLimit());
            } catch (MessagingException e) {
                logger.warn("[{}] Can't read messages from server", serverId, e);
                messages = new ArrayList<Message>();
            }
        }
        logger.info("[{}] Number of messages to treat : {}", new Object[] {serverId, messages.size()});
        setProgressMails(0);
        setTotalMails(messages.size());

        final int total = messages.size();
        MailLoadingResult result = null;
        for (Message message : messages) {
            logger.debug("[{}] Loading message {}/{}", new Object[] {serverId, currentMsg, total});
            try {
                try {
                    logger.debug("DEBUG MODE ? " + session.isDebugMode());
                    if (session.isDebugMode()) {
                        try {
                            final String id =
                                JavamailMessageParser.extractSingleHeader(message, "Message-ID").replaceAll(
                                    "[^a-zA-Z0-9-_\\.]", "_");
                            final File emlFile =
                                new File(environment.getPermanentDirectory(), "mailarchive/dump/" + id + ".eml");
                            final FileOutputStream fo = new FileOutputStream(emlFile);
                            message.writeTo(fo);
                            fo.flush();
                            fo.close();

                            logger.debug("Message dumped into " + id + ".eml");
                        } catch (Throwable t) {
                            // we catch Throwable because we don't want to cause problems in debug mode
                            logger.debug("Could not dump message for debug", t);
                        }
                    }
                    result = loadMail(message, !session.isSimulationMode(), false, null);
                } catch (Exception me) {
                    if (me instanceof MessagingException || me instanceof IOException) {
                        logger.debug("[" + serverId + "] Could not load email because of", me);
                        logger.info("[{}] Could not load email {}, trying to load a clone", currentMsg, serverId);

                        message = mailReader.cloneEmail(message);
                        if (message != null) {
                            result = loadMail(message, !session.isSimulationMode(), false, null);
                        }
                    }

                }
                setProgressMails(getProgressMails() + 1);
                if (result != null && result.isSuccess()) {
                    nbSuccess++;
                    if (!session.isSimulationMode()) {
                        message.setFlag(Flags.Flag.SEEN, true);

                        // Save it in our built-in store, only if we're not already loading emails from that same store
                        // :-)
                        if (config.isUseStore() && !builtinStore.equals(mailReader)) {
                            try {
                                // Use server id as folder to avoid colliding folders from different servers
                                builtinStore.write(serverId, message);
                            } catch (MessagingException e) {
                                logger.error("Can't copy mail to local store", e);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                logger.warn("Failed to load mail", e);
            }
            currentMsg++;
        }

        // nbMessages += currentMsg;
        return nbSuccess;
    }

    public String computeTimeline() throws XWikiException, InitializationException, MailArchiveException
    {
        if (!this.isConfigured) {
            configure();
        }
        return timelineGenerator.compute();
    }

    /**
     * @throws InitializationException
     * @throws MailArchiveException
     */
    protected void configure() throws InitializationException, MailArchiveException
    {
        // Init
        if (!this.isInitialized) {
            initialize();
        }

        config.reloadConfiguration();

        if (config.getItemsSpaceName() != null && !"".equals(config.getItemsSpaceName())) {
            XWikiPersistence.SPACE_ITEMS = config.getItemsSpaceName();
        }
        if (config.isUseStore()) {
            File maStoreLocation = new File(environment.getPermanentDirectory(), "mailarchive/storage");
            logger.info("Local Store Location: " + maStoreLocation.getAbsolutePath());
            logger.info("Local Store Provider: mstor");
            try {
                this.builtinStore = mailManager.getStoreManager("mstor", maStoreLocation.getAbsolutePath());
            } catch (ComponentLookupException e) {
                throw new InitializationException("Could not create or connect to built-in store");
            }
        }

        TextUtils.setLogger(this.logger);

        this.isConfigured = true;
    }

    protected void loadTopicsAndMails() throws MailArchiveException
    {
        existingTopics = store.loadStoredTopics();
        existingMessages = store.loadStoredMessages();
    }

    public IType getType(String name)
    {
        return config.getMailTypes().get(name);
    }

    /**
     * @param m
     */
    public void setMailSpecificParts(final MailItem m)
    {
        logger.debug("Extracting types");
        try {
            // Types
            List<IType> foundTypes = mailutils.extractTypes(config.getMailTypes().values(), m);
            logger.debug("Extracted types " + foundTypes);
            foundTypes.remove(getType(IType.BUILTIN_TYPE_MAIL));
            if (foundTypes.size() > 0) {
                for (IType foundType : foundTypes) {
                    logger.debug("Adding extracted type " + foundType);
                    m.addType(foundType.getId());
                }
            } else {
                logger.debug("No specific type found for this mail");
                m.addType(getType(IType.BUILTIN_TYPE_MAIL).getId());
            }

            // User
            logger.debug("Extracting user information");
            String userwiki = null;
            if (config.isMatchProfiles()) {
                IMAUser maUser = mailutils.parseUser(m.getFrom(), config.isMatchLdap());
                userwiki = maUser.getWikiProfile();
            }
            if (StringUtils.isBlank(userwiki)) {
                if (config.isMatchProfiles()) {
                    userwiki = UNKNOWN_USER;
                } else {
                    userwiki = config.getLoadingUser();
                }
            }
            m.setWikiuser(userwiki);

            // Compatibility: crop ids
            if (config.isCropTopicIds() && m.getTopicId().length() >= 30) {
                m.setTopicId(m.getTopicId().substring(0, 29));
            }
        } catch (Throwable t) {
            logger.warn("Exception ", t);
            t.printStackTrace();
        }
    }

    @Override
    public IMAUser parseUser(String internetAddress)
    {
        logger.debug("parseUser {}", internetAddress);
        try {
            configure();
        } catch (Exception e) {
            logger.warn("parseUser: failed to configure the Mail Archive", e);
            return null;
        }
        IMAUser user = mailutils.parseUser(internetAddress, config.isMatchLdap());
        logger.debug("parseUser return {}", user);
        return user;
    }

    /**
     * @param mail
     * @param confirm
     * @param isAttachedMail
     * @param parentMail
     * @return
     * @throws XWikiException
     * @throws ParseException
     * @throws IOException
     * @throws MessagingException
     */
    public MailLoadingResult loadMail(Part mail, boolean confirm, boolean isAttachedMail, String parentMail)
        throws XWikiException, ParseException, MessagingException, IOException
    {
        MailItem m = null;

        logger.warn("Parsing headers");
        m = mailManager.parseHeaders(mail);
        logger.warn("Parsing specific parts");
        setMailSpecificParts(m);
        // Compatibility option with old version of the mail archive
        if (config.isCropTopicIds() && m.getTopicId().length() > 30) {
            m.setTopicId(m.getTopicId().substring(0, 29));
        }
        logger.warn("PARSED MAIL  " + m);

        return loadMail(m, confirm, isAttachedMail, parentMail);
    }

    /**
     * @param m
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
        String topicDocName = null;

        logger.debug("Loading mail content into wiki objects");

        // set loading user for rights - loading user must have edit rights on IMailArchive and MailArchiveCode spaces
        context.setUser(config.getLoadingUser());
        logger.debug("Loading user " + config.getLoadingUser() + " set in context");

        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ", m.getLocale());

        // Create a new topic if needed
        String existingTopicId = "";
        // we don't create new topics for attached emails
        if (!isAttachedMail) {
            existingTopicId =
                existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId(), m.getRefs());
            if (existingTopicId == null) {
                logger.debug("  did not find existing topic, creating a new one");
                if (existingTopics.containsKey(m.getTopicId())) {
                    // Topic hack ...
                    logger.debug("  new topic but topicId already loaded, using messageId as new topicId");
                    m.setTopicId(m.getMessageId());
                    // FIX: "cut" properly mail history when creating a new topic
                    m.setReplyToId("");
                    existingTopicId =
                        existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId(), m.getRefs());
                } else {
                    existingTopicId = m.getTopicId();
                }
                logger.debug("   creating new topic");
                topicDocName = createTopicPage(m, confirm);

                logger.debug("  loaded new topic " + topicDocName);
            } else if (TextUtils.similarSubjects(m.getTopic(), existingTopics.get(existingTopicId).getSubject())) {
                logger.debug("  topic already loaded " + m.getTopicId() + " : " + existingTopics.get(existingTopicId));
                topicDocName =
                    updateTopicPage(m, existingTopics.get(existingTopicId).getFullName(), dateFormatter, confirm);
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
                existingTopicId =
                    existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId(), m.getRefs());
                logger.debug("  creating new topic");
                topicDocName = createTopicPage(m, confirm);

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
                return new MailLoadingResult(false, topicDocName, null);
            }

            return new MailLoadingResult(true, topicDocName, msgDoc != null ? msgDoc.getFullName() : null);
        } else {
            // message already loaded
            logger.debug("Mail already loaded - checking for updates ...");

            MailDescriptor msg = existingMessages.get(m.getMessageId());
            logger.debug("TopicId of existing message " + msg.getTopicId() + " and of topic " + existingTopicId
                + " are different ?" + (!msg.getTopicId().equals(existingTopicId)));
            if (!msg.getTopicId().equals(existingTopicId)) {
                msgDoc = xwiki.getDocument(existingMessages.get(m.getMessageId()).getFullName(), context);
                BaseObject msgObj = msgDoc.getObject(XWikiPersistence.SPACE_CODE + ".MailClass");
                msgObj.set("topicid", existingTopicId, context);
                if (confirm) {
                    logger.debug("saving message " + m.getSubject());
                    persistence.saveAsUser(msgDoc, null, config.getLoadingUser(),
                        "Updated mail with existing topic id found");
                }
            }

            return new MailLoadingResult(true, topicDocName, msgDoc != null ? msgDoc.getFullName() : null);
        }
    }

    /**
     * createTopicPage Creates a wiki page for a Topic.
     * 
     * @throws XWikiException
     */
    protected String createTopicPage(MailItem m, boolean create) throws XWikiException
    {
        String pageName = "T" + m.getTopic().replaceAll(" ", "");

        // Materialize mailing-lists information and mail IType in Tags
        ArrayList<String> taglist = extractTags(m);

        String createdTopicName = persistence.createTopic(pageName, m, taglist, config.getLoadingUser(), create);

        // add the existing topic created to the map
        existingTopics.put(m.getTopicId(), new TopicDescriptor(createdTopicName, m.getTopic()));

        return createdTopicName;
    }

    protected String updateTopicPage(MailItem m, String existingTopicId, SimpleDateFormat dateFormatter, boolean create)
        throws XWikiException
    {
        final String existingTopicPage = existingTopics.get(existingTopicId).getFullName();

        String updatedTopicName =
            persistence.updateTopicPage(m, existingTopicPage, dateFormatter, config.getLoadingUser(), create);

        existingTopics.put(m.getTopicId(), new TopicDescriptor(updatedTopicName, m.getTopic()));

        return updatedTopicName;
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
        logger.debug("createMailPage(" + m + "," + existingTopicId + "," + isAttachedMail + "," + parentMail + ","
            + create + ")");

        XWikiDocument msgDoc;
        String content = "";
        String htmlcontent = "";
        String zippedhtmlcontent = "";

        // a map to store attachment filename = contentId for replacements in HTML retrieved from mails
        HashMap<String, String> attachmentsMap = new HashMap<String, String>();
        ArrayList<MimeBodyPart> attbodyparts = new ArrayList<MimeBodyPart>();

        char prefix = 'M';
        if (isAttachedMail) {
            prefix = 'A';
        }
        String msgwikiname = xwiki.clearName(prefix + m.getTopic().replaceAll(" ", ""), context);
        if (msgwikiname.length() >= ExtendedDocumentAccessBridge.MAX_PAGENAME_LENGTH) {
            msgwikiname = msgwikiname.substring(0, ExtendedDocumentAccessBridge.MAX_PAGENAME_LENGTH);
        }
        String pagename = xwiki.getUniquePageName(XWikiPersistence.SPACE_ITEMS, msgwikiname, context);
        msgDoc = xwiki.getDocument(XWikiPersistence.SPACE_ITEMS + '.' + pagename, context);
        logger.debug("NEW MSG msgwikiname=" + msgwikiname + " pagename=" + pagename);

        Object bodypart = m.getBodypart();
        logger.debug("bodypart class " + bodypart.getClass());
        // addDebug("mail content type " + m.contentType)
        // Retrieve mail body(ies)
        logger.debug("Fetching mail content");
        MailContent mailContent = mailManager.parseContent(m.getOriginalMessage());
        // Resolve attachment urls against wiki document
        for (MailAttachment wikiAttachment : mailContent.getWikiAttachments().values()) {
            final String attachmentUrl = msgDoc.getAttachmentURL(wikiAttachment.getFilename(), context);
            wikiAttachment.setUrl(attachmentUrl);
        }

        List<Message> attachedMails = mailContent.getAttachedMails();
        final List<String> attachedMailsPages = loadAttachedMails(attachedMails, msgDoc, create);

        content = mailContent.getText();
        htmlcontent = mailContent.getHtml();

        // Truncate body
        content = TextUtils.truncateStringForBytes(content, 65500, 65500);

        // Treat Html part
        zippedhtmlcontent = treatHtml(htmlcontent, mailContent.getWikiAttachments());

        // Treat lengths
        m.setMessageId(TextUtils.truncateForString(m.getMessageId()));
        m.setSubject(TextUtils.truncateForString(m.getSubject()));
        existingTopicId = TextUtils.truncateForString(existingTopicId);
        m.setTopicId(TextUtils.truncateForString(m.getTopicId()));
        m.setTopic(TextUtils.truncateForString(m.getTopic()));
        m.setReplyToId(TextUtils.truncateForLargeString(m.getReplyToId()));
        m.setRefs(TextUtils.truncateForLargeString(m.getRefs()));
        m.setFrom(TextUtils.truncateForLargeString(m.getFrom()));
        m.setTo(TextUtils.truncateForLargeString(m.getTo()));
        m.setCc(TextUtils.truncateForLargeString(m.getCc()));

        // Assign text body converted from html content if there is no pure-text content
        if (StringUtils.isBlank(content) && !StringUtils.isBlank(htmlcontent)) {
            String converted = null;
            try {

                WikiPrinter printer = new DefaultWikiPrinter();
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
        BaseObject msgObj = msgDoc.newObject(XWikiPersistence.SPACE_CODE + ".MailClass", context);
        msgObj.set("messageid", m.getMessageId(), context);
        msgObj.set("messagesubject", m.getSubject(), context);

        msgObj.set("topicid", existingTopicId, context);
        msgObj.set("topicsubject", m.getTopic(), context);
        msgObj.set("inreplyto", m.getReplyToId(), context);
        msgObj.set("references", m.getRefs(), context);
        msgObj.set("date", m.getDate(), context);
        msgDoc.setCreationDate(m.getDate());
        msgDoc.setDate(m.getDate());
        msgDoc.setContentUpdateDate(m.getDate());
        msgObj.set("from", m.getFrom(), context);
        msgObj.set("to", m.getTo(), context);
        msgObj.set("cc", m.getCc(), context);
        msgObj.set("body", content, context);
        msgObj.set("bodyhtml", zippedhtmlcontent, context);
        msgObj.set("sensitivity", m.getSensitivity(), context);
        if (attachedMails.size() != 0) {
            msgObj.set("attachedMails", StringUtils.join(attachedMailsPages, ','), context);
        }
        if (isAttachedMail) {
            // FIXME: should be a built-in type, or something else maybe (ie mail/topic/attached mail ?)
            // For example, add field "topicType"=Topic|Calendar Event and "mailType"=Mail|Attached mail|Calendar Item
            // A Calendar Event could be a MailItem + some specific fields (start date, end date, id,...)
            // The MailItem could be stored as usual with specific type, and an additional object CalendarEvent could
            // store the calendary information
            // with a link to the related email(s) (for cancelled, update meeting,...)
            m.addType(IType.BUILTIN_TYPE_ATTACHED_MAIL);
        }
        if (m.getTypes().size() > 0) {
            String types = StringUtils.join(m.getTypes().toArray(new String[] {}), ',');
            msgObj.set("type", types, context);
        }
        if (parentMail != null) {
            msgDoc.setParent(parentMail);
        } else if (existingTopics.get(m.getTopicId()) != null) {
            msgDoc.setParent(existingTopics.get(m.getTopicId()).getFullName());
        }
        msgDoc.setTitle("Message " + m.getSubject());
        if (!isAttachedMail) {
            msgDoc.setComment("Created message");
        } else {
            msgDoc.setComment("Attached mail created");
        }
        ArrayList<String> taglist = extractTags(m);
        if (taglist.size() > 0) {
            BaseObject tagobj = msgDoc.newObject("XWiki.TagClass", context);
            String tags = StringUtils.join(taglist.toArray(new String[] {}), ',');
            tagobj.set("tags", tags.replaceAll(" ", "_"), context);
        }

        if (create && !checkMsgIdExistence(m.getMessageId())) {
            logger.debug("saving message " + m.getSubject());
            persistence.saveAsUser(msgDoc, m.getWikiuser(), config.getLoadingUser(),
                "Created message from mailing-list");
        }
        existingMessages.put(m.getMessageId(),
            new MailDescriptor(m.getSubject(), existingTopicId, msgDoc.getFullName()));
        logger.debug("  mail loaded and saved :" + msgDoc.getFullName());
        logger.debug("adding attachments to document");
        addAttachmentsFromMail(msgDoc, attbodyparts, attachmentsMap);

        return msgDoc;
    }

    private List<String> loadAttachedMails(List<Message> attachedMails, XWikiDocument msgDoc, boolean create)
    {
        final List<String> attachedMailsPages = new ArrayList<String>();
        if (attachedMails.size() > 0) {
            logger.debug("Loading attached mails ...");
            for (Message message : attachedMails) {
                try {
                    MailLoadingResult result = loadMail(message, create, true, msgDoc.getFullName());
                    if (result.isSuccess()) {
                        attachedMailsPages.add(result.getCreatedMailDocumentName());
                    } else {
                        logger.warn("Could not create attached mail " + message.getSubject());
                    }
                } catch (Exception e) {
                    logger.warn("Could not create attached mail because of " + e.getMessage());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not create attached mail ", e);
                    }
                }
            }
        }
        return attachedMailsPages;
    }

    /*
     * Cleans up HTML content and treat it to replace cid tags with correct image urls (targeting attachments), then zip
     * it.
     */
    String treatHtml(String htmlcontent, HashMap<String, MailAttachment> attachmentsMap) throws IOException
    {
        if (!StringUtils.isBlank(htmlcontent)) {
            logger.debug("Original HTML length " + htmlcontent.length());

            // Replacement to avoid issue of "A circumflex" characters displayed (???)
            htmlcontent = htmlcontent.replaceAll("&Acirc;", " ");

            // Replace attachment URLs in HTML content for images to be shown
            for (Entry<String, MailAttachment> att : attachmentsMap.entrySet()) {
                // remove starting "<" and finishing ">"
                final String cid = att.getKey();
                // If there is no cid, it means this attachment is not INLINE, so there's nothing more to do
                if (!StringUtils.isEmpty(cid)) {
                    String pattern = att.getKey().substring(1, att.getKey().length() - 2);
                    pattern = "cid:" + pattern;

                    logger.debug("Testing for CID pattern " + Util.encodeURI(pattern, context) + " " + pattern);
                    String replacement = att.getValue().getUrl();
                    logger.debug("To be replaced by " + replacement);
                    htmlcontent = htmlcontent.replaceAll(pattern, replacement);
                } else {
                    logger.warn("treatHtml: attachment is supposed not inline as cid is null or empty: "
                        + att.getValue().getFilename());
                }
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

            if (htmlcontent.length() > TextUtils.LONG_STRINGS_MAX_LENGTH) {
                logger.debug("Failed to have HTML fit in target field, truncating");
                htmlcontent = TextUtils.truncateForLargeString(htmlcontent);
            }

        } else {
            logger.debug("No HTML to treat");
        }

        logger.debug("Html Zipped length : " + htmlcontent.length());
        return htmlcontent;
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
        HashMap<String, String> attachmentsMap) throws MessagingException
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

                    logger.debug("Treating attachment of type: " + contentType);

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
                    addAttachmentFromMail(doc1, fileName, data);
                    nb++;
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
    public void addAttachmentFromMail(XWikiDocument doc, String afilename, byte[] adata) throws XWikiException
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
    }

    /*
     * Returns a valid name for an attachment from its original name
     */
    public String getAttachmentValidName(String afilename)
    {
        int i = afilename.lastIndexOf("\\");
        if (i == -1) {
            i = afilename.lastIndexOf("/");
        }
        String filename = afilename.substring(i + 1);
        filename = filename.replaceAll("\\+", " ");
        return filename;
    }

    protected ArrayList<String> extractTags(MailItem m)
    {
        // Materialize mailing-lists information and mail IType in Tags
        ArrayList<String> taglist = extractMailingListsTags(m);

        for (String typeid : m.getTypes()) {
            IType type = config.getMailTypes().get(typeid);
            taglist.add(type.getName());
        }

        return taglist;
    }

    /**
     * @param m
     * @return
     */
    protected ArrayList<String> extractMailingListsTags(MailItem m)
    {
        ArrayList<String> taglist = new ArrayList<String>();

        for (IMailingList list : config.getMailingLists().values()) {
            if (m.getFrom().contains(list.getDisplayName()) || m.getTo().contains(list.getPattern())
                || m.getCc().contains(list.getPattern())) {
                // Add tag of this mailing-list to the list of tags
                taglist.add(list.getTag());
            }
        }

        return taglist;
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
    public String existsTopic(String topicId, String topicSubject, String inreplyto, String messageid, String refs)
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
                BaseObject msgObj = msgDoc.getObject(XWikiPersistence.SPACE_CODE + ".MailClass");
                if (msgObj != null) {
                    logger
                        .debug("existsTopic : message " + replyId + " is a reply to " + existingMessages.get(replyId));
                    if (TextUtils.similarSubjects(previousSubject, msgObj.getStringValue("topicsubject"))) {
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
            if (!StringUtils.isBlank(topicId) && existingTopics.containsKey(topicId)) {
                logger.debug("existsTopic : found topic id in loaded topics");
                if (TextUtils.similarSubjects(topicSubject, existingTopics.get(topicId).getSubject())) {
                    foundTopicId = topicId;
                } else {
                    logger.debug("... but subjects are too different");
                }
            }
        }
        // Search with references
        if (foundTopicId == null) {
            String xwql =
                "select distinct mail.topicid from Document doc, doc.object(" + XWikiPersistence.CLASS_MAILS
                    + ") as mail where mail.references like '%" + messageid + "%'";
            try {
                List<String> topicIds = queryManager.createQuery(xwql, Query.XWQL).execute();
                // We're not supposed to find several topics related to messages having this id in references ...
                if (topicIds.size() == 1) {
                    foundTopicId = topicIds.get(0);
                }
                if (topicIds.size() > 1) {
                    logger.warn("We should have found only one topicId instead of this list : " + topicIds
                        + ", using the first found");
                }
            } catch (QueryException e) {
                logger.warn("Issue while searching for references", e);
            }
        }
        // Search in existing topics with exactly same subject
        if (foundTopicId == null) {
            for (String currentTopicId : existingTopics.keySet()) {
                TopicDescriptor currentTopic = existingTopics.get(currentTopicId);
                if (currentTopic.getSubject().trim().equalsIgnoreCase(topicSubject.trim())) {
                    logger.debug("existsTopic : found subject in loaded topics");
                    if (!StringUtils.isBlank(inreplyto)) {
                        logger.debug("existsTopic : not first message in topic, so we assume it's linked to it");
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
                        if (!StringUtils.isBlank(refs)) {
                            logger.debug("existsTopic : ... but references are not empty, so attach to found topicId "
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
     * {@inheritDoc}
     * 
     * @throws IOException
     * @throws XWikiException
     * @throws MailArchiveException
     * @throws InitializationException
     * @see org.xwiki.contrib.mailarchive.IMailArchive#getDecodedMailText(java.lang.String, boolean)
     */
    @SuppressWarnings("deprecation")
    @Override
    public DecodedMailContent getDecodedMailText(String mailPage, boolean cut) throws IOException, XWikiException,
        InitializationException, MailArchiveException
    {
        if (!this.isConfigured) {
            configure();
        }
        if (!StringUtils.isBlank(mailPage)) {
            XWikiDocument htmldoc = null;
            try {
                htmldoc = xwiki.getDocument(mailPage, this.context);
            } catch (Throwable t) {
                // FIXME Ugly workaround for "org.hibernate.SessionException: Session is closed!"
                try {
                    htmldoc = xwiki.getDocument(mailPage, this.context);
                } catch (Exception e) {
                    htmldoc = null;
                }
            }
            if (htmldoc != null) {
                BaseObject htmlobj = htmldoc.getObject("MailArchiveCode.MailClass");
                if (htmlobj != null) {
                    String ziphtml = htmlobj.getLargeStringValue("bodyhtml");
                    String body = htmlobj.getLargeStringValue("body");

                    return (mailutils.decodeMailContent(ziphtml, body, cut));
                }
            }
        }

        return new DecodedMailContent(false, "");

    }

    public void enterDebugMode()
    {
        // Logs level
        aggregatedLoggerManager.pushLogLevel(LogLevel.DEBUG);
        logger.debug("DEBUG MODE ON");
    }

    public void quitDebugMode()
    {
        logger.debug("DEBUG MODE OFF");
        aggregatedLoggerManager.popLogLevel();
    }

    /**
     * @return the progressMails
     */
    public int getProgressMails()
    {
        return progressMails;
    }

    /**
     * @param progressMails the progressMails to set
     */
    public void setProgressMails(int progressMails)
    {
        this.progressMails = progressMails;
    }

    /**
     * @return the progressSources
     */
    public int getProgressSources()
    {
        return progressSources;
    }

    /**
     * @param progressSources the progressSources to set
     */
    public void setProgressSources(int progressSources)
    {
        this.progressSources = progressSources;
    }

    /**
     * @return the totalMails
     */
    public int getTotalMails()
    {
        return totalMails;
    }

    /**
     * @param totalMails the totalMails to set
     */
    public void setTotalMails(int totalMails)
    {
        this.totalMails = totalMails;
    }

    /**
     * @return the totalSources
     */
    public int getTotalSources()
    {
        return totalSources;
    }

    /**
     * @param totalSources the totalSources to set
     */
    public void setTotalSources(int totalSources)
    {
        this.totalSources = totalSources;
    }

}
