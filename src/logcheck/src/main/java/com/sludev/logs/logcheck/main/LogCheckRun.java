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
package com.sludev.logs.logcheck.main;

import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.log.impl.builder.NCSACommonLogBuilder;
import com.sludev.logs.logcheck.log.impl.builder.SingleLineBuilder;
import com.sludev.logs.logcheck.model.LogEntry;
import com.sludev.logs.logcheck.log.ILogEntrySink;
import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.log.impl.builder.MultiLineDelimitedBuilder;
import com.sludev.logs.logcheck.log.impl.LogEntryQueueSink;
import com.sludev.logs.logcheck.log.impl.LogEntryQueueSource;
import com.sludev.logs.logcheck.store.ILogEntryStore;
import com.sludev.logs.logcheck.store.LogEntryConsole;
import com.sludev.logs.logcheck.store.LogEntryElasticSearch;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import com.sludev.logs.logcheck.tail.LogCheckTail;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckLockFile;
import com.sludev.logs.logcheck.utils.LogCheckUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kervin
 */
public class LogCheckRun implements Callable<LogCheckResult>
{
    private static final Logger log 
                             = LogManager.getLogger(LogCheckRun.class);
    
    private LogCheckConfig config;
    private Path lockFile;
    
    public Path getLockFile()
    {
        return lockFile;
    }

    public void setLockFile(Path l)
    {
        lockFile = l;
    }
    
    public LogCheckConfig getConfig()
    {
        return config;
    }

    public void setConfig(LogCheckConfig config)
    {
        this.config = config;
    }

    @Override
    public LogCheckResult call() throws Exception
    {
        // Use a thread-safe queue.  We enqueue/dequeue on different threads
        BlockingDeque<LogEntry> currQ = new LinkedBlockingDeque<>();

        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);
        
        if( config.getShowVersion() != null
                && config.getShowVersion() )
        {
            LogCheckUtil.displayVersion();
            res = LogCheckResult.from(LCResultStatus.SUCCESS);
            
            return res;
        }
        
        setLockFile( config.getLockFilePath() );
        
        // Setup the acquiring and release of the lock file
        acquireLockFile( getLockFile() );
        setupLockFileShutdownHook( getLockFile() );

        final ILogEntrySource logEntrySource = LogEntryQueueSource.from(currQ);
        final ILogEntrySink logEntrySink = LogEntryQueueSink.from(currQ,
                config.getLogDeduplicationDuration(),
                config.getLogCutoffDate(),
                config.getLogCutoffDuration(),
                null);

        ILogEntryBuilder currLogEntryBuilder = null;

        // Choose the correct Log Entry Builder
        switch(config.getLogEntryBuilder())
        {
            case MULTILINE_DELIMITED:
                {
                    // Log tailing related objects
                    List<String> ignoreList = new ArrayList<>();
                    ignoreList.add(LogCheckConstants.DEFAULT_MULTILINE_IGNORE_LINE);

                    currLogEntryBuilder = MultiLineDelimitedBuilder.from(
                            LogCheckConstants.DEFAULT_MULTILINE_ROW_START_PATTERN,
                            LogCheckConstants.DEFAULT_MULTILINE_ROW_END_PATTERN,
                            null,
                            LogCheckConstants.DEFAULT_MULTILINE_COL_END_PATTERN,
                            ignoreList,
                            logEntrySink);
                }
                break;

            case NCSACOMMONLOG:
                {
                    currLogEntryBuilder = NCSACommonLogBuilder.from(null, logEntrySink);
                }
                break;

            case SINGLELINE:
                {
                    currLogEntryBuilder = SingleLineBuilder.from(null, logEntrySink);
                }
                break;

            default:
                String errMsg = String.format("Error creating LogEntry builder '%s'",
                        config.getLogEntryBuilder());

                log.debug(errMsg);
                throw new LogCheckException(errMsg);
        }

        // Create the main Tailer.
        // And pass into it the previously selected Log Entry Builder.
        LogCheckTail lct = LogCheckTail.from(currLogEntryBuilder,
                config.getLogPath(),
                config.getPollIntervalSeconds(),
                config.isTailFromEnd(),
                null,
                config.getSaveState(),
                null,
                null,
                config.getIdBlockHashType(),
                config.getIdBlockSize(),
                config.getSetName(),
                config.getStateFilePath(),
                config.getErrorFilePath());

