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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mailarchive.IMAUser;
import org.xwiki.contrib.mailarchive.IMailMatcher;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.internal.data.MAUser;
import org.xwiki.contrib.mailarchive.internal.persistence.IPersistence;
import org.xwiki.contrib.mailarchive.internal.xwiki.IExtendedDocumentAccessBridge;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
@Component
@Singleton
public class MailUtils implements IMailUtils
{
    @Inject
    private ITextUtils textUtils;

    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    @Inject
    private IPersistence persistence;

    @Inject
    @Named("extended")
    private IExtendedDocumentAccessBridge bridge;

    /**
     * Parses an ID-type mail header, and extracts address part. An ID mail header usually is of the form:<br/>
     * User Name <user.address@host.com>
     * 
     * @param id an address mail header value
     * @return extracted address, or id itself if could not extract an address part
     */
    public String extractAddress(final String id)
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
    public IMAUser parseUser(final String user, final boolean isMatchLdap)
    {
        logger.debug("parseUser {}, {}", user, isMatchLdap);

        MAUser maUser = new MAUser();
        maUser.setOriginalAddress(user);

        if (StringUtils.isBlank(user)) {
            return maUser;
        }

        String address = null;
        String personal = null;
        // Do our best to extract an address and a personal
        try {
            InternetAddress ia = null;
            InternetAddress[] result = InternetAddress.parse(user, true);
            if (result != null && result.length > 0) {
                ia = result[0];
                if (!StringUtils.isBlank(ia.getAddress())) {
                    address = ia.getAddress();
                }
                if (!StringUtils.isBlank(ia.getPersonal())) {
                    personal = ia.getPersonal();
                }
            }
        } catch (AddressException e) {
            logger.warn("Address does not follow standards : " + user);
        }
        if (StringUtils.isBlank(address)) {
            String[] substrs = StringUtils.substringsBetween(user, "<", ">");
            if (substrs != null && substrs.length > 0) {
                address = substrs[0];
            } else {
                // nothing matches, we suppose recipient only contains email address
                address = user;
            }
        }
        if (StringUtils.isBlank(personal)) {
            if (user.contains("<")) {
                personal = StringUtils.substringBeforeLast(user, "<");
                if (StringUtils.isBlank(personal)) {
                    personal = StringUtils.substringBefore(address, "@");
                }
            }

        }
        maUser.setAddress(address);
        maUser.setDisplayName(personal);

        // Now to match a wiki profile
        logger.debug("parseUser extracted email {}", address);
        String parsedUser = null;
        if (!StringUtils.isBlank(address)) {
            // to match "-external" emails and old mails with '@gemplus.com'...
            String pattern = address.toLowerCase();
            pattern = pattern.replace("-external", "").replaceAll("^(.*)@.*[.]com$", "$1%@%.com");
            logger.debug("parseUser pattern applied {}", pattern);
            // Try to find a wiki profile with this email as parameter.
            // TBD : do this in the loading phase, and only try to search db if it was not found ?
            String xwql =
                "select doc.fullName from Document doc, doc.object(XWiki.XWikiUsers) as user where LOWER(user.email) like :pattern";

            List<String> profiles = null;
            try {
                profiles = queryManager.createQuery(xwql, Query.XWQL).bindValue("pattern", pattern).execute();
            } catch (QueryException e) {
                logger.warn("parseUser Query threw exception", e);
                profiles = null;
            }
            if (profiles == null || profiles.size() == 0) {
                logger.debug("parseUser found no wiki profile from db");
                return maUser;
            } else {
                if (isMatchLdap) {
                    logger.debug("parseUser Checking for LDAP authenticated profile(s) ...");
                    // If there exists one, we prefer the user that's been authenticated through LDAP
                    for (String usr : profiles) {
                        if (bridge.exists(usr, "XWiki.LDAPProfileClass")) {
                            parsedUser = usr;
                            logger.debug("parseUser Found LDAP authenticated profile {}", parsedUser);
                        }
                    }
                    if (parsedUser != null) {
                        maUser.setWikiProfile(parsedUser);
                        logger.info("parseUser return {}", maUser);
                        return maUser;
                    }
                }
            }

            // If none has authenticated from LDAP, we return the first user found
            maUser.setWikiProfile(profiles.get(0));
            logger.info("parseUser return {}", maUser);
            return maUser;

        } else {
            logger.info("parseUser No email found to match");
            return maUser;
        }

    }

