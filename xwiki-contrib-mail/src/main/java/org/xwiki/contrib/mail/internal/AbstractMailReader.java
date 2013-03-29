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

import org.xwiki.contrib.mail.IMailReader;
import org.xwiki.contrib.mail.source.IMailSource;

/**
 * @version $Id$
 */
public abstract class AbstractMailReader implements IMailReader
{

    private IMailSource mailSource;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailReader#setMailSource(org.xwiki.contrib.mail.source.IMailSource)
     */
    @Override
    public void setMailSource(final IMailSource mailSource)
    {
        if (mailSource == null) {
            throw new IllegalArgumentException("Mail source can't be null");
        }

        this.mailSource = mailSource;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.IMailReader#getMailSource()
     */
    @Override
    public IMailSource getMailSource()
    {
        return this.mailSource;
    }

}
