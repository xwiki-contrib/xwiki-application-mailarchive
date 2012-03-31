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
package org.xwiki.component.mailarchive.internal.data;

import java.util.HashMap;
import java.util.List;

import org.xwiki.component.mailarchive.MailType;

/**
 * Mail Type
 * 
 * @version $Id$
 */
public class MailTypeImpl implements MailType
{
    private String name;

    private String icon;

    private HashMap<List<String>, String> patterns;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public HashMap<List<String>, String> getPatterns()
    {
        return patterns;
    }

    public void setPatterns(HashMap<List<String>, String> patterns)
    {
        this.patterns = patterns;
    }

    public void addPattern(List<String> fields, String expr)
    {
        this.patterns.put(fields, expr);
    }
}
