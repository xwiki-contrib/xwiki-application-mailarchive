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
package org.xwiki.contrib.mail.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import javax.mail.search.MessageIDTerm;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.xwiki.contrib.mail.IStoreManager;
import org.xwiki.contrib.mail.SourceConnectionErrors;
import org.xwiki.contrib.mail.internal.source.StoreSource;

/**
 * @version $Id$
 */
public abstract class AbstractMailStore extends AbstractMailReader implements IStoreManager
{

    public abstract Logger getLogger();

    public void setMailSource(StoreSource mailSource)
    {
        super.setMailSource(mailSource);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailReader#getMailSource()
     */
    @Override
    public StoreSource getMailSource()
    {
        return (StoreSource) super.getMailSource();
    }

    public abstract Properties getStoreProperties();

    /**
     * @param folder
     * @param message
     * @throws MessagingException
     */
    public void write(String folder, Message message) throws MessagingException
    {
        // getLogger().info("Delivering " + message + " to " + this.location + " / " + folder);

        Store store = getJavamailStore(true);
        store.connect();
        Folder mailFolder = store.getDefaultFolder().getFolder(folder);
        if (!mailFolder.exists()) {
            mailFolder.create(Folder.HOLDS_MESSAGES);
        }
        mailFolder.open(Folder.READ_WRITE);
        // If message is already archived, do nothing
        Message existingMessage = read(folder, message.getHeader("Message-ID")[0]);
        if (existingMessage == null) {
            mailFolder.appendMessages(new Message[] {message});
        }

        mailFolder.close(true);
        store.close();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @see org.xwiki.contrib.mail.IMailReader#readFromStore(java.lang.String)
     */
    public Message read(String folder, String messageid) throws MessagingException
    {
        Store store = getJavamailStore();
        store.connect();

        Folder mailFolder = store.getDefaultFolder().getFolder(folder);
        mailFolder.open(Folder.READ_ONLY);
        Message[] messages = mailFolder.search(new MessageIDTerm(messageid));
        mailFolder.close(false);
        store.close();
        if (messages.length > 0) {
            return messages[0];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailReader#readFromStore(java.lang.String)
     */
    @Override
    public List<Message> read(String folder, boolean onlyUnred, int max) throws MessagingException
    {
        Store store = getJavamailStore();
        store.connect();
        Folder mailFolder = store.getDefaultFolder().getFolder(folder);
        mailFolder.open(Folder.READ_WRITE);
        Message[] msgsArray = mailFolder.getMessages();
        if (max > 0 && msgsArray.length > max) {
            msgsArray = (Message[]) ArrayUtils.subarray(msgsArray, 0, max);
        }
        List<Message> messages = new ArrayList<Message>(Arrays.asList(msgsArray));
        mailFolder.close(false);
        store.close();
        return messages;
    }

    /**
     * Creates appropriate javamail Store object.
     * 
     * @param debug
     * @return
     * @throws NoSuchProviderException
     */
    protected Store getJavamailStore(boolean debug) throws NoSuchProviderException
    {
        Properties props = getStoreProperties();
        Session session = Session.getInstance(props);
        if (debug) {
            session.setDebug(true);
        }

        String url = getProvider() + ":" + getMailSource().getLocation();

        return session.getStore(new URLName(url));
    }

    /**
     * Creates appropriate javamail Store object.
     * 
     * @return
     * @throws NoSuchProviderException
     */
    protected Store getJavamailStore() throws NoSuchProviderException
    {
        return getJavamailStore(false);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailReader#read(java.lang.String, boolean)
     */
    @Override
    public List<Message> read(String folder, boolean onlyUnread) throws MessagingException
    {
        return read(folder, onlyUnread, -1);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailReader#check(java.lang.String, boolean)
     */
    @Override
    public int check(String folder, boolean onlyUnread)
    {
        List<Message> messages = null;

        File storePath = new File(getMailSource().getLocation());
        if (!storePath.exists() || !storePath.canRead()) {
            return SourceConnectionErrors.CONNECTION_ERROR.getCode();
        }

        try {
            messages = read(folder, onlyUnread);
        } catch (FolderNotFoundException e) {
            return SourceConnectionErrors.FOLDER_NOT_FOUND.getCode();
        } catch (MessagingException e) {
            // TODO Very basic implementation of check() ...
            e.printStackTrace();
            return SourceConnectionErrors.UNEXPECTED_EXCEPTION.getCode();
        }
        return messages == null ? -1 : messages.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @see org.xwiki.contrib.mail.IMailReader#cloneEmail(javax.mail.Message)
     */
    @Override
    public MimeMessage cloneEmail(Message mail)
    {
        MimeMessage copy = (MimeMessage) mail;

        try {
            copy = new MimeMessage((MimeMessage) mail);
        } catch (MessagingException e) {
            // failed to copy
        }

        return copy;
    }

}
