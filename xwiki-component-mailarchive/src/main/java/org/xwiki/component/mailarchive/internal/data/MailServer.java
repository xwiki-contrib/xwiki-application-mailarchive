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
package org.xwiki.component.mailarchive.internal.data;

/**
 * @version $Id$
 */
/**
 * 
 * @version $Id$
 */
/**
 * @version $Id$
 */
public class MailServer
{
    private String host;

    private int port;

    private String protocol;

    private String user;

    private String password;

    private String folder;

    private String wikiDoc;

    public MailServer()
    {
        super();
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return Mail server folder name (label for gmail)
     */
    public String getFolder()
    {
        return folder;
    }

    /**
     * @param folder Mail server folder name (label for gmail)
     */
    public void setFolder(String folder)
    {
        this.folder = folder;
    }

    public String getWikiDoc()
    {
        return wikiDoc;
    }

    public void setWikiDoc(String wikiDoc)
    {
        this.wikiDoc = wikiDoc;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MailServer [host=").append(host).append(", port=").append(port).append(", protocol=")
            .append(protocol).append(", user=").append(user).append(", password=").append("*****").append(", folder=")
            .append(folder).append(", wikiDoc=").append(wikiDoc).append("]");
        return builder.toString();
    }

}
