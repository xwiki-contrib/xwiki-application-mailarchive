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
package org.xwiki.contrib.mail.source;

/**
 * Defines a mail server connection, ie a mail data source.
 */
public interface IServerSource extends IMailSource
{
    static final int DEFAULT_PORT = 993;

    /**
     * The hostname of this server connection.
     * 
     * @return
     */
    String getHostname();

    /**
     * The port of this server connection.
     * 
     * @return
     */
    int getPort();

    /**
     * The protocol ("imap", "imaps", "pop3"...) to connect to this server. IMAP is recommended.
     * 
     * @return
     */
    String getProtocol();

    /**
     * The login user for this connection.
     * 
     * @return
     */
    String getUsername();

    /**
     * The password for this connection.
     * 
     * @return
     */
    String getPassword();
    
    /**
     * 
     * @return
     */
    Boolean isAutoTrustSSLCertificates();

}
