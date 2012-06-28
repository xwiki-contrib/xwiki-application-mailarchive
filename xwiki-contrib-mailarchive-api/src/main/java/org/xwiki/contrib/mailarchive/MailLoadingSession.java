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

/**
 * @version $Id$
 */
public class MailLoadingSession
{
    private boolean withDelete = false;

    private boolean loadAll = false;

    private boolean debugMode = false;

    private boolean simulationMode = false;

    private int maxMailsNb = -1;

    private String serverPrefsDoc = null;

    private final IMailArchive ma;

    public MailLoadingSession(IMailArchive ma)
    {
        this.ma = ma;
    }

    public MailLoadingSession(IMailArchive ma, String serverPrefsDoc)
    {
        this(ma);
        this.serverPrefsDoc = serverPrefsDoc;
    }

    public MailLoadingSession withDelete()
    {
        this.withDelete = true;
        return this;
    }

    public MailLoadingSession loadAll()
    {
        this.loadAll = true;
        return this;
    }

    public MailLoadingSession debugMode()
    {
        this.debugMode = true;
        return this;
    }

    public MailLoadingSession simulationMode()
    {
        this.simulationMode = true;
        return this;
    }

    public MailLoadingSession setLimit(int maxMailsNb)
    {
        this.maxMailsNb = maxMailsNb;
        return this;
    }

    public boolean isWithDelete()
    {
        return withDelete;
    }

    public boolean isLoadAll()
    {
        return loadAll;
    }

    public boolean isDebugMode()
    {
        return debugMode;
    }

    public boolean isSimulationMode()
    {
        return simulationMode;
    }

    public int getLimit()
    {
        return maxMailsNb;
    }

    public String getServerPrefsDoc()
    {
        return serverPrefsDoc;
    }

    public int loadMails()
    {
        // clone the session to avoid it to be updated during loading phase ...
        return this.ma.loadMails(this.clone());
    }

    @Override
    protected MailLoadingSession clone()
    {
        MailLoadingSession clone = null;
        if (serverPrefsDoc != null) {
            clone = new MailLoadingSession(ma, serverPrefsDoc);
        } else {
            clone = new MailLoadingSession(ma);
        }
        if (debugMode) {
            clone.debugMode();
        }
        if (loadAll) {
            clone.loadAll();
        }
        if (simulationMode) {
            clone.simulationMode();
        }
        if (withDelete) {
            clone.withDelete();
        }
        clone.setLimit(getLimit());
        return clone;
    }

}
