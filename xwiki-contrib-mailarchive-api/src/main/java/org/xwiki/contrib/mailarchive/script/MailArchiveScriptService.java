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
package org.xwiki.contrib.mailarchive.script;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mailarchive.IMAUser;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.contrib.mailarchive.internal.LoadingJob;
import org.xwiki.contrib.mailarchive.internal.data.IFactory;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.threads.ThreadMessageBean;
import org.xwiki.contrib.mailarchive.internal.timeline.ITimeLineGenerator;
import org.xwiki.contrib.mailarchive.internal.utils.DecodedMailContent;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.Job;
import org.xwiki.job.JobManager;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.objects.BaseObject;

/**
 * Make the IMailArchive API available to scripting.
 */
@Component
@Named("mailarchive")
@Singleton
/**
 * Mail Archive Script Service.
 * 
 * @version $Id$
 */
public class MailArchiveScriptService implements ScriptService, IMailArchiveScriptService
{
    @Inject
    private IMailArchive mailArchive;

    @Inject
    private ITimeLineGenerator timeline;

    @Inject
    private IFactory factory;

    @Inject
    private JobManager jobManager;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#session()
     */
    @Override
    public LoadingSession session()
    {
        return new LoadingSession();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#session(java.lang.String)
     */
    @Override
    public LoadingSession session(final String sessionPrefsDoc)
    {
        return factory.createLoadingSession(sessionPrefsDoc);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#session(com.xpn.xwiki.objects.BaseObject)
     */
    @Override
    public LoadingSession sessionFromXObject(final BaseObject sessionObject)
    {
        logger.error("Create LoadingSession from BaseObject " + sessionObject);
        return factory.createLoadingSession(sessionObject);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#check(java.lang.String)
     */
    @Override
    public int check(final String serverPrefsDoc)
    {
        return mailArchive.checkSource(serverPrefsDoc);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#load(org.xwiki.contrib.mailarchive.LoadingSession)
     */
    @Override
    public int load(final LoadingSession session)
    {

        LoadingJob job = createLoadingJob(session, true);
        return job != null ? job.getNbSuccess() : -1;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#startLoadingJob(org.xwiki.contrib.mailarchive.LoadingSession)
     */
    @Override
    public Job startLoadingJob(final LoadingSession session)
    {
        return createLoadingJob(session, false);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#getCurrentJob()
     */
    @Override
    public Job getCurrentJob()
    {
        return jobManager.getCurrentJob();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#thread(java.lang.String)
     */
    @Override
    public ArrayList<ThreadMessageBean> thread(final String topicid)
    {
        // We "flatten" the output to avoid needing recursivity to display the thread(s).
        return this.mailArchive.computeThreads(topicid).flatten();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#thread()
     */
    @Override
    public ArrayList<ThreadMessageBean> thread()
    {
        return thread(null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#parseUser(java.lang.String)
     */
    @Override
    public IMAUser parseUser(final String internetAddress)
    {
        return mailArchive.parseUser(internetAddress);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#getTimeline()
     */
    @Override
    public ITimeLineGenerator getTimeline()
    {
        return this.timeline;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#getDecodedMailText(java.lang.String, boolean)
     */
    @Override
    public DecodedMailContent getDecodedMailText(final String mailPage, final boolean cut)
    {
        try {
            return this.mailArchive.getDecodedMailText(mailPage, cut);
        } catch (Exception e) {
            System.out.println("MailArchiveScriptService: failed to decode mail content");
            e.printStackTrace();
            return new DecodedMailContent(false, "<<invalid content>>");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService#getConfig()
     */
    @Override
    public IMailArchiveConfiguration getConfig()
    {
        try {
            return this.mailArchive.getConfiguration();
        } catch (InitializationException e) {
            e.printStackTrace();
            return null;
        } catch (MailArchiveException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a loading job either in synchronous or asynchronous mode.<br/>
     * To check for progress/results, cf. {@link org.xwiki.contrib.}
     * 
     * @param session The Loading Session description.
     * @param synchronous If true creates a job and waits for its end, if false returns right away.
     * @return Created LoadingJob, or null if a problem occurred.
     */
    private LoadingJob createLoadingJob(final LoadingSession session, boolean synchronous)
    {
        if (session == null) {
            logger.warn("Missing loading session object.");
            return null;
        }
        LoadingJob job;
        try {
            job = this.componentManager.getInstance(Job.class, LoadingJob.JOBTYPE);
        } catch (ComponentLookupException e) {
            logger.error("Failed to lookup any Job for role hint [" + LoadingJob.JOBTYPE + "]", e);
            return null;
        }

        final String id = LoadingJob.JOB_ID;

        DefaultRequest request = new DefaultRequest();
        request.setId(id);
        request.setProperty(LoadingJob.JOB_PROPERTY_TYPE, LoadingJob.JOBTYPE);
        request.setInteractive(false);
        request.setProperty(LoadingJob.JOB_PROPERTY_SESSION, session);
        job.initialize(request);
        jobManager.addJob(job);
        if (synchronous) {
            try {
                job.join();
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        return job;
    }

}
