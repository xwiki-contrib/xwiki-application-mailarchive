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

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mailarchive.IMAUser;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.contrib.mailarchive.internal.data.IFactory;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.threads.ThreadMessageBean;
import org.xwiki.contrib.mailarchive.internal.timeline.ITimeLineGenerator;
import org.xwiki.contrib.mailarchive.internal.utils.DecodedMailContent;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobManager;
import org.xwiki.script.service.ScriptService;

/**
 * Make the IMailArchive API available to scripting.
 */
@Component
@Named("mailarchive")
@Singleton
public class MailArchiveScriptService implements ScriptService
{
    @Inject
    private IMailArchive mailArchive;

    @Inject
    private ITimeLineGenerator timeline;

    @Inject
    private IFactory factory;

    @Inject
    private JobManager jobManager;

    /**
     * Creates a loading session based on default session configuration, stored in document
     * MailArchivePrefs.LoadingSession_default.
     * 
     * @return
     */
    public LoadingSession session()
    {
        return new LoadingSession(mailArchive);
    }

    /**
     * Creates a loading session, based on configuration stored in specified wiki page.
     * 
     * @param serverPrefsDoc
     * @return
     */
    public LoadingSession session(final String sessionPrefsDoc)
    {
        return factory.createLoadingSession(sessionPrefsDoc, mailArchive);
    }

    public int check(final String serverPrefsDoc)
    {
        return this.mailArchive.checkSource(serverPrefsDoc);
    }

    public int load(final LoadingSession session)
    {
        LoadingJob job = createLoadingJob(session, true);
        return job != null ? job.getNbSuccess() : -1;
    }

    public Job startLoadingJob(final LoadingSession session)
    {
        return createLoadingJob(session, false);
    }

    private LoadingJob createLoadingJob(final LoadingSession session, boolean synchronous)
    {
        LoadingJob job = null;
        DefaultRequest request = new DefaultRequest();
        request.setId("test");
        request.setInteractive(false);
        request.setProperty("sessionobj", session);
        try {
            if (synchronous) {
                job = (LoadingJob) jobManager.executeJob("mailarchivejob", request);
            } else {
                job = (LoadingJob) jobManager.addJob("mailarchivejob", request);
            }
        } catch (JobException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return job;
    }

    /**
     * Threads messages related to a topic, given its topic ID.<br/>
     * 
     * @param topicid A topic ID, as can be found in a TopicClass object instance in "topicId" field.
     * @return An array of threaded messages.
     */
    public ArrayList<ThreadMessageBean> thread(final String topicid)
    {
        // We "flatten" the output to avoid needing recursivity to display the thread(s).
        return this.mailArchive.computeThreads(topicid).flatten();
    }

    /**
     * Threads all messages in the mail archive.<br/>
     * The result is an array, and not a recursive structure, in order to facilitate display of the thread.<br/>
     * For each message, property "level" provides the current level in the thread hierarchy,<br/>
     * and "index" provides the sequence number in the whole thread stack.<br/>
     * For example for the following thread:<br/>
     * - "I have a question" (level:0, index:0)<br/>
     * -- "Re: I have a question" (level:1, index:1)<br/>
     * --- "Re: I have a question" (level:2, index:2)<br/>
     * -- "Re: I have a question" (level:1, index:3)<br/>
     * This allows to easily sort by thread, and display thread hierarchy.
     * 
     * @return An array of threaded messages.
     */
    public ArrayList<ThreadMessageBean> thread()
    {
        return thread(null);
    }

    public IMAUser parseUser(final String internetAddress)
    {
        return this.mailArchive.parseUser(internetAddress);
    }

    // FIXME: for manual tests only, to be removed
    public ITimeLineGenerator getTimeline()
    {
        return this.timeline;
    }

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

}
