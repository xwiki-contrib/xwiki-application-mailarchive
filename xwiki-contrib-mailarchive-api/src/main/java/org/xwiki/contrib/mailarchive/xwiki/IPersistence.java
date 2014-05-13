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
package org.xwiki.contrib.mailarchive.xwiki;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.mail.MessagingException;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.mail.MailItem;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Defines a layer above XWiki API for persistence of Mail Archive items as XWiki pages and objects.
 * 
 * @version $Id$
 */
@Role
public interface IPersistence
{

    public String createTopic(final String pagename, final MailItem m, final List<String> taglist,
        final String loadingUser, final boolean create) throws XWikiException;

    public String updateTopicPage(MailItem m, String existingTopicPage, SimpleDateFormat dateFormatter,
        final String loadingUser, boolean create) throws XWikiException;

    public String createMailPage(MailItem m, String pageName, String existingTopicId, boolean isAttachedMail,
        final List<String> taglist, List<String> attachedMailsPages, String parentMail, String loadingUser,
        boolean create) throws XWikiException, MessagingException, IOException;

    public void updateMailServerState(String serverPrefsDoc, int status) throws XWikiException;

    public void updateMailStoreState(String storePrefsDoc, int status) throws XWikiException;

    public void saveAsUser(final XWikiDocument doc, final String user, final String contentUser, final String comment)
        throws XWikiException;

    public String getMessageUniquePageName(MailItem m, boolean isAttachedMail);

    public boolean existsMessage(final String msgid);

}
