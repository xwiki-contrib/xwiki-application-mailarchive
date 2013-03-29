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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mail.IMailComponent;
import org.xwiki.contrib.mail.IMailReader;
import org.xwiki.contrib.mail.IStoreManager;
import org.xwiki.contrib.mail.MailContent;
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mail.internal.source.ServerAccountSource;
import org.xwiki.contrib.mail.internal.source.StoreSource;

/**
 * @version $Id$
 */
@Singleton
@Component
public class DefaultMailComponent implements IMailComponent, Initializable
{

    @Inject
    private Logger logger;

    @Inject
    private ComponentManager componentManager;

    @Inject
    @Named("javamail")
    private IMessageParser<Part> parser;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        // this.parser = new JavamailMessageParser(logger);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ComponentLookupException
     * @see org.xwiki.contrib.mail.IMailComponent#getMailReader(java.lang.String, int, java.lang.String,
     *      java.lang.String, java.lang.String, java.util.Properties)
     */
    @Override
    public IMailReader getMailReader(final String hostname, final int port, final String protocol,
        final String username, final String password, final Properties additionalProperties)
        throws ComponentLookupException
    {
        final IMailReader reader = componentManager.getInstance(IMailReader.class);
        final ServerAccountSource source =
            new ServerAccountSource(hostname, port, protocol, username, password, additionalProperties);
        reader.setMailSource(source);
        return reader;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailComponent#getMailReader(org.xwiki.contrib.mail.internal.source.ServerAccountSource)
     */
    @Override
    public IMailReader getMailReader(ServerAccountSource source) throws ComponentLookupException
    {
        final IMailReader reader = componentManager.getInstance(IMailReader.class);
        reader.setMailSource(source);
        return reader;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ComponentLookupException
     * @see org.xwiki.contrib.mail.IMailComponent#getStoreManager(java.lang.String, java.lang.String)
     */
    @Override
    public IStoreManager getStoreManager(final String format, final String location) throws ComponentLookupException
    {
        IStoreManager store = null;

        final String storeLocation = location.replaceAll("\\\\", "/");
        File storeRoot = new File(storeLocation);
        if (!storeRoot.exists()) {
            storeRoot.mkdirs();
        }
        // Component hint is the format supported by provider of this store
        store = this.componentManager.getInstance(IStoreManager.class, format);
        StoreSource source = new StoreSource(format, location);
        store.setMailSource(source);

        return store;

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailComponent#getStoreManager(org.xwiki.contrib.mail.internal.source.StoreSource)
     */
    @Override
    public IStoreManager getStoreManager(StoreSource source) throws ComponentLookupException
    {
        IStoreManager store = null;

        // Component hint is the format supported by provider of this store
        store = this.componentManager.getInstance(IStoreManager.class, source.getFormat());
        store.setMailSource(source);

        return store;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     * @throws MessagingException
     * @see org.xwiki.contrib.mail.IMailReader#parse(javax.mail.Message)
     */
    @Override
    public MailItem parseHeaders(final Part mail) throws MessagingException, IOException
    {
        return parser.parseHeaders(mail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MailContent parseContent(final Part mail) throws MessagingException, IOException
    {
        return parser.extractMailContent(mail);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailReader#parseAddressHeader(java.lang.String)
     */
    @Override
    public String parseAddressHeader(final String header)
    {
        try {
            return InternetAddress.parseHeader(header, false)[0].getPersonal();
        } catch (AddressException e) {
            logger.error("Could not parse " + header, e);
            return "";
        }
    }

}
