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
package org.xwiki.contrib.mailarchive;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.mailarchive.exceptions.MailArchiveException;

/**
 * Configuration of the Mail Archive.
 */
@Role
public interface IMailArchiveConfiguration
{
    static final String LDAP_PHOTO_CONTENT_BINARY = "binary";

    static final String LDAP_PHOTO_CONTENT_URL = "url";

    Map<String, IMailingList> getMailingLists();

    /**
     * Mailing-list groups.
     * 
     * @return A map of mailing-list groups, key being the "name" of the group.
     */
    Map<String, IMailingListGroup> getMailingListGroups();

    List<IMASource> getServers();

    Map<String, IType> getMailTypes();

    String getLoadingUser();

    String getDefaultHomeView();

    String getDefaultTopicsView();

    String getDefaultMailsOpeningMode();

    boolean isManageTimeline();

    int getMaxTimelineItemsToLoad();

    boolean isMatchProfiles();

    boolean isMatchLdap();

    boolean isLdapCreateMissingProfiles();

    boolean isLdapForcePhotoUpdate();

    String getLdapPhotoFieldName();

    String getLdapPhotoFieldContent();

    String getItemsSpaceName();

    boolean isCropTopicIds();

    boolean isUseStore();

    String getEmailIgnoredText();

    String getAdminPrefsPage();

    Map<String, LoadingSession> getLoadingSessions();

    public abstract void reloadConfiguration() throws MailArchiveException;

}
