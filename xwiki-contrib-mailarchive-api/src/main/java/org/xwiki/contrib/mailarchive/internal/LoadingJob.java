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

import org.codehaus.plexus.util.ExceptionUtils;
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
public class LoadingJob extends AbstractJob<DefaultRequest, EmailLoadingJobStatus> implements Initializable
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

    /**
     * 
     */
    public static final String JOB_PROPERTY_TYPE = "job.type";

    /**
     * 
     */
    public static final String JOB_ID = "mailarchive";

    /**
     * 
     */
    public static final String JOB_PROPERTY_SESSION = "sessionobj";

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
        logger.info("Starting MailArchive Loading Job");
        getStatus().setState(State.RUNNING);
        try {
            LoadingSession session = (LoadingSession) getRequest().getProperty("sessionobj");
            if (session == null) {
                logger.info("No loading session provided from context, job is invalid");
                return;
            } else {
                logger.debug("Loading job will use session {}", session);
            }
            
            if (session.isDebugMode()) {
                enterDebugMode();
            }

            int success = loader.loadMails(session, this);
            getStatus().setNbSuccess(success);
            
            logger.debug("MailArchive loading job finished, successfully loaded {} emails for this session", success);

            if (session.isDebugMode()) {
                quitDebugMode();
            }
        } catch (Throwable t) {
            logger.error("Loading Job failed", ExceptionUtils.getRootCause(t));
        } finally {
            this.status.setState(State.FINISHED);
        }
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
    
    /**
     * @param request contains information related to the job to execute
     * @return the status of the job
     */
    @Override
    protected EmailLoadingJobStatus createNewStatus(DefaultRequest request)
    {
        return new EmailLoadingJobStatus(request, this.observationManager, this.loggerManager,
            this.jobContext.getCurrentJob() != null);
    }    
}
