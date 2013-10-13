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

import java.util.ArrayList;
import java.util.List;

import org.xwiki.contrib.mailarchive.IMailMatcher;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * A mail type.
 * 
 * @version $Id$
 */
public class Type implements IType
{
    private String id;

    private String name;

    private String icon;

    private List<IMailMatcher> matchers;

    public Type()
    {
        super();
        this.matchers = new ArrayList<IMailMatcher>();
    }

    /**
     * @param id
     * @param name
     * @param icon
     */
    public Type(String id, String name, String icon)
    {
        this();
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IType#getId()
     */
    public String getId()
    {
        return id;
    }

    public void setId(String name)
    {
        this.id = name;
    }

    public void setName(String displayName)
    {
        this.name = displayName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IType#getName()
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public List<IMailMatcher> getMatchers()
    {
        return matchers;
    }

    public void setMatchers(List<IMailMatcher> matchers)
    {
        this.matchers = matchers;
    }

    public void addMatcher(final List<String> fields, final String pattern, final boolean advancedMode,
        final boolean ignoreCase, final boolean multiLine)
    {
        if (this.matchers == null) {
            this.matchers = new ArrayList<IMailMatcher>();
        }
        final MailMatcher matcher = new MailMatcher(fields, pattern);
        matcher.setAdvancedMode(advancedMode);
        matcher.setIgnoreCase(ignoreCase);
        matcher.setMultiLine(multiLine);
        this.matchers.add(matcher);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        XWikiToStringBuilder builder = new XWikiToStringBuilder(this);
        builder.append("id", id);
        builder.append("name", name);
        builder.append("icon", icon);
        builder.append("matchers", matchers);
        return builder.toString();
    }

}
