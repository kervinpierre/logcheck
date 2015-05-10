/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.tail;

import com.sludev.logs.logcheck.log.LogEntryBuilder;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Management class for tailing log files.
 * 
 * @author kervin
 */
public class LogCheckTail implements Callable<LogCheckResult>
{
    private static final Logger log 
                             = LogManager.getLogger(LogCheckTail.class);
    
    private Tailer mainTailer;
    private LogCheckTailerListener mainTailerListener;
    private LogEntryBuilder mainLogEntryBuilder;
    
    private File logFile;
    private long delay;
    private boolean tailFromEnd;
    private boolean reOpenOnChunk;
    private int bufferSize;
    private long stopAfter;

    public long getStopAfter()
    {
        return stopAfter;
    }

    public void setStopAfter(long s)
    {
        this.stopAfter = s;
    }

    public LogEntryBuilder getMainLogEntryBuilder()
    {
        return mainLogEntryBuilder;
    }

    public void setMainLogEntryBuilder(LogEntryBuilder m)
    {
        this.mainLogEntryBuilder = m;
    }

    public Tailer getMainTailer()
    {
        return mainTailer;
    }

    public void setMainTailer(Tailer mainTailer)
    {
        this.mainTailer = mainTailer;
    }

    public LogCheckTailerListener getMainTailerListener()
    {
        return mainTailerListener;
    }

    public void setMainTailerListener(LogCheckTailerListener mainTailerListener)
    {
        this.mainTailerListener = mainTailerListener;
    }

    public File getLogFile()
    {
        return logFile;
    }

    public void setLogFile(File l) throws LogCheckException
    {
        if( l == null || l.exists() == false )
        {
            String errMsg = String.format("Log path '%s' is invalid.", l);
            log.debug(errMsg);
            
            throw new LogCheckException(errMsg);
        }
        
        this.logFile = l;
    }

    public void setLogFile(Path l) throws LogCheckException
    {
        if( l == null )
        {
            String errMsg = "Log path is empty.";
            log.debug(errMsg);
            
            throw new LogCheckException(errMsg);
        }
        
        File f = l.toFile();
        
        this.setLogFile(f);
    }
    
    public void setLogFile(String l)
    {
        File f = Paths.get(l).toFile();
        this.logFile = f;
    }
    
    public long getDelay()
    {
        return delay;
    }

    public void setDelay(long delay)
    {
        this.delay = delay;
    }

    public boolean isTailFromEnd()
    {
        return tailFromEnd;
    }

    public void setTailFromEnd(boolean tailFromEnd)
    {
        this.tailFromEnd = tailFromEnd;
    }

    public boolean isReOpenOnChunk()
    {
        return reOpenOnChunk;
    }

    public void setReOpenOnChunk(boolean reOpenOnChunk)
    {
        this.reOpenOnChunk = reOpenOnChunk;
    }

    public int getBufferSize()
    {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    public LogCheckTail()
    {
        // Don't bother with logs we missed earlier
        tailFromEnd = true;
        
        reOpenOnChunk = true;
        bufferSize = LogCheckConstants.DEFAULT_LOG_READ_BUFFER_SIZE_BYTES;
        delay      = LogCheckConstants.DEFAULT_POLL_INTERVAL;
        
        mainTailer = null;
        
        stopAfter = 0;
    }
    
    
    @Override
    public LogCheckResult call()
    {
        LogCheckResult res = new LogCheckResult();
        
        mainTailerListener = new LogCheckTailerListener();
        mainTailerListener.setMainLogEntryBuilder(mainLogEntryBuilder);
        mainTailer = new Tailer(logFile, mainTailerListener, delay, 
                                   tailFromEnd, reOpenOnChunk );
        
        if( stopAfter > 0 )
        {
            ScheduledExecutorService scheduler;
            scheduler = Executors.newScheduledThreadPool(1);

            scheduler.schedule(new Runnable() 
            {
              @Override
              public void run()
              { 
                  stop(); 
              }
            }, stopAfter, SECONDS);
            
            scheduler.shutdown();
        }
        
        mainTailer.run();
        
        return res;
    }

    public void stop()
    {
        if( mainTailer != null )
        {
            mainTailer.stop();
        }
    }
}
