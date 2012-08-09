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
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.contrib.mailarchive.IServer;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.internal.DefaultMailArchive;

/**
 * @version $Id$
 */
public class Factory
{
    private DocumentAccessBridge dab;

    public Factory(final DocumentAccessBridge dab)
    {
        this.dab = dab;
    }

    /**
     * Creates a IServer from preferences document.
     * 
     * @param serverPrefsDoc wiki page name of preferences document.
     * @return a IServer object, or null if preferences document does not exist
     */
    public Server createMailServer(final String serverPrefsDoc)
    {
        if (!dab.exists(serverPrefsDoc)) {
            return null;
        }
        Server server = new Server();

        // Retrieve connection properties from prefs
        String className = DefaultMailArchive.SPACE_CODE + ".ServerSettingsClass";
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
     * Creates a IType from name, icon and patterns list
     * 
     * @param name
     * @param icon
     * @param patternsList a list of carriage-return separated patterns : each pattern occupies 2 lines, first line
     *            being the fields to match against (comma separated), second line is the regular expression to match
     * @return a IType object or null if patternsList could not be parsed
     */
    public IType createMailType(final String name, final String displayName, final String icon,
        final String patternsList)
    {

        Type typeobj = new Type();

        typeobj.setName(name);
        typeobj.setDisplayName(displayName);
        typeobj.setIcon(icon);

        HashMap<List<String>, String> patterns = new HashMap<List<String>, String>();
        String[] splittedPatterns = patternsList.replaceAll("(?m)^\\s+$", "").split("\n", -1);
        int nbPatterns = splittedPatterns.length % 2;

        for (int i = 0; i < nbPatterns; i += 2) {
            List<String> fields = Arrays.asList(splittedPatterns[i].split(",", 0));
            // Trim white-space from fields
            for (int j = 0; j < fields.size(); j++) {
                fields.set(j, fields.get(j).trim());
            }
            patterns.put(fields, splittedPatterns[i + 1]);
        }
        typeobj.setPatterns(patterns);

        return typeobj;
    }

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
