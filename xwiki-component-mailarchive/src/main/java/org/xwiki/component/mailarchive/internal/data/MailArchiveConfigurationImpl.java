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
import org.xwiki.model.reference.ObjectReference;

/**
 * @version $Id$
 */
public class MailArchiveConfigurationImpl implements MailArchiveConfiguration
{
    private static final String SPACE_CODE = "MailArchiveCode";
    
    private static final String CLASS_ADMIN = SPACE_CODE + ".AdminClass";
    private static final String CLASS_LISTS = SPACE_CODE + ".ListsSettingsClass";
    private static final String CLASS_SERVERS = SPACE_CODE + ".ServersSettingsClass";
    private static final String CLASS_TYPES = SPACE_CODE + ".TypesSettingsClass";
    
    /** Provides access to documents. Injected by the Component Manager. */
    @Inject
    private static DocumentAccessBridge dab;

    private List<MailServer> servers;

    private List<MailingList> lists;

    private List<MailType> types;

    public MailArchiveConfigurationImpl()
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getLoadingUser()
     */
    @Override
    public String getLoadingUser()
    {
        return (String)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "user");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getDefaultHomeView()
     */
    @Override
    public String getDefaultHomeView()
    {
        return (String)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "defaulthomeview");    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getDefaultTopicsView()
     */
    @Override
    public String getDefaultTopicsView()
    {
        return (String)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "defaulttopicsview");    
    }    
    

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getDefaultMailsOpeningMode()
     */
    @Override
    public String getDefaultMailsOpeningMode()
    {
        return (String)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "mailsopeningmode");    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#manageTimeline()
     */
    @Override
    public boolean manageTimeline()
    {
        return (Boolean)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "timeline");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getMaxTimelineItemsToLoad()
     */
    @Override
    public int getMaxTimelineItemsToLoad()
    {
        return (Integer)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "timelinemaxload");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getMatchProfiles()
     */
    @Override
    public boolean getMatchProfiles()
    {
        return (Boolean)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "matchwikiprofiles");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getMatchLdap()
     */
    @Override
    public boolean getMatchLdap()
    {
        return (Boolean)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "matchldap");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getLdapCreateMissingProfiles()
     */
    @Override
    public boolean getLdapCreateMissingProfiles()
    {
        return (Boolean)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "createmissingprofiles");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getLdapForcePhotoUpdate()
     */
    @Override
    public boolean getLdapForcePhotoUpdate()
    {
        return (Boolean)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "ldapphotoforceupdate");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getLdapPhotoFieldName()
     */
    @Override
    public String getLdapPhotoFieldName()
    {
        return (String)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "ldapphotofield");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.mailarchive.MailArchiveConfiguration#getLdapPhotoFieldContent()
     */
    @Override
    public String getLdapPhotoFieldContent()
    {
        return (String)getPropertyValue(MailArchiveConfiguration.ADMIN_PAGE, CLASS_ADMIN, "ldapphototype");
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

}
