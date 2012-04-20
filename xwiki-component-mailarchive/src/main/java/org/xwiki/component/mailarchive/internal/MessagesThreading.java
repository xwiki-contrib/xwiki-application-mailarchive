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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id$
 */
public class MessagesThreading
{
    private XWikiContext context;

    private XWiki xwiki;

    private QueryManager queryManager;

    private Logger logger;

    private MailUtils mailutils;

    private ArrayList<MailBean> beansMap;

    public MessagesThreading(XWikiContext context, XWiki xwiki, QueryManager queryManager, Logger logger,
        MailUtils mailutils)
    {
        super();
        this.context = context;
        this.xwiki = xwiki;
        this.queryManager = queryManager;
        this.logger = logger;
        this.mailutils = mailutils;
    }

    public void thread(String topicId) throws QueryException, XWikiException
    {
        String xwql =
            "select doc.fullName from Document doc, doc.object(" + DefaultMailArchive.SPACE_CODE
                + ".MailClass) as  mail where  mail.topicid='" + topicId + "' and doc.space='"
                + DefaultMailArchive.SPACE_ITEMS + "'";

        // formatter for parsing dates
        SimpleDateFormat parser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
        // formatter for formatting dates for display
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        HashMap<String, List<MailEntry>> msgByIdMap = new HashMap<String, List<MailEntry>>();
        Date minDate = new Date(Long.MAX_VALUE);
        String firstReplyId = "";

        // def threads = new ThreadsUtils(xwiki)
        // threads.setFormatter(formatter)
        // threads.setMode(mode)
        // threads.setTopic(doc.web + "." + doc.name)

        List<String> msgs = queryManager.createQuery(xwql, Query.XWQL).execute();
        for (String msg : msgs) {
            XWikiDocument msgdoc = xwiki.getDocument(msg, context);
            if (msgdoc != null) {
                BaseObject msgobj = msgdoc.getObject("MailArchiveCode.MailClass");
                if (msgobj != null) {
                    Date date = msgobj.getDateValue("date");
                    if (date == null) {
                        date = new Date();
                    }
                    String inreplyto = msgobj.getStringValue("inreplyto");
                    if (msgByIdMap.containsKey(inreplyto)) {
                        MailEntry newvalue = new MailEntry(date, msgobj.getStringValue("messageid"), msg, msgobj);
                        msgByIdMap.get(inreplyto).add(newvalue);
                    } else {
                        msgByIdMap.put(inreplyto, new ArrayList<MailEntry>());
                        MailEntry newvalue =
                            new MailEntry(date, MailUtils.extractAddress(msgobj.getStringValue("messageid")), msg,
                                msgobj);
                        msgByIdMap.get(inreplyto).add(newvalue);
                    }
                    if (date.getTime() < minDate.getTime()) {
                        minDate = date;
                        firstReplyId = inreplyto;
                    }
                }
            } else {
                logger.warn("could not find associated document");
            }
        }

        while (msgByIdMap.size() != 0) {
            /*
             * threads.setOutput("") threads.beansMap = [:] // First find the first message replyid (entry point) //
             * Note : it should be "", because first message in topic should not be a reply, but it can happen // In
             * this case, we try to find the replyid of the most ancient message threads.computeReplies (msgByIdMap,
             * threads.getFirstReplyId(msgByIdMap), 0) threads.showReplies() println(threads.getOutput())
             */
            String firstReply = getFirstReplyId(msgByIdMap);
            computeReplies(msgByIdMap, firstReply, 0);
        }
    }

    /*
     * Gets the initial replyId to take care about. It is either "" if it exists, or the one of the oldest message in
     * the thread map.
     */
    public String getFirstReplyId(HashMap<String, List<MailEntry>> map)
    {

        long minDate = (new Date(Long.MAX_VALUE)).getTime();
        String oldestReplyId = "";

        if (!map.containsKey("")) {
            for (Entry<String, List<MailEntry>> entry : map.entrySet()) {

                String currentReplyId = entry.getKey();
                List<MailEntry> msgVector = entry.getValue();
                for (MailEntry msg : msgVector) {
                    if (msg.date.getTime() < minDate) {
                        minDate = msg.date.getTime();
                        oldestReplyId = currentReplyId;
                    }
                }
            }
        }
        return oldestReplyId;
    }

