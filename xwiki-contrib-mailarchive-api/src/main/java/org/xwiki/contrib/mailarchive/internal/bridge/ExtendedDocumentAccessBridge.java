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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.DefaultDocumentAccessBridge;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * XWiki implementation of IExtendedDocumentAccessBridge.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class ExtendedDocumentAccessBridge extends DefaultDocumentAccessBridge implements IExtendedDocumentAccessBridge,
    Initializable
{
    public static final int MAX_PAGENAME_LENGTH = 30;

    @Inject
    private Logger logger;

    @Inject
    private Execution execution;

    private XWiki xwiki;

    private XWikiContext context;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        ExecutionContext context = execution.getContext();
        this.context = (XWikiContext) context.getProperty("xwikicontext");
        this.xwiki = this.context.getWiki();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#exists(java.lang.String)
     */
    @Override
    public boolean exists(String docname)
    {
        return xwiki.exists(docname, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#exists(java.lang.String,
     *      java.lang.String)
     */
    public boolean exists(String docname, String classname) throws XWikiException
    {
        return xwiki.getDocument(docname, context).getObject(classname) != null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#getValidUniqueName(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String getValidUniqueName(String pagename, String space)
    {
        String wikiname = context.getWiki().clearName(pagename, context);
        if (wikiname.length() >= MAX_PAGENAME_LENGTH) {
            wikiname = wikiname.substring(0, MAX_PAGENAME_LENGTH);
        }
        String uniquePageName = context.getWiki().getUniquePageName(space, wikiname, context);

        return uniquePageName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#getStringValue(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public String getStringValue(String docname, String classname, String fieldname) throws XWikiException
    {
        XWikiDocument prefsdoc = xwiki.getDocument(docname, context);
        BaseObject prefsobj = prefsdoc.getObject(classname);
        return prefsobj.getStringValue(fieldname);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#getIntValue(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public int getIntValue(String docname, String classname, String fieldname) throws XWikiException
    {
        XWikiDocument prefsdoc = xwiki.getDocument(docname, context);
        BaseObject prefsobj = prefsdoc.getObject(classname);
        return prefsobj.getIntValue(fieldname);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#getBooleanValue(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean getBooleanValue(String docname, String classname, String fieldname) throws XWikiException
    {
        return getIntValue(docname, classname, fieldname) != 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#createDocObject(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.HashMap, java.lang.String)
     */
    @Override
    public boolean createDocObject(String docname, String title, String classname, HashMap<String, Object> fields,
        String user)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#updateDocObject(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.HashMap, java.lang.String)
     */
    @Override
    public boolean updateDocObject(String docname, String title, String classname, HashMap<String, Object> fields,
        String user)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
