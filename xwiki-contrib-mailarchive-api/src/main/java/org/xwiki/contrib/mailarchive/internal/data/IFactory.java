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

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IType;

/**
 * @version $Id$
 */
@ComponentRole
public interface IFactory
{

    /**
     * Creates a IServer from preferences document.
     * 
     * @param serverPrefsDoc wiki page name of preferences document.
     * @return a IServer object, or null if preferences document does not exist
     */
    Server createMailServer(String serverPrefsDoc);

    /**
     * Creates a IType from name, icon and patterns list
     * 
     * @param name
     * @param icon
     * @param patternsList a list of carriage-return separated patterns : each pattern occupies 2 lines, first line
     *            being the fields to match against (comma separated), second line is the regular expression to match
     * @return a IType object or null if patternsList could not be parsed
     */
    IType createMailType(String name, String displayName, String icon, String patternsList);

    IMailingList createMailingList(String pattern, String displayName, String tag, String color);

}
