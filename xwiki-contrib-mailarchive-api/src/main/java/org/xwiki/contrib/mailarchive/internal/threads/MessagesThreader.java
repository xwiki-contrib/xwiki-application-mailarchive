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
package org.xwiki.contrib.mailarchive.internal.threads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mailarchive.internal.DefaultMailArchive;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

/**
 * Threading methods allowing to thread a specific topic, or the full archive.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class MessagesThreader implements IMessagesThreader
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.threads.IMessagesThreader#thread()
     */
    @Override
    public ThreadableMessage thread() throws QueryException, MailArchiveException
    {
        String xwql =
            "select mail.name, mail.topicsubject, mail.messagesubject, mail.messageid, mail.references, mail.inreplyto, mail.date from Document doc, doc.object("
                + DefaultMailArchive.SPACE_CODE
                + ".MailClass) as  mail where doc.space='"
                + DefaultMailArchive.SPACE_ITEMS + "'";

        List<Object[]> msgs = queryManager.createQuery(xwql, Query.XWQL).execute();

        List<ThreadableMessage> messages = toThreadableMessages(msgs);

        return thread(messages);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.threads.IMessagesThreader#thread(java.lang.String)
     */
    @Override
    public ThreadableMessage thread(String topicId) throws QueryException, MailArchiveException
    {
        String xwql =
            "select mail.name, mail.topicsubject, mail.messagesubject, mail.messageid, mail.references, mail.inreplyto, mail.date, mail.from from Document doc, doc.object("
                + DefaultMailArchive.SPACE_CODE
                + ".MailClass) as  mail where  mail.topicid='"
                + topicId
                + "' and doc.space='" + DefaultMailArchive.SPACE_ITEMS + "'";

        List<Object[]> msgs = queryManager.createQuery(xwql, Query.XWQL).execute();

        List<ThreadableMessage> messages = toThreadableMessages(msgs);

        return thread(messages);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.threads.IMessagesThreader#thread(java.util.List)
     */
    @Override
    public ThreadableMessage thread(List<ThreadableMessage> messages) throws MailArchiveException
    {

        HashMap<String, Container> id_table = new HashMap<String, MessagesThreader.Container>();

        id_table = fillIdTable(messages);

        Container root = findRoot(id_table);
        id_table.clear();
        id_table = null;

        pruneEmptyContainers(root);

        root.reverseChildren();
        gatherSubjects(root);

        if (root.next != null)
            throw new MailArchiveException("root node may not have a next " + root);

        for (Container r = root.child; r != null; r = r.next) {
            // If this direct child of the root node has no message in it,
            // manufacture a dummy container to bind its children together.
            // Note that these dummies can only ever occur as elements of
            // the root set.
            if (r.message == null) {
                ThreadableMessage message = new ThreadableMessage();
                message.id = "dummy";
                message.isReply = false;
                message.subject = r.child.message.topicsubject + "(not loaded)";
                message.wikidoc = null;
                message.topicsubject = r.child.message.topicsubject;
                message.date = new Date();
                r.message = message;
            }
        }

        ThreadableMessage result = (root.child == null ? null : root.child.message);

        // Flush the tree structure of each element of the root set down into
        // their underlying threadables.
        root.flush();
        root = null;

        logger.debug("thread() return " + result.toString());

        return result;

    }

    /**
     * Convert raw data retrieved from database to an array of ThreadableMessage
     * 
     * @param fromdb
     * @return
     */
    private List<ThreadableMessage> toThreadableMessages(List<Object[]> fromdb)
    {

        ArrayList<ThreadableMessage> messages = new ArrayList<ThreadableMessage>();

        for (Object[] msg : fromdb) {

            // Prepare the Message info
            ThreadableMessage message = new ThreadableMessage();
            message.wikidoc = (String) msg[0];
            message.topicsubject = (String) msg[1];
            message.subject = (String) msg[2];
            message.isReply = !message.subject.equals(message.topicsubject);
            message.id = (String) msg[3];
            message.references = new ArrayList<String>(Arrays.asList(((String) msg[4]).split("[,\n\r]", -2)));
            for (int i = 0; i < message.references.size(); i++) {
                String ref = message.references.get(i).trim();
                if (ref.startsWith("<")) {
                    ref = ref.substring(1);
                }
                if (ref.endsWith(">")) {
                    ref = ref.substring(0, ref.length() - 2);
                }
                message.references.set(i, ref);
            }
            String inreplyto = (String) msg[5];
            if (inreplyto != null && !"".equals(inreplyto)) {
                message.references.add(inreplyto);
            }
            message.date = (Date) msg[6];
            message.user = (String) msg[7];

            messages.add(message);
        }

        return messages;
    }

    /**
     * Parses all messages to fill id_table with linked Containers.
     * 
     * @param id_table
     * @param msgs
     */
    private HashMap<String, Container> fillIdTable(List<ThreadableMessage> msgs) throws MailArchiveException
    {
        HashMap<String, Container> id_table = new HashMap<String, MessagesThreader.Container>();

        for (ThreadableMessage message : msgs) {
            Container container = null;

            // 1.
            // A - initialize container for this message in id_table
            if (id_table.containsKey(message.id)) {
                if (id_table.get(message.id).message == null) {
                    container = id_table.get(message.id);
                    container.message = message;
                } else {
                    logger.debug("Invalid duplicate id found : " + message.id);
                }
            } else {
                container = new Container();
                container.message = message;
                id_table.put(message.id, container);
            }
            // B - create containers for References field
            Container parentRef = null;
            for (String ref : message.references) {
                Container refContainer = id_table.get(ref);
                if (refContainer == null) {
                    refContainer = new Container();
                    refContainer.message = null;
                    id_table.put(ref, refContainer);
                }
                // - Link containers together
                if (parentRef != null && refContainer.parent == null && parentRef != refContainer
                    && !checkCycle(parentRef, refContainer)) {
                    refContainer.parent = parentRef;
                    refContainer.next = parentRef.child;
                    parentRef.child = refContainer;
                }
                parentRef = refContainer;

            }

            // C - Set parent of this message to the last element in references
            if (parentRef != null && (parentRef.equals(container) || checkCycle(parentRef, container))) {
                parentRef = null;
            }

            // Unlink parent if it exists
            if (container.parent != null) {
                Container rest, prev;
                for (prev = null, rest = container.parent.child; rest != null; prev = rest, rest = rest.next) {
                    if (rest == container)
                        break;
                }
                if (rest == null) {
                    throw new MailArchiveException("Container " + container
                        + " is not listed in its own parent's children " + container.parent);
                }

                if (prev == null)
                    container.parent.child = container.next;
                else
                    prev.next = container.next;

                container.next = null;
                container.parent = null;
            }

            // If we have a parent, link c into the parent's child list.
            if (parentRef != null) {
                container.parent = parentRef;
                container.next = parentRef.child;
                parentRef.child = container;
            }

        }
        return id_table;
    }

    /**
     * Find roots, that is, containers without parent.
     * 
     * @param id_table
     * @return
     */
    private Container findRoot(HashMap<String, Container> id_table)
    {
        Container root = new Container();

        for (Container container : id_table.values()) {
            if (container.parent == null) {
                if (container.next != null) {
                    throw new Error("c.next is " + container.next.toString());
                }
                container.next = root.child;
                root.child = container;
            }
        }

        return root;
    }

    private void pruneEmptyContainers(Container root)
    {
        Container container, prev, next;
        for (prev = null, container = root.child, next = container.next; container != null; prev = container, container =
            next, next = (container == null ? null : container.next)) {

            if (container.message == null && container.child == null) {
                // This is an empty container with no kids. Nuke it.
                //
                // Normally such containers won't occur, but they can show up when
                // two messages have References lines that disagree.
                if (prev == null)
                    root.child = container.next;
                else
                    prev.next = container.next;

                // Set container to prev so that prev keeps its same value
                // the next time through the loop.
                container = prev;

            } else if (container.message == null && // expired, and
                container.child != null && // has kids, and
                (container.parent != null || // not at root, or
                container.child.next == null)) { // only one kid

                // Expired message with kids. Promote the kids to this level.
                // Don't do this if we would be promoting them to the root level,
                // unless there is only one kid.

                Container tail;
                Container kids = container.child;

                // Remove this container from the list, replacing it with `kids'.
                if (prev == null)
                    root.child = kids;
                else
                    prev.next = kids;

                // make each child's parent be this level's parent.
                // make the last child's next be this container's next
                // (splicing `kids' into the list in place of `container'.)
                for (tail = kids; tail.next != null; tail = tail.next)
                    tail.parent = container.parent;

                tail.parent = container.parent;
                tail.next = container.next;

                // Since we've inserted items in the chain, `next' currently points
                // to the item after them (tail.next); reset that so that we process
                // the newly promoted items the very next time around.
                next = kids;

                // Set container to prev so that prev keeps its same value
                // the next time through the loop.
                container = prev;

            } else if (container.child != null) {
                // A real message with kids.
                // Iterate over its children, and try to strip out the junk.

                pruneEmptyContainers(container);
            }
        }
    }

    // If any two members of the root set have the same subject, merge them.
    // This is so that messages which don't have References headers at all
    // still get threaded (to the extent possible, at least.)
    //
    private void gatherSubjects(Container root)
    {

        int count = 0;
        for (Container c = root.child; c != null; c = c.next)
            count++;

        // Make the hash table large enough to not need to be rehashed.
        Hashtable<String, Container> subj_table = new Hashtable<String, Container>((int) (count * 1.2), (float) 0.9);

        count = 0;
        for (Container c = root.child; c != null; c = c.next) {
            ThreadableMessage message = c.message;

            // If there is no threadable, this is a dummy node in the root set.
            // Only root set members may be dummies, and they always have at least
            // two kids. Take the first kid as representative of the subject.
            if (message == null)
                message = c.child.message;

            String subj = message.topicsubject;

            if (subj == null || "".equals(subj))
                continue;

            Container old = (Container) subj_table.get(subj);

            // Add this container to the table if:
            // - There is no container in the table with this subject, or
            // - This one is a dummy container and the old one is not: the dummy
            // one is more interesting as a root, so put it in the table instead.
            // - The container in the table has a "Re:" version of this subject,
            // and this container has a non-"Re:" version of this subject.
            // The non-re version is the more interesting of the two.
            //
            if (old == null || (c.message == null && old.message != null)
                || (old.message != null && old.message.isReply && c.message != null && !c.message.isReply)) {
                subj_table.put(subj, c);
                count++;
            }
        }

        if (count == 0) // if the table is empty, we're done.
            return;

        // The subj_table is now populated with one entry for each subject which
        // occurs in the root set. Now iterate over the root set, and gather
        // together the difference.
        //
        Container prev, c, rest;
        for (prev = null, c = root.child, rest = c.next; c != null; prev = c, c = rest, rest =
            (rest == null ? null : rest.next)) {

            ThreadableMessage message = c.message;
            if (message == null) // might be a dummy -- see above
                message = c.child.message;

            String subj = message.topicsubject;

            // Don't thread together all subjectless messages; let them dangle.
            if (subj == null || "".equals(subj))
                continue;

            Container old = (Container) subj_table.get(subj);
            if (old == c) // oops, that's us
                continue;

            // Ok, so now we have found another container in the root set with
            // the same subject. There are a few possibilities:
            //
            // - If both are dummies, append one's children to the other, and remove
            // the now-empty container.
            //
            // - If one container is a dummy and the other is not, make the non-dummy
            // one be a child of the dummy, and a sibling of the other "real"
            // messages with the same subject (the dummy's children.)
            //
            // - If that container is a non-dummy, and that message's subject does
            // not begin with "Re:", but *this* message's subject does, then
            // make this be a child of the other.
            //
            // - If that container is a non-dummy, and that message's subject begins
            // with "Re:", but *this* message's subject does *not*, then make that
            // be a child of this one -- they were misordered. (This happens
            // somewhat implicitly, since if there are two messages, one with Re:
            // and one without, the one without will be in the hash table,
            // regardless of the order in which they were seen.)
            //
            // - Otherwise, make a new dummy container and make both messages be a
            // child of it. This catches the both-are-replies and neither-are-
            // replies cases, and makes them be siblings instead of asserting a
            // hierarchical relationship which might not be true.
            //
            // (People who reply to messages without using "Re:" and without using
            // a References line will break this slightly. Those people suck.)
            //
            // (It has occurred to me that taking the date or message number into
            // account would be one way of resolving some of the ambiguous cases,
            // but that's not altogether straightforward either.)

            // Remove the "second" message from the root set.
            if (prev == null)
                root.child = c.next;
            else
                prev.next = c.next;
            c.next = null;

            if (old.message == null && c.message == null) {
                // They're both dummies; merge them.
                Container tail;
                for (tail = old.child; tail != null && tail.next != null; tail = tail.next)
                    ;
                tail.next = c.child;
                for (tail = c.child; tail != null; tail = tail.next)
                    tail.parent = old;
                c.child = null;

            } else if (old.message == null || // old is empty, or
                (c.message != null && c.message.isReply && // c has Re, and
                !old.message.isReply)) { // old does not.
                // Make this message be a child of the other.
                c.parent = old;
                c.next = old.child;
                old.child = c;

            } else {
                // Make the old and new messages be children of a new dummy container.
                // We do this by creating a new container object for old->msg and
                // transforming the old container into a dummy (by merely emptying it),
                // so that the hash table still points to the one that is at depth 0
                // instead of depth 1.

                Container newc = new Container();
                newc.message = old.message;
                // newc.debug_id = old.debug_id;
                newc.child = old.child;
                for (Container tail = newc.child; tail != null; tail = tail.next)
                    tail.parent = newc;

                old.message = null;
                old.child = null;
                // old.debug_id = null;

                c.parent = old;
                newc.parent = old;

                // old is now a dummy; make it have exactly two kids, c and newc.
                old.child = c;
                c.next = newc;
            }

            // we've done a merge, so keep the same `prev' next time around.
            c = prev;
        }

        subj_table.clear();
        subj_table = null;
    }

    public boolean checkCycle(Container a, Container b)
    {

        if (a == null || b == null || a.message == null || b.message == null) {
            return false;
        }
        // Search b in descendants of a
        Container current = a;
        do {
            current = current.child;
            if (current != null) {
                if (current.equals(b)) {
                    return true;
                }
            }
        } while (current != null);
        // Search a in descendants of b
        current = b;
        do {
            current = current.child;
            if (current != null) {
                if (current.equals(a)) {
                    return true;
                }
            }
        } while (current != null);

        return false;
    }

    /**
     * Contains messages in a threaded structure (parent / direct child and siblings).
     * 
     * @version $Id$
     */
    class Container
    {
        public ThreadableMessage message;

        public Container parent;

        public Container child;

        public Container next;

        void reverseChildren()
        {
            if (child != null) {
                // nreverse the children (child through child.next.next.next...)
                Container kid, prev, rest;
                for (prev = null, kid = child, rest = kid.next; kid != null; prev = kid, kid = rest, rest =
                    (rest == null ? null : rest.next))
                    kid.next = prev;
                child = prev;

                // then do it for the kids
                for (kid = child; kid != null; kid = kid.next)
                    kid.reverseChildren();
            }
        }

        // Copy the ThreadContainer tree structure down into the underlying
        // IThreadable objects (that is, make the ThreadableMessage tree look like
        // the Container tree.)
        //
        void flush()
        {
            if (parent != null && message == null)
                // Only the root_node is allowed to not have a threadable.
                throw new Error("no threadable in " + this.toString());

            parent = null;

            if (message != null)
                message.child = child == null ? null : child.message;

            if (child != null) {
                child.flush();
                child = null;
            }

            if (message != null)
                message.next = next == null ? null : next.message;

            if (next != null) {
                next.flush();
                next = null;
            }

            message = null;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((message == null) ? 0 : message.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Container other = (Container) obj;
            if (message == null) {
                if (other.message != null)
                    return false;
            } else if (!message.equals(other.message))
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("Container [messageid=").append(message != null ? message.id : null).append(", parent=")
                .append(parent).append(", child=").append(child).append(", next=").append(next).append("]");
            return builder.toString();
        }

    }
}
