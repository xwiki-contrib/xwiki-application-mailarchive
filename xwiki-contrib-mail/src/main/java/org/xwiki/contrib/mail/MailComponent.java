package org.xwiki.contrib.mail;

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
//package org.xwiki.contrib.mail;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.api.Attachment;

/**
 * @version $Id$
 */
@ComponentRole
public interface MailComponent
{
    List<Message> fetch(String hostname, int port, String protocol, String folder, String username, String password,
        boolean onlyUnread) throws MessagingException;

    // Fetch up to max messages from a server
    List<Message> fetch(String hostname, int port, String protocol, String folder, String username, String password,
        boolean onlyUnread, int max) throws MessagingException;

    List<Message> fetchFromPst(String pstFileName, String folder);

    /**
     * Checks mail server connection and if connection succeeds, returns number of available messages to be read.
     * 
     * @param hostname
     * @param port
     * @param protocol usually "imap", "imaps", "pop3", "pop3s", ...
     * @param folder the folder containing emails. To access an Exchange generic mailbox use "" TODO exchange generic
     *            mailbox folder format
     * @param username
     * @param password
     * @param onlyUnread if true, counts only message without "READ" flag.
     * @return if >=0, the number of available messages, if negative, represents a connection error.
     */
    int check(String hostname, int port, String protocol, String folder, String username, String password,
        boolean onlyUnread);

    /**
     * Clones an email.
     * 
     * @param mail the message to clone.
     * @param protocol the protocol used to connect to the mail server.
     * @param hostname the hostname to connect to.
     * @return
     */
    MimeMessage cloneEmail(Message mail, String protocol, String hostname);

    MailItem parseHeaders(Part message) throws MessagingException, IOException;

    MailContent parseContent(Part message) throws MessagingException, IOException;

    String extractText(Message message);

    String extractHtml(Message message);

    String extractHistoryText(Message message);

    String extractHistoryHtml(Message message);

    // key : content-ID
    Map<String, Attachment> extractAttachments(Message message);

    // key : message-ID
    Map<String, MailItem> extractAttachedEmails(Message message);

    // Persistence to FS (backup)
    void setStore(String location, String provider);

    void writeToStore(String folder, Message message) throws MessagingException;

    /**
     * Reads a message with given Message-ID value from the local store.
     * 
     * @param folder
     * @param messageid
     * @return null if not message was found
     * @throws MessagingException In case of javamail issue accessing the store.
     */
    Message readFromStore(String folder, String messageid) throws MessagingException;

    /**
     * Reads all messages from folder from the local store.
     * 
     * @param folder
     * @return An empty array if no message was found.
     * @throws MessagingException
     */
    Message[] readFromStore(String folder) throws MessagingException;

    /**
     * Reads all messages from folder corresponding to provided SearchTerm from the local store.
     * 
     * @param folder
     * @param term
     * @return
     * @throws MessagingException
     */
    Message[] readFromStore(String folder, SearchTerm term) throws MessagingException;

    // Creates a topic page - returns new page name
    String createTopicPage(MailItem m);

    // Updates a topic page according to a (new) message information
    // Returns true if something was updated
    boolean updateTopicPage(String topicId, MailItem m);

    // Creates mail page - returns new page name
    String createMailPage(MailItem m);

    // needed ?
    boolean updateMailPage(MailItem m);

    String parseAddressHeader(String header);
}
