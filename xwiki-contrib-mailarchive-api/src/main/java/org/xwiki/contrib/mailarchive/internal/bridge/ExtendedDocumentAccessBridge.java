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

import javax.inject.Inject;
import javax.inject.Named;
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
import com.xpn.xwiki.api.PropertyClass;
import com.xpn.xwiki.doc.DefaultDocumentAccessBridge;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * XWiki implementation of IExtendedDocumentAccessBridge.
 * 
 * @version $Id$
 */
@Component
@Named("extended")
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

    public boolean createOrUpdate(final DocumentEntity document)
    {
        return false;
    }

    public ObjectEntity getObject(final String xdocument, final String xclass)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#getObjectEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public ObjectEntity getObjectEntity(String xdocument, String xclass)
    {
        ObjectEntity objectEntity = null;
        try {
            XWikiDocument document = xwiki.getDocument(xdocument, context);
            if (document != null && exists(xdocument)) {
                BaseObject baseObject = document.getObject(xclass);
                if (baseObject != null) {
                    objectEntity = new ObjectEntity();
                    objectEntity.setXclass(xclass);
                    objectEntity.setXdoc(xdocument);
                    for (String propname : baseObject.getPropertyList()) {

                        PropertyClass propclass = (PropertyClass) baseObject.getProperties()[0];
                        String proptype = propclass.getType();
                        if ("StringProperty".equals(proptype)) {
                            objectEntity.setFieldValue(propname, baseObject.getStringValue(propname));
                        }
                    }

                } else {
                    logger.info("Object does not exist: " + xclass + " from " + xdocument);
                }
            } else {
                logger.info("Document does not exist: " + xdocument);
            }
        } catch (XWikiException e) {
            logger.error("Could not retrieve Object", e);
        }
        return objectEntity;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#getDocumentEntity(java.lang.String)
     */
    @Override
    public DocumentEntity getDocumentEntity(String xdocument)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#getDocumentEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public DocumentEntity getDocumentEntity(String space, String page)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.bridge.IExtendedDocumentAccessBridge#getDocumentEntity(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public DocumentEntity getDocumentEntity(String wiki, String space, String page)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
