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
package org.xwiki.contrib.mailarchive.internal;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
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
    @SuppressWarnings("deprecation")
    public String parseUser(String user, boolean isMatchLdap)
    {
        String mail = null;
        try {
            InternetAddress ia = InternetAddress.parse(user, true)[0];
            mail = ia.getAddress();
        } catch (AddressException e) {
            mail = user;
        }
        String parsedUser = null;
        if (!"".equals(mail)) {
            // to match "-external" emails and old mails with '@gemplus.com'...
            mail = mail.toLowerCase();
            mail = mail.replace("-external", "").replaceAll("^(.*)@.*[.]com$", "$1%@%.com");
            // Try to find a wiki profile with this email as parameter.
            // TBD : do this in the loading phase, and only try to search db if it was not found ?
            String xwql =
                "select doc.fullName from Document doc, doc.object(XWiki.XWikiUsers) as user where LOWER(user.email) like '?'";

            List<String> profiles = null;
            try {
                profiles = queryManager.createQuery(xwql, Query.XWQL).bindValue(0, mail).execute();
            } catch (QueryException e) {
                profiles = null;
            }
            if (profiles == null || profiles.size() == 0) {
                return null;
            } else {
                if (isMatchLdap) {
                    // If there exists one, we prefer the user that's been authenticated through LDAP
                    for (String usr : profiles) {
                        try {
                            if (xwiki.getDocument(usr, context).getObject("XWiki.LDAPProfileClass") != null) {
                                parsedUser = usr;
                            }
                        } catch (XWikiException e) {
                            // nothing to do
                        }
                    }
                    if (parsedUser != null) {
                        return parsedUser;
                    }
                }
            }

            // If none has authenticated from LDAP, we return the first user found
            return profiles.get(0);

        } else {
            return null;
        }

    }

    /**
     * Returns the Levenshtein distance between two strings, averaged by the length of the largest string provided, in
     * order to return a value n so that 0 < n < 1.
     * 
     * @param s
     * @param t
     * @return
     */
    public double getAveragedLevenshteinDistance(String s, String t)
    {
        return (double) (StringUtils.getLevenshteinDistance(s, t)) / ((double) Math.max(s.length(), t.length()));
    }

    /**
     * Compare 2 strings for similarity Returns true if strings can be considered similar enough<br/>
     * - s1 and s2 have a levenshtein distance < 25% <br/>
     * - s1 or s2 begins with s2 or s1 respectively
     * 
     * @param defaultMailArchive TODO
     * @param s1
     * @param s2
     * @return
     */
    boolean similarSubjects(DefaultMailArchive defaultMailArchive, String s1, String s2)
    {
        logger.debug("similarSubjects : comparing [" + s1 + "] and [" + s2 + "]");
        s1 = s1.replaceAll("^([Rr][Ee]:|[Ff][Ww]:)(.*)$", "$2");
        s2 = s2.replaceAll("^([Rr][Ee]:|[Ff][Ww]:)(.*)$", "$2");
        logger.debug("similarSubjects : comparing [" + s1 + "] and [" + s2 + "]");
        if (s1 == s2) {
            logger.debug("similarSubjects : subjects are equal");
            return true;
        }
        if (s1 != null && s1.equals(s2)) {
            logger.debug("similarSubjects : subjects are the equal");
            return true;
        }
        if (s1.length() == 0 || s2.length() == 0) {
            logger.debug("similarSubjects : one subject is empty, we consider them different");
            return false;
        }
        try {
            double d = getAveragedLevenshteinDistance(s1, s2);
            logger.debug("similarSubjects : Levenshtein distance d=" + d);
            if (d <= 0.25) {
                logger.debug("similarSubjects : subjects are considered similar because d <= 0.25");
                return true;
            }
        } catch (IllegalArgumentException iaE) {
            return false;
        }
        if ((s1.startsWith(s2) || s2.startsWith(s1))) {
            logger.debug("similarSubjects : subjects are considered similar because one start with the other");
            return true;
        }
        return false;
    }

    // Truncate a string "s" to obtain less than a certain number of bytes "maxBytes", starting with "maxChars"
    // characters.
    public String truncateStringForBytes(String s, int maxChars, int maxBytes)
    {

        String substring = s;
        if (s.length() > maxChars) {
            substring = s.substring(0, maxChars);
        }

        byte[] bytes = new byte[] {};
        try {
            bytes = substring.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (bytes.length > maxBytes) {

            logger.debug("Truncate string to " + substring.length() + " characters, result in " + bytes.length
                + " bytes array");
            return truncateStringForBytes(s, maxChars - (bytes.length - maxChars) / 4, maxBytes);

        } else {

            logger.debug("String truncated to " + substring.length() + " characters, resulting in " + bytes.length
                + " bytes array");
            return substring;
        }

    }

}
