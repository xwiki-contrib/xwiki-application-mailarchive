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
package org.xwiki.contrib.mailarchive.internal;

import java.util.List;
import java.util.Map;

import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IServer;
import org.xwiki.contrib.mailarchive.IType;

/**
 * Interface (aka Role) of the Component
 */
public interface IMailArchiveConfiguration
{
    public static final String LDAP_PHOTO_CONTENT_BINARY = "binary";

    public static final String LDAP_PHOTO_CONTENT_URL = "url";

    public String getLoadingUser();

    public String getDefaultHomeView();

    public String getDefaultTopicsView();

    public String getDefaultMailsOpeningMode();

    public boolean isManageTimeline();

    public int getMaxTimelineItemsToLoad();

    public boolean isMatchProfiles();

    public boolean isMatchLdap();

    public boolean isLdapCreateMissingProfiles();

    public boolean isLdapForcePhotoUpdate();

    public String getLdapPhotoFieldName();

    public String getLdapPhotoFieldContent();

    public Map<String, IMailingList> getMailingLists();

    public List<IServer> getServers();

    public Map<String, IType> getMailTypes();

    public String getItemsSpaceName();

    public boolean isCropTopicIds();

    public boolean isUseStore();
}
