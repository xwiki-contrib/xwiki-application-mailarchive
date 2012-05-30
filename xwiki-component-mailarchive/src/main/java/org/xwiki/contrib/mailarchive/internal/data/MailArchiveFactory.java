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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.contrib.mailarchive.MailType;
import org.xwiki.contrib.mailarchive.internal.DefaultMailArchive;

/**
 * @version $Id$
 */
public class MailArchiveFactory
{
    private DocumentAccessBridge dab;

    public MailArchiveFactory(final DocumentAccessBridge dab)
    {
        this.dab = dab;
    }

    /**
     * Creates a MailServer from preferences document.
     * 
     * @param serverPrefsDoc wiki page name of preferences document.
     * @return a MailServer object, or null if preferences document does not exist
     */
    public MailServerImpl createMailServer(final String serverPrefsDoc)
    {
        if (!dab.exists(serverPrefsDoc)) {
            return null;
        }
        MailServerImpl server = new MailServerImpl();

        // Retrieve connection properties from prefs
        String className = DefaultMailArchive.SPACE_CODE + ".ServerSettingsClass";
        server.setId((String) dab.getProperty(serverPrefsDoc, className, "id"));
        server.setHost((String) dab.getProperty(serverPrefsDoc, className, "hostname"));
        server.setPort(Integer.parseInt((String) dab.getProperty(serverPrefsDoc, className, "port")));
        server.setProtocol((String) dab.getProperty(serverPrefsDoc, className, "protocol"));
        server.setUser((String) dab.getProperty(serverPrefsDoc, className, "user"));
        server.setPassword((String) dab.getProperty(serverPrefsDoc, className, "password"));
        server.setFolder((String) dab.getProperty(serverPrefsDoc, className, "folder"));
        server.setWikiDoc(serverPrefsDoc);

        return server;
    }

    /**
     * Creates a MailType from name, icon and patterns list
     * 
     * @param name
     * @param icon
     * @param patternsList a list of carriage-return separated patterns : each pattern occupies 2 lines, first line
     *            being the fields to match against (comma separated), second line is the regular expression to match
     * @return a MailType object or null if patternsList could not be parsed
     */
    public MailType createMailType(final String name, final String displayName, final String icon,
        final String patternsList)
    {

        MailTypeImpl typeobj = new MailTypeImpl();

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
}
