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
import com.sludev.logs.logcheck.enums.LCLogSourceType;
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
import com.sludev.logs.logcheck.tail.ILogCheckTail;
import com.sludev.logs.logcheck.tail.impl.FileLogCheckTail;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckLockFile;
import com.sludev.logs.logcheck.utils.LogCheckUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mainly starts and manages threads, or concurrency in general.
 *
 * @author Kervin
 */
public class LogCheckRun implements Callable<Map<Integer, LogCheckResult>>
{
    private static final Logger LOGGER
                             = LogManager.getLogger(LogCheckRun.class);
    
    private final LinkedHashMap<Integer, LogCheckConfig> m_configs;
    private Path m_lockFile;

    public LogCheckRun(LinkedHashMap<Integer, LogCheckConfig> configs)
    {
        this.m_configs = configs;
    }

    public Path getLockFile()
    {
        return m_lockFile;
    }

    public void setLockFile(Path l)
    {
        m_lockFile = l;
    }
    
    public LinkedHashMap<Integer, LogCheckConfig> getConfigs()
    {
        return m_configs;
    }

    @Override
    public Map<Integer, LogCheckResult> call() throws LogCheckException, ExecutionException
    {
        if( m_configs == null || m_configs.size() < 1 )
        {
            throw new LogCheckException("No configuration objects passed");
        }

        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(String.format("call(): configs count :\n%s\n",
                    m_configs.size()));
            for( Integer ky : m_configs.keySet() )
            {
                LOGGER.debug(String.format("\n%d => %s\n",
                                            ky, m_configs.get(ky)));
            }
        }

        UUID currRunUUID = UUID.randomUUID();

        // Use a thread-safe queue.  We enqueue/dequeue on different threads
        // One queue per destination/store
        Map<ILogEntryStore, BlockingDeque<LogEntry>> storeQueues = new HashMap<>();

        Map<Integer, LogCheckResult> res = new HashMap<>(10);

        LogCheckConfig firstConf = m_configs.get(0);

        if( BooleanUtils.isTrue(firstConf.isShowVersion()) )
        {
            LogCheckUtil.displayVersion();
            res.put(0, LogCheckResult.from(LCResultStatus.SUCCESS));
            
            return res;
        }

        // Preferred directory setup in first config
        if( firstConf.getPreferredDir() != null )
        {
            // This doesn't do much, but less set user.dir for CWD hint
            if( Files.notExists(firstConf.getPreferredDir() ) )
            {
                String msg = String.format("Preferred Directory supplied does not exist. '%s'",
                        firstConf.getPreferredDir());

                LOGGER.debug(msg);

                throw new LogCheckException(msg);
            }

            // FIXME : Though this doesn't *actually* modify the CWD
            String prefDirStr = firstConf.getPreferredDir().toAbsolutePath().toString();
            System.setProperty("user.dir", prefDirStr);
        }

        m_lockFile = firstConf.getLockFilePath();

        // Setup the acquiring and release of the lock file
        acquireLockFile(m_lockFile);
        setupLockFileShutdownHook(m_lockFile);

        // Loop configs for log sources
        Map<Integer, LCLogSourceType> logSourceTypeMap = new HashMap<>(10);
        Map<Integer, ILogEntrySource> logSourceMap = new HashMap<>(10);
        Map<Integer, List<ILogEntryBuilder>> logBuilderMap = new HashMap<>(10);
        for(Integer confKey : m_configs.keySet() )
        {
            if( confKey == 0 )
            {
                continue;
            }

            LogCheckConfig currConfig = m_configs.get(confKey);

            LCLogSourceType currSrcType = currConfig.getLogSourceType();
            if( currSrcType == null )
            {
                String errMsg = String.format("Missing source for configuration '%d'",
                        confKey);

                LOGGER.debug(errMsg);

                throw new LogCheckException(errMsg);
            }

            BlockingDeque<LogEntry> currQ = new LinkedBlockingDeque<>();

            // Every log source should have its own dedupe queue
            final ILogEntrySource logEntrySource = LogEntryQueueSource.from(currQ);
            final ILogEntrySink logEntrySink = LogEntryQueueSink.from(currQ,
                    currConfig.getLogDeduplicationDuration(),
                    currConfig.getLogCutoffDate(),
                    currConfig.getLogCutoffDuration(),
                    null);

            logSourceMap.put(confKey, logEntrySource);
            logSourceTypeMap.put(confKey, currSrcType);

            List<ILogEntryBuilder> currLogEntryBuilders = new ArrayList<>(10);

            if( (currConfig.getLogEntryBuilders() == null)
                    || (currConfig.getLogEntryBuilders().size() < 1) )
            {
                throw new LogCheckException("No valid Log Entry Builders found");
            }

            logBuilderMap.put(confKey, currLogEntryBuilders);

            // FIXME : Support multiple builders.  But for now we don't need it so all but the last will be ignored.
            for( LCLogEntryBuilderType builder : currConfig.getLogEntryBuilders() )
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
                                currConfig.getLogEntryBuilders());

