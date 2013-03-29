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

import java.util.Properties;

import org.xwiki.contrib.mailarchive.IMASource;

/**
 * @version $Id$
 */
public abstract class AbstractMASource implements IMASource
{
    private String id;

    private String folder;

    private Properties additionalProperties;

    private String wikiDoc;

    private boolean enabled;

    private int state;

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the folder
     */
    public String getFolder()
    {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(String folder)
    {
        this.folder = folder;
    }

    /**
     * @return the additionalProperties
     */
    public Properties getAdditionalProperties()
    {
        return additionalProperties;
    }

    /**
     * @param additionalProperties the additionalProperties to set
     */
    public void setAdditionalProperties(Properties additionalProperties)
    {
        this.additionalProperties = additionalProperties;
    }

    /**
     * @return the wikiDoc
     */
    public String getWikiDoc()
    {
        return wikiDoc;
    }

    /**
     * @param wikiDoc the wikiDoc to set
     */
    public void setWikiDoc(String wikiDoc)
    {
        this.wikiDoc = wikiDoc;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * @return the state
     */
    public int getState()
    {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(final int state)
    {
        this.state = state;
    }

}
