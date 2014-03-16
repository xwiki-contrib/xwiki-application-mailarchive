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
import org.xwiki.contrib.mail.source.SourceType;
import org.xwiki.text.XWikiToStringBuilder;

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
    
    private Boolean autoTrustSSLCertificates;

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
    
    @Override
    public SourceType getType()
    {
        return SourceType.SERVER;
    }
    
    @Override
    public Boolean isAutoTrustSSLCertificates()
    {        
        return autoTrustSSLCertificates;
    }
    
    public void setAutoTrustSSLCertificates(final Boolean autoTrust)
    {        
        this.autoTrustSSLCertificates = autoTrust;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        XWikiToStringBuilder builder = new XWikiToStringBuilder(this);
        builder.append("hostname", hostname);
        builder.append("port", port);
        builder.append("protocol", protocol);
        builder.append("username", username);
        builder.append("password", "***");
        builder.append("getId()", getId());
        builder.append("getFolder()", getFolder());
        builder.append("getAdditionalProperties()", getAdditionalProperties());
        builder.append("getWikiDoc()", getWikiDoc());
        builder.append("isEnabled()", isEnabled());
        builder.append("getState()", getState());
        return builder.toString();
    }



}
