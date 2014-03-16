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

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.mailarchive.internal.LoadingJob;

/**
 * A component dedicated to load emails into the archive, given some configuration provided by a LoadingSession, and
 * optionally using a specific LoadingJob.
 * 
 * @author jbousque
 * @version $Id$
 */
@Role
public interface IMailArchiveLoader
{
    int loadMails(LoadingSession session);

    int loadMails(LoadingSession session, LoadingJob job);

}
