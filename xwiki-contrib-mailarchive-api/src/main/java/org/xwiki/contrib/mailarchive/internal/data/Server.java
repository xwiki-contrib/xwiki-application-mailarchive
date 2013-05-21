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
package org.xwiki.contrib.mailarchive.internal.data;

import org.xwiki.contrib.mail.source.IServerSource;

/**
 * @author jbousque
 * @version $Id$
 */
public class Server extends AbstractMASource implements IServerSource
{
    public static final int DEFAULT_PORT = 993;

    private String hostname;

    private int port;

    private String protocol;

    private String username;

    private String password;

    public Server()
    {
        super();
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(final String host)
    {
        this.hostname = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(final int port)
    {
        this.port = port;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(final String protocol)
    {
        this.protocol = protocol;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String user)
    {
        this.username = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMASource#getType()
     */
    @Override
    public SourceType getType()
    {
        return SourceType.SERVER;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Server [hostname=").append(hostname).append(", port=").append(port).append(", protocol=")
            .append(protocol).append(", username=").append(username).append(", password=*****").append(", getId()=")
            .append(getId()).append(", getFolder()=").append(getFolder()).append(", getAdditionalProperties()=")
            .append(getAdditionalProperties()).append(", getWikiDoc()=").append(getWikiDoc()).append(", isEnabled()=")
            .append(isEnabled()).append("]");
        return builder.toString();
    }

}
