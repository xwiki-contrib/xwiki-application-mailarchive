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
package org.xwiki.contrib.mail;

import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Part;

import org.xwiki.component.annotation.Role;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.contrib.mail.internal.source.ServerAccountSource;
import org.xwiki.contrib.mail.internal.source.StoreSource;

/**
 * API Component to manage emails reading and parsing.
 * 
 * @author jbousque
 * @version $Id$
 */
@Role
public interface IMailComponent
{
    /**
     * Provides a Mail server manager.
     * 
     * @param hostname
     * @param port
     * @param protocol
     * @param username
     * @param password
     * @param additionalProperties
     * @return
     * @throws ComponentLookupException
     */
    IMailReader getMailReader(String hostname, int port, String protocol, String username, String password,
        Properties additionalProperties) throws ComponentLookupException;

    IMailReader getMailReader(ServerAccountSource source) throws ComponentLookupException;

    /**
     * Provides a Store manager.
     * 
     * @param format Format used by this store (hint of {@link IStoreManager} implementations).
     * @param location
     * @return
     * @throws ComponentLookupException
     */
    IStoreManager getStoreManager(String format, String location) throws ComponentLookupException;

    IStoreManager getStoreManager(StoreSource source) throws ComponentLookupException;

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
     * Extracts Personal part of an internet address header.
     * 
     * @param header
     * @return
     */
    String parseAddressHeader(String header);
}
