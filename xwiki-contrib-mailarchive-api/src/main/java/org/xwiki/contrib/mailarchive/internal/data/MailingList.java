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

import org.xwiki.contrib.mailarchive.IMailingList;

/**
 * @version $Id$
 */
public class MailingList implements IMailingList
{

    private String displayName;

    private String pattern;

    private String tag;

    private String color;

    /**
     * @return the displayName
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * @return the pattern
     */
    public String getPattern()
    {
        return pattern;
    }

    /**
     * @param pattern the pattern to set
     */
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    /**
     * @return the tag
     */
    public String getTag()
    {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    }

    /**
     * @return the color
     */
    public String getColor()
    {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(String color)
    {
        this.color = color;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MailingListImpl [displayName=").append(displayName).append(", pattern=").append(pattern)
            .append(", tag=").append(tag).append(", color=").append(color).append("]");
        return builder.toString();
    }

}
