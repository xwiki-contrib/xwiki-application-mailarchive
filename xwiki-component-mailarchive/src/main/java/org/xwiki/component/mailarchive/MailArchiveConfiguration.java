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

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

/**
 * Interface (aka Role) of the Component
 */
public interface MailArchiveConfiguration
{
    String ADMIN_PAGE = "MailArchive.Admin";

    String SERVERS_SETTINGS_PAGE = "MailArchive.Settings";

    String LISTS_SETTINGS_PAGE = "MailArchive.ListsSettings";

    String TYPES_SETTINGS_PAGE = "MailArchive.TypesSettings";

    public String getLoadingUser();

    public String getDefaultHomeView();

    public String getDefaultTopicsView();

    public String getDefaultMailsOpeningMode();

    public boolean manageTimeline();

    public int getMaxTimelineItemsToLoad();

    public boolean getMatchProfiles();

    public boolean getMatchLdap();

    public boolean getLdapCreateMissingProfiles();

    public boolean getLdapForcePhotoUpdate();

    public String getLdapPhotoFieldName();

    public String getLdapPhotoFieldContent();

    public List<MailingList> getMailingLists();

    public List<MailServer> getServers();

    public List<MailType> getMailTypes();
}
