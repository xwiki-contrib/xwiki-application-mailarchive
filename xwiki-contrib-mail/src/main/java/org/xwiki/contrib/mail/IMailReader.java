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

import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.mail.internal.FolderItem;
import org.xwiki.contrib.mail.source.IMailSource;

/**
 * @version $Id$
 * @param <K>
 */
@Role
public interface IMailReader
{

    /**
     * Sets the source to read emails from.
     * 
     * @param mailSource
     */
    void setMailSource(IMailSource mailSource);

    /**
     * The source to read emails from.
     * 
     * @return
     */
    IMailSource getMailSource();

    /**
     * Reads email messages from a server.
     * 
     * @param folder Folder to get messages from.
     * @param onlyUnread If true, reads only messages that were not already read (by anyone).
     * @return
     * @throws MessagingException
     */
    List<Message> read(String folder, boolean onlyUnread) throws MessagingException;

    /**
     * Reads up to 'max' number of email messages from a server.
     * 
     * @param folder Folder to get messages from.
     * @param onlyUnread If true, reads only messages that were not already read (by anyone).
     * @param max Maximum number of email messages to retrieve. If negative, then all available messages are returned.
     * @return
     * @throws MessagingException
     */
    List<Message> read(String folder, boolean onlyUnread, int max) throws MessagingException;

    /**
     * Reads a particular message from a folder, given value of its Message-Id header.
     * 
     * @param folder
     * @param messageid
     * @return
     * @throws MessagingException
     */
    Message read(String folder, String messageid) throws MessagingException;

    /**
     * Checks availability of mail source, and if ok returns number of available messages to be read.
     * 
     * @param folder The folder to check for messages.
     * @param onlyUnread if true, counts only message without "READ" flag.
     * @return if >=0, the number of available messages, if negative, represents a connection error.
     */
    int check(String folder, boolean onlyUnread);
    
    ArrayList<FolderItem> getFolderTree() throws MessagingException;

    /**
     * Returns a clone of provided email message. The clone should be unrelated to the store it was read from, even if
     * original email is not.
     * 
     * @param mail the message to clone.
     * @param protocol the protocol used to connect to the mail server.
     * @param hostname the hostname to connect to.
     * @return
     */
    MimeMessage cloneEmail(Message mail);

    void close();

}
