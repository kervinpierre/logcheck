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

import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.tail.impl.LogCheckTailerListener;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckResult;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Management class for tailing log files.
 * 
 * @author kervin
 */
public final class LogCheckTail implements Callable<LogCheckResult>
{
    private static final Logger log 
                             = LogManager.getLogger(LogCheckTail.class);

    private final ILogEntryBuilder mainLogEntryBuilder;
    
    private final Path logFile;
    private final Long delay;
    private final Boolean tailFromEnd;
    private final Boolean reOpenOnChunk;
    private final Integer bufferSize;
    private final Long stopAfter;

    private LogCheckTail(final ILogEntryBuilder mainLogEntryBuilder,
                         final Path logFile,
                         final Long delay,
                         final Boolean tailFromEnd,
                         final Boolean reOpenOnChunk,
                         final Integer bufferSize,
                         final Long stopAfter)
    {
        this.mainLogEntryBuilder = mainLogEntryBuilder;
        // Don't bother with logs we missed earlier

        if( tailFromEnd != null )
        {
            this.tailFromEnd = tailFromEnd;
        }
        else
        {
            this.tailFromEnd = true;
        }

        if( reOpenOnChunk != null )
        {
            this.reOpenOnChunk = reOpenOnChunk;
        }
        else
        {
            this.reOpenOnChunk = false;
        }

        if( bufferSize != null )
        {
            this.bufferSize = bufferSize;
        }
        else
        {
            this.bufferSize = LogCheckConstants.DEFAULT_LOG_READ_BUFFER_SIZE_BYTES;
        }

        if( delay != null )
        {
            this.delay = delay;
        }
        else
        {
            this.delay = LogCheckConstants.DEFAULT_POLL_INTERVAL;
        }

        if( stopAfter != null )
        {
            this.stopAfter = stopAfter;
        }
        else
        {
            this.stopAfter = 0L;
        }

        if( logFile != null )
        {
            this.logFile = logFile;
        }
        else
        {
            this.logFile = null;
        }
    }

    public static LogCheckTail from(final ILogEntryBuilder mainLogEntryBuilder,
                                    final Path logFile,
                                    final Long delay,
                                    final Boolean tailFromEnd,
                                    final Boolean reOpenOnChunk,
                                    final Integer bufferSize,
                                    final Long stopAfter)
    {
        LogCheckTail res = new LogCheckTail(mainLogEntryBuilder,
                logFile,
                delay,
                tailFromEnd,
                reOpenOnChunk,
                bufferSize,
                stopAfter);

        return res;
    }

    @Override
    public LogCheckResult call()
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);

        long currDelay = delay==null?0:delay;
        boolean currTailFromEnd = tailFromEnd==null?false:tailFromEnd;
        boolean currReOpenOnChunk = reOpenOnChunk==null?false:reOpenOnChunk;
        final ScheduledExecutorService schedulerExe;

        LogCheckTailerListener mainTailerListener = LogCheckTailerListener.from(mainLogEntryBuilder);

        final Tailer mainTailer = Tailer.from(logFile,
                Tailer.DEFAULT_CHARSET,
                mainTailerListener,
                currDelay,
                currTailFromEnd,
                currReOpenOnChunk,
                Tailer.DEFAULT_BUFSIZE);
        
        if( stopAfter != null
                && stopAfter > 0 )
        {
            schedulerExe = Executors.newScheduledThreadPool(1);

            schedulerExe.schedule(new Runnable() 
            {
              @Override
              public void run()
              {
                  if( mainTailer != null )
                  {
                      mainTailer.stop();
                  }

                  if( schedulerExe != null )
                  {
                      schedulerExe.shutdownNow();
                  }
              }
            }, stopAfter, TimeUnit.SECONDS);
            
            schedulerExe.shutdown();
        }
        else
        {
            schedulerExe = null;
        }
            
        mainTailer.call();
        
        /**
         * FIXME : We can choose to check the interrupted flag here.
         */

        mainTailer.stop();


        if( schedulerExe != null )
        {
            schedulerExe.shutdownNow();
        }
        
        return res;
    }
}
