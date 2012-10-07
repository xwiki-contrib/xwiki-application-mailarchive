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
package org.xwiki.contrib.mailarchive.internal.ldap;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPSearchAttribute;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPUtils;
import com.xpn.xwiki.util.Util;

/**
 * @version $Id$
 */
@Component
@Singleton
class LDAPHelper implements ILDAPHelper
{
    @Inject
    private IMailArchiveConfiguration config;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.ldap.ILDAPHelper#searchLDAPUser(java.lang.String,
     *      com.xpn.xwiki.plugin.ldap.XWikiLDAPUtils)
     */
    @Override
    public LDAPUser searchLDAPUser(String uid, XWikiLDAPUtils ldaputils)
    {
        // TODO : retrieve ldap attributes names from configuration
        String[] attr_names = new String[] {"sn", "givenName", "mail"};
        List<XWikiLDAPSearchAttribute> attrs = ldaputils.searchUserAttributesByUid(uid, attr_names);
        if (null != attrs && 0 != attrs.size()) {
            LDAPUser user = new LDAPUser();
            user.setUid(uid);
            user.setFirst_name(attrs.get(1).value);
            user.setLast_name(attrs.get(2).value);
            if (null != attrs.get(3))
                user.setEmail(attrs.get(3).value);
            if (null != attrs.get(4)) {
                user.setAvatar(attrs.get(4).value);
            }
            return user;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.ldap.ILDAPHelper#searchLDAPUserByMail(java.lang.String,
     *      com.xpn.xwiki.plugin.ldap.XWikiLDAPUtils)
     */
    @Override
    public LDAPUser searchLDAPUserByMail(String mail, XWikiLDAPUtils ldaputils)
    {
        // TODO : retrieve ldap attributes names from configuration
        String[] attr_names = new String[] {"sn", "givenName", ldaputils.getUidAttributeName()};
        String query =
            MessageFormat.format(ldaputils.getUserSearchFormatString(), new String[] {"mail", mail.toLowerCase()});
        List<XWikiLDAPSearchAttribute> attrs =
            ldaputils.getConnection().searchLDAP(ldaputils.getBaseDN(), query, attr_names, 2);
        if (null != attrs && 0 != attrs.size()) {
            LDAPUser user = new LDAPUser();
            user.setUid(attrs.get(2).value);
            user.setFirst_name(attrs.get(1).value);
            user.setLast_name(attrs.get(3).value);
            user.setEmail(mail);
            return user;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.internal.ldap.ILDAPHelper#searchLDAPPhoto(java.lang.String,
     *      com.xpn.xwiki.plugin.ldap.XWikiLDAPUtils)
     */
    @Override
    public byte[] searchLDAPPhoto(String uid, XWikiLDAPUtils ldaputils) throws LDAPException
    {
        String[] attr_names = new String[] {config.getLdapPhotoFieldName()};
        String query =
            MessageFormat.format(ldaputils.getUserSearchFormatString(), new String[] {ldaputils.getUidAttributeName(),
            uid.toLowerCase()});
        LDAPSearchResults results =
            searchLDAP(ldaputils.getConnection().getConnection(), ldaputils.getBaseDN(), query, attr_names, 2);
        if (results != null) {
            LDAPEntry nextEntry = results.next();
            if (nextEntry != null) {
                LDAPAttributeSet attributeSet = nextEntry.getAttributeSet();
                LDAPAttribute bytesattr = attributeSet.getAttribute(config.getLdapPhotoFieldName());
                if (bytesattr != null) {
                    if (IMailArchiveConfiguration.LDAP_PHOTO_CONTENT_BINARY.equals(config.getLdapPhotoFieldContent())) {
                        return bytesattr.getByteValue();
                    } else if (IMailArchiveConfiguration.LDAP_PHOTO_CONTENT_URL.equals(config
                        .getLdapPhotoFieldContent())) {
                        // FIXME: manage ldap photo field content type 'url'
                        return null;
                    }
                }
            }
        }
        return null;
    }

    boolean sames(LDAPUser user, LDAPUser xuser)
    {
        String sn = user.getFirst_name();
        sn = Util.noaccents(sn).replace('-', ' ');
        String gn = user.getLast_name();
        gn = gn.replaceAll(config.getEmailIgnoredText(), "");
        gn = Util.noaccents(gn).replace('-', ' ');
        String ln = Util.noaccents(xuser.getLast_name()).replace('-', ' ');
        String fn = Util.noaccents(xuser.getFirst_name()).replace('-', ' ');
        // FIXME "A©" ????
        while (fn.indexOf("A©") > 0) {
            fn = fn.substring(0, fn.indexOf("A©")) + "e" + fn.substring(fn.indexOf("A©") + 2);
        }
        while (ln.indexOf("A©") > 0) {
            ln = ln.substring(0, ln.indexOf("A©")) + "e" + ln.substring(ln.indexOf("A©") + 2);
        }

        if ((sn.equalsIgnoreCase(fn) && gn.equalsIgnoreCase(ln))
            || (sn.equalsIgnoreCase(ln) && gn.equalsIgnoreCase(fn))) {
            return true;
        }
        if ((null != user.getEmail()) && user.getEmail().equalsIgnoreCase(xuser.getEmail())) {
            return true;
        }
        return false;
    }

    /**
     * Execute a LDAP search query.
     * 
     * @param baseDN the root DN where to search.
     * @param query the LDAP query.
     * @param attr the attributes names of values to return.
     * @param ldapScope {@link LDAPConnection#SCOPE_SUB} oder {@link LDAPConnection#SCOPE_BASE}.
     * @return the found LDAP attributes.
     */
    LDAPSearchResults searchLDAP(LDAPConnection connection, String baseDN, String query, String[] attr, int ldapScope)
    {

        LDAPSearchResults searchResults = null;

        try {
            LDAPSearchConstraints cons = new LDAPSearchConstraints();
            cons.setTimeLimit(1000);

            // filter return all attributes return attrs and values time out value
            searchResults = connection.search(baseDN, ldapScope, query, attr, false, cons);

            if (!searchResults.hasMore()) {
                return null;
            }

        } catch (LDAPException e) {
        } finally {
            if (searchResults != null) {
                try {
                    connection.abandon(searchResults);
                } catch (LDAPException e) {
                }
            }
        }

        return searchResults;
    }

}
