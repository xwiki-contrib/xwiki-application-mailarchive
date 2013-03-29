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
package org.xwiki.contrib.mail.internal.source;

import java.util.Properties;

import org.xwiki.contrib.mail.source.IServerSource;

/**
 * @version $Id$
 */
public class ServerAccountSource implements IServerSource
{
    private String hostname;

    private int port;

    private String protocol;

    private String username;

    private String password;

    private Properties additionalProperties;

    /**
     * @param hostname
     * @param port
     * @param protocol
     * @param username
     * @param password
     * @param additionalProperties
     */
    public ServerAccountSource(String hostname, int port, String protocol, String username, String password,
        Properties additionalProperties)
    {
        super();
        this.hostname = hostname;
        this.port = port;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.additionalProperties = additionalProperties;
    }

    /**
     * @return the hostname
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(final String hostname)
    {
        this.hostname = hostname;
    }

    /**
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(final int port)
    {
        this.port = port;
    }

    /**
     * @return the protocol
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(final String protocol)
    {
        this.protocol = protocol;
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(final String username)
    {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(final String password)
    {
        this.password = password;
    }

    /**
     * @return the additionalProperties
     */
    public Properties getAdditionalProperties()
    {
        return additionalProperties;
    }

    /**
     * @param additionalProperties the additionalProperties to set
     */
    public void setAdditionalProperties(final Properties additionalProperties)
    {
        this.additionalProperties = additionalProperties;
    }

}
