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
import org.xwiki.contrib.mailarchive.IMAUser;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.contrib.mailarchive.internal.threads.ThreadMessageBean;
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

    public LoadingSession session()
    {
        return new LoadingSession(this.mailArchive);
    }

    public LoadingSession session(final String serverPrefsDoc)
    {
        return new LoadingSession(this.mailArchive, serverPrefsDoc);
    }

    public int check(final String serverPrefsDoc)
    {
        return this.mailArchive.queryServerInfo(serverPrefsDoc);
    }

    public int load(final LoadingSession session)
    {
        return this.mailArchive.loadMails(session);
    }

    /**
     * Threads messages related to a topic, given its topic ID.<br/>
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
     * @param topicid A topic ID, as can be found in a MailTopicClass object instance in "topicId" field.
     * @return An array of threaded messages.
     */
    public ArrayList<ThreadMessageBean> thread(final String topicid)
    {
        // We "flatten" the output to avoid needing recursivity to display the thread(s).
        return this.mailArchive.computeThreads(topicid).flatten();
    }

    public ArrayList<ThreadMessageBean> thread()
    {
        return thread(null);
    }

    public IMAUser parseUser(final String internetAddress)
    {
        return this.mailArchive.parseUser(internetAddress);
    }

    // FIXME: for manual tests only, to be removed
    public String getTimelineFeed()
    {
        String timelineFeed = "";
        try {
            timelineFeed = this.mailArchive.computeTimeline();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return timelineFeed;
    }

    public String getDecodedMailText(final String mailPage, final boolean cut)
    {
        try {
            return this.mailArchive.getDecodedMailText(mailPage, cut);
        } catch (Exception e) {
            return "<<invalid content>>";
        }
    }
}
