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
package org.xwiki.contrib.mailarchive.script.internal;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.codehaus.plexus.util.StringOutputStream;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mail.internal.FolderItem;
import org.xwiki.contrib.mailarchive.IMAUser;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.contrib.mailarchive.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.LoadingJob;
import org.xwiki.contrib.mailarchive.internal.data.IFactory;
import org.xwiki.contrib.mailarchive.internal.threads.ThreadMessageBean;
import org.xwiki.contrib.mailarchive.script.IMailArchiveScriptService;
import org.xwiki.contrib.mailarchive.timeline.ITimeLineGenerator;
import org.xwiki.contrib.mailarchive.utils.DecodedMailContent;
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

    @Override
    public LoadingSession session(final String sessionPrefsDoc)
    {
        return factory.createLoadingSession(sessionPrefsDoc);
    }

    @Override
    public LoadingSession sessionFromXObject(final BaseObject sessionObject)
    {
        logger.error("Create LoadingSession from BaseObject " + sessionObject);
        return factory.createLoadingSession(sessionObject);
    }

    @Override
    public int check(final String serverPrefsDoc)
    {
        return mailArchive.checkSource(serverPrefsDoc);
    }    

    @Override
    public ArrayList<FolderItem> getFolderTree(String serverPrefsDoc)
    {
        return mailArchive.getFolderTree(serverPrefsDoc);
    }

    @Override
    public int load(final LoadingSession session)
    {

        LoadingJob job = createLoadingJob(session, true);
        return job != null ? job.getStatus().getNbSuccess() : -1;
    }

    @Override
    public Job startLoadingJob(final LoadingSession session)
    {
        return createLoadingJob(session, false);
    }

    @Override
    public Job getCurrentJob()
    {
        return jobManager.getCurrentJob();
    }

    @Override
    public ArrayList<ThreadMessageBean> thread(final String topicid)
    {
        // We "flatten" the output to avoid needing recursivity to display the thread(s).
        return this.mailArchive.computeThreads(topicid).flatten();
    }

    @Override
    public ArrayList<ThreadMessageBean> thread()
    {
        return thread(null);
    }

    @Override
    public IMAUser parseUser(final String internetAddress)
    {
        return mailArchive.parseUser(internetAddress);
    }

    @Override
    public ITimeLineGenerator getTimeline()
    {
        return this.timeline;
    }

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

        if (mailArchive.isLocked()) {
            logger.info("Archive is locked, not running new job");
            return null;
        }
        
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
        // FIXME: ID of request might be mailarchive/<sessionId>/<namespace> ? ... to retrieve jobs later on ...
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

    @Override
    public String getOriginal(String serverId, String messageId)
    {       
        String original = null;
        
        try {
            Message message = this.mailArchive.getFromStore(serverId, messageId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            message.writeTo(baos);
            // FIXME: not sure about utf-8 here...
            original = baos.toString("UTF-8");            
        } catch (Exception e) {
            logger.debug("Error retrieving original message", e);
        } 
        
        return original;
    }

}