    /**
     * @param mailPage
     * @param cut
     * @return
     * @throws IOException
     * @throws XWikiException
     */
    public DecodedMailContent decodeMailContent(final String originalHtml, final String originalBody, final boolean cut)
        throws IOException
    {
        String html = "";
        String body = "";

        if (!StringUtils.isEmpty(originalHtml)) {
            html = textUtils.unzipString(originalHtml);

            // body is only plain text
        } else {
            if (!StringUtils.isBlank(originalBody)) {
                if (originalBody.startsWith("<html") || originalBody.startsWith("<meta")
                    || originalBody.contains("<br>") || originalBody.contains("<br/>")) {
                    html = originalBody;
                }
            }
        }

        if (!StringUtils.isBlank(html)) {
            Matcher m = Pattern.compile("<span [^>]*>From:<\\\\/span>", Pattern.MULTILINE).matcher(html);
            if (cut && m.find()) {
                html = html.substring(0, m.start() - 1);
            } else if (cut && html.contains("<b>From:</b>")) {
                html = html.substring(0, html.indexOf("<b>From:</b>") - 1);
            }
            return new DecodedMailContent(true, html);
        } else {
            body = originalBody;
            Matcher m = Pattern.compile("\\n[\\s]*From:", Pattern.MULTILINE).matcher(body);
            if (cut && m.find()) {
                body = body.substring(0, m.start() + 1);
            }
            return new DecodedMailContent(false, body);
        }

    }

    /**
     * Find matching types for this mail.
     * 
     * @param m
     */
    @Override
    public List<IType> extractTypes(final Collection<IType> types, final MailItem m)
    {
        List<IType> result = new ArrayList<IType>();

        if (types == null || m == null) {
            throw new IllegalArgumentException("extractTypes: Types and mailitem can't be null");
        }

        // set IType
        for (IType type : types) {
            logger.debug("Checking for type " + type);
            boolean matched = true;
            for (IMailMatcher mailMatcher : type.getMatchers()) {
                logger.debug("  Checking for matcher " + mailMatcher);
                List<String> fields = mailMatcher.getFields();
                String regexp = mailMatcher.getExpression();

                Pattern pattern = null;
                if (mailMatcher.isAdvancedMode()) {
                    // Manage multi line and ignore case in regexp if needed
                    if (mailMatcher.isIgnoreCase() || mailMatcher.isMultiLine()) {
                        String prefix = "(?";
                        if (mailMatcher.isIgnoreCase()) {
                            prefix += 'i';
                        }
                        if (mailMatcher.isMultiLine()) {
                            prefix += 'm';
                        }
                        prefix += ')';
                        regexp = prefix + regexp;
                    }

                    try {
                        pattern = Pattern.compile(regexp);
                    } catch (PatternSyntaxException e) {
                        logger.warn("Invalid Pattern " + regexp + "can't be compiled, skipping this mail type");
                        break;
                    }

                }

                Matcher matcher = null;
                boolean fieldMatch = false;
                for (String field : fields) {
                    String fieldValue = "";
                    if ("from".equals(field)) {
                        fieldValue = m.getFrom();
                    } else if ("to".equals(field)) {
                        fieldValue = m.getTo();
                    } else if ("cc".equals(field)) {
                        fieldValue = m.getCc();
                    } else if ("subject".equals(field)) {
                        fieldValue = m.getSubject();
                    }
                    logger.debug("  Checking field " + field + " with value [" + fieldValue + "] against pattern ["
                        + regexp + "] DEBUG [" + textUtils.byte2hex(regexp.getBytes()) + "]");
                    if (mailMatcher.isAdvancedMode()) {
                        matcher = pattern.matcher(fieldValue);
                        logger.debug("Matcher : " + matcher);
                        if (matcher != null) {
                            fieldMatch = matcher.find();
                            logger.debug("fieldMatch: " + fieldMatch);
                            logger.debug("matcher.matches : " + matcher.matches());
                        }
                    } else {
                        if (mailMatcher.isIgnoreCase()) {
                            fieldMatch = StringUtils.containsIgnoreCase(fieldValue, regexp);
                        } else {
                            fieldMatch = StringUtils.contains(fieldValue, regexp);
                        }
                    }
                    if (fieldMatch) {
                        logger.debug("Field " + field + " value [" + fieldValue + "] matches pattern [" + regexp + "]");
                        break;
                    }
                }
                matched = matched && fieldMatch;
            }
            if (matched) {
                logger.info("Matched type " + type.getName());
                result.add(type);
                matched = true;
            }
        }
        return result;
    }
}
