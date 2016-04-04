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
import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.enums.LCLogEntryStoreType;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.log.impl.builder.NCSACommonLogBuilder;
import com.sludev.logs.logcheck.log.impl.builder.SingleLineBuilder;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.log.ILogEntrySink;
import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.log.impl.builder.MultiLineDelimitedBuilder;
import com.sludev.logs.logcheck.log.impl.LogEntryQueueSink;
import com.sludev.logs.logcheck.log.impl.LogEntryQueueSource;
import com.sludev.logs.logcheck.store.ILogEntryStore;
import com.sludev.logs.logcheck.store.LogEntryStore;
import com.sludev.logs.logcheck.store.impl.LogEntryConsole;
import com.sludev.logs.logcheck.store.impl.LogEntryElasticSearch;
import com.sludev.logs.logcheck.store.impl.LogEntrySimpleFile;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import com.sludev.logs.logcheck.tail.LogCheckTail;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckLockFile;
import com.sludev.logs.logcheck.utils.LogCheckUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mainly starts and manages threads, or concurrency in general.
 *
 * @author Kervin
 */
public class LogCheckRun implements Callable<LogCheckResult>
{
    private static final Logger LOGGER
                             = LogManager.getLogger(LogCheckRun.class);
    
    private final LogCheckConfig m_config;
    private Path m_lockFile;

    public LogCheckRun(LogCheckConfig config)
    {
        this.m_config = config;
    }

    public Path getLockFile()
    {
        return m_lockFile;
    }

    public void setLockFile(Path l)
    {
        m_lockFile = l;
    }
    
    public LogCheckConfig getConfig()
    {
        return m_config;
    }

    @Override
    public LogCheckResult call() throws LogCheckException, ExecutionException
    {
        LOGGER.debug(String.format("call(): config :\n%s\n", m_config));

        UUID currRunUUID = UUID.randomUUID();

        // Use a thread-safe queue.  We enqueue/dequeue on different threads
        BlockingDeque<LogEntry> currQ = new LinkedBlockingDeque<>();

        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);
        
        if( BooleanUtils.isTrue(m_config.isShowVersion()) )
        {
            LogCheckUtil.displayVersion();
            res = LogCheckResult.from(LCResultStatus.SUCCESS);
            
            return res;
        }

        if( m_config.getPreferredDir() != null )
        {
            // This doesn't do much, but less set user.dir for CWD hint
            if( Files.notExists(m_config.getPreferredDir() ) )
            {
                String msg = String.format("Preferred Directory supplied does not exist. '%s'",
                        m_config.getPreferredDir());

                LOGGER.debug(msg);

                throw new LogCheckException(msg);
            }

            // FIXME : Though this doesn't *actually* modify the CWD
            String prefDirStr = m_config.getPreferredDir().toAbsolutePath().toString();
            System.setProperty("user.dir", prefDirStr);
        }

        m_lockFile = m_config.getLockFilePath();

        // Setup the acquiring and release of the lock file
        acquireLockFile(m_lockFile);
        setupLockFileShutdownHook(m_lockFile);

        final ILogEntrySource logEntrySource = LogEntryQueueSource.from(currQ);
        final ILogEntrySink logEntrySink = LogEntryQueueSink.from(currQ,
                m_config.getLogDeduplicationDuration(),
                m_config.getLogCutoffDate(),
                m_config.getLogCutoffDuration(),
                null);

        List<ILogEntryBuilder> currLogEntryBuilders = new ArrayList<>(10);

        // FIXME : Support multiple builders.  But for now we don't need it so all but the last will be ignored.
        for( LCLogEntryBuilderType builder : m_config.getLogEntryBuilders() )
        {
            if( builder == null )
            {
                throw new LogCheckException("Log Check Builder cannot be null");
            }

            // Choose the correct Log Entry Builder
            switch( builder )
            {
                case MULTILINE_DELIMITED:
                {
                    // Log tailing related objects
                    List<String> ignoreList = new ArrayList<>(10);
                    ignoreList.add(LogCheckConstants.DEFAULT_MULTILINE_IGNORE_LINE);

                    currLogEntryBuilders.add(MultiLineDelimitedBuilder.from(
                            LogCheckConstants.DEFAULT_MULTILINE_ROW_START_PATTERN,
                            LogCheckConstants.DEFAULT_MULTILINE_ROW_END_PATTERN,
                            null,
                            LogCheckConstants.DEFAULT_MULTILINE_COL_END_PATTERN,
                            ignoreList,
                            logEntrySink));
                }
                break;

                case NCSA_COMMON_LOG:
                {
                    currLogEntryBuilders.add(NCSACommonLogBuilder.from(null, logEntrySink));
                }
                break;

                case SINGLELINE:
                {
                    currLogEntryBuilders.add(SingleLineBuilder.from(null, logEntrySink));
                }
                break;

                default:
                    String errMsg = String.format("Error creating LogEntry builder '%s'",
                            m_config.getLogEntryBuilders());

                    LOGGER.debug(errMsg);
                    throw new LogCheckException(errMsg);
            }
        }

