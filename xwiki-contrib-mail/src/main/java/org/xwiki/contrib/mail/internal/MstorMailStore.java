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
@Named("mbox")
public class MstorMailStore extends AbstractMailStore implements IStoreManager
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
        return "mbox";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IStoreManager#getProvider()
     */
    @Override
    public String getProvider()
    {
        return "mstor";
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
        props.put("mstor.mbox.metadataStrategy", "none");
        // Avoid caching to allow handling large files, and also to avoid dependency on ehcache
        System.setProperty("mstor.cache.disabled", "true");
        System.setProperty("mstor.mbox.cacheBuffers", "false");
        System.setProperty("net.sf.ehcache.disabled", "true");
        // Options to make it work with mbox files generated from Mozilla Thunderbird
        System.setProperty("mstor.mbox.mozillaCompatibility", "true");
        System.setProperty("mstor.mbox.bufferStrategy", "default");
        System.setProperty("mstor.mbox.parsing.relaxed", "true");
        return props;
    }

}
