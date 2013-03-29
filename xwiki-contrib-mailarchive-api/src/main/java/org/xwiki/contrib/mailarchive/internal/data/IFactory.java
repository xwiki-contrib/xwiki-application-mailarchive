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

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.contrib.mailarchive.IMailMatcher;
import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.LoadingSession;

/**
 * @version $Id$
 */
@Role
public interface IFactory
{

    /**
     * Creates a Server from preferences document.
     * 
     * @param serverPrefsDoc wiki page name of preferences document.
     * @return a IServer object, or null if preferences document does not exist
     */
    // FIXME: factory should not return concrete implementations
    Server createMailServer(String serverPrefsDoc);

    /**
     * Creates a MailStore from preferences document.
     * 
     * @param storePrefsDoc
     * @return
     */
    // FIXME: factory should not return concrete implementations
    MailStore createMailStore(String storePrefsDoc);

    /**
     * Creates a LoadingSession from preferences document.
     * 
     * @param sessionPrefsDoc
     * @param mailArchive
     * @return
     */
    LoadingSession createLoadingSession(String sessionPrefsDoc, IMailArchive mailArchive);

    /**
     * Creates a IType from name, icon and patterns list
     * 
     * @param id
     * @param name
     * @param icon
     * @return a IType object
     */
    IType createMailType(String id, String name, String icon);

    /**
     * Creates a IMailMatcher.
     * 
     * @param fields
     * @param expression
     * @param isAdvanced
     * @param isIgnoreCase
     * @param isMultiLine
     * @return
     */
    IMailMatcher createMailMatcher(String fields, String expression, Integer isAdvanced, Integer isIgnoreCase,
        Integer isMultiLine);

    /**
     * @param pattern
     * @param displayName
     * @param tag
     * @param color
     * @return
     */
    IMailingList createMailingList(String pattern, String displayName, String tag, String color);

}
