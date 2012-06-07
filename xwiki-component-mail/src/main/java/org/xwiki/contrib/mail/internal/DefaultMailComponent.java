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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;
import javax.mail.util.SharedByteArrayInputStream;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mail.ConnectionErrors;
import org.xwiki.contrib.mail.MailComponent;
import org.xwiki.contrib.mail.MailItem;

import com.xpn.xwiki.api.Attachment;

/**
 * @version $Id$
 */
public class DefaultMailComponent implements MailComponent, Initializable
{
    private String storeLocation = "storage";

    private String storeProvider = "mstor";

    private JavamailMessageParser parser;

    // TODO manage topics max length for compatibility
    private static final int MAIL_HEADER_MAX_LENGTH = 255;

    @Inject
    private Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        this.parser = new JavamailMessageParser(logger);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @see org.xwiki.contrib.mail.MailComponent#fetch(java.lang.String, int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, boolean)
     */
    @Override
    public List<Message> fetch(String hostname, int port, String protocol, String folder, String username,
        String password, boolean onlyUnread) throws MessagingException
    {
        return fetch(hostname, port, protocol, folder, username, password, onlyUnread, -1);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @throws
     * @throws Exception
     * @see org.xwiki.contrib.mail.MailComponent#fetch(java.lang.String, int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, boolean, int)
     */
    @Override
    public List<Message> fetch(String hostname, int port, String protocol, String folder, String username,
        String password, boolean onlyUnread, int max) throws MessagingException
    {
        assert (hostname != null);

        List<Message> messages = new ArrayList<Message>();
        boolean isGmail = hostname != null && hostname.endsWith(".gmail.com");

        // try {

        logger.info("Trying to retrieve mails from server " + hostname);

        Session session = createSession(protocol, isGmail);

        // Get a Store object
        Store store = session.getStore();

        // Connect to the mail account
        store.connect(hostname, port, username, password);
        Folder fldr;
        // Specifically for GMAIL ...
        if (hostname.endsWith(".gmail.com")) {
            fldr = store.getDefaultFolder();
        }
        fldr = store.getFolder(folder);

        fldr.open(Folder.READ_WRITE);

        // Searches for mails not already read
        FlagTerm searchterms = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        Message[] msgsArray = fldr.search(searchterms);
        if (max > 0 && msgsArray.length > max) {
            for (int index = max - 1; index < msgsArray.length - 1; index++) {
                ArrayUtils.remove(msgsArray, max - 1);
            }
        }
        messages = new ArrayList<Message>(Arrays.asList(msgsArray));
        /*
         * } catch (GeneralSecurityException e) { // TODO Auto-generated catch block e.printStackTrace(); }
         */

        logger.info("Found " + messages.size() + " messages");

        return messages;
    }

    @Override
    public List<Message> fetchFromPst(String pstFileName, String folder)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#check(java.lang.String, int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, boolean)
     */
    @Override
    public int check(String hostname, int port, String protocol, String folder, String username, String password,
        boolean onlyUnread)
    {
        int nbMessages;
        Store store = null;
        boolean isGmail = hostname != null && hostname.endsWith(".gmail.com");

        try {
            // Create the session
            Session session = createSession(protocol, isGmail);

            // Get a Store object
            store = session.getStore();

            // Connect to the mail account
            if (store.isConnected()) {
                store.close();
            }
            store.connect(hostname, port, username, password);
            Folder fldr;
            // Specifically for GMAIL ...
            if (isGmail) {
                fldr = store.getDefaultFolder();
            }

            fldr = store.getFolder(folder);
            if (!fldr.exists()) {
                logger.warn("checkMails : Folder " + folder + " does not exist in this mailbox");
                return ConnectionErrors.FOLDER_NOT_FOUND.getCode();
            }
            fldr.open(Folder.READ_ONLY);

            // Searches for mails not already read
            Message[] messages;
            if (onlyUnread) {
                FlagTerm searchterms = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                messages = fldr.search(searchterms);
            } else {
                messages = fldr.getMessages();
            }

            nbMessages = messages.length;

        } catch (AuthenticationFailedException e) {
            logger.warn("checkMails : ", e);
            return ConnectionErrors.AUTHENTICATION_FAILED.getCode();
        } catch (FolderNotFoundException e) {
            logger.warn("checkMails : ", e);
            return ConnectionErrors.FOLDER_NOT_FOUND.getCode();
        } catch (MessagingException e) {
            logger.warn("checkMails : ", e);
            if (e.getCause() instanceof java.net.UnknownHostException) {
                return ConnectionErrors.UNKNOWN_HOST.getCode();
            } else {
                return ConnectionErrors.CONNECTION_ERROR.getCode();
            }
        } catch (IllegalStateException e) {
            return ConnectionErrors.ILLEGAL_STATE.getCode();
        } catch (Throwable t) {
            logger.warn("checkMails : ", t);
            return ConnectionErrors.UNEXPECTED_EXCEPTION.getCode();
        } finally {
            try {
                store.close();
            } catch (MessagingException e) {
                logger.debug("checkMails : Could not close connection", e);
            }
        }
        logger.debug("checkMails : " + nbMessages + " available from " + hostname);

        return nbMessages;
    }

    private Session createSession(String protocol, boolean isGmail)
    {
        // Get a session. Use a blank Properties object.
        Properties props = new Properties();
        // necessary to work with Gmail
        if (isGmail) {
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

        return session;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     * @throws MessagingException
     * @see org.xwiki.contrib.mail.MailComponent#parse(javax.mail.Message)
     */
    @Override
    public MailItem parseHeaders(Part mail) throws MessagingException, IOException
    {
        return parser.parseHeaders(mail);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#extractText(javax.mail.Message)
     */
    @Override
    public String extractText(Message message)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#extractHtml(javax.mail.Message)
     */
    @Override
    public String extractHtml(Message message)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#extractHistoryText(javax.mail.Message)
     */
    @Override
    public String extractHistoryText(Message message)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#extractHistoryHtml(javax.mail.Message)
     */
    @Override
    public String extractHistoryHtml(Message message)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#extractAttachments(javax.mail.Message)
     */
    @Override
    public Map<String, Attachment> extractAttachments(Message message)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#extractAttachedEmails(javax.mail.Message)
     */
    @Override
    public Map<String, MailItem> extractAttachedEmails(Message message)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setStore(String location, String provider)
    {
        this.storeLocation = location.replaceAll("\\\\", "/");
        this.storeProvider = provider;
        File storeRoot = new File(location);
        if (!storeRoot.exists()) {
            storeRoot.mkdirs();
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @see org.xwiki.contrib.mail.MailComponent#writeToStore(javax.mail.Message, java.lang.String)
     */
    @Override
    public void writeToStore(String folder, Message message) throws MessagingException
    {
        logger.info("Delivering " + message + " to " + this.storeLocation + " / " + folder);

        Properties props = new Properties();
        if ("maildir".equals(this.storeProvider)) {
            // the following specifies whether to create maildirpath if it is not existent
            // if not specified then autocreatedir is false
            props.put("mail.store.maildir.autocreatedir", "true");
        }
        if ("mstor".equals(this.storeProvider)) {
            props.put("mstor.mbox.metadataStrategy", "XML");
        }
        Session session = Session.getInstance(props);
        session.setDebug(true);

        Store store = session.getStore(new URLName(this.storeProvider + ":" + this.storeLocation));
        store.connect();
        Folder mailFolder = store.getDefaultFolder().getFolder(folder);
        if (!mailFolder.exists()) {
            mailFolder.create(Folder.HOLDS_MESSAGES);
        }
        mailFolder.open(Folder.READ_WRITE);
        // If message is already archived, delete and re-add it
        Message existingMessage = readFromStore(folder, message.getHeader("Message-ID")[0]);
        if (existingMessage != null) {

        }
        mailFolder.appendMessages(new Message[] {message});
        mailFolder.close(true);
        store.close();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @see org.xwiki.contrib.mail.MailComponent#readFromStore(java.lang.String)
     */
    @Override
    public Message readFromStore(String folder, String messageid) throws MessagingException
    {
        Session session = Session.getInstance(new Properties());

        String url = this.storeProvider + ":" + this.storeLocation;

        Store store = session.getStore(new URLName(url));

        store.connect(); // useless with Maildir but included here for consistency
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
     * @see org.xwiki.contrib.mail.MailComponent#readFromStore(java.lang.String)
     */
    @Override
    public Message[] readFromStore(String folder) throws MessagingException
    {
        Session session = Session.getInstance(new Properties());

        String url = this.storeProvider + ":" + this.storeLocation;

        Store store = session.getStore(new URLName(url));

        store.connect(); // useless with Maildir but included here for consistency
        Folder mailFolder = store.getFolder(folder);
        mailFolder.open(Folder.READ_ONLY);
        Message[] messages = mailFolder.getMessages();
        mailFolder.close(false);
        store.close();
        return messages;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#readFromStore(java.lang.String, javax.mail.search.SearchTerm)
     */
    @Override
    public Message[] readFromStore(String folder, SearchTerm term) throws MessagingException
    {
        Session session = Session.getInstance(new Properties());

        String url = this.storeProvider + ":" + this.storeLocation;

        Store store = session.getStore(new URLName(url));

        store.connect(); // useless with Maildir but included here for consistency
        Folder mailFolder = store.getFolder(folder);
        mailFolder.open(Folder.READ_ONLY);
        Message[] messages = mailFolder.search(term);
        mailFolder.close(false);
        store.close();
        return messages;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#createTopicPage(org.xwiki.contrib.mail.MailItem)
     */
    @Override
    public String createTopicPage(MailItem m)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#updateTopicPage(java.lang.String, org.xwiki.contrib.mail.MailItem)
     */
    @Override
    public boolean updateTopicPage(String topicId, MailItem m)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#createMailPage(org.xwiki.contrib.mail.MailItem)
     */
    @Override
    public String createMailPage(MailItem m)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#updateMailPage(org.xwiki.contrib.mail.MailItem)
     */
    @Override
    public boolean updateMailPage(MailItem m)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.MailComponent#parseAddressHeader(java.lang.String)
     */
    @Override
    public String parseAddressHeader(String header)
    {
        try {
            return InternetAddress.parseHeader(header, false)[0].getPersonal();
        } catch (AddressException e) {
            logger.error("Could not parse " + header, e);
            return "";
        }
    }

    public MimeMessage cloneEmail(Message mail, String protocol, String hostname)
    {
        MimeMessage cmail;
        try {
            boolean isGmail = hostname != null && hostname.endsWith(".gmail.com");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mail.writeTo(bos);
            bos.close();
            SharedByteArrayInputStream bis = new SharedByteArrayInputStream(bos.toByteArray());
            cmail = new MimeMessage(createSession(protocol, isGmail), bis);
            bis.close();
        } catch (Exception e) {
            logger.warn("Could not clone email", e);
            return null;
        }

        return cmail;
    }

    protected static String removeCRLF(String str)
    {
        return str == null ? null : str.replaceAll("\n", " ").replaceAll("\r", " ");
    }

}
