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
package org.xwiki.contrib.mailarchive.internal.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mailarchive.IMASource;
import org.xwiki.contrib.mailarchive.IMailMatcher;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.persistence.XWikiPersistence;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

/**
 * @version $Id$
 */
@Component
@Singleton
public class MailArchiveConfiguration implements IMailArchiveConfiguration, Initializable
{
    private static final String CLASS_ADMIN = XWikiPersistence.SPACE_CODE + ".AdminClass";

    private String adminPrefsPage;

    /* ***** GLOBAL PARAMETERS ***** */

    private List<IMASource> servers;

    private Map<String, IMailingList> lists;

    private Map<String, IType> types;

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

    private boolean useStore;

    private String emailIgnoredText;

    // Components

    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    @Inject
    private IFactory factory;

    @Inject
    @Named("extended")
    private IExtendedDocumentAccessBridge bridge;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        logger.debug("initialize()");
        this.adminPrefsPage = XWikiPersistence.SPACE_PREFS + ".GlobalParameters";
    }

    @Override
    public void reloadConfiguration() throws MailArchiveException
    {
        if (!bridge.exists(adminPrefsPage)) {
            throw new MailArchiveException("Preferences page does not exist");
        }

        this.loadingUser = bridge.getStringValue(adminPrefsPage, CLASS_ADMIN, "user");
        this.defaultHomeView = bridge.getStringValue(adminPrefsPage, CLASS_ADMIN, "defaulthomeview");
        this.defaultTopicsView = bridge.getStringValue(adminPrefsPage, CLASS_ADMIN, "defaulttopicsview");
        this.defaultMailsOpeningMode = bridge.getStringValue(adminPrefsPage, CLASS_ADMIN, "mailsopeningmode");
        this.manageTimeline = bridge.getBooleanValue(adminPrefsPage, CLASS_ADMIN, "timeline");
        this.maxTimelineItemsToLoad = bridge.getIntValue(adminPrefsPage, CLASS_ADMIN, "timelinemaxload");
        this.matchProfiles = bridge.getBooleanValue(adminPrefsPage, CLASS_ADMIN, "matchwikiprofiles");
        this.matchLdap = bridge.getBooleanValue(adminPrefsPage, CLASS_ADMIN, "matchldap");
        this.ldapCreateMissingProfiles = bridge.getBooleanValue(adminPrefsPage, CLASS_ADMIN, "createmissingprofiles");
        this.ldapForcePhotoUpdate = bridge.getIntValue(adminPrefsPage, CLASS_ADMIN, "ldapphotoforceupdate") != 0;
        this.ldapPhotoFieldName = bridge.getStringValue(adminPrefsPage, CLASS_ADMIN, "ldapphotofield");
        this.ldapPhotoFieldContent = bridge.getStringValue(adminPrefsPage, CLASS_ADMIN, "ldapphototype");
        this.cropTopicIds = bridge.getBooleanValue(adminPrefsPage, CLASS_ADMIN, "adv_croptopicid");
        this.itemsSpaceName = bridge.getStringValue(adminPrefsPage, CLASS_ADMIN, "adv_itemsspace");
        this.useStore = bridge.getBooleanValue(adminPrefsPage, CLASS_ADMIN, "store");

        List<IMASource> sources = loadServersDefinitions();
        sources.addAll(loadStoresDefinitions());
        this.servers = sources;
        this.lists = loadMailingListsDefinitions();
        this.types = loadMailTypesDefinitions();

        if (logger.isDebugEnabled()) {
            logger.debug("loaded mail archive configuration: " + toString());
        }
    }

    /**
     * Loads the mailing-lists definitions.
     * 
     * @return A map of mailing-lists definitions with key being the mailing-list pattern to check, and value an array
     *         [displayName, Tag]
     * @throws MailArchiveException
     */
    protected HashMap<String, IMailingList> loadMailingListsDefinitions() throws MailArchiveException
    {
        final HashMap<String, IMailingList> lists = new HashMap<String, IMailingList>();

        String xwql =
            "select list.pattern, list.displayname, list.Tag, list.color from Document doc, doc.object('"
                + XWikiPersistence.CLASS_MAIL_LISTS + "') as list where doc.space='" + XWikiPersistence.SPACE_PREFS
                + "'";
        try {
            List<Object[]> props = this.queryManager.createQuery(xwql, Query.XWQL).execute();

            for (Object[] prop : props) {
                if (StringUtils.isNotBlank((String) prop[0])) {
                    IMailingList list =
                        factory.createMailingList((String) prop[0], (String) prop[1], (String) prop[2],
                            (String) prop[3]);
                    lists.put(list.getPattern(), list);
                    logger.info("Loaded list " + list);
                } else {
                    logger.warn("Incorrect mailing-list found in db " + prop[1]);
                }
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured mailing-lists", e);
        }
        return lists;
    }

    /**
     * Loads mail types from database.
     * 
     * @return A map of mail types definitions, key of map entries being the type name, and value the IType
     *         representation.
     * @throws MailArchiveException
     */
    protected Map<String, IType> loadMailTypesDefinitions() throws MailArchiveException
    {
        logger.info("Loading mail types...");
        Map<String, IType> mailTypes = new HashMap<String, IType>();

        String xwql =
            "select type.id, type.name, type.icon, doc.fullName from Document doc, doc.object("
                + XWikiPersistence.CLASS_MAIL_TYPES + ") as type where doc.space='" + XWikiPersistence.SPACE_PREFS
                + "'";
        try {
            List<Object[]> types = this.queryManager.createQuery(xwql, Query.XWQL).execute();

            for (Object[] type : types) {

                IType typeobj = factory.createMailType((String) type[0], (String) type[1], (String) type[2]);
                if (logger.isDebugEnabled()) {
                    logger.debug("Loaded type " + typeobj);
                }
                if (typeobj != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading matchers for type...");
                    }
                    xwql =
                        "select matcher.fields, matcher.expression, matcher.isAdvanced, matcher.isIgnoreCase, matcher.isMultiLine "
                            + "from Document doc, doc.object(" + XWikiPersistence.CLASS_MAIL_MATCHERS
                            + ") as matcher where " + "doc.fullName='" + (String) type[3] + "'";
                    List<Object[]> matchers = this.queryManager.createQuery(xwql, Query.XWQL).execute();
                    for (Object[] matcher : matchers) {
                        logger.debug("FIELDS " + matcher[0] + " " + matcher[0].getClass());
                        IMailMatcher matcherobj =
                            factory.createMailMatcher((String) matcher[0], (String) matcher[1], (Integer) matcher[2],
                                (Integer) matcher[3], (Integer) matcher[4]);
                        typeobj.getMatchers().add(matcherobj);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Loaded matcher " + matcherobj);
                        }
                    }

                    mailTypes.put(typeobj.getId(), typeobj);
                    logger.info("Loaded mail type " + typeobj);
                } else {
                    logger.warn("Invalid type " + type[0]);
                }
            }

        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured mail types", e);
        }

        return mailTypes;
    }

    /**
     * Loads the mailing-lists
     * 
     * @return
     * @throws MailArchiveException
     */
    protected List<IMASource> loadServersDefinitions() throws MailArchiveException
    {
        final List<IMASource> lists = new ArrayList<IMASource>();

        String xwql =
            "select doc.fullName from Document doc, doc.object('" + XWikiPersistence.CLASS_MAIL_SERVERS
                + "') as server where doc.space='" + XWikiPersistence.SPACE_PREFS + "'";
        try {
            List<String> props = this.queryManager.createQuery(xwql, Query.XWQL).execute();

            for (String serverPrefsDoc : props) {
                logger.info("Loading server definition from page " + serverPrefsDoc + " ...");
                if (StringUtils.isNotBlank(serverPrefsDoc)) {
                    Server server = factory.createMailServer(serverPrefsDoc);
                    if (server != null) {
                        lists.add(server);
                        logger.info("Loaded IServer connection definition " + server);
                    } else {
                        logger.warn("Invalid server definition from document " + serverPrefsDoc);
                    }

                } else {
                    logger.info("Incorrect IServer preferences doc found in db");
                }
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured servers", e);
        }
        return lists;
    }

    /**
     * Loads the mailing-lists
     * 
     * @return
     * @throws MailArchiveException
     */
    protected List<IMASource> loadStoresDefinitions() throws MailArchiveException
    {
        final List<IMASource> lists = new ArrayList<IMASource>();

        String xwql =
            "select doc.fullName from Document doc, doc.object('" + XWikiPersistence.CLASS_MAIL_STORES
                + "') as store where doc.space='" + XWikiPersistence.SPACE_PREFS + "'";
        try {
            List<String> props = this.queryManager.createQuery(xwql, Query.XWQL).execute();

            for (String storePrefsDoc : props) {
                logger.info("Loading store definition from page " + storePrefsDoc + " ...");
                if (StringUtils.isNotBlank(storePrefsDoc)) {
                    MailStore store = factory.createMailStore(storePrefsDoc);
                    if (store != null) {
                        lists.add(store);
                        logger.info("Loaded IServer connection definition " + store);
                    } else {
                        logger.warn("Invalid server definition from document " + storePrefsDoc);
                    }

                } else {
                    logger.info("Incorrect IServer preferences doc found in db");
                }
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured mail stores", e);
        }
        return lists;
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

    public void setCropTopicIds(boolean cropTopicIds)
    {
        this.cropTopicIds = cropTopicIds;
    }

    @Override
    public String getItemsSpaceName()
    {
        return itemsSpaceName;
    }

    public void setItemsSpaceName(String itemsSpaceName)
    {
        this.itemsSpaceName = itemsSpaceName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration#isUseStore()
     */
    @Override
    public boolean isUseStore()
    {
        return useStore;
    }

    public void setUseStore(boolean useStore)
    {
        this.useStore = useStore;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration#getMailingLists()
     */
    @Override
    public Map<String, IMailingList> getMailingLists()
    {
        return this.lists;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration#getServers()
     */
    @Override
    public List<IMASource> getServers()
    {
        return this.servers;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration#getMailTypes()
     */
    @Override
    public Map<String, IType> getMailTypes()
    {
        return this.types;
    }

    /**
     * @return The page from which configuration is retrieved.
     */
    public String getAdminPrefsPage()
    {
        return adminPrefsPage;
    }

    /**
     * Update page to load configuration from, and triggers a reload of configuration from this page.
     * 
     * @param adminPrefsPage
     * @throws MailArchiveException
     */
    public void setAdminPrefsPage(String adminPrefsPage) throws MailArchiveException
    {
        this.adminPrefsPage = adminPrefsPage;
        reloadConfiguration();
    }

    public String getEmailIgnoredText()
    {
        return emailIgnoredText;
    }

    public void setEmailIgnoredText(String emailIgnoredText)
    {
        this.emailIgnoredText = emailIgnoredText;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MailArchiveConfiguration [adminPrefsPage=").append(adminPrefsPage).append(", servers=")
            .append(servers).append(", lists=").append(lists).append(", types=").append(types).append(", loadingUser=")
            .append(loadingUser).append(", defaultHomeView=").append(defaultHomeView).append(", defaultTopicsView=")
            .append(defaultTopicsView).append(", defaultMailsOpeningMode=").append(defaultMailsOpeningMode)
            .append(", manageTimeline=").append(manageTimeline).append(", maxTimelineItemsToLoad=")
            .append(maxTimelineItemsToLoad).append(", matchProfiles=").append(matchProfiles).append(", matchLdap=")
            .append(matchLdap).append(", ldapCreateMissingProfiles=").append(ldapCreateMissingProfiles)
            .append(", ldapForcePhotoUpdate=").append(ldapForcePhotoUpdate).append(", ldapPhotoFieldName=")
            .append(ldapPhotoFieldName).append(", ldapPhotoFieldContent=").append(ldapPhotoFieldContent)
            .append(", cropTopicIds=").append(cropTopicIds).append(", itemsSpaceName=").append(itemsSpaceName)
            .append(", useStore=").append(useStore).append(", emailIgnoredText=").append(emailIgnoredText).append("]");
        return builder.toString();
    }

}
