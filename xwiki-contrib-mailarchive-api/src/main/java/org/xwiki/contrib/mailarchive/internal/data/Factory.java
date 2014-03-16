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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mailarchive.IMailMatcher;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IMailingListGroup;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.LoadingSession;
import org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge;
import org.xwiki.contrib.mailarchive.xwiki.ObjectEntity;
import org.xwiki.contrib.mailarchive.xwiki.internal.XWikiPersistence;

import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id$
 */
@Component
@Singleton
public class Factory implements IFactory
{

    @Inject
    private Logger logger;

    @Inject
    @Named("extended")
    private IExtendedDocumentAccessBridge dab;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.data.IFactory#createMailServer(java.lang.String)
     */
    @SuppressWarnings("deprecation")
    @Override
    public Server createMailServer(final String serverPrefsDoc)
    {
        if (!dab.exists(serverPrefsDoc)) {
            logger.error("createMailServer: Page " + serverPrefsDoc + " does not exist");
            return null;
        }
        if (!dab.exists(serverPrefsDoc, XWikiPersistence.CLASS_MAIL_SERVERS)) {
            logger.error("createMailServer: Page " + serverPrefsDoc + " does not contain an object of class "
                + XWikiPersistence.CLASS_MAIL_SERVERS);
            return null;
        }
        Server server = new Server();

        try {
            // Retrieve connection properties from prefs
            String className = XWikiPersistence.CLASS_MAIL_SERVERS;
            server.setId(dab.getStringValue(serverPrefsDoc, className, "id"));
            server.setHostname(dab.getStringValue(serverPrefsDoc, className, "hostname"));
            server.setPort(dab.getIntValue(serverPrefsDoc, className, "port"));
            server.setProtocol(dab.getStringValue(serverPrefsDoc, className, "protocol"));
            server.setUsername(dab.getStringValue(serverPrefsDoc, className, "user"));
            server.setPassword(dab.getStringValue(serverPrefsDoc, className, "password"));
            server.setFolder(dab.getStringValue(serverPrefsDoc, className, "folder"));            
            server.setAutoTrustSSLCertificates(dab.getBooleanValue(serverPrefsDoc, className, "autotrust"));            
            server.setEnabled("on".equals(dab.getStringValue(serverPrefsDoc, className, "state")));
            server.setState(dab.getIntValue(serverPrefsDoc, className, "status"));
            String additionalProperties = dab.getStringValue(serverPrefsDoc, className, "additionalProperties");
            if (StringUtils.isNotBlank(additionalProperties)) {
                InputStream is = new ByteArrayInputStream(additionalProperties.getBytes());
                Properties props = new Properties();
                try {
                    props.load(is);
                } catch (IOException e) {
                    // TODO ?
                }
                server.setAdditionalProperties(props);
            }
        } catch (Exception e) {
            logger.error("Could not load Server from " + serverPrefsDoc);
        }
        server.setWikiDoc(serverPrefsDoc);

        if (server.getId() == null || server.getHostname() == null || server.getProtocol() == null) {
            logger.error("createMailServer: Server " + serverPrefsDoc + " miss mandatory parameters");
            return null;
        } else {
            logger.debug("createMailServer: Loaded Server " + server);
            return server;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.data.IFactory#createMailStore(java.lang.String)
     */
    @Override
    public MailStore createMailStore(String storePrefsDoc)
    {
        logger.debug("Loading Mail Store from " + storePrefsDoc);

        if (!dab.exists(storePrefsDoc)) {
            logger.error("createMailStore: page " + storePrefsDoc + " does not exist");
            return null;
        }
        if (!dab.exists(storePrefsDoc, XWikiPersistence.CLASS_MAIL_STORES)) {
            logger.error("createMailServer: Page " + storePrefsDoc + " does not contain an object of class "
                + XWikiPersistence.CLASS_MAIL_STORES);
            return null;
        }
        MailStore store = new MailStore();

        // Retrieve connection properties from prefs
        try {
            String className = XWikiPersistence.CLASS_MAIL_STORES;
            store.setLocation(dab.getStringValue(storePrefsDoc, className, "location"));
            store.setFormat(dab.getStringValue(storePrefsDoc, className, "format"));
            store.setId(dab.getStringValue(storePrefsDoc, className, "id"));
            store.setFolder(dab.getStringValue(storePrefsDoc, className, "folder"));
            store.setEnabled("on".equals(dab.getStringValue(storePrefsDoc, className, "state")));
            store.setState(dab.getIntValue(storePrefsDoc, className, "status"));
            String additionalProperties = dab.getStringValue(storePrefsDoc, className, "additionalProperties");
            if (StringUtils.isNotBlank(additionalProperties)) {
                InputStream is = new ByteArrayInputStream(additionalProperties.getBytes());
                Properties props = new Properties();
                try {
                    props.load(is);
                } catch (IOException e) {
                    // TODO ?
                }
                store.setAdditionalProperties(props);
            }
        } catch (Exception e) {
            logger.error("Could not load Mail Store from " + storePrefsDoc, e);
            return null;
        }
        store.setWikiDoc(storePrefsDoc);

        if (store.getId() == null || store.getLocation() == null || store.getFormat() == null) {
            logger.error("createMailStore: Store " + storePrefsDoc + " miss mandatory parameters");
            return null;
        } else {
            logger.debug("Loaded Mail Store " + store);
            return store;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.data.IFactory#createMailType(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public IType createMailType(final String id, final String name, final String icon)
    {

        Type typeobj = new Type();

        typeobj.setId(id);
        typeobj.setName(name);
        typeobj.setIcon(icon);

        return typeobj;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.data.IFactory#createMailMatcher(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public IMailMatcher createMailMatcher(String fields, String expression, Integer isAdvanced, Integer isIgnoreCase,
        Integer isMultiLine)
    {
        MailMatcher matcherobj = new MailMatcher();

        List<String> fieldsList = Arrays.asList(fields.split(","));
        matcherobj.setFields(fieldsList);
        matcherobj.setExpression(expression);
        matcherobj.setAdvancedMode(isAdvanced != null && isAdvanced != 0);
        matcherobj.setIgnoreCase(isIgnoreCase != null && isIgnoreCase != 0);
        matcherobj.setMultiLine(isMultiLine != null && isMultiLine != 0);

        return matcherobj;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.data.IFactory#createMailingList(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public IMailingList createMailingList(final String pattern, final String displayName, final String tag,
        final String color)
    {
        MailingList list = new MailingList();

        list.setPattern(pattern);
        list.setTag(tag);
        list.setDisplayName(displayName);
        list.setColor(color);

        return list;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.data.IFactory#createMailingList(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public IMailingListGroup createMailingListGroup(final String name, final List<IMailingList> mailingLists,
        final String loadingUser, final String destinationWiki, final String destinationSpace)
    {
        MailingListGroup listgroup = new MailingListGroup();

        listgroup.setName(name);
        listgroup.setMailingLists(mailingLists);
        listgroup.setLoadingUser(loadingUser);
        listgroup.setDestinationWiki(destinationWiki);
        listgroup.setDestinationSpace(destinationSpace);

        return listgroup;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.data.IFactory#createLoadingSession(java.lang.String)
     */
    public LoadingSession createLoadingSession(final String sessionPrefsDoc)
    {
        if (!dab.exists(sessionPrefsDoc)) {
            logger.error("createLoadingSession: page " + sessionPrefsDoc + " does not exist");
            return null;
        }
        if (!dab.exists(sessionPrefsDoc, XWikiPersistence.CLASS_LOADING_SESSION)) {
            logger.error("createLoadingSession: Page " + sessionPrefsDoc + " does not contain an object of class "
                + XWikiPersistence.CLASS_LOADING_SESSION);
            return null;
        }

        // Retrieve connection properties from prefs
        String className = XWikiPersistence.CLASS_LOADING_SESSION;
        ObjectEntity sessionObject = dab.getObjectEntity(sessionPrefsDoc, className);

        return createLoadingSession(sessionObject);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.data.IFactory#createLoadingSession(java.lang.String)
     */
    @Override
    public LoadingSession createLoadingSession(final BaseObject xObject)
    {
        // Retrieve connection properties from prefs
        ObjectEntity sessionObject = dab.getObjectEntity(xObject);

        return createLoadingSession(sessionObject);
    }

    /**
     * Creates a LoadingSession from an ObjectEntity.
     * 
     * @param sessionObject
     * @return
     */
    private LoadingSession createLoadingSession(ObjectEntity sessionObject)
    {
        logger.debug("Parsing LoadingClass XObject " + sessionObject);
        LoadingSession session = null;

        try {
            session = null;
            final String id = (String) sessionObject.getFieldValue("id");
            session = new LoadingSession(id);

            if (sessionObject.getFieldValue("debugMode") != null
                && (Integer) sessionObject.getFieldValue("debugMode") == 1) {
                session = session.debugMode();
            }
            if (sessionObject.getFieldValue("simulationMode") != null
                && (Integer) sessionObject.getFieldValue("simulationMode") == 1) {
                session = session.simulationMode();
            }
            if (sessionObject.getFieldValue("loadAll") != null && (Integer) sessionObject.getFieldValue("loadAll") == 1) {
                session = session.loadAll();
            }
            if (sessionObject.getFieldValue("recentMails") != null
                && (Integer) sessionObject.getFieldValue("recentMails") == 1) {
                session = session.recentMails();
            }
            if (sessionObject.getFieldValue("withDelete") != null
                && (Integer) sessionObject.getFieldValue("withDelete") == 1) {
                session = session.withDelete();
            }
            if (sessionObject.getFieldValue("maxMailsNb") != null) {
                session = session.setLimit(((Long) sessionObject.getFieldValue("maxMailsNb")).intValue());
            }
            List<String> servers = new ArrayList<String>();
            if (sessionObject.getFieldValue("servers") != null) {
                servers = (List<String>) sessionObject.getFieldValue("servers");
            }
            List<String> stores = new ArrayList<String>();
            if (sessionObject.getFieldValue("stores") != null) {
                stores = (List<String>) sessionObject.getFieldValue("stores");
            }

            for (String serverId : servers) {
                session = session.addServer(serverId);
            }
            for (String storeId : stores) {
                session = session.addStore(storeId);
            }
        } catch (Throwable e) {
            logger.error("Could not load LoadingSession", e);
            return null;
        }

        logger.debug("Parsed XObject into LoadingSession " + session);

        return session;
    }

}
