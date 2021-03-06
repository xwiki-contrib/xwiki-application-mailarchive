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

import org.xwiki.contrib.mail.source.IStoreSource;
import org.xwiki.contrib.mail.source.SourceType;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * @version $Id$
 */
public class MailStore extends AbstractMASource implements IStoreSource
{
    private String format;

    private String location;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IStore#getFormat()
     */
    @Override
    public String getFormat()
    {
        return this.format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IStore#getLocation()
     */
    @Override
    public String getLocation()
    {
        return this.location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMASource#getType()
     */
    @Override
    public SourceType getType()
    {
        return SourceType.STORE;
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
        builder.append("format", format);
        builder.append("location", location);
        builder.append("AbstractMASource", super.toString());
        return builder.toString();
    }

}
