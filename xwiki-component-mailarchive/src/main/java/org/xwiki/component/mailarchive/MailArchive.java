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
package org.xwiki.component.mailarchive;

import org.xwiki.component.annotation.ComponentRole;

/**
 * Interface (aka Role) of the Component
 */
@ComponentRole
public interface MailArchive
{
    String MA_ADMIN_PAGE = "MailArchive.Admin";
    String MA_SERVERS_SETTINGS_PAGE = "MailArchive.Settings";
    String MA_LISTS_SETTINGS_PAGE = "MailArchive.ListsSettings";
    String MA_TYPES_SETTINGS_PAGE = "MailArchive.TypesSettings";
    
    /**
     * Checks connection to mail server, and presence of mails to be treated.
     * @return true if connection is ok and there are mails to treat.
     */
    public int checkMails(String serverPrefsDoc);
    
    /**
     * Loads mails.
     *
     * @param nb The maximum number of mails to read
     * @param withDelete If true, deletes treated mail, if false, only set them "read" flag
     * @return false if no mails at all could be read
     */
    public boolean loadMails(int nb, boolean withDelete);
    
    /**
     * 
     * @return
     */
    public MailArchiveConfiguration getConfiguration();
}

