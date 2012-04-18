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
package org.xwiki.component.mailarchive.internal;

import java.util.List;

import org.slf4j.Logger;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
public class MailUtils
{

    private QueryManager queryManager;

    private Logger logger;

    private XWikiContext context;

    private XWiki xwiki;

    public MailUtils(XWiki xwiki, XWikiContext context, Logger logger, QueryManager queryManager)
    {
        this.xwiki = xwiki;
        this.context = context;
        this.logger = logger;
        this.queryManager = queryManager;
    }

    /**
     * Parses an ID-type mail header, and extracts address part. An ID mail header usually is of the form:<br/>
     * <user.address@host.com>
     * 
     * @param id an address mail header value
     * @return extracted address, or id itself if could not extract an address part
     */
    public static String extractAddress(String id)
    {
        int start = id.indexOf('<');
        int end = id.indexOf('>');
        if (start != -1 && end != -1) {
            return id.substring(start + 1, end);
        } else {
            return id;
        }
    }

    /**
     * parseUser Parses a user string of the form "user <usermail@com>" - extract mail and if matched in xwiki user
     * profiles, returns page name for this profile - returns null string if no match is found - tries to return profile
     * of a user that's authenticated from LDAP, if any, or else first profile found
     */
    public String parseUser(String user, boolean isMatchProfiles, String loadingUser)
    {
        if (isMatchProfiles) {
            String parsedUser = null;
            int start = user.indexOf('<');
            int end = user.indexOf('>');
            String mail = "";
            if (start != -1 && end != -1) {
                mail = user.substring(start + 1, end).toLowerCase();
            }
            if (!"".equals(mail)) {
                // to match "-external" emails and old mails with '@gemplus.com'...
                mail = mail.toLowerCase();
                mail = mail.replace("-external", "").replaceAll("^(.*)@.*[.]com$", "$1%@%.com");
                // Try to find a wiki profile with this email as parameter.
                // TBD : do this in the loading phase, and only try to search db if it was not found ?
                String hql =
                    "select obj.name from BaseObject as obj, StringProperty as prop where obj.className='XWiki.XWikiUsers' and obj.id=prop.id and prop.name='email' and LOWER(prop.value) like '"
                        + mail + "'";

                List<String> wikiuser = null;
                try {
                    wikiuser = this.queryManager.createQuery(hql, Query.HQL).execute();
                } catch (QueryException e) {
                    wikiuser = null;
                }
                if (wikiuser == null || wikiuser.size() == 0) {
                    return null;
                } else {
                    // If there exists one, we prefer the user that's been authenticated through LDAP
                    for (String usr : wikiuser) {
                        try {
                            if (xwiki.getDocument(usr, context).getObject("XWiki.LDAPProfileClass") != null) {
                                parsedUser = usr;
                            }
                        } catch (XWikiException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                if (parsedUser != null) {
                    return parsedUser;
                } else {
                    // If none has authenticated from LDAP, we return the first user found
                    return wikiuser.get(0);
                }
            } else {
                return null;
            }
        } else {
            return loadingUser;
        }

    }

}
