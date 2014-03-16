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

import javax.mail.Message;

import org.xwiki.contrib.mail.internal.FolderItem;
import org.xwiki.contrib.mailarchive.IMAUser;
import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.contrib.mailarchive.internal.threads.ThreadMessageBean;
import org.xwiki.contrib.mailarchive.timeline.ITimeLineGenerator;
import org.xwiki.contrib.mailarchive.utils.DecodedMailContent;
import org.xwiki.job.Job;

import com.xpn.xwiki.objects.BaseObject;

/**
 * Mail Archive Script API.
 * 
 * @version $Id$
 */
public interface IMailArchiveScriptService
{

    /**
     * Checks a server/store account, using account described in specified preference page.
     * 
     * @param serverPrefsDoc
     * @return
     */
    int check(String serverPrefsDoc);
    
    ArrayList<FolderItem> getFolderTree(String serverPrefsDoc);

    /**
     * Creates a new default loading session.
     * 
     * @return
     */
    LoadingSession session();

    /**
     * Creates a loading session, based on configuration stored in specified wiki page.
     * 
     * @param serverPrefsDoc
     * @return
     */
    LoadingSession session(String sessionPrefsDoc);

    /**
     * Creates a loading session from an XObject.
     * 
     * @param sessionObject
     * @return
     */
    LoadingSession sessionFromXObject(BaseObject sessionObject);

    /**
     * Triggers a synchronous Loading Session.
     * 
     * @param session
     * @return
     */
    int load(LoadingSession session);

    /**
     * Triggers an asynchronous loading session.
     * 
     * @param session
     * @return
     */
    Job startLoadingJob(LoadingSession session);

    /**
     * Returns the current job, if any.
     * 
     * @return
     */
    Job getCurrentJob();

    /**
     * Threads messages related to a topic, given its topic ID.<br/>
     * 
     * @param topicid A topic ID, as can be found in a TopicClass object instance in "topicId" field.
     * @return An array of threaded messages.
     */
    ArrayList<ThreadMessageBean> thread(String topicid);

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
    ArrayList<ThreadMessageBean> thread();

    /**
     * Parses a user internet address.<br/>
     * Specified in {@link org.xwiki.contrib.mailarchive.utils.IMailUtils#parseUser(String, boolean)}
     * 
     * @param internetAddress
     * @return A "mail archive" user.
     */
    IMAUser parseUser(String internetAddress);

    /**
     * Returns the timeline generator component. Interface specified in
     * {@link org.xwiki.contrib.mailarchive.timeline.ITimeLineGenerator}
     * 
     * @return
     */
    ITimeLineGenerator getTimeline();

    /**
     * Retrieves the content of an email formatted for display.
     * 
     * @param mailPage The wiki page containing the Mail XObject.
     * @param cut If email history should be "cut" out of the content.
     * @return
     */
    DecodedMailContent getDecodedMailText(String mailPage, boolean cut);

    /**
     * Returns the mail archive configuration object.<br/>
     * Specified in {@link org.xwiki.contrib.mailarchive.IMailArchive#getConfiguration()}
     * 
     * @return The configuration object, or null if a problem occurred.
     */
    IMailArchiveConfiguration getConfig();
    
    /**
     * 
     * @param serverId
     * @param messageId
     * @return
     */
    String getOriginal(String serverId, String messageId);
    

}
