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
package org.xwiki.contrib.mail.internal.source;

import java.util.Properties;

import org.xwiki.contrib.mail.source.IMailSource;
import org.xwiki.contrib.mail.source.IStoreSource;

/**
 * @version $Id$
 */
public class StoreSource implements IStoreSource
{
    private String format;

    private String location;

    private Properties additionalProperties;

    /**
     * @param format
     * @param location
     */
    public StoreSource(final String format, final String location)
    {
        super();
        this.format = format;
        this.location = location;
    }

    /**
     * @return the provider
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * @param format the provider to set
     */
    public void setFormat(final String format)
    {
        this.format = format;
    }

    /**
     * @return the location
     */
    public String getLocation()
    {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(final String location)
    {
        this.location = location;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.source.IMailSource#getAdditionalProperties()
     */
    @Override
    public Properties getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    /**
     * @param additionalProperties the additionalProperties to set
     */
    public void setAdditionalProperties(final Properties additionalProperties)
    {
        this.additionalProperties = additionalProperties;
    }

}
