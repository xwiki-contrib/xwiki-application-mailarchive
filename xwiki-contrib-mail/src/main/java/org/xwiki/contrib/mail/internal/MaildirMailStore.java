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
package org.xwiki.contrib.mail.internal;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mail.IStoreManager;

/**
 * @version $Id$
 */
@Component
@Named("maildir")
public class MaildirMailStore extends AbstractMailStore implements IStoreManager
{
    @Inject
    private Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IStoreManager#getLogger()
     */
    @Override
    public Logger getLogger()
    {
        return logger;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IStoreManager#getSupportedFormat()
     */
    @Override
    public String getSupportedFormat()
    {
        return "maildir";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IStoreManager#getProvider()
     */
    @Override
    public String getProvider()
    {
        return "maildir";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IStoreManager#getStoreProperties()
     */
    @Override
    public Properties getStoreProperties()
    {
        Properties props = new Properties();
        // the following specifies whether to create maildirpath if it is not existent
        // if not specified then autocreatedir is false
        props.put("mail.store.maildir.autocreatedir", "true");

        return props;
    }

}
