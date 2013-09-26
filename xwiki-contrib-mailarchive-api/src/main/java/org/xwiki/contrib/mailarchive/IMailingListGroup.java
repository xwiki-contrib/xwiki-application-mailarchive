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
package org.xwiki.contrib.mailarchive;

import java.util.List;

/**
 * A group of mailing-lists.
 * 
 * @author jbousque
 * @version $Id$
 */
public interface IMailingListGroup
{

    /**
     * The name of this mailing-list group.
     * 
     * @return the name
     */
    public abstract String getName();

    /**
     * The mailing-lists belonging to this group.
     * 
     * @return the mailingLists
     */
    public abstract List<IMailingList> getMailingLists();

    /**
     * Optionally, The ID of the destination wiki where to create pages for emails belonging to this mailing-list group.
     * 
     * @return the destinationWiki
     */
    public abstract String getDestinationWiki();

    /**
     * Optionally, the name of a Space where to create pages for emails belonging to this mailing-list group.
     * 
     * @return the destinationSpace
     */
    public abstract String getDestinationSpace();

    /**
     * Optionally, a specific loading user for this mailing-list group.
     * 
     * @return the loadingUser
     */
    public abstract String getLoadingUser();

}
