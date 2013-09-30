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
package org.xwiki.contrib.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.script.service.ScriptService;

/**
 * @version $Id$
 */
@Component
@Named("mail")
@Singleton
public class MailScriptService implements ScriptService, IMailScriptService
{
    @Inject
    IMailComponent mailComp;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailScriptService#fetch(java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Properties, boolean)
     */
    @Override
    public List<Message> fetch(String hostname, int port, String protocol, String folder, String username,
        String password, Properties additionalProperties, boolean onlyUnread)
    {
        try {
            IMailReader mailReader =
                mailComp.getMailReader(hostname, port, protocol, username, password, additionalProperties);
            if (mailReader != null) {

                return mailReader.read(folder, onlyUnread);
            }
        } catch (MessagingException e) {
            // FIXME that's pretty bad to hide that exception...
            return new ArrayList<Message>();
        } catch (ComponentLookupException e) {
            // TODO Auto-generated catch block
            return new ArrayList<Message>();
        }

        return new ArrayList<Message>();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailScriptService#check(java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Properties, boolean)
     */
    @Override
    public int check(String hostname, int port, String protocol, String folder, String username, String password,
        Properties additionalProperties, boolean onlyUnread)
    {
        try {
            IMailReader mailReader =
                mailComp.getMailReader(hostname, port, protocol, username, password, additionalProperties);
            if (mailReader != null) {
                return mailReader.check(folder, onlyUnread);
            }
        } catch (ComponentLookupException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailScriptService#parseAddressHeader(java.lang.String)
     */
    @Override
    public String parseAddressHeader(String header)
    {
        return mailComp.parseAddressHeader(header);
    }
}
