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


/**
 * @version $Id$
 */
public class DocumentEntity
{
    private String space;

    private String name;

    private String parent;

    private String title;

    private HashMap<String, ObjectEntity> objects = new HashMap<String, ObjectEntity>();

    private HashMap<String, UpdateMode> objectsUpdateMode = new HashMap<String, UpdateMode>();

    public String getSpace()
    {
        return space;
    }

    public void setSpace(final String space)
    {
        this.space = space;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getFullName()
    {
        return space + '.' + name;
    }

    /**
     * @return the parent
     */
    public String getParent()
    {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(final String parent)
    {
        this.parent = parent;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    public void addObject(final String key, final ObjectEntity object, final UpdateMode updateMode)
    {
        this.objects.put(key, object);
        this.objectsUpdateMode.put(key, updateMode);
    }

    public void addObject(final String key, final ObjectEntity object)
    {
        addObject(key, object, UpdateMode.CREATE_OR_UPDATE);
    }

    public ObjectEntity getObject(final String key)
    {
        return this.objects.get(key);
    }

    public UpdateMode getUpdateMode(final String key)
    {
        return this.objectsUpdateMode.get(key);
    }

}
