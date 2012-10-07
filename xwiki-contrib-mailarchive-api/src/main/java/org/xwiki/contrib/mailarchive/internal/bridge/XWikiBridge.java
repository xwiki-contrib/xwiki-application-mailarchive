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

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id$
 */
@Component
@Singleton
public class XWikiBridge implements IBridge, Initializable
{
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

    @Override
    public boolean existsDoc(String docname)
    {
        return xwiki.exists(docname, context);
    }

    public boolean existsObject(String docname, String classname) throws XWikiException
    {
        return xwiki.getDocument(docname, context).getObject(classname) != null;
    }

    @Override
    public String getStringValue(String docname, String classname, String fieldname) throws XWikiException
    {
        XWikiDocument prefsdoc = xwiki.getDocument(docname, context);
        BaseObject prefsobj = prefsdoc.getObject(classname);
        return prefsobj.getStringValue(fieldname);
    }

    @Override
    public int getIntValue(String docname, String classname, String fieldname) throws XWikiException
    {
        XWikiDocument prefsdoc = xwiki.getDocument(docname, context);
        BaseObject prefsobj = prefsdoc.getObject(classname);
        return prefsobj.getIntValue(fieldname);
    }

    @Override
    public boolean getBooleanValue(String docname, String classname, String fieldname) throws XWikiException
    {
        return getIntValue(docname, classname, fieldname) != 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IBridge#createDocObject(java.lang.String, java.lang.String,
     *      java.lang.String, java.util.HashMap, java.lang.String)
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
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IBridge#updateDocObject(java.lang.String, java.lang.String,
     *      java.lang.String, java.util.HashMap, java.lang.String)
     */
    @Override
    public boolean updateDocObject(String docname, String title, String classname, HashMap<String, Object> fields,
        String user)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
