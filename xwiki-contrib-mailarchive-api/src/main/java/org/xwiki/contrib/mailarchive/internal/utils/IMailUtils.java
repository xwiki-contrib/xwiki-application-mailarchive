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
package org.xwiki.contrib.mailarchive.internal.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mailarchive.IMAUser;
import org.xwiki.contrib.mailarchive.IType;

import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
@Role
public interface IMailUtils
{

    /**
     * parseUser Parses a user string of the form "user <usermail@com>" - extract mail and if matched in xwiki user
     * profiles, returns page name for this profile - returns null string if no match is found - tries to return profile
     * of a user that's authenticated from LDAP, if any, or else first profile found
     */
    IMAUser parseUser(String user, boolean isMatchLdap);

    /**
     * @param mailPage
     * @param cut
     * @return
     * @throws IOException
     * @throws XWikiException
     */
    String decodeMailContent(String originalHtml, String originalBody, boolean cut) throws IOException, XWikiException;

    /**
     * @param types
     * @param m
     * @return
     */
    List<IType> extractTypes(Collection<IType> types, MailItem m);

}
