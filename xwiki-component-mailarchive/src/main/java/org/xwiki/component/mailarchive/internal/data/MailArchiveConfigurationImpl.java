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
package org.xwiki.component.mailarchive.internal.data;

import java.util.List;

import javax.inject.Inject;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.mailarchive.MailArchiveConfiguration;
import org.xwiki.component.mailarchive.MailServer;
import org.xwiki.component.mailarchive.MailType;
import org.xwiki.component.mailarchive.MailingList;
import org.xwiki.component.mailarchive.internal.DefaultMailArchive;
import org.xwiki.component.mailarchive.internal.exceptions.MailArchiveException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id$
 */
public class MailArchiveConfigurationImpl implements MailArchiveConfiguration
{
    private static final String CLASS_ADMIN = DefaultMailArchive.SPACE_CODE + ".AdminClass";

    /** Provides access to documents. Injected by the Component Manager. */
    @Inject
    private static DocumentAccessBridge dab;

    private List<MailServer> servers;

    private List<MailingList> lists;

    private List<MailType> types;

    private String adminPrefsPage;

    /* ***** GLOBAL PARAMETERS ***** */

    private String loadingUser;

    private String defaultHomeView;

    private String defaultTopicsView;

    private String defaultMailsOpeningMode;

    private boolean manageTimeline;

    private int maxTimelineItemsToLoad;

    private boolean matchProfiles;

    private boolean matchLdap;

    private boolean ldapCreateMissingProfiles;

    private boolean ldapForcePhotoUpdate;

    private String ldapPhotoFieldName;

    private String ldapPhotoFieldContent;

    private boolean cropTopicIds;

    private String itemsSpaceName;

    public MailArchiveConfigurationImpl(String adminPrefsPage, XWikiContext context) throws MailArchiveException
    {
        try {
            this.adminPrefsPage = adminPrefsPage;
            XWiki xwiki = context.getWiki();
            if (!xwiki.exists(adminPrefsPage, context)) {
                throw new MailArchiveException("Preferences page does not exist");
            } else {
                XWikiDocument prefsdoc = xwiki.getDocument(adminPrefsPage, context);
                BaseObject prefsobj = prefsdoc.getObject(CLASS_ADMIN);
                this.loadingUser = prefsobj.getStringValue("user");
                this.defaultHomeView = prefsobj.getStringValue("defaulthomeview");
                this.defaultTopicsView = prefsobj.getStringValue("defaulttopicsview");
                this.defaultMailsOpeningMode = prefsobj.getStringValue("mailsopeningmode");
                this.manageTimeline = prefsobj.getIntValue("timeline") != 0;
                this.maxTimelineItemsToLoad = prefsobj.getIntValue("timelinemaxload");
                this.matchProfiles = prefsobj.getIntValue("matchwikiprofiles") != 0;
                this.matchLdap = prefsobj.getIntValue("matchldap") != 0;
                this.ldapCreateMissingProfiles = prefsobj.getIntValue("createmissingprofiles") != 0;
                this.ldapForcePhotoUpdate = prefsobj.getIntValue("ldapphotoforceupdate") != 0;
                this.ldapPhotoFieldName = prefsobj.getStringValue("ldapphotofield");
                this.ldapPhotoFieldContent = prefsobj.getStringValue("ldapphototype");
                this.cropTopicIds = prefsobj.getIntValue("adv_croptopicid") != 0;
                this.itemsSpaceName = prefsobj.getStringValue("adv_itemsspace");
            }
        } catch (XWikiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getLoadingUser()
    {
        return loadingUser;
    }

    public String getDefaultHomeView()
    {
        return defaultHomeView;
    }

    public String getDefaultTopicsView()
    {
        return defaultTopicsView;
    }

    public String getDefaultMailsOpeningMode()
    {
        return defaultMailsOpeningMode;
    }

    public boolean isManageTimeline()
    {
        return manageTimeline;
    }

    public int getMaxTimelineItemsToLoad()
    {
        return maxTimelineItemsToLoad;
    }

    public boolean isMatchProfiles()
    {
        return matchProfiles;
    }

    public boolean isMatchLdap()
    {
        return matchLdap;
    }

    public boolean isLdapCreateMissingProfiles()
    {
        return ldapCreateMissingProfiles;
    }

    public boolean isLdapForcePhotoUpdate()
    {
        return ldapForcePhotoUpdate;
    }

    public String getLdapPhotoFieldName()
    {
        return ldapPhotoFieldName;
    }

    public String getLdapPhotoFieldContent()
    {
        return ldapPhotoFieldContent;
    }

    @Override
    public boolean isCropTopicIds()
    {
        return cropTopicIds;
    }

    @Override
    public void setCropTopicIds(boolean cropTopicIds)
    {
        this.cropTopicIds = cropTopicIds;
    }

    @Override
    public String getItemsSpaceName()
    {
        return itemsSpaceName;
    }

    @Override
    public void setItemsSpaceName(String itemsSpaceName)
    {
        this.itemsSpaceName = itemsSpaceName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getMailingLists()
     */
    @Override
    public List<MailingList> getMailingLists()
    {

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getServers()
     */
    @Override
    public List<MailServer> getServers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getMailTypes()
     */
    @Override
    public List<MailType> getMailTypes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public static Object getPropertyValue(String docname, String classname, String propname)
    {
        return dab.getProperty(docname, classname, 0, propname);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MailArchiveConfigurationImpl [loadingUser=").append(loadingUser).append(", defaultHomeView=")
            .append(defaultHomeView).append(", defaultTopicsView=").append(defaultTopicsView)
            .append(", defaultMailsOpeningMode=").append(defaultMailsOpeningMode).append(", manageTimeline=")
            .append(manageTimeline).append(", maxTimeLineItemsToLoad=").append(maxTimelineItemsToLoad)
            .append(", matchProfiles=").append(matchProfiles).append(", matchLdap=").append(matchLdap)
            .append(", ldapCreateMissingProfiles=").append(ldapCreateMissingProfiles).append(", ldapForcePhotoUpdate=")
            .append(ldapForcePhotoUpdate).append(", ldapPhotoFieldName=").append(ldapPhotoFieldName)
            .append(", ldapPhotoFieldContent=").append(ldapPhotoFieldContent).append("]");
        return builder.toString();
    }

}
