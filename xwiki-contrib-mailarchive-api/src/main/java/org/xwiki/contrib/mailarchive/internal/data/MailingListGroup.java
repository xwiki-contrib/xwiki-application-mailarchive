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

import java.util.List;

import org.xwiki.contrib.mailarchive.IMailingList;
import org.xwiki.contrib.mailarchive.IMailingListGroup;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * @version $Id$
 */
public class MailingListGroup implements IMailingListGroup
{
    private String name;

    private List<IMailingList> mailingLists;

    private String destinationWiki;

    private String destinationSpace;

    private String loadingUser;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailingListGroup#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailingListGroup#getMailingLists()
     */
    @Override
    public List<IMailingList> getMailingLists()
    {
        return mailingLists;
    }

    /**
     * @param mailingLists the mailingLists to set
     */
    public void setMailingLists(List<IMailingList> mailingLists)
    {
        this.mailingLists = mailingLists;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailingListGroup#getDestinationWiki()
     */
    @Override
    public String getDestinationWiki()
    {
        return destinationWiki;
    }

    /**
     * @param destinationWiki the destinationWiki to set
     */
    public void setDestinationWiki(String destinationWiki)
    {
        this.destinationWiki = destinationWiki;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailingListGroup#getDestinationSpace()
     */
    @Override
    public String getDestinationSpace()
    {
        return destinationSpace;
    }

    /**
     * @param destinationSpace the destinationSpace to set
     */
    public void setDestinationSpace(String destinationSpace)
    {
        this.destinationSpace = destinationSpace;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailingListGroup#getLoadingUser()
     */
    @Override
    public String getLoadingUser()
    {
        return loadingUser;
    }

    /**
     * @param loadingUser the loadingUser to set
     */
    public void setLoadingUser(String loadingUser)
    {
        this.loadingUser = loadingUser;
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
        builder.append("name", name);
        builder.append("mailingLists", mailingLists);
        builder.append("destinationWiki", destinationWiki);
        builder.append("destinationSpace", destinationSpace);
        builder.append("loadingUser", loadingUser);
        return builder.toString();
    }

}
