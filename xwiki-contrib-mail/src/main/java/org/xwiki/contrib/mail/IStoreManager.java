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

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.xwiki.component.annotation.Role;

/**
 * @version $Id$
 */
@Role
public interface IStoreManager extends IMailReader
{
    Properties getStoreProperties();

    String getSupportedFormat();

    String getProvider();

    /**
     * Writes an email into the filesystem store.
     * 
     * @param folder
     * @param message
     * @throws MessagingException
     */
    void write(String folder, Message message) throws MessagingException;

}
