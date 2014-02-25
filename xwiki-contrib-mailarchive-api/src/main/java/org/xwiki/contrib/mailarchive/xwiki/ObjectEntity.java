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

import java.util.HashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * A simple POJO to represent an XObject in memory with no link to XWiki API.
 * 
 * @version $Id$
 */
public class ObjectEntity
{
    private HashMap<String, Object> fields = new HashMap<String, Object>();
    
    private HashMap<String, Boolean> dirty = new HashMap<String, Boolean>();

    private String xdoc;

    private String xclass;
    
    public ObjectEntity(){
        
    }
    
    public ObjectEntity(final String xdoc, final String xclass) {
        this.xdoc = xdoc;
        this.xclass = xclass;
    }

    /**
     * @return the name
     */
    public String getXdoc()
    {
        return xdoc;
    }

    /**
     * @param name the name to set
     */
    public void setXdoc(final String xdoc)
    {
        this.xdoc = xdoc;
    }

    /**
     * @return the clazzName
     */
    public String getXclass()
    {
        return xclass;
    }

    /**
     * @param clazzName the clazzName to set
     */
    public void setXclass(final String xclass)
    {
        this.xclass = xclass;
    }

    /**
     * Sets or updates a field value in memory.
     * 
     * @param key
     * @param value
     */
    public void setFieldValue(final String key, final Object value)
    {
        this.fields.put(key, value);
        this.dirty.put(key, false);
    }

    /**
     * @param key
     * @return Value of field identified by key.
     */
    public Object getFieldValue(final String key)
    {
        return this.fields.get(key);
    }
    
    /**
     * Sets or updates a field value in order for it to be persisted.
     * 
     * @param key
     * @param value
     */
    public void updateFieldValue(final String key, final Object value)
    {
        this.fields.put(key, value);
        this.dirty.put(key, true);
    }
    

    public String toString()
    {
        ToStringBuilder builder = new XWikiToStringBuilder(this);
        builder =
            builder.append("xdoc", xdoc).append("xclass", xclass).append("fields", fields);

        return builder.toString();
    }
    
}
