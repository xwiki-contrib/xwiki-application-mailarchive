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
import java.util.Properties;

import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;

import org.xwiki.component.annotation.Role;

/**
 * @version $Id$
 */
@Singleton
@Role
public interface IMailComponent
{
    /**
     * Dumps email messages from a server.
     * 
     * @param hostname
     * @param port
     * @param protocol Could be "imap", "imaps", "pop3", "pop3s", ...
     * @param folder Folder to get messages from.
     * @param username
     * @param password
     * @param additionalProperties Additional properties for Javamail for this server connection.
     * @param onlyUnread If true, dumps only messages without 'READ' flag.
     * @return
     * @throws MessagingException
     */
    List<Message> fetch(String hostname, int port, String protocol, String folder, String username, String password,
        Properties additionalProperties, boolean onlyUnread) throws MessagingException;

    /**
     * Dumps up to 'max' email messages from a server.
     * 
     * @param hostname
     * @param port
     * @param protocol
     * @param folder Folder to get messages from.
     * @param username
     * @param password
     * @param additionalProperties Additional properties for Javamail for this server connection.
     * @param onlyUnread If true, dumps only messages without 'READ' flag.
     * @param max Maximum number of email messages to retrieve.
     * @return
     * @throws MessagingException
     */
    List<Message> fetch(String hostname, int port, String protocol, String folder, String username, String password,
        Properties additionalProperties, boolean onlyUnread, int max) throws MessagingException;

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
        Properties additionalProperties, boolean onlyUnread);

    /**
     * Returns the javamail Session related to provided server information, if one exists currently.
     * 
     * @param hostname
     * @param port
     * @param protocol
     * @param folder
     * @param username
     * @return
     */
    Session getSession(String hostname, int port, String protocol, String folder, String username);

    /**
     * Clones an email from a session.
     * 
     * @param mail the message to clone.
     * @param protocol the protocol used to connect to the mail server.
     * @param hostname the hostname to connect to.
     * @return
     */
    MimeMessage cloneEmail(Message mail, Session session);

    /**
     * Extracts and parses an email headers.
     * 
     * @param message
     * @return A parsed email.
     * @throws MessagingException
     * @throws IOException
     */
    MailItem parseHeaders(Part message) throws MessagingException, IOException;

    /**
     * Extracts and parses an email content (ie., bodies).
     * 
     * @param message
     * @return A parsed email content.
     * @throws MessagingException
     * @throws IOException
     */
    MailContent parseContent(Part message) throws MessagingException, IOException;

    /**
     * Sets store for emails storage on filesystem.
     * 
     * @param location
     * @param provider
     */
    void setStore(String location, String provider);

    /**
     * Writes an email into the filesystem store.
     * 
     * @param folder
     * @param message
     * @throws MessagingException
     */
    void writeToStore(String folder, Message message) throws MessagingException;

    /**
     * Reads an email from the local store.
     * 
     * @param folder
     * @param messageid
     * @return null if not message was found
     * @throws MessagingException In case of javamail issue accessing the store.
     */
    Message readFromStore(String folder, String messageid) throws MessagingException;

    /**
     * Reads all messages from a folder from the local store.
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

    /**
     * Extracts Personal part of an internet address header.
     * 
     * @param header
     * @return
     */
    String parseAddressHeader(String header);
}
