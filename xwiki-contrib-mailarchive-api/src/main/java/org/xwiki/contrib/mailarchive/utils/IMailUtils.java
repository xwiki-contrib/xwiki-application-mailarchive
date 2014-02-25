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
package org.xwiki.contrib.mailarchive.utils;

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
     * Parses an ID-type mail header, and extracts address part. An ID mail header usually is of the form:<br/>
     * User Name <user.address@host.com>
     * 
     * @param id an address mail header value
     * @return extracted address, or id itself if could not extract an address part
     */
    String extractAddress(String id);
    
    /**
     * parseUser Parses a user string of the form "user <usermail@com>" - extract mail and if matched in xwiki user
     * profiles, returns page name for this profile - returns null string if no match is found - tries to return profile
     * of a user that's authenticated from LDAP, if any, or else first profile found
     */
    IMAUser parseUser(String user, boolean isMatchLdap);

    /**
     * Decodes an email body. 
     * This methods decodes html part, returns both html and text part, optionally "cut" to remove history sections from content.
     * 
     * @param originalHtml The encoded HTML part of body.
     * @param originalBody The plain text part of body.
     * @param cut To remove history
     * @return A DecodedMailContent with HTML part decoded.
     * @throws IOException If HTML content can't be decoded.
     */
    DecodedMailContent decodeMailContent(String originalHtml, String originalBody, boolean cut) throws IOException;

    /**
     * Filters the types that match provided email.
     * 
     * @param types The types to match against email fields.
     * @param mailItem The email.
     * @return A list of types matching the types/filters, or an empty list if none matches.
     */
    List<IType> extractTypes(Collection<IType> types, MailItem mailItem);

}
