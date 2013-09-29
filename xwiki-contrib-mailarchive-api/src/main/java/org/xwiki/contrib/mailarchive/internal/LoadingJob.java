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

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mailarchive.IMailArchiveLoader;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.event.status.JobStatus.State;
import org.xwiki.job.internal.AbstractJob;
import org.xwiki.job.internal.DefaultJobStatus;

/*
 * @version $Id$
 */
@Component
@Named(LoadingJob.JOBTYPE)
public class LoadingJob extends AbstractJob<DefaultRequest, DefaultJobStatus<DefaultRequest>> implements Initializable
{
    public static final String JOBTYPE = "mailarchivejob";

    @Inject
    private IMailArchiveLoader loader;

    /** Aggregated component logger */
    /*
     * @Inject private IAggregatedLoggerManager aggregatedLoggerManager;
     */

    // @Inject
    // private Logger logger;

    private int nbSuccess = 0;

    private int nbFailure = 0;

    private String currentSource = null;

    private String currentMail = null;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        logger.debug("MailArchive Loading Job INITIALIZED");
        /*
         * aggregatedLoggerManager.addComponentLogger(IMailArchive.class);
         * aggregatedLoggerManager.addComponentLogger(IMailComponent.class);
         * aggregatedLoggerManager.addComponentLogger(StreamParser.class);
         */
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.job.Job#getType()
     */
    @Override
    public String getType()
    {
        return JOBTYPE;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.job.AbstractJob#start()
     */
    @Override
    protected void runInternal()
    {
        logger.debug("Starting Loading Job");
        this.status.setState(State.RUNNING);
        try {
            LoadingSession session = (LoadingSession) getRequest().getProperty("sessionobj");

            if (session.isDebugMode()) {
                enterDebugMode();
            }

            int success = loader.loadMails(session, this);
            setNbSuccess(success);

            if (session.isDebugMode()) {
                quitDebugMode();
            }
        } catch (Throwable t) {
            logger.error("Loading Job failure", t);
        } finally {
            this.status.setState(State.FINISHED);
        }
    }

    /**
     * @return the success
     */
    public int getNbSuccess()
    {
        return nbSuccess;
    }

    protected void setNbSuccess(final int nbSuccess)
    {
        this.nbSuccess = nbSuccess;
    }

    public void incNbSuccess()
    {
        this.nbSuccess++;
    }

    public int getNbFailure()
    {
        return nbFailure;
    }

    public void incNbFailure()
    {
        this.nbFailure++;
    }

    /**
     * @return the currentSource
     */
    public String getCurrentSource()
    {
        return currentSource;
    }

    /**
     * @param currentSource the currentSource to set
     */
    public void setCurrentSource(final String currentSource)
    {
        this.currentSource = currentSource;
    }

    /**
     * @return the currentMail
     */
    public String getCurrentMail()
    {
        return currentMail;
    }

    /**
     * @param currentMail the currentMail to set
     */
    public void setCurrentMail(final String currentMail)
    {
        this.currentMail = currentMail;
    }

    public void enterDebugMode()
    {
        // Logs level
        logger.error("enterDebugMode()");
        /*
         * aggregatedLoggerManager.pushLogLevel(LogLevel.DEBUG); logger.debug("DEBUG MODE ON");
         */
    }

    public void quitDebugMode()
    {
        logger.debug("DEBUG MODE OFF");
        /*
         * aggregatedLoggerManager.popLogLevel(); logger.error("quitDebugMode()");
         */
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.job.AbstractJob#notifyPopLevelProgress()
     */
    @Override
    public void notifyPopLevelProgress()
    {
        super.notifyPopLevelProgress();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.job.AbstractJob#notifyPushLevelProgress(int)
     */
    @Override
    public void notifyPushLevelProgress(final int steps)
    {
        super.notifyPushLevelProgress(steps);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.job.AbstractJob#notifyStepPropress()
     */
    @Override
    public void notifyStepPropress()
    {
        super.notifyStepPropress();
    }

}
