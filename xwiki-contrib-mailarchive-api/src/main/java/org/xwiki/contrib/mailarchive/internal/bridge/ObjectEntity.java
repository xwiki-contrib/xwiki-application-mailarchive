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

/**
 * @version $Id$
 */
public class ObjectEntity
{
    private HashMap<String, Object> fields = new HashMap<String, Object>();

    private String xdoc;

    private String xclass;

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

    public void setFieldValue(final String key, final Object value)
    {
        this.fields.put(key, value);
    }

    public Object getFieldValue(final String key)
    {
        return this.fields.get(key);
    }

}