        FutureTask<LogCheckResult> logStoreTask = null;
        ILogEntryStore currStore = null;

        // Log storage related objects
        if( config.getElasticsearchURL() != null )
        {
            // Elastic Search
            LogEntryElasticSearch lees = LogEntryElasticSearch.from(logEntrySource);

            lees.setElasticsearchURL(config.getElasticsearchURL());
            lees.setElasticsearchIndexPrefix(config.getElasticsearchIndexPrefix());
            lees.setElasticsearchIndexNameFormat(config.getElasticsearchIndexNameFormat());
            lees.setElasticsearchIndexName(config.getElasticsearchIndexName());
            lees.setElasticsearchLogType(config.getElasticsearchLogType());

            currStore = lees;
        }
        else if( config.getPrintLog() != null
                   && config.getPrintLog() )
        {
            LogEntryConsole lec = LogEntryConsole.from(logEntrySource);

            currStore = lec;
        }

        if( currStore == null )
        {
            throw new LogCheckException("No valid log store found");
        }

        currStore.init();
        logStoreTask = new FutureTask<>(currStore);

        // Start the relevant threads
        FutureTask<LogCheckResult> logCheckTailerTask = new FutureTask<>(lct);

        BasicThreadFactory logCheckTailerFactory = new BasicThreadFactory.Builder()
            .namingPattern("logCheckTailerThread-%d")
            .build();
        
        BasicThreadFactory logStoreFactory = new BasicThreadFactory.Builder()
            .namingPattern("logstorethread-%d")
            .build();
        
        ExecutorService logCheckTailerExe = Executors.newSingleThreadExecutor(logCheckTailerFactory);
        ExecutorService logStoreExe = Executors.newSingleThreadExecutor(logStoreFactory);
        
        Future fileTailExeRes = logCheckTailerExe.submit(logCheckTailerTask);
        Future logStoreExeRes = logStoreExe.submit(logStoreTask);
        
        logCheckTailerExe.shutdown();
        logStoreExe.shutdown();
        
        LogCheckResult fileTailRes = null;
        LogCheckResult logStoreRes = null;
        
        try
        {
            while( fileTailRes == null || logStoreRes == null )
            {
                if( fileTailExeRes.isDone() )
                {
                    // Log polling thread has completed.  Generally this should
                    // not happen until we're shutting down.
                    fileTailRes = logCheckTailerTask.get();
                }
                
                if( logStoreExeRes.isDone() )
                {
                    // Log storage thread has completed.  Generally this should
                    // not happen until we're shutting down.
                    logStoreRes = logStoreTask.get();
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
            logCheckTailerExe.shutdownNow();
            logStoreExe.shutdownNow();
        }
        
        return res;
    }

    private void acquireLockFile(final Path lk) throws LogCheckException
    {
        if( lk != null )
        {
            if( Files.exists(lk) )
            {
                int runningPID = 0;
                
                try
                {
                    runningPID = LogCheckLockFile.getLockPID(lk);
                }
                catch (IOException ex)
                {
                    throw new LogCheckException(String.format(
                            "Exception reading lock file '%s'", lk), ex);
                }
                
                throw new LogCheckException(String.format(
                            "Process '%d' already has the lock file '%s'",
                                            runningPID, lk));
            }
            else
            {
                if( LogCheckLockFile.acquireLockFile(lk) == false )
                {
                    String errMsg = String.format(
                            "Error aquiring the lock file '%s'", lk);
                    throw new LogCheckException(errMsg);
                }
            }
        }
    }
    
    private void setupLockFileShutdownHook(final Path lk)
    {
        Runtime.getRuntime().addShutdownHook(new Thread() 
        {
            @Override
            public void run() 
            {
                // Release the locks if necessary
                if( lk != null )
                {
                    if( Files.exists(lk) )
                    {
                        if( LogCheckLockFile.releaseLockFile(lk) == false )
                        {
                            String errMsg = String.format(
                                    "Error releasing the lock file '%s'", lk);
                            
                            log.error(errMsg);
                        }
                    }
                    else
                    {
                        log.error(String.format(
                                    "Expected lock file '%s' to exist.", lk));
                    }
                }
            }
        }); 
    }
}