        Path currDeDupeDirPath = m_config.fixPathWithPreferred(
                                            m_config.getDeDupeDirPath());

        // Create missing directories if necessary
        if( BooleanUtils.isTrue(m_config.willCreateMissingDirs()) )
        {
            if( (currDeDupeDirPath != null)
                    && Files.notExists(currDeDupeDirPath) )
            {
                try
                {
                    Files.createDirectory(currDeDupeDirPath);
                }
                catch( IOException ex )
                {
                    String msg = String.format("Failed creating De-duplication directory '%s'",
                            currDeDupeDirPath);

                    LOGGER.debug(msg, ex);

                    throw new LogCheckException(msg, ex);
                }
            }
        }

        // Create the main Tailer.
        // And pass into it the previously selected Log Entry Builder.
        LogCheckTail lct = LogCheckTail.from(currLogEntryBuilders,
                m_config.fixPathWithPreferred(m_config.getLogPath()),
                m_config.fixPathWithPreferred(m_config.getDeDupeDirPath()),
                null, // startPosition
                m_config.getPollIntervalSeconds(),
                m_config.willContinueState(),
                m_config.isTailFromEnd(),
                m_config.willReadReOpenLogFile(),
                m_config.willSaveState(),
                m_config.willIgnoreStartPositionError(),
                m_config.willValidateTailerStats(),
                m_config.willCollectState(),
                true, // watch backup directory
                m_config.willTailerBackupReadLog(),
                m_config.willTailerBackupReadLogReverse(),
                m_config.willTailerBackupReadPriorLog(),
                m_config.willStopOnEOF(),
                m_config.isReadOnlyFileMode(),
                true, // Is main thread?
                false, // Reset statistics?
                null, // bufferSize
                m_config.getReadLogFileCount(),
                m_config.getReadMaxDeDupeEntries(),
                m_config.getStopAfter(), // stopAfter
                m_config.getIdBlockHashType(),
                m_config.getIdBlockSize(),
                m_config.getSetName(),
                m_config.fixPathWithPreferred(m_config.getStateFilePath()),
                m_config.fixPathWithPreferred(m_config.getStateProcessedLogsFilePath()),
                m_config.fixPathWithPreferred(m_config.getErrorFilePath()),
                m_config.fixPathWithPreferred(m_config.getTailerLogBackupDir()),
                m_config.getPreferredDir(),
                m_config.getTailerBackupLogNameComps(),
                m_config.getTailerBackupLogCompression(),
                m_config.getTailerBackupLogNameRegex(),
                m_config.getDebugFlags());

        List<ILogEntryStore> currStores = new ArrayList<>(10);

        for( LCLogEntryStoreType store : m_config.getLogEntryStores() )
        {
            if( store == null )
            {
                throw new LogCheckException("Log Check Store cannot be null");
            }

            switch( store )
            {
                case ELASTICSEARCH:
                    // Elastic Search
                    LogEntryElasticSearch lees = LogEntryElasticSearch.from(m_config.getElasticsearchURL(),
                            null,
                            m_config.getElasticsearchLogType());

                    lees.setElasticsearchIndexPrefix(m_config.getElasticsearchIndexPrefix());
                    lees.setElasticsearchIndexNameFormat(m_config.getElasticsearchIndexNameFormat());
                    lees.setElasticsearchIndexName(m_config.getElasticsearchIndexName());

                    currStores.add(lees);
                    break;

                case CONSOLE:
                    LogEntryConsole lec = LogEntryConsole.from();

                    currStores.add(lec);
                    break;

                case SIMPLEFILE:
                    LogEntrySimpleFile lesf = LogEntrySimpleFile.from(
                            m_config.fixPathWithPreferred(m_config.getStoreLogPath()),
                            m_config.willStoreReOpenLogFile());

                    currStores.add(lesf);
                    break;

                default:
                    LOGGER.debug(String.format("Unknown Log Entry %s", m_config.getLogEntryStores()));
                    break;
            }
        }

        if( currStores.size() < 1 )
        {
            throw new LogCheckException("No valid log store found");
        }

        for( ILogEntryStore store : currStores )
        {
            store.init();
        }

        LogEntryStore storeWrapper = LogEntryStore.from(logEntrySource,
                currStores,
                m_config.fixPathWithPreferred(m_config.getDeDupeDirPath()),
                m_config.getSetName(),
                currRunUUID,
                m_config.getDeDupeMaxLogsBeforeWrite(),
                m_config.getDeDupeMaxLogsPerFile(),
                m_config.getDeDupeMaxLogFiles(),
                m_config.getDeDupeIgnorePercent(),
                m_config.getDeDupeSkipPercent(),
                m_config.getDeDupeIgnoreCount(),
                m_config.getDeDupeSkipCount(),
                m_config.getDeDupeDefaultAction());

        // Start the relevant threads
        BasicThreadFactory logCheckTailerFactory = new BasicThreadFactory.Builder()
            .namingPattern("runTailerThread-%d")
            .build();
        
