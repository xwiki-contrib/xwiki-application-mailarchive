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
package org.xwiki.contrib.mailarchive.xwiki.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.mailarchive.xwiki.DocumentEntity;
import org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge;
import org.xwiki.contrib.mailarchive.xwiki.ObjectEntity;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.DefaultDocumentAccessBridge;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

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
     * @see org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge#exists(java.lang.String)
     */
    @Override
    public boolean exists(String docname)
    {
        return xwiki.exists(docname, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge#exists(java.lang.String,
     *      java.lang.String)
     */
    public boolean exists(String docname, String classname)
    {
        try {
            return xwiki.getDocument(docname, context).getObject(classname) != null;
        } catch (XWikiException e) {
            logger.info("Document " + docname + " does not exist");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge#getValidUniqueName(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String getValidUniqueName(String pagename, String space)
    {
        String wikiname = context.getWiki().clearName(pagename, context);
        if (wikiname.length() >= MAX_PAGENAME_LENGTH) {
            wikiname = wikiname.substring(0, MAX_PAGENAME_LENGTH);
        }

        return xwiki.getUniquePageName(space, wikiname, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge#getStringValue(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public String getStringValue(String docname, String classname, String fieldname)
    {
        try {
            XWikiDocument prefsdoc = xwiki.getDocument(docname, context);
            BaseObject prefsobj = prefsdoc.getObject(classname);
            return prefsobj.getStringValue(fieldname);
        } catch (XWikiException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge#getIntValue(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public int getIntValue(String docname, String classname, String fieldname)
    {
        try {
            XWikiDocument prefsdoc = xwiki.getDocument(docname, context);
            BaseObject prefsobj = prefsdoc.getObject(classname);
            return prefsobj.getIntValue(fieldname);
        } catch (XWikiException e) {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge#getBooleanValue(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean getBooleanValue(String docname, String classname, String fieldname)
    {
        return getIntValue(docname, classname, fieldname) > 0;
    }

    public boolean createOrUpdate(final DocumentEntity document)
    {
        return false;
    }

    @Override
    public ObjectEntity getObjectEntity(final String xdocument, final String xclass)
    {
        try {
            XWikiDocument document = xwiki.getDocument(xdocument, context);
            BaseObject baseObject = document.getObject(xclass);
            ObjectEntity objectEntity = new ObjectEntity();
            objectEntity.setXdoc(xdocument);
            objectEntity.setXclass(xclass);

            for (String name : baseObject.getXClass(context).getPropertyList()) {
                BaseProperty property = (BaseProperty) baseObject.get(name);
                objectEntity.setFieldValue(property.getName(), property.getValue());
            }
            return objectEntity;
        } catch (XWikiException e) {
            logger.error("Could not parse XObject[" + xdocument + ',' + xclass + "]");
        }
        return null;
    }

    @Override
    public ObjectEntity getObjectEntity(final BaseObject xObject)
    {
        try {
            ObjectEntity objectEntity = new ObjectEntity();
            objectEntity.setXdoc(xObject.getName());
            objectEntity.setXclass(xObject.getXClassReference().toString());

            for (String name : xObject.getXClass(context).getPropertyList()) {
                BaseProperty property = (BaseProperty) xObject.get(name);
                if (property != null) {
                    objectEntity.setFieldValue(property.getName(), property.getValue());
                } else {
                    objectEntity.setFieldValue(name, null);
                }
            }
            return objectEntity;
        } catch (XWikiException e) {
            logger.error("Could not parse XObject[" + xObject + "]");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge#getDocumentEntity(java.lang.String)
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
     * @see org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge#getDocumentEntity(java.lang.String,
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
     * @see org.xwiki.contrib.mailarchive.xwiki.IExtendedDocumentAccessBridge#getDocumentEntity(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public DocumentEntity getDocumentEntity(String wiki, String space, String page)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
