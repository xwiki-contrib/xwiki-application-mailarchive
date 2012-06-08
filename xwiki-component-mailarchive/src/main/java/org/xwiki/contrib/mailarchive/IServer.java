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
package org.xwiki.contrib.mailarchive;

import java.util.Properties;

import javax.mail.Session;

/**
 * A Mail Server connection properties.
 */
public interface IServer
{
    public static final int DEFAULT_PORT = 993;

    /**
     * The unique identifier of this server. Used notably to distinguish mails from this server into the Store.
     * 
     * @return
     */
    public String getId();

    /**
     * The mail server host name.
     * 
     * @return
     */
    public String getHost();

    /**
     * The mail server port number.
     * 
     * @return
     */
    public int getPort();

    /**
     * The protocol to use to connect to this server.
     * 
     * @return
     */
    public String getProtocol();

    /**
     * Folder containing emails to read. Use '/' as separator to specify folders path.
     * 
     * @return
     */
    public String getFolder();

    /**
     * Connection user name.
     * 
     * @return
     */
    public String getUser();

    /**
     * Connection password.
     * 
     * @return
     */
    public String getPassword();

    /**
     * Java mail additional properties to use to connect to this server.
     * 
     * @return
     */
    public Properties getAdditionalProperties();

    /**
     * The XWiki document name that hosts server preferences.
     * 
     * @return
     */
    public String getWikiDoc();

    /**
     * The currently used Session for this server connection, if any.
     * 
     * @return null if server is not currently connected.
     */
    public Session getSession();

}
