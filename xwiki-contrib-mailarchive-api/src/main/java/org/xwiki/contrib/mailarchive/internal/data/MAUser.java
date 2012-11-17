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

import org.xwiki.contrib.mailarchive.IMAUser;

/**
 * @version $Id$
 */
public class MAUser implements IMAUser
{

    private String originalAddress = null;

    private String address = null;

    private String displayName = null;

    private String wikiProfile = null;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMAUser#getOriginalAddress()
     */
    @Override
    public String getOriginalAddress()
    {
        return this.originalAddress;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMAUser#getAddress()
     */
    @Override
    public String getAddress()
    {
        return this.address;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMAUser#getDisplayName()
     */
    @Override
    public String getDisplayName()
    {
        return this.displayName;
    }

    /**
     * @param originalAddress the originalAddress to set
     */
    public void setOriginalAddress(final String originalAddress)
    {
        this.originalAddress = originalAddress;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(final String address)
    {
        this.address = address;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(final String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * @return the wikiProfile
     */
    public String getWikiProfile()
    {
        return wikiProfile;
    }

    /**
     * @param wikiProfile the wikiProfile to set
     */
    public void setWikiProfile(final String wikiProfile)
    {
        this.wikiProfile = wikiProfile;
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
        builder.append("MAUser [originalAddress=").append(originalAddress).append(", address=").append(address)
            .append(", displayName=").append(displayName).append(", wikiProfile=").append(wikiProfile).append("]");
        return builder.toString();
    }

}
