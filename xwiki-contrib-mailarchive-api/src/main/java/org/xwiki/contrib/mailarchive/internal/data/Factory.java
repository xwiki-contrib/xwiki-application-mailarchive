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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mailarchive.IMailMatcher;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IServer;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.internal.persistence.XWikiPersistence;

/**
 * @version $Id$
 */
@Component
@Singleton
public class Factory implements IFactory
{
    @Inject
    private DocumentAccessBridge dab;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.data.IFactory#createMailServer(java.lang.String)
     */
    // @SuppressWarnings("deprecation")
    @Override
    public Server createMailServer(final String serverPrefsDoc)
    {
        if (!dab.exists(serverPrefsDoc)) {
            return null;
        }
        Server server = new Server();

        // Retrieve connection properties from prefs
        String className = XWikiPersistence.CLASS_MAIL_SERVERS;
        server.setId((String) dab.getProperty(serverPrefsDoc, className, "id"));
        server.setHost((String) dab.getProperty(serverPrefsDoc, className, "hostname"));
        try {
            server.setPort(Integer.parseInt((String) dab.getProperty(serverPrefsDoc, className, "port")));
        } catch (NumberFormatException e1) {
            server.setPort(IServer.DEFAULT_PORT);
        }
        server.setProtocol((String) dab.getProperty(serverPrefsDoc, className, "protocol"));
        server.setUser((String) dab.getProperty(serverPrefsDoc, className, "user"));
        server.setPassword((String) dab.getProperty(serverPrefsDoc, className, "password"));
        server.setFolder((String) dab.getProperty(serverPrefsDoc, className, "folder"));
        String additionalProperties = (String) dab.getProperty(serverPrefsDoc, className, "additionalProperties");
        InputStream is = new ByteArrayInputStream(additionalProperties.getBytes());
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            // TODO ?
        }
        server.setAdditionalProperties(props);
        server.setWikiDoc(serverPrefsDoc);

        if (server.getId() == null || server.getHost() == null || server.getProtocol() == null) {
            return null;
        } else {
            return server;
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

        /*
         * String[] splittedPatterns = patternsList.replaceAll("(?m)^\\s+$", "").split("\n", -1); int nbPatterns =
         * splittedPatterns.length % 2; for (int i = 0; i < nbPatterns; i += 2) { List<String> fields =
         * Arrays.asList(splittedPatterns[i].split(",", 0)); // Trim white-space from fields for (int j = 0; j <
         * fields.size(); j++) { fields.set(j, fields.get(j).trim()); } String pattern = splittedPatterns[i + 1].trim();
         * typeobj.addMatcher(fields, pattern); }
         */

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
}
