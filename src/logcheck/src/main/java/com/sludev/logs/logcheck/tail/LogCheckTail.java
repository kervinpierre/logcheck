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

import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.tail.impl.LogCheckTailerListener;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
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
    private final Boolean saveState;
    private final Integer bufferSize;
    private final Long stopAfter;
    private final LCHashType idBlockHash;
    private final Integer idBlockSize;
    private final String setName;
    private final Path stateFile;
    private final Path errorFile;

    private LogCheckTail(final ILogEntryBuilder mainLogEntryBuilder,
                         final Path logFile,
                         final Long delay,
                         final Boolean tailFromEnd,
                         final Boolean reOpenOnChunk,
                         final Boolean saveState,
                         final Integer bufferSize,
                         final Long stopAfter,
                         final LCHashType idBlockHash,
                         final Integer idBlockSize,
                         final String setName,
                         final Path stateFile,
                         final Path errorFile)
    {
        this.mainLogEntryBuilder = mainLogEntryBuilder;
        this.idBlockHash = idBlockHash;
        this.idBlockSize = idBlockSize;
        this.setName = setName;
        // Don't bother with logs we missed earlier

        if( tailFromEnd != null )
        {
            this.tailFromEnd = tailFromEnd;
        }
        else
        {
            this.tailFromEnd = true;
        }

        if( saveState != null )
        {
            this.saveState = saveState;
        }
        else
        {
            this.saveState = false;
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

        if( stateFile != null )
        {
            this.stateFile = stateFile;
        }
        else
        {
            this.stateFile = null;
        }

        if( errorFile != null )
        {
            this.errorFile = errorFile;
        }
        else
        {
            this.errorFile = null;
        }
    }

    public static LogCheckTail from(final ILogEntryBuilder mainLogEntryBuilder,
                                    final Path logFile,
                                    final Long delay,
                                    final Boolean tailFromEnd,
                                    final Boolean reOpenOnChunk,
                                    final Boolean saveState,
                                    final Integer bufferSize,
                                    final Long stopAfter,
                                    final LCHashType idBlockHash,
                                    final Integer idBlockSize,
                                    final String setName,
                                    final Path stateFile,
                                    final Path errorFile)
    {
        LogCheckTail res = new LogCheckTail(mainLogEntryBuilder,
                logFile,
                delay,
                tailFromEnd,
                reOpenOnChunk,
                saveState,
                bufferSize,
                stopAfter,
                idBlockHash,
                idBlockSize,
                setName,
                stateFile,
                errorFile);

        return res;
    }

    @Override
    public LogCheckResult call()
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);

        long currDelay = delay==null?0:delay;
        boolean currTailFromEnd = tailFromEnd==null?false:tailFromEnd;
        boolean currReOpenOnChunk = reOpenOnChunk==null?false:reOpenOnChunk;

        final ScheduledExecutorService stopSchedulerExe;
        final ScheduledExecutorService statsSchedulerExe;

        TailerStatistics stats = TailerStatistics.from(logFile,
                stateFile,
                errorFile,
                idBlockHash,
                idBlockSize,
                setName);

        LogCheckTailerListener mainTailerListener
                            = LogCheckTailerListener.from(mainLogEntryBuilder);

        final Tailer mainTailer = Tailer.from(logFile,
                Tailer.DEFAULT_CHARSET,
                mainTailerListener,
                currDelay,
                currTailFromEnd,
                currReOpenOnChunk,
                Tailer.DEFAULT_BUFSIZE,
                stats,
                setName,
                stateFile);
        
        if( stopAfter != null
                && stopAfter > 0 )
        {
            stopSchedulerExe = Executors.newScheduledThreadPool(1);

            stopSchedulerExe.schedule(() ->
            {
                if( mainTailer != null )
                {
                    mainTailer.stop();
                }

                stopSchedulerExe.shutdownNow();
            }, stopAfter, TimeUnit.SECONDS);
            
            stopSchedulerExe.shutdown();
        }
        else
        {
            stopSchedulerExe = null;
        }

        if( saveState != null
                && saveState )
        {
            BasicThreadFactory tailerSaveFactory = new BasicThreadFactory.Builder()
                    .namingPattern("tailerSaveThread-%d")
                    .build();

            statsSchedulerExe = Executors.newScheduledThreadPool(1, tailerSaveFactory);

            statsSchedulerExe.scheduleWithFixedDelay(() ->
            {
                try
                {
                    stats.save();
                }
                catch( LogCheckException ex )
                {
                    log.debug("Error saving the logger state", ex);
                }
            }, LogCheckConstants.DEFAULT_SAVE_STATE_INTERVAL_SECONDS,
               LogCheckConstants.DEFAULT_SAVE_STATE_INTERVAL_SECONDS,
               TimeUnit.SECONDS );
        }
        else
        {
            statsSchedulerExe = null;
        }

        BasicThreadFactory tailerFactory = new BasicThreadFactory.Builder()
                .namingPattern("tailerthread-%d")
                .build();

        ExecutorService tailerExe = Executors.newSingleThreadExecutor(tailerFactory);
        FutureTask<Long> mainTailerTask = new FutureTask<>(mainTailer);

        // Tailer start
        Future tailerExeRes = tailerExe.submit(mainTailer);

        tailerExe.shutdown();

        Long tailerRes = null;

        try
        {
            while( tailerRes == null )
            {
                if( tailerExeRes.isDone() )
                {
                    // Log polling thread has completed.  Generally this should
                    // not happen until we're shutting down.
                    tailerRes = mainTailerTask.get();
                }

                // At this point we can block/wait on all threads but I'll
                // sleep for now until there's some processing to be done on
                // the main thread.
                Thread.sleep(2000);
            }
        }
        catch (InterruptedException ex)
        {
            log.error("Application 'run' thread was interrupted", ex);

            // We don't have to do much here because the interrupt got us out
            // of the while loop.

        }
        catch (ExecutionException ex)
        {
            log.error("Application 'run' execution error", ex);
        }
        finally
        {
            tailerExe.shutdownNow();

            if( stopSchedulerExe != null )
            {
                stopSchedulerExe.shutdownNow();
            }

            if( statsSchedulerExe != null )
            {
                statsSchedulerExe.shutdownNow();
            }
        }

        return res;
    }
}
