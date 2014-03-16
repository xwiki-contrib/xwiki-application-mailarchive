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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mail.IMailComponent;
import org.xwiki.contrib.mail.IMailReader;
import org.xwiki.contrib.mailarchive.IMASource;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.IMailArchiveLoader;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.contrib.mailarchive.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.data.MailStore;
import org.xwiki.contrib.mailarchive.internal.data.Server;
import org.xwiki.contrib.mailarchive.utils.IAggregatedLoggerManager;
import org.xwiki.logging.LogLevel;
import org.xwiki.rendering.parser.StreamParser;

/**
 * @version $Id$
 */
@Component
@Singleton
public class DefaultMailLoader implements IMailArchiveLoader, Initializable
{

    @Inject
    private IMailArchive mailArchive;

    /** Provides access to low-level mail api component */
    @Inject
    private IMailComponent mailManager;

    /** Provides access to the Mail archive configuration */
    @Inject
    private IMailArchiveConfiguration config;

    /** Aggregated component logger */
    @Inject
    private IAggregatedLoggerManager aggregatedLoggerManager;

    @Inject
    private Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        aggregatedLoggerManager.addComponentLogger(IMailArchive.class);
        aggregatedLoggerManager.addComponentLogger(IMailComponent.class);
        aggregatedLoggerManager.addComponentLogger(StreamParser.class);
    }

    /**
     * @param session
     * @return
     */
    public int loadMails(final LoadingSession session)
    {
        return loadMails(session, null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailArchive#loadMails(org.xwiki.contrib.mailarchive.LoadingSession)
     */
    public int loadMails(final LoadingSession session, final LoadingJob job)
    {
        int nbSuccess = 0;

        if (session == null) {
            logger.warn("Invalid loading session (null) ...");
            return -1;
        } else {
            logger.info("Starting new mail loading session...");
        }

        boolean hasLock = mailArchive.lock();
        if (!hasLock) {
            logger.warn("Loading process already in progress ...");
            return -1;
        }
        logger.debug("Locked the archive");

        try {                       

            if (session.isDebugMode()) {
                aggregatedLoggerManager.pushLogLevel(LogLevel.DEBUG);
            } else {
                aggregatedLoggerManager.pushLogLevel(LogLevel.INFO);
            }
            
            logger.debug("Loading parameters: " + session.toString());

            // Reinitialize configuration
            // FIXME: not very nice to call getConfiguration to reload configuration from db ...
            this.config = mailArchive.getConfiguration();

            final List<IMASource> servers = mailArchive.getSourcesList(session);
            if (servers != null && !servers.isEmpty()) {
                if (job != null) {
                    job.notifyPushLevelProgress(servers.size());
                }

                // Loop on all servers
                for (IMASource server : servers) {

                    if (job != null) {
                        if (job.getStatus().isStopped())
                            break;
                        while (job.getStatus().isPaused()) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                            }
                        }
                    }

                    IMailReader mailReader = getReader(server);
                    if (job != null) {
                        job.getStatus().setCurrentSource("" + server.getType() + ':' + server.getId());
                    }
                    if (mailReader != null) {
                        nbSuccess += loadMails(mailReader, server.getFolder(), session, server.getId(), job);
                        mailReader.close();
                    }
                    if (job != null) {
                        job.notifyStepPropress();
                    }

                }

                if (job != null) {
                    job.notifyPopLevelProgress();
                }

                try {
                    // Compute timeline feed
                    if (config.isManageTimeline() && nbSuccess > 0) {
                        mailArchive.computeTimeline();
                    }
                } catch (Exception e) {
                    logger.warn("Could not compute timeline data", e);
                }
            } else {
                logger.warn("No Server nor Store found to load from, nothing to do");
            }

        } catch (InitializationException e) {
            logger.error("Could not initialize a component", e);
            nbSuccess = -1;
        } catch (MailArchiveException e) {
            logger.error("Could not retrieve configuration", e);
            nbSuccess = -1;
        } finally {
            mailArchive.unlock();
            aggregatedLoggerManager.popLogLevel();
        }

        return nbSuccess;

    }

    /**
     * @param mailReader
     * @param folder
     * @param session
     * @param serverId
     * @param job
     * @return
     */
    protected int loadMails(IMailReader mailReader, final String folder, final LoadingSession session,
        final String serverId, final LoadingJob job)
    {
        logger.debug("[{}] loadMails(mailReader={}, folder={}, session={}, serverId={}, job={}", serverId, mailReader,
            folder, session, serverId, job);

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
        final int total = messages.size();
        int limit = session.getLimit();
        if (limit > 0) {
            if (limit > total) {
                limit = total;
            }
        } else {
            limit = total;
        }
        logger.info("[{}] Number of messages to treat : {} / {}", new Object[] {serverId, limit, total});
        if (job != null) {
            job.notifyPushLevelProgress(limit);
        }

        MailLoadingResult result = null;
        while (currentMsg < limit && (job != null && !job.getStatus().isStopped())) {
            logger.info("Current {} / limit {}", currentMsg, limit);

            while (job != null && job.getStatus().isPaused()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }

            Message message = messages.get(currentMsg - 1);
            logger.debug("[{}] Loading message {}/{}", new Object[] {serverId, currentMsg, total});
            try {
                try {
                    if (session.isDebugMode()) {
                        mailArchive.dumpEmail(message);
                    }
                    if (job != null) {
                        job.getStatus().setCurrentMail(message.getSubject());
                    }
                    result = mailArchive.loadMail(message, !session.isSimulationMode(), false, null);
                } catch (Exception me) {
                    if (me instanceof MessagingException || me instanceof IOException) {
                        logger.debug("[" + serverId + "] Could not load email because of", me);
                        logger.info("[{}] Could not load email {}, trying to load a clone", currentMsg, serverId);

                        message = mailReader.cloneEmail(message);
                        if (message != null) {
                            result = mailArchive.loadMail(message, !session.isSimulationMode(), false, null);
                        }
                    } else {
                        logger.warn("Unexpected exception occurred while loading email [{}]",
                            ExceptionUtils.getRootCause(me));
                    }

                }

                if (job != null) {
                    job.notifyStepPropress();
                }

                if (result != null) {
                    if (result.isSuccess()) {
                        nbSuccess++;
                        if (!session.isSimulationMode()) {
                            message.setFlag(Flags.Flag.SEEN, true);

                            // Back it up in the built-in store
                            if (config.isUseStore()) {
                                mailArchive.saveToInternalStore(serverId, mailReader.getMailSource(), message);
                            }
                        }
                        if (job != null) {
                            job.getStatus().incNbSuccess();
                        }
                    } else if (job != null) {
                        job.getStatus().incNbFailure();
                    }
                    if (job != null) {
                        if (MailLoadingResult.STATUS.ALREADY_LOADED.equals(result.getStatus())) {
                            job.getStatus().incNbAlreadyLoaded();
                        }
                        if (MailLoadingResult.STATUS.NOT_MATCHING_MAILING_LISTS.equals(result.getStatus())) {
                            job.getStatus().incNbNotMatchingMailingLists();
                        }
                    }

                }

            } catch (Throwable e) {
                logger.error("[{}] Failed to load mail [{}]", serverId, ExceptionUtils.getRootCause(e));
            }

            currentMsg++;
        }

        if (job != null) {
            job.getStatus().setCurrentMail("");
            job.notifyPopLevelProgress();
        }

        return nbSuccess;
    }

    protected IMailReader getReader(final IMASource source)
    {
        if (!source.isEnabled()) {
            logger.info("[{}] Server not enabled, skipping it", source.getId());
            return null;
        }
        logger.info("[{}] Creating reader", source.getId());

        IMailReader mailReader = null;
        try {
            if (source instanceof Server) {
                Server s = (Server) source;
                mailReader =
                    mailManager.getMailReader(s.getHostname(), s.getPort(), s.getProtocol(), s.getUsername(),
                        s.getPassword(), source.getAdditionalProperties(), s.isAutoTrustSSLCertificates());

            } else if (source instanceof MailStore) {
                MailStore s = (MailStore) source;
                mailReader = mailManager.getStoreManager(s.getFormat(), s.getLocation());
            }
        } catch (Exception e1) {
            logger.warn("[{}] Can't retrieve a mail reader", source.getId(), e1);
        }

        return mailReader;
    }

}
