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
package org.xwiki.contrib.mailarchive.it.tests;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import net.fortuna.mstor.MStorFolder;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class MstorMailboxReadIT
{
    private File dir;

    @Before
    public void setUp()
    {
        // Init mbox input files folder
        URL url = Thread.currentThread().getContextClassLoader().getResource("mbox/WIKI");
        File f;
        try {
            f = new File(url.toURI());
        } catch (URISyntaxException e) {
            f = new File(url.getPath());
        }
        this.dir = f.getParentFile();
    }

    @Test
    public void testReadMboxFolder() throws MessagingException
    {
        Properties props = new Properties();
        // props.put("mstor.mbox.metadataStrategy", "XML");
        // Avoid caching to allow handling large files, and also to avoid dependency on ehcache
        /*
         * props.put("mstor.cache.disabled", "true"); props.put("mstor.mbox.cacheBuffers", "false");
         * props.put("net.sf.ehcache.disabled", "true");
         */

        props.setProperty("mstor.mbox.metadataStrategy", "none");
        System.setProperty("mstor.cache.disabled", "true");
        System.setProperty("mstor.mbox.cacheBuffers", "false");
        System.setProperty("net.sf.ehcache.disabled", "true");
        System.setProperty("mstor.mbox.mozillaCompatibility", "true");
        System.setProperty("mstor.mbox.bufferStrategy", "default");
        System.setProperty("mstor.mbox.parsing.relaxed", "true");

        // System.out.println("Properties : mstor.cache.disabled " + System.getProperty("mstor.cache.disabled"));
        Session session = Session.getInstance(props);

        String url = "mstor:" + this.dir.getAbsolutePath();
        System.out.println("MSTOR URL: " + url);
        Store store = session.getStore(new URLName(url));
        store.connect();
        System.out.println("Connected to store");
        MStorFolder mailFolder = (MStorFolder) store.getDefaultFolder().getFolder("WIKI");

        mailFolder.open(Folder.READ_ONLY);

        if (mailFolder.getType() != MStorFolder.HOLDS_MESSAGES) {
            System.out.println("This folder does not hold messages, type " + mailFolder.getType());
            // return;
        }
        int messageCount = mailFolder.getMessageCount();
        System.out.println("Number of messages found: " + messageCount);
        Message[] messages = mailFolder.getMessages();
        System.out.println("Showing one message...");
        if (messages != null && messages.length >= 1) {
            System.out.println("Loaded number of messages: " + messages.length);
            for (Message message : messages) {
                if (message == null) {
                    System.out.println("Invalid message found");
                } else {
                    System.out.println("Message: " + message.toString());
                    if (message.getFrom() == null) {
                        System.out.println("Invalid FROM header");
                    } else {
                        System.out.println("From " + message.getFrom()[0]);
                    }
                    System.out.println("Subject " + message.getSubject());
                    if (message.getHeader("Date") == null) {
                        System.out.println("Invalid DATE header");
                    } else {
                        System.out.println("Date " + message.getHeader("Date")[0]);
                    }
                }
            }
        }

        /*
         * mailFolder.open(Folder.READ_ONLY); Message[] msgsArray = mailFolder.getMessages(); List<Message> messages =
         * new ArrayList<Message>(Arrays.asList(msgsArray)); mailFolder.close(false);
         */
        store.close();
    }

}
