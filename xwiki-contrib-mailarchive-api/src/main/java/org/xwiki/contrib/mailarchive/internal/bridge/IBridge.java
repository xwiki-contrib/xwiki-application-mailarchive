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
package org.xwiki.contrib.mailarchive.internal.bridge;

import java.util.HashMap;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiException;

/**
 * A component to bridge access to old core services.
 * 
 * @version $Id$
 */
@ComponentRole
public interface IBridge
{
    // FIXME : Missing methods should be added to DocumentAccessBridge instead.

    boolean existsDoc(String docname);

    boolean existsObject(String docname, String classname) throws XWikiException;

    String getStringValue(String docname, String classname, String fieldname) throws XWikiException;

    int getIntValue(String docname, String classname, String fieldname) throws XWikiException;

    boolean getBooleanValue(String docname, String classname, String fieldname) throws XWikiException;

    boolean createDocObject(String docname, String title, String classname, HashMap<String, Object> fields, String user);

    boolean updateDocObject(String docname, String title, String classname, HashMap<String, Object> fields, String user);
}
