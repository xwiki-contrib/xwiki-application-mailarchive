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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.util.SharedByteArrayInputStream;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.mail.SourceConnectionErrors;
import org.xwiki.contrib.mail.internal.source.ServerAccountSource;

/**
 * @version $Id$
 */
@Component
public class DefaultMailReader extends AbstractMailReader
{

    // TODO manage topics max length for compatibility
    private static final int MAIL_HEADER_MAX_LENGTH = 255;

    private Session session;

    private Store store;

    @Inject
    private Logger logger;

    @Inject
    private ComponentManager componentManager;

    public void setMailSource(final ServerAccountSource mailSource)
    {
        super.setMailSource(mailSource);
    }

    public ServerAccountSource getMailSource()
    {
        return (ServerAccountSource) super.getMailSource();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @see org.xwiki.contrib.mail.IMailReader#fetch(java.lang.String, int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, boolean)
     */
    @Override
    public List<Message> read(final String folder, final boolean onlyUnread) throws MessagingException
    {
        return read(folder, onlyUnread, -1);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @throws
     * @throws Exception
     * @see org.xwiki.contrib.mail.IMailReader#read(java.lang.String, int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, boolean, int)
     */
    @Override
    public List<Message> read(final String folder, final boolean onlyUnread, final int max) throws MessagingException
    {
        assert (getMailSource() != null);
        assert (getMailSource().getHostname() != null);

        store = null;
        List<Message> messages = new ArrayList<Message>();
        boolean isGmail = getMailSource().getHostname() != null && getMailSource().getHostname().endsWith(".gmail.com");

        logger.info("Trying to retrieve mails from server " + getMailSource().getHostname());

        this.session =
            createSession(getMailSource().getProtocol(), getMailSource().getAdditionalProperties(), isGmail,
                getMailSource().isAutoTrustSSLCertificates());

        // Get a Store object
        store = session.getStore();

        // Connect to the mail account
        store.connect(getMailSource().getHostname(), getMailSource().getPort(), getMailSource().getUsername(),
            getMailSource().getPassword());
        Folder fldr;
        // Specifically for GMAIL ...
        if (isGmail) {
            fldr = store.getDefaultFolder();
        }
        fldr = store.getFolder(folder);

        fldr.open(Folder.READ_WRITE);

        Message[] msgsArray;
        // Searches for mails not already read
        if (onlyUnread) {
            FlagTerm searchterms = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            msgsArray = fldr.search(searchterms);
        } else {
            msgsArray = fldr.getMessages();
        }

        if (max > 0 && msgsArray.length > max) {
            msgsArray = (Message[]) ArrayUtils.subarray(msgsArray, 0, max);
        }
        messages = new ArrayList<Message>(Arrays.asList(msgsArray));

        logger.info("Found " + messages.size() + " messages");

        // Note: we leave the Store opened to allow reading returned Messages

        return messages;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @throws
     * @throws Exception
     * @see org.xwiki.contrib.mail.IMailReader#read(java.lang.String, int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, boolean, int)
     */
    @Override
    public Message read(final String folder, final String messageid) throws MessagingException
    {
        assert (getMailSource() != null);
        assert (getMailSource().getHostname() != null);

        Message message = null;
        store = null;
        boolean isGmail = getMailSource().getHostname() != null && getMailSource().getHostname().endsWith(".gmail.com");

        logger.info("Trying to retrieve mails from server " + getMailSource().getHostname());

        this.session =
            createSession(getMailSource().getProtocol(), getMailSource().getAdditionalProperties(), isGmail,
                getMailSource().isAutoTrustSSLCertificates());

        // Get a Store object
        store = session.getStore();

        // Connect to the mail account
        store.connect(getMailSource().getHostname(), getMailSource().getPort(), getMailSource().getUsername(),
            getMailSource().getPassword());
        Folder fldr;
        // Specifically for GMAIL ...
        if (isGmail) {
            fldr = store.getDefaultFolder();
        }
        fldr = store.getFolder(folder);

        fldr.open(Folder.READ_WRITE);

        // Search with message id
        Message[] messages = fldr.search(new MessageIDTerm(messageid));

        if (messages.length > 0) {
            message = messages[0];
        }

        logger.info("Found message " + message);

        return message;
    }

    @Override
    public ArrayList<FolderItem> getFolderTree() throws MessagingException
    {
        assert (getMailSource() != null);
        assert (getMailSource().getHostname() != null);

        ArrayList<FolderItem> folderItems = new ArrayList<FolderItem>();
        store = null;
        boolean isGmail = getMailSource().getHostname() != null && getMailSource().getHostname().endsWith(".gmail.com");

        logger.info("Listing folders for " + getMailSource().getHostname());

        this.session =
            createSession(getMailSource().getProtocol(), getMailSource().getAdditionalProperties(), isGmail,
                getMailSource().isAutoTrustSSLCertificates());

        // Get a Store object
        store = session.getStore();

        // Connect to the mail account
        store.connect(getMailSource().getHostname(), getMailSource().getPort(), getMailSource().getUsername(),
            getMailSource().getPassword());
        Folder defaultFolder = store.getDefaultFolder();
        FolderItem item = new FolderItem();
        item.setIndex(0);
        item.setLevel(0);
        item.setName(defaultFolder.getName());
        item.setFullName(defaultFolder.getFullName());
        if ((defaultFolder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
            item.setMessageCount(defaultFolder.getMessageCount());
            item.setUnreadMessageCount(defaultFolder.getUnreadMessageCount());
            item.setNewMessageCount(defaultFolder.getNewMessageCount());
        }
        Folder[] folders = defaultFolder.list("*");
        int index = 1;
        int level = 1;
        // TODO not really managing folders here, just listing them
        for (Folder folder : folders) {

            item = new FolderItem();
            item.setIndex(index);
            item.setLevel(level);
            item.setName(folder.getName());
            item.setFullName(folder.getFullName());
            if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
                item.setMessageCount(folder.getMessageCount());
                item.setUnreadMessageCount(folder.getUnreadMessageCount());
                item.setNewMessageCount(folder.getNewMessageCount());
                folderItems.add(item);
            }
        }

        store.close();

        return folderItems;

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailReader#check(java.lang.String, int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, boolean)
     */
    @Override
    public int check(final String folder, final boolean onlyUnread)
    {
        int result;
        boolean toClose = true;

        // If store is currently connected, we don't need to close it after the check.
        // If it is not, we close it after to leave everything as it was.
        if (store != null && store.isConnected()) {
            toClose = false;
        }

        try {

            List<Message> messages = read(folder, onlyUnread);

            result = messages.size();

            // FIXME: instead of converting exceptions to int code, would be better to create new Exception class
            // (like MailException) with provided error code, message and inner stacktrace, and present that to UI.
        } catch (AuthenticationFailedException e) {
            logger.warn("checkMails : ", e);
            return SourceConnectionErrors.AUTHENTICATION_FAILED.getCode();
        } catch (FolderNotFoundException e) {
            logger.warn("checkMails : ", e);
            return SourceConnectionErrors.FOLDER_NOT_FOUND.getCode();
        } catch (MessagingException e) {
            logger.warn("checkMails : ", e);
            if (e.getCause() instanceof java.net.UnknownHostException) {
                return SourceConnectionErrors.UNKNOWN_HOST.getCode();
            } else {
                return SourceConnectionErrors.CONNECTION_ERROR.getCode();
            }
        } catch (IllegalStateException e) {
            return SourceConnectionErrors.ILLEGAL_STATE.getCode();
        } catch (Throwable t) {
            logger.warn("checkMails : ", t);
            return SourceConnectionErrors.UNEXPECTED_EXCEPTION.getCode();
        }
        if (toClose) {
            close();
        }
        logger.debug("checkMails : " + result + " available from " + getMailSource().getHostname());

        return result;
    }

    private Session createSession(final String protocol, final Properties additionalProperties, final boolean isGmail,
        final boolean autoTrustSsl)
    {
        // Get a session. Use a blank Properties object.
        Properties props = new Properties(additionalProperties);
        // necessary to work with Gmail
        if (isGmail && !props.containsKey("mail.imap.partialfetch") && !props.containsKey("mail.imaps.partialfetch")) {
            props.put("mail.imap.partialfetch", "false");
            props.put("mail.imaps.partialfetch", "false");
        }
        props.put("mail.store.protocol", protocol);
        // TODO set this as an option (auto-trust certificates for SSL)
        props.put("mail.imap.ssl.checkserveridentity", "false");
        props.put("mail.imaps.ssl.trust", "*");
        /*
         * MailSSLSocketFactory socketFactory = new MailSSLSocketFactory(); socketFactory.setTrustAllHosts(true);
         * props.put("mail.imaps.ssl.socketFactory", socketFactory);
         */
        Session session = Session.getInstance(props, null);
        session.setDebug(true);

        return session;
    }

    public Session getSession()
    {
        return this.session;
    }

    @Override
    public void close()
    {
        if (store != null && store.isConnected()) {
            try {
                store.close();
            } catch (MessagingException e) {
                logger.debug("Could not close connection.");
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailReader#cloneEmail(javax.mail.Message, java.lang.String, java.lang.String)
     */
    public MimeMessage cloneEmail(final Message mail)
    {
        MimeMessage cmail = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mail.writeTo(bos);
            bos.close();
            SharedByteArrayInputStream bis = new SharedByteArrayInputStream(bos.toByteArray());

            cmail = new MimeMessage(this.session, bis);
            bis.close();
        } catch (Exception e) {
            logger.warn("Could not clone email", e);
            return null;
        }

        return cmail;
    }

}
