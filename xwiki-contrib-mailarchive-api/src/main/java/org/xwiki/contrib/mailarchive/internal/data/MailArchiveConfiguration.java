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
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mailarchive.IMASource;
import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
import org.xwiki.contrib.mailarchive.IMailMatcher;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IMailingListGroup;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.contrib.mailarchive.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge;
import org.xwiki.contrib.mailarchive.xwiki.internal.XWikiPersistence;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;
import org.xwiki.text.XWikiToStringBuilder;

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

    private Map<String, IMailingListGroup> mailingListGroups;

    private Map<String, IType> types;

    private Map<String, LoadingSession> loadingSessions;

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

    @Inject
    private ComponentManager componentManager;

    /*
     * @Inject private IMailArchive mailArchive;
     */

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
        this.mailingListGroups = loadMailingListGroupsDefinitions();
        this.types = loadMailTypesDefinitions();
        this.loadingSessions = loadLoadingSessions();

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
        logger.debug("Loading configured mailing-lists");
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
                    lists.put(list.getDisplayName(), list);
                    logger.info("Loaded list " + list);
                } else {
                    logger.warn("Incorrect mailing-list found in db " + prop[1]);
                }
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured mailing-lists", e);
        }
        logger.debug("Loaded {} mailing-lists from configuration", lists.size());
        return lists;
    }

    /**
     * Loads the mailing-lists definitions.
     * 
     * @return A map of mailing-lists definitions with key being the mailing-list pattern to check, and value an array
     *         [name, IMailingListItem]
     * @throws MailArchiveException
     */
    protected HashMap<String, IMailingListGroup> loadMailingListGroupsDefinitions() throws MailArchiveException
    {
        logger.debug("Loading mailing-list groups configuration");
        final HashMap<String, IMailingListGroup> listgroups = new HashMap<String, IMailingListGroup>();

        String xwql =
            "select listgroup.name, listgroup.mailingLists, listgroup.loadingUser, listgroup.destinationWiki, listgroup.destinationSpace from Document doc, doc.object('"
                + XWikiPersistence.CLASS_MAIL_LIST_GROUPS
                + "') as listgroup where doc.space='"
                + XWikiPersistence.SPACE_PREFS + "'";
        try {
            List<Object[]> props = this.queryManager.createQuery(xwql, Query.XWQL).execute();

            for (Object[] prop : props) {
                final String mailingListGroupName = (String) prop[0];
                final String loadingUser = (String) prop[2];
                final String destinationWiki = (String) prop[3];
                final String destinationSpace = (String) prop[4];
                List<IMailingList> mailingListItems = new ArrayList<IMailingList>();
                if (StringUtils.isNotBlank(mailingListGroupName)) {
                    logger.debug("Searching mailing-lists for group {}", mailingListGroupName);
                    final String mailingListsString = (String) prop[1];
                    if (StringUtils.isNotBlank(mailingListsString)) {
                        String[] splittedList = mailingListsString.split("|");
                        if (splittedList.length > 0) {
                            for (String list : splittedList) {
                                if (this.getMailingLists().containsKey(list)) {
                                    final MailingList mailingListItem = (MailingList) this.getMailingLists().get(list);
                                    mailingListItems.add(mailingListItem);
                                    logger.debug("Added list {}", mailingListItem.getDisplayName());
                                }
                            }
                        }
                    }
                    IMailingListGroup listgroup =
                        factory.createMailingListGroup(mailingListGroupName, mailingListItems, loadingUser,
                            destinationWiki, destinationSpace);
                    listgroups.put(listgroup.getName(), listgroup);
                    // Attach the group back to each mailing-list
                    for (IMailingList list : listgroup.getMailingLists()) {
                        ((MailingList) list).setGroup(listgroup);
                    }
                    logger.debug("Loaded list group {}", listgroup);
                } else {
                    logger.warn("Incorrect mailing-list group {}", prop[1]);
                }
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured mailing-list groups",
                ExceptionUtils.getRootCause(e));
        }
        logger.debug("Loaded {} mailing-list groups from configuration", listgroups.size());
        return listgroups;
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
        logger.debug("Loading mail types");
        Map<String, IType> mailTypes = new HashMap<String, IType>();

        String xwql =
            "select type.id, type.name, type.icon, doc.fullName from Document doc, doc.object("
                + XWikiPersistence.CLASS_MAIL_TYPES + ") as type where doc.space='" + XWikiPersistence.SPACE_PREFS
                + "' and type.id<>'mail' and type.id<>'calendar' and type.id<>'attachedMail'";
        try {
            List<Object[]> types = this.queryManager.createQuery(xwql, Query.XWQL).execute();

            for (Object[] type : types) {

                try {
                    IType typeobj = factory.createMailType((String) type[0], (String) type[1], (String) type[2]);

                    logger.debug("Loaded type {}", typeobj);

                    if (typeobj != null) {
                        logger.debug("Loading matchers for type");
                        xwql =
                            "select matcher.fields, matcher.expression, matcher.isAdvanced, matcher.isIgnoreCase, matcher.isMultiLine "
                                + "from Document doc, doc.object(" + XWikiPersistence.CLASS_MAIL_MATCHERS
                                + ") as matcher where " + "doc.fullName='" + (String) type[3] + "'";
                        List<Object[]> matchers = this.queryManager.createQuery(xwql, Query.XWQL).execute();
                        for (Object[] matcher : matchers) {
                            logger.debug("FIELDS " + matcher[0] + " " + matcher[0].getClass());
                            IMailMatcher matcherobj =
                                factory.createMailMatcher((String) matcher[0], (String) matcher[1],
                                    (Integer) matcher[2], (Integer) matcher[3], (Integer) matcher[4]);
                            typeobj.getMatchers().add(matcherobj);
                            logger.debug("Loaded matcher {}", matcherobj);
                        }

                        mailTypes.put(typeobj.getId(), typeobj);
                        logger.debug("Loaded mail type {}", typeobj);
                    } else {
                        logger.warn("Invalid type {}", type[0]);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to load type", ExceptionUtils.getRootCause(e));
                }
            }

        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured mail types", e);
        }

        logger.debug("Loaded {} types from configuration", mailTypes.size());
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
        logger.debug("Loading servers from configuration");
        final List<IMASource> servers = new ArrayList<IMASource>();

        String xwql =
            "select doc.fullName from Document doc, doc.object(" + XWikiPersistence.CLASS_MAIL_SERVERS
                + ") as server where doc.space='" + XWikiPersistence.SPACE_PREFS + "'";
        try {
            List<String> props = this.queryManager.createQuery(xwql, Query.XWQL).execute();

            for (String serverPrefsDoc : props) {
                logger.debug("Loading server definition from page " + serverPrefsDoc + " ...");
                if (StringUtils.isNotBlank(serverPrefsDoc)) {
                    Server server = factory.createMailServer(serverPrefsDoc);
                    if (server != null) {
                        servers.add(server);
                        logger.debug("Loaded IServer connection definition {}", server);
                    } else {
                        logger.warn("Invalid server definition from document {}", serverPrefsDoc);
                    }

                } else {
                    logger.info("Incorrect server preferences doc skipped");
                }
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured servers", e);
        }
        logger.debug("Loaded {} servers from configuration", servers.size());
        return servers;
    }

    /**
     * Loads the mailing-lists
     * 
     * @return
     * @throws MailArchiveException
     */
    protected List<IMASource> loadStoresDefinitions() throws MailArchiveException
    {
        logger.debug("Loading stores from configuration");
        final List<IMASource> stores = new ArrayList<IMASource>();

        String xwql =
            "select doc.fullName from Document doc, doc.object(" + XWikiPersistence.CLASS_MAIL_STORES
                + ") as store where doc.space='" + XWikiPersistence.SPACE_PREFS + "'";
        try {
            List<String> props = this.queryManager.createQuery(xwql, Query.XWQL).execute();

            for (String storePrefsDoc : props) {
                logger.debug("Loading store definition from page " + storePrefsDoc + " ...");
                if (StringUtils.isNotBlank(storePrefsDoc)) {
                    MailStore store = factory.createMailStore(storePrefsDoc);
                    if (store != null) {
                        stores.add(store);
                        logger.debug("Loaded IServer connection definition {}", store);
                    } else {
                        logger.warn("Invalid server definition from document {}", storePrefsDoc);
                    }

                } else {
                    logger.info("Incorrect IServer preferences doc");
                }
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured mail stores", e);
        }
        logger.debug("Loaded {} stores from configuration", stores.size());
        return stores;
    }

    protected Map<String, LoadingSession> loadLoadingSessions() throws MailArchiveException
    {
        logger.debug("Loading loading sessions from configuration");
        final Map<String, LoadingSession> sessions = new HashMap<String, LoadingSession>();

        final String xwql =
            "select doc.fullName from Document doc, doc.object(" + XWikiPersistence.CLASS_LOADING_SESSION
                + ") as session where doc.space='" + XWikiPersistence.SPACE_PREFS + "'";
        try {
            List<String> props = this.queryManager.createQuery(xwql, Query.XWQL).execute();

            for (String sessionPrefsDoc : props) {
                logger.debug("Loading loading session from page {} ...");
                if (StringUtils.isNotBlank(sessionPrefsDoc)) {
                    // FIXME: ugly trick - MailArchiveConfiguration should not depend on IMailArchive...
                    LoadingSession session = factory.createLoadingSession(sessionPrefsDoc);
                    if (session != null) {
                        sessions.put(session.getId(), session);
                        logger.debug("Loaded Loading Session definition {}" + session);
                    } else {
                        logger.warn("Invalid loading session definition from document {}", sessionPrefsDoc);
                    }

                } else {
                    logger.info("Incorrect LoadingSession preferences doc");
                }
            }
        } catch (Exception e) {
            throw new MailArchiveException("Failed to load configured loading sessions", e);
        }
        logger.debug("Loaded {} loading sessions from configuration", sessions.size());
        return sessions;
    }

    @Override
    public String getLoadingUser()
    {
        return loadingUser;
    }

    @Override
    public String getDefaultHomeView()
    {
        return defaultHomeView;
    }

    @Override
    public String getDefaultTopicsView()
    {
        return defaultTopicsView;
    }

    @Override
    public String getDefaultMailsOpeningMode()
    {
        return defaultMailsOpeningMode;
    }

    @Override
    public boolean isManageTimeline()
    {
        return manageTimeline;
    }

    @Override
    public int getMaxTimelineItemsToLoad()
    {
        return maxTimelineItemsToLoad;
    }

    @Override
    public boolean isMatchProfiles()
    {
        return matchProfiles;
    }

    @Override
    public boolean isMatchLdap()
    {
        return matchLdap;
    }

    @Override
    public boolean isLdapCreateMissingProfiles()
    {
        return ldapCreateMissingProfiles;
    }

    @Override
    public boolean isLdapForcePhotoUpdate()
    {
        return ldapForcePhotoUpdate;
    }

    @Override
    public String getLdapPhotoFieldName()
    {
        return ldapPhotoFieldName;
    }

    @Override
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
     * @see org.xwiki.contrib.mailarchive.IMailArchiveConfiguration#isUseStore()
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
     * @see org.xwiki.contrib.mailarchive.IMailArchiveConfiguration#getMailingLists()
     */
    @Override
    public Map<String, IMailingList> getMailingLists()
    {
        return this.lists;
    }

    /**
     * @return the mailingListGroups
     */
    @Override
    public Map<String, IMailingListGroup> getMailingListGroups()
    {
        return mailingListGroups;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailArchiveConfiguration#getServers()
     */
    @Override
    public List<IMASource> getServers()
    {
        return this.servers;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailArchiveConfiguration#getMailTypes()
     */
    @Override
    public Map<String, IType> getMailTypes()
    {
        return this.types;
    }

    @Override
    public Map<String, LoadingSession> getLoadingSessions()
    {
        return this.loadingSessions;
    }

    /**
     * @return The page from which configuration is retrieved.
     */
    @Override
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

    @Override
    public String getEmailIgnoredText()
    {
        return emailIgnoredText;
    }

    public void setEmailIgnoredText(String emailIgnoredText)
    {
        this.emailIgnoredText = emailIgnoredText;
    }

    @Override
    public String toString()
    {
        XWikiToStringBuilder builder = new XWikiToStringBuilder(this);
        if (adminPrefsPage != null)
            builder.append("adminPrefsPage", adminPrefsPage);
        if (servers != null)
            builder.append("servers", servers);
        if (lists != null)
            builder.append("lists", lists);
        if (mailingListGroups != null)
            builder.append("mailingListGroups", mailingListGroups);
        if (types != null)
            builder.append("types", types);
        if (loadingSessions != null)
            builder.append("loadingSessions", loadingSessions);
        if (loadingUser != null)
            builder.append("loadingUser", loadingUser);
        if (defaultHomeView != null)
            builder.append("defaultHomeView", defaultHomeView);
        if (defaultTopicsView != null)
            builder.append("defaultTopicsView", defaultTopicsView);
        if (defaultMailsOpeningMode != null)
            builder.append("defaultMailsOpeningMode", defaultMailsOpeningMode);
        builder.append("manageTimeline", manageTimeline);
        builder.append("maxTimelineItemsToLoad", maxTimelineItemsToLoad);
        builder.append("matchProfiles", matchProfiles);
        builder.append("matchLdap", matchLdap);
        builder.append("ldapCreateMissingProfiles", ldapCreateMissingProfiles);
        builder.append("ldapForcePhotoUpdate", ldapForcePhotoUpdate);
        if (ldapPhotoFieldName != null)
            builder.append("ldapPhotoFieldName", ldapPhotoFieldName);
        if (ldapPhotoFieldContent != null)
            builder.append("ldapPhotoFieldContent", ldapPhotoFieldContent);
        builder.append("cropTopicIds", cropTopicIds);
        if (itemsSpaceName != null)
            builder.append("itemsSpaceName", itemsSpaceName);
        builder.append("useStore", useStore);
        if (emailIgnoredText != null)
            builder.append("emailIgnoredText", emailIgnoredText);
        if (queryManager != null)
            builder.append("queryManager", queryManager);
        if (logger != null)
            builder.append("logger", logger);
        if (factory != null)
            builder.append("factory", factory);
        if (bridge != null)
            builder.append("bridge", bridge);
        if (componentManager != null)
            builder.append("componentManager", componentManager);
        return builder.toString();
    }

}
