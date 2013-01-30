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

/**
 * @version $Id$
 */
public class MailServerConnectionException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 3110149565099510746L;

    private ConnectionErrors code;

    /**
     * 
     */
    public MailServerConnectionException()
    {
        super();
        this.code = ConnectionErrors.OTHER_ERROR;
    }

    public MailServerConnectionException(final String message)
    {
        super(message);
        this.code = ConnectionErrors.OTHER_ERROR;
    }

    /**
     * @param arg0
     * @param arg1
     */
    public MailServerConnectionException(String message, Throwable throwable)
    {
        super(message, throwable);
        this.code = ConnectionErrors.OTHER_ERROR;
    }

    /**
     * @param arg0
     */
    public MailServerConnectionException(Throwable throwable)
    {
        super(throwable);
        this.code = ConnectionErrors.OTHER_ERROR;
    }

    public MailServerConnectionException(final ConnectionErrors code)
    {
        super();
        this.code = code;
    }

    public MailServerConnectionException(final String message, final ConnectionErrors code)
    {
        super(message);
        this.code = code;
    }

    public MailServerConnectionException(final String message, final Throwable throwable, final ConnectionErrors code)
    {
        super(message, throwable);
        this.code = code;
    }

    public MailServerConnectionException(final Throwable throwable, final ConnectionErrors code)
    {
        super(throwable);
        this.code = code;
    }

    public String getRootCauseMessage()
    {
        Throwable cause = getCause();
        while (true) {
            if (cause.getCause() != null) {
                cause = cause.getCause();
            } else {
                break;
            }
        }
        if (cause != null) {
            return cause.getMessage();
        }
        return "";
    }

    public ConnectionErrors getCode()
    {
        return this.code;
    }

}
