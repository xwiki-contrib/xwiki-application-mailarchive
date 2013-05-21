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

import java.util.Properties;

/**
 * Defines some characteristics common to a mail source configuration.
 */
public interface IMASource
{
    /**
     * The id of this server connection.
     * 
     * @return
     */
    String getId();

    /**
     * The type of mail source, ie "SERVER" or "STORE".
     * 
     * @return
     */
    SourceType getType();

    /**
     * The folder to load emails from.
     * 
     * @return
     */
    String getFolder();

    /**
     * Additional properties to pass to the underlying javamail session.
     * 
     * @return
     */
    Properties getAdditionalProperties();

    /**
     * The preferences xwiki document holding this store connection parameters.
     * 
     * @return
     */
    String getWikiDoc();

    /**
     * If true, this source is "enabled", meaning it's taken into account by scheduled job.
     * 
     * @return
     */
    boolean isEnabled();

    /**
     * @return
     */
    int getState();

    enum SourceType
    {
        SERVER,
        STORE;
    }

}