                        LOGGER.debug(errMsg);
                        throw new LogCheckException(errMsg);
                }
            }

        }

        Path currDeDupeDirPath = firstConf.fixPathWithPreferred(
                                            firstConf.getDeDupeDirPath());

        // Create missing directories if necessary
        if( BooleanUtils.isTrue(firstConf.willCreateMissingDirs()) )
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

        Map<Integer, ILogCheckTail> tailerMap = new HashMap<>(10);
        for( Integer confKey : logSourceTypeMap.keySet() )
        {
            LCLogSourceType tailerType = logSourceTypeMap.get(confKey);
            LogCheckConfig currConfig = m_configs.get(confKey);

            // Create the tailer for each config
            // And pass into it the previously selected Log Entry Builder.
            switch( tailerType )
            {
                case FILE_LOCAL:
                    FileLogCheckTail lct = FileLogCheckTail.from(
                            logBuilderMap.get(confKey),
                        currConfig.fixPathWithPreferred(currConfig.getLogPath()),
                        currConfig.fixPathWithPreferred(currConfig.getDeDupeDirPath()),
                        null, // startPosition
                        currConfig.getPollIntervalSeconds(),
                        currConfig.willContinueState(),
                        currConfig.isTailFromEnd(),
                        currConfig.willReadReOpenLogFile(),
                        currConfig.willSaveState(),
                        currConfig.willIgnoreStartPositionError(),
                        currConfig.willValidateTailerStats(),
                        currConfig.willCollectState(),
                        true, // watch backup directory
                        currConfig.willTailerBackupReadLog(),
                        currConfig.willTailerBackupReadLogReverse(),
                        currConfig.willTailerBackupReadPriorLog(),
                        currConfig.willStopOnEOF(),
                        currConfig.isReadOnlyFileMode(),
                        true, // Is main thread?
                        false, // Reset statistics?
                        null, // bufferSize
                        currConfig.getReadLogFileCount(),
                        currConfig.getReadMaxDeDupeEntries(),
                        currConfig.getStopAfter(), // stopAfter
                        currConfig.getIdBlockHashType(),
                        currConfig.getIdBlockSize(),
                        currConfig.getSetName(),
                        currConfig.fixPathWithPreferred(currConfig.getStateFilePath()),
                        currConfig.fixPathWithPreferred(currConfig.getStateProcessedLogsFilePath()),
                        currConfig.fixPathWithPreferred(currConfig.getErrorFilePath()),
                        currConfig.fixPathWithPreferred(currConfig.getTailerLogBackupDir()),
                        currConfig.getPreferredDir(),
                        currConfig.getTailerBackupLogNameComps(),
                        currConfig.getTailerBackupLogCompression(),
                        currConfig.getTailerBackupLogNameRegex(),
                        currConfig.getDebugFlags());

                    tailerMap.put(confKey, lct);
                    break;
            }
        }

        // Loop configs for stores
        Map<Integer, List<ILogEntryStore>> storeMap = new HashMap<>(10);
        Map<Integer, LogEntryStore> storeWrapperMap = new HashMap<>(10);
        for(Integer confKey : m_configs.keySet() )
        {
            if( confKey == 0 )
            {
                continue;
            }

            LogCheckConfig currConfig = m_configs.get(confKey);

            List<ILogEntryStore> currStores = new ArrayList<>(10);

            storeMap.put(confKey, currStores);

            // FIXME : Store may have already be configured

            for( LCLogEntryStoreType store : currConfig.getLogEntryStores() )
            {
                if( store == null )
                {
                    throw new LogCheckException("Log Check Store cannot be null");
                }

                switch( store )
                {
                    case ELASTICSEARCH:
                        // Elastic Search
                        LogEntryElasticSearch lees = LogEntryElasticSearch.from(currConfig.getElasticsearchURL(),
                                null,
                                currConfig.getElasticsearchLogType());

                        lees.setElasticsearchIndexPrefix(currConfig.getElasticsearchIndexPrefix());
                        lees.setElasticsearchIndexNameFormat(currConfig.getElasticsearchIndexNameFormat());
                        lees.setElasticsearchIndexName(currConfig.getElasticsearchIndexName());

                        currStores.add(lees);
                        break;

                    case CONSOLE:
                        LogEntryConsole lec = LogEntryConsole.from();

                        currStores.add(lec);
                        break;

                    case SIMPLEFILE:
                        LogEntrySimpleFile lesf = LogEntrySimpleFile.from(
                                currConfig.fixPathWithPreferred(currConfig.getStoreLogPath()),
                                currConfig.willStoreReOpenLogFile());

                        currStores.add(lesf);
                        break;

                    default:
                        LOGGER.debug(String.format("Unknown Log Entry %s", currConfig.getLogEntryStores()));
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

            LogEntryStore storeWrapper = LogEntryStore.from(logSourceMap.get(confKey),
                    currStores,
                    currConfig.fixPathWithPreferred(currConfig.getDeDupeDirPath()),
                    currConfig.getSetName(),
                    currRunUUID,
                    currConfig.getDeDupeMaxLogsBeforeWrite(),
                    currConfig.getDeDupeMaxLogsPerFile(),
                    currConfig.getDeDupeMaxLogFiles(),
                    currConfig.getDeDupeIgnorePercent(),
                    currConfig.getDeDupeSkipPercent(),
                    currConfig.getDeDupeIgnoreCount(),
                    currConfig.getDeDupeSkipCount(),
                    currConfig.getDeDupeDefaultAction());

            storeWrapperMap.put(confKey, storeWrapper);
        }

        // Start the tailer threads
        BasicThreadFactory logCheckTailerFactory = new BasicThreadFactory.Builder()
                .namingPattern("runTailerThread-%d")
                .build();

        Map<Integer, Future<LogCheckResult>> tailerThreads = new HashMap<>(10);
        ExecutorService logCheckTailerExe = Executors.newCachedThreadPool(logCheckTailerFactory);

        for(Integer currK : tailerMap.keySet())
        {
            ILogCheckTail currTail = tailerMap.get(currK);

            Future<LogCheckResult> fileTailFuture = logCheckTailerExe.submit(currTail);

            tailerThreads.put(currK, fileTailFuture);

            LogCheckResult fileTailRes = null;
        }
        logCheckTailerExe.shutdown();

        // Start the store threads
        BasicThreadFactory logStoreFactory = new BasicThreadFactory.Builder()
                .namingPattern("runLogStoreThread-%d")
                .build();
        ExecutorService logStoreExe = Executors.newCachedThreadPool(logStoreFactory);

        Map<Integer, List<Future<LogCheckResult>>> storeThreads = new HashMap<>(10);
        for(Integer currK : tailerMap.keySet())
        {
            List<ILogEntryStore> currStores = storeMap.get(currK);
            List<Future<LogCheckResult>> currStoreFutures = new ArrayList<>(10);

            for( ILogEntryStore iles : currStores )
            {
                Future<LogCheckResult> logStoreFuture = logStoreExe.submit(storeWrapperMap.get(currK));

                currStoreFutures.add(logStoreFuture);

                LogCheckResult logStoreRes = null;
            }
            storeThreads.put(currK, currStoreFutures);

        }
        logStoreExe.shutdown();

        try
        {
            boolean run = true;
            boolean allStoresAreRunning = true;

            Map<Integer, LogCheckResult> tailResList = new HashMap<>(10);
            Map<Integer, LogCheckResult> storeResList = new HashMap<>(10);

            //while( (fileTailRes == null) || (logStoreRes == null) )
            while( run )
            {
                if( allStoresAreRunning == false )
                {
                    // The Log Store stopped so we should stop the tailer as well
                    for( Integer currK : storeThreads.keySet() )
                    {
                        List<Future<LogCheckResult>> currFutures = storeThreads.get(currK);
                        for( Future<LogCheckResult> currFuture : currFutures )
                        {
                            currFuture.cancel(true);
                        }

                    }

                    run = false;
                }
                else
                {
                    boolean allTailerThreadsDone = true;
                    for( Integer currK : tailerThreads.keySet() )
                    {
                        Future<LogCheckResult> currFuture = tailerThreads.get(currK);
                        if( currFuture.isDone() == false )
                        {
                            allTailerThreadsDone = false;
                            break;
                        }
                    }

                    if( allTailerThreadsDone )
                    {
                        run = false;

                        // Log polling thread has completed.  Generally this should
                        // not happen until we're shutting down.
                        for( Integer currK : tailerThreads.keySet() )
                        {
                            Future<LogCheckResult> currFuture = tailerThreads.get(currK);
                            tailResList.put(currK,currFuture.get());

                            // If Tailer thread is done, then cancel/interrupt the store thread
                            // E.g. useful for implementing the --stop-after feature
                            if( currFuture.get().getStatus() == LCResultStatus.SUCCESS )
                            {
                                LOGGER.debug("\n==============================\n"
                                        + "CANCELLING the Log Store thread because the Tailer thread was done first."
                                        + "\n==============================\n");

                                List<Future<LogCheckResult>> currStores = storeThreads.get(currK);
                                for( Future<LogCheckResult> store : currStores )
                                {
                                    store.cancel(true);
                                }
                             //   logStoreFuture.cancel(true);
                            }
                        }
                    }
                }

                // Check on the Log Store Thread
                if( allStoresAreRunning )
                {
                    for( Integer currK : storeThreads.keySet() )
                    {
                        List<Future<LogCheckResult>> currStores = storeThreads.get(currK);
                        for( Future<LogCheckResult> store : currStores )
                        {
                            if( store.isCancelled() )
                            {
                                allStoresAreRunning = false;

                                break;
                            }
                            else if( store.isDone() )
                            {
                                allStoresAreRunning = false;

                                // Log storage thread has completed.  Generally this should
                                // not happen until we're shutting down.
                                try
                                {
                                    storeResList.put(currK, store.get());
                                }
                                catch( InterruptedException ex )
                                {
                                    LOGGER.error("Log Store Thread interrupted.", ex);

                                    storeResList.put(currK,
                                            LogCheckResult.from(LCResultStatus.INTERRUPTED));
                                }
                                catch( ExecutionException ex )
                                {
                                    LOGGER.error("Log Store Thread failed.", ex);
                                    storeResList.put(currK,
                                            LogCheckResult.from(LCResultStatus.FAIL));
                                }
                                break;
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

            if( ((storeResList.isEmpty() == false)
                    && (storeResList.values().stream()
                                    .anyMatch( s -> s.getStatus() == LCResultStatus.INTERRUPTED))
                    || (tailResList.values().stream()
                                    .anyMatch( s -> s.getStatus() == LCResultStatus.INTERRUPTED)) ))
            {
                // If either of the threads were interrupted, then mark as interrupted.
                res.put(0, LogCheckResult.from(LCResultStatus.INTERRUPTED));
            }
            else
            {
                if( ((storeResList.isEmpty() == false)
                        && (storeResList.values().stream()
                        .anyMatch( s -> s.getStatus() == LCResultStatus.FAIL))
                        || (tailResList.values().stream()
                        .anyMatch( s -> s.getStatus() == LCResultStatus.FAIL)) ))
                {
                    // If either of the threads failed, then mark as failed
                    res.put(0, LogCheckResult.from(LCResultStatus.FAIL));
                }
                else if( tailResList.values().stream()
                        .anyMatch( s -> s.getStatus() == LCResultStatus.FAIL) )
                {
                    // if Tailer thread succeed, ignore the Log Store result
                    res.put(0, LogCheckResult.from(LCResultStatus.SUCCESS));
                }
            }
        }
        catch (InterruptedException ex)
        {
            LOGGER.error("Log Check Run thread was interrupted", ex);
            
            // We don't have to do much here because the interrupt got us out
            // of the while loop.

            res.put(0, LogCheckResult.from(LCResultStatus.INTERRUPTED));
        }
        finally
        {
            logCheckTailerExe.shutdownNow();
            logStoreExe.shutdownNow();

            if( (storeMap != null) && (storeMap.size() > 1) )
            {
                for( Integer currKey : storeMap.keySet() )
                {
                    List<ILogEntryStore> stores = storeMap.get(currKey);
                    for( ILogEntryStore store : stores )
                    {
                        store.destroy();
                    }
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
