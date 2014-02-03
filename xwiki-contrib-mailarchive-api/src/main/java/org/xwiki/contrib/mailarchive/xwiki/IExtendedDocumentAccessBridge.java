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
package org.xwiki.contrib.mailarchive.xwiki;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Role;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

/**
 * A component to bridge access to old core services.
 * 
 * @version $Id$
 */
@Role
public interface IExtendedDocumentAccessBridge extends DocumentAccessBridge
{
    // FIXME : Missing methods should be added to DocumentAccessBridge instead.

    /**
     * Checks for document existence.
     * 
     * @param docname
     * @return
     */
    boolean exists(String docname);

    /**
     * Check for object existence of certain type in a document.
     * 
     * @param docname The document
     * @param classname The object type
     * @return
     * @throws XWikiException
     */
    boolean exists(String docname, String classname);

    /**
     * Generates a unique and valid name for a document in a space.
     * 
     * @param pagename
     * @param space
     * @return
     */
    String getValidUniqueName(String pagename, String space);

    /**
     * Gets a string property from a document / object of certain type / field.
     * 
     * @param docname
     * @param classname
     * @param fieldname
     * @return
     * @throws XWikiException
     */
    String getStringValue(String docname, String classname, String fieldname);

    /**
     * Gets an int value from a document / object of certain type / field.
     * 
     * @param docname
     * @param classname
     * @param fieldname
     * @return
     * @throws XWikiException
     */
    int getIntValue(String docname, String classname, String fieldname);

    /**
     * Gets a boolean value from a document / object of certain type / field.
     * 
     * @param docname
     * @param classname
     * @param fieldname
     * @return
     * @throws XWikiException
     */
    boolean getBooleanValue(String docname, String classname, String fieldname);

    boolean createOrUpdate(DocumentEntity document);

    ObjectEntity getObjectEntity(final String xdocument, final String xclass);

    DocumentEntity getDocumentEntity(final String xdocument);

    DocumentEntity getDocumentEntity(final String space, final String page);

    DocumentEntity getDocumentEntity(final String wiki, final String space, final String page);

    public abstract ObjectEntity getObjectEntity(final BaseObject xObject);
}
