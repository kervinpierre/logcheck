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
import com.sludev.logs.logcheck.utils.LogCheckUtil;
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
        
        // Log tailing related objects
        LogEntryBuilder currLogEntryBuilder = new LogEntryBuilder();
        currLogEntryBuilder.setCompletionCallback(logEntrySink);
        
        LogCheckTail lct = new LogCheckTail();
        lct.setLogFile(config.getLogPath());
        lct.setDelay(config.getPollIntervalSeconds());
        lct.setTailFromEnd(!config.isFileFromStart());
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
            log.error("Application thread was interrupted", ex);
        }
        catch (ExecutionException ex)
        {
            log.error("Application execution error", ex);
        }
        
        return res;
    }

}
