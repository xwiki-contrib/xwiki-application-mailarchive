package org.xwiki.contrib.mailarchive.internal;

import org.xwiki.job.DefaultRequest;
import org.xwiki.job.event.status.JobStatus.State;
import org.xwiki.job.internal.AbstractJob;
import org.xwiki.job.internal.DefaultJobStatus;
import org.xwiki.logging.LoggerManager;
import org.xwiki.observation.ObservationManager;

public class EmailLoadingJobStatus extends DefaultJobStatus<DefaultRequest>
{

    private int nbSuccess = 0;

    private int nbFailure = 0;
    
    private int nbAlreadyLoaded = 0;
    
    private int nbNotMatchingMailingLists = 0;

    private String currentSource = null;

    private String currentMail = null;
    
    private boolean paused = false;
    
    private boolean stopped = false;
    
    private static final long serialVersionUID = 6928292574160986297L;

    public EmailLoadingJobStatus(DefaultRequest request, ObservationManager observationManager,
        LoggerManager loggerManager, boolean subJob)
    {
        super(request, observationManager, loggerManager, subJob);
    }

    
    /**
     * @return the success
     */
    public int getNbSuccess()
    {
        return nbSuccess;
    }

    protected void setNbSuccess(final int nbSuccess)
    {
        this.nbSuccess = nbSuccess;
    }

    public void incNbSuccess()
    {
        this.nbSuccess++;
    }

    public int getNbFailure()
    {
        return nbFailure;
    }

    public void incNbFailure()
    {
        this.nbFailure++;
    }
    
    public int getNbAlreadyLoaded()
    {
        return nbAlreadyLoaded;
    }
    
    public void incNbAlreadyLoaded()
    {
        this.nbAlreadyLoaded++;
    }
    
    public int getNbNotMatchingMailingLists()
    {
        return nbNotMatchingMailingLists;
    }
    
    public void incNbNotMatchingMailingLists()
    {
        this.nbNotMatchingMailingLists++;
    }

    /**
     * @return the currentSource
     */
    public String getCurrentSource()
    {
        return currentSource;
    }

    /**
     * @param currentSource the currentSource to set
     */
    public void setCurrentSource(final String currentSource)
    {
        this.currentSource = currentSource;
    }

    /**
     * @return the currentMail
     */
    public String getCurrentMail()
    {
        return currentMail;
    }

    /**
     * @param currentMail the currentMail to set
     */
    public void setCurrentMail(final String currentMail)
    {
        this.currentMail = currentMail;
    }


    public boolean isPaused()
    {
        return paused;
    }


    public void setPaused(boolean paused)
    {
        this.paused = paused;
    }


    public boolean isStopped()
    {
        return stopped;
    }


    public void setStopped(boolean stopped)
    {
        this.stopped = stopped;
    }

    
    
}
