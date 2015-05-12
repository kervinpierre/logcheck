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

import com.sludev.logs.logcheck.config.LogCheckConfig;
import com.sludev.logs.logcheck.enums.LogCheckResultStatus;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.log.ILogEntrySink;
import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.log.LogEntryBuilder;
import com.sludev.logs.logcheck.log.LogEntryQueueSink;
import com.sludev.logs.logcheck.log.LogEntryQueueSource;
import com.sludev.logs.logcheck.store.LogEntryElasticSearch;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import com.sludev.logs.logcheck.tail.LogCheckTail;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckLockFile;
import com.sludev.logs.logcheck.utils.LogCheckUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private final ILogEntrySource logEntrySource;
    private final ILogEntrySink logEntrySink;
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

    public LogCheckRun()
    {
        // Use a thread-safe queue.  We enque/deque on different threads
        BlockingDeque<LogEntry> currQ = new LinkedBlockingDeque<>();
        
        logEntrySource = new LogEntryQueueSource();
        logEntrySink = new LogEntryQueueSink();
        
        logEntrySource.setCompletedLogEntries(currQ);
        logEntrySink.setCompletedLogEntries(currQ);
    }

    @Override
    public LogCheckResult call() throws Exception
    {
        LogCheckResult res = new LogCheckResult();
        
        if( config.isShowVersion() )
        {
            LogCheckUtil.displayVersion();
            res.setStatus(LogCheckResultStatus.SUCCESS);
            
            return res;
        }
        
        setLockFile( config.getLockFilePath() );
        
        // Setup the acquiring and release of the lock file
        acquireLockFile( getLockFile() );
        setupLockFileShutdownHook( getLockFile() );
        
        // Log tailing related objects
        LogEntryBuilder currLogEntryBuilder = new LogEntryBuilder();
        currLogEntryBuilder.setCompletionCallback(logEntrySink);
        
        LogCheckTail lct = new LogCheckTail();
        lct.setLogFile(config.getLogPath());
        lct.setDelay(config.getPollIntervalSeconds());
        lct.setTailFromEnd(config.isTailFromEnd());
        lct.setMainLogEntryBuilder(currLogEntryBuilder);
        
        // Log storage related objects
        LogEntryElasticSearch lees = new LogEntryElasticSearch();
        lees.setMainLogEntrySource(logEntrySource);
        lees.setElasticsearchURL(config.getElasticsearchURL());
        lees.setElasticsearchIndexPrefix(config.getElasticsearchIndexPrefix());
        lees.setElasticsearchIndexNameFormat(config.getElasticsearchIndexNameFormat());
        lees.setElasticsearchIndexName(config.getElasticsearchIndexName());
        lees.setElasticsearchLogType(config.getElasticsearchLogType());
        
        lees.init();
        
        // Start the relevant threads
        FutureTask<LogCheckResult> logFileTailTask = new FutureTask<>(lct);
        FutureTask<LogCheckResult> logStoreTask = new FutureTask<>(lees);
        
        BasicThreadFactory fileTailFactory = new BasicThreadFactory.Builder()
            .namingPattern("logpollthread-%d")
            .build();
        
        BasicThreadFactory logStoreFactory = new BasicThreadFactory.Builder()
            .namingPattern("logstorethread-%d")
            .build();
        
        ExecutorService fileTailExe = Executors.newSingleThreadExecutor(fileTailFactory);
        ExecutorService logStoreExe = Executors.newSingleThreadExecutor(logStoreFactory);
        
        Future fileTailExeRes = fileTailExe.submit(logFileTailTask);
        Future logStoreExeRes = logStoreExe.submit(logStoreTask);
        
        fileTailExe.shutdown();
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
                    fileTailRes = logFileTailTask.get();
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
            fileTailExe.shutdownNow();
            logStoreExe.shutdownNow();
            
            // But work-around.  Interrupt Tailer object using a separate 'stop'
            // thread.  This should not be necessary in Commons-IO 2.5+ since
            // Tailer will stop on InterruptedException.
            if( lct.getStopThreadExe() != null )
            {
                lct.getStopThreadExe().shutdownNow();
            }
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
