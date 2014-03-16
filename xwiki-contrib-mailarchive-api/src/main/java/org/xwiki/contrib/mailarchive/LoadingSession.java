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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.contrib.mail.source.SourceType;
import org.xwiki.job.JobManager;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * A Loading session definition.
 * 
 * @author jbousque
 * @version $Id$
 */
public class LoadingSession
{
    @Inject
    private JobManager jobManager;

    private String id;

    private boolean withDelete = false;

    private boolean loadAll = false;

    private boolean debugMode = false;

    private boolean simulationMode = false;

    private boolean recentMails = false;
    
    private int maxMailsNb = -1;

    private Map<SourceType, String> sources = null;

    public LoadingSession()
    {
    }

    public LoadingSession(final String id)
    {
        this.id = id;
    }

    public LoadingSession addServer(final String serverPrefsDoc)
    {
        if (this.sources == null) {
            this.sources = new HashMap<SourceType, String>();
        }
        this.sources.put(SourceType.SERVER, serverPrefsDoc);
        return this;
    }

    public LoadingSession addStore(final String storePrefsDoc)
    {
        if (this.sources == null) {
            this.sources = new HashMap<SourceType, String>();
        }
        this.sources.put(SourceType.STORE, storePrefsDoc);
        return this;
    }

    protected void setSources(final Map<SourceType, String> sources)
    {
        this.sources = sources;
    }

    public String getId()
    {
        return this.id;
    }

    public LoadingSession withDelete()
    {
        this.withDelete = true;
        return this;
    }

    public LoadingSession loadAll()
    {
        this.loadAll = true;
        return this;
    }

    public LoadingSession debugMode()
    {
        this.debugMode = true;
        return this;
    }

    public LoadingSession simulationMode()
    {
        this.simulationMode = true;
        return this;
    }

    public LoadingSession recentMails()
    {
        this.recentMails = true;
        return this;
    }

    public LoadingSession setLimit(int maxMailsNb)
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

    public boolean getRecentMails()
    {
        return recentMails;
    }

    public int getLimit()
    {
        return maxMailsNb;
    }

    public Map<SourceType, String> getSources()
    {
        return this.sources;
    }

    @Override
    protected LoadingSession clone() throws CloneNotSupportedException
    {
        LoadingSession clone = new LoadingSession(this.id);

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
        clone.setSources(this.sources);

        clone.setLimit(getLimit());
        return clone;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        ToStringBuilder builder = new XWikiToStringBuilder(this);
        builder =
            builder.append("id", id).append("withDelete", withDelete).append("loadAll", loadAll)
                .append("debugMode", debugMode).append("simulationMode", simulationMode)
                .append("maxMailsNb", maxMailsNb).append("sources", sources);

        return builder.toString();
    }

}