    /*
     * Display, formatted as mode ("tree" or "table" or "forum"), a thread of message contained in a map. Displayed
     * items are removed from the map, so this method can be called again until the map is empty. This is useful when
     * messages are incorrectly grouped in the same thread, which can always happen, to be sure to show all the existing
     * messages. Messages are in a map msgByIDMap, replyid targets the initial level of messages, level should be 0 at
     * first, formatter is used to display dates.
     */
    ArrayList<MailBean> computeReplies(HashMap<String, List<MailEntry>> map, String replyid, int level)
        throws XWikiException
    {

        ArrayList<MailBean> beansMap = new ArrayList<MessagesThreading.MailBean>();

        // def mode = getMode()
        // def formatter = getFormatter()

        // Display all messages for this "level"
        List<MailEntry> msgsForId = map.get(replyid);
        if (msgsForId != null && msgsForId.size() != 0) {
            level++;

            for (MailEntry msgForId : msgsForId) {
                Date realdate = msgForId.date;
                String currentMsgId = msgForId.messageid;
                String msg = msgForId.msg;
                BaseObject msgobj = msgForId.msgobj;
                XWikiDocument msgDoc = xwiki.getDocument(msg, context);

                Date date = realdate;// formatter.format(realdate)

                String author = "";// msgdoc.author
                String wikiUser = null;
                if (author == null || author == "" || author == "XWiki.UserDoesNotExist") {
                    author = msgobj.getStringValue("from");
                    if (author == "" || author == null) {
                        author = "unknown";
                    }
                    wikiUser = mailutils.parseUser(author, true, ""); // TODO call to parse user
                } else {
                    wikiUser = author;
                }
                if (wikiUser == null) {
                    author = MailUtils.extractAddress(author);
                    wikiUser = author;
                } else {
                    String prettyName = xwiki.getUserName(wikiUser, context);
                    String userstr = "[[${prettyName}>>${wikiUser}]]";
                    // if (mode == "table") {
                    // userstr = "{{useravatar username='${wikiUser}' width='30'/}} " + userstr;
                    // } else if (mode == "forum") {
                    userstr = "{{useravatar username='${wikiUser}' width='90'/}} <br/>" + userstr;
                    // }
                    wikiUser = userstr;
                }

                String subject = msgobj.getStringValue("messagesubject");

                // Populate map
                beansMap.add(new MailBean(this.beansMap.size(), level, wikiUser, subject, date, msgDoc.getFullName()));

                // Recursive call to compute next level of messages, if any
                beansMap.addAll(computeReplies(map, currentMsgId, level));

                // Remove treated level of message(s)
                map.remove(replyid);
            }
        }

        return beansMap;
    }

    class MailBean
    {
        int index;

        int level;

        String subject;

        String user;

        Date date;

        String page;

        public MailBean(int index, int level, String user, String subject, Date date, String page)
        {
            this.index = index;
            this.level = level;
            this.user = user;
            this.subject = subject;
            this.date = date;
            this.page = page;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("MailBean [index=").append(index).append(", level=").append(level).append(", subject=")
                .append(subject).append(", user=").append(user).append(", date=").append(date).append(", page=")
                .append(page).append("]");
            return builder.toString();
        }

    }

    class MailEntry
    {
        Date date;

        String messageid;

        String msg;

        BaseObject msgobj;

        public MailEntry(Date date, String messageid, String msg, BaseObject msgobj)
        {
            this.date = date;
            this.messageid = messageid;
            this.msg = msg;
            this.msgobj = msgobj;
        }

    }
}