        BasicThreadFactory logStoreFactory = new BasicThreadFactory.Builder()
            .namingPattern("runLogStoreThread-%d")
            .build();
        
        ExecutorService logCheckTailerExe = Executors.newSingleThreadExecutor(logCheckTailerFactory);
        ExecutorService logStoreExe = Executors.newSingleThreadExecutor(logStoreFactory);
        
        Future<LogCheckResult> fileTailFuture = logCheckTailerExe.submit(lct);
        Future<LogCheckResult> logStoreFuture = logStoreExe.submit(storeWrapper);
        
        logCheckTailerExe.shutdown();
        logStoreExe.shutdown();
        
        LogCheckResult fileTailRes = null;
        LogCheckResult logStoreRes = null;
        
        try
        {
            boolean run = true;
            boolean storeIsRunning = true;

            //while( (fileTailRes == null) || (logStoreRes == null) )
            while( run )
            {
                if( storeIsRunning == false )
                {
                    // The Log Store stopped so we should stop the tailer as well
                    fileTailFuture.cancel(true);
                    fileTailRes = LogCheckResult.from(LCResultStatus.CANCELLED);
                    run = false;
                }
                else if( fileTailFuture.isDone() )
                {
                    run = false;

                    // Log polling thread has completed.  Generally this should
                    // not happen until we're shutting down.
                    fileTailRes = fileTailFuture.get();

                    // If Tailer thread is done, then cancel/interrupt the store thread
                    // E.g. useful for implementing the --stop-after feature
                    if( fileTailRes.getStatus() == LCResultStatus.SUCCESS )
                    {
                        LOGGER.debug("\n==============================\n"
                                + "CANCELLING the Log Store thread because the Tailer thread was done first."
                                + "\n==============================\n");

                        logStoreFuture.cancel(true);
                    }
                }

                // Check on the Log Store Thread
                if( storeIsRunning )
                {
                    if( logStoreFuture.isCancelled() )
                    {
                        storeIsRunning = false;

                        logStoreRes = LogCheckResult.from(LCResultStatus.CANCELLED);
                    }
                    else
                    {
                        if( logStoreFuture.isDone() )
                        {
                            storeIsRunning = false;

                            // Log storage thread has completed.  Generally this should
                            // not happen until we're shutting down.
                            try
                            {
                                logStoreRes = logStoreFuture.get();
                            }
                            catch( InterruptedException ex )
                            {
                                LOGGER.error("Log Store Thread interrupted.", ex);

                                logStoreRes = LogCheckResult.from(LCResultStatus.INTERRUPTED);
                            }
                            catch( ExecutionException ex )
                            {
                                LOGGER.error("Log Store Thread failed.", ex);

                                logStoreRes = LogCheckResult.from(LCResultStatus.FAIL);
                            }
                        }
                    }
                }
                
                // At this point we can block/wait on all threads but I'll
                // sleep for now until there's some processing to be done on
                // the main thread.
                if( run )
                {
                    Thread.sleep(2000);
                }
            }

            if( ((logStoreRes != null) && (logStoreRes.getStatus() == LCResultStatus.INTERRUPTED))
                    || (fileTailRes.getStatus() == LCResultStatus.INTERRUPTED) )
            {
                // If either of the threads were interrupted, then mark as interrupted.
                res = LogCheckResult.from(LCResultStatus.INTERRUPTED);
            }
            else
            {
                if( ((logStoreRes != null) && (logStoreRes.getStatus() == LCResultStatus.FAIL))
                        || (fileTailRes.getStatus() == LCResultStatus.FAIL) )
                {
                    // If either of the threads failed, then mark as failed
                    res = LogCheckResult.from(LCResultStatus.FAIL);
                }
                else if( fileTailRes.getStatus() == LCResultStatus.SUCCESS )
                {
                    // if Tailer thread succeed, ignore the Log Store result
                    res = LogCheckResult.from(LCResultStatus.SUCCESS);
                }
            }
        }
        catch (InterruptedException ex)
        {
            LOGGER.error("Log Check Run thread was interrupted", ex);
            
            // We don't have to do much here because the interrupt got us out
            // of the while loop.

            res = LogCheckResult.from(LCResultStatus.INTERRUPTED);
        }
        finally
        {
            logCheckTailerExe.shutdownNow();
            logStoreExe.shutdownNow();

            if( (currStores != null) && (currStores.size() > 1) )
            {
                for( ILogEntryStore store : currStores )
                {
                    store.destroy();
                }
            }
        }
        
        return res;
    }

    public static void acquireLockFile(final Path lk) throws LogCheckException
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
                catch (IOException|LogCheckException ex)
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
    
    public static void setupLockFileShutdownHook(final Path lk)
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
                            
                            LOGGER.error(errMsg);
                        }
                    }
                    else
                    {
                        LOGGER.error(String.format(
                                    "Expected lock file '%s' to exist.", lk));
                    }
                }
            }
        }); 
    }
}
