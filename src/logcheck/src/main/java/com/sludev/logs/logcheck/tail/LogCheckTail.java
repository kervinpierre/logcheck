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
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.enums.LCCompressionType;
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckFileRotate;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Management class for tailing log files.
 *
 * @author kervin
 */
public final class LogCheckTail implements Callable<LogCheckResult>
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckTail.class);

    private final List<ILogEntryBuilder> m_mainLogEntryBuilders;

    private final Path m_logFile;
    private final Path m_deDupeDir;
    private final Long m_startPosition;
    private final Long m_delay;
    private final Boolean m_tailFromEnd;
    private final Boolean m_reOpenLogFile;
    private final Boolean m_continueState;
    private final Boolean m_saveState;
    private final Boolean m_startPositionIgnoreError;
    private final Boolean m_validateTailerStatistics;
    private final Boolean m_collectTailerStatistics;
    private final Boolean m_tailerBackupReadLog;
    private final Boolean m_tailerBackupReadPriorLog;
    private final Boolean m_stopOnEOF;
    private final Boolean m_readOnlyFileMode;
    private final Boolean m_mainThread;
    private final Integer m_bufferSize;
    private final Integer m_readLogFileCount;
    private final Integer m_readMaxDeDupeEntries;
    private final Long m_stopAfter;
    private final LCHashType m_idBlockHash;
    private final Integer m_idBlockSize;
    private final String m_setName;
    private final Path m_stateFile;
    private final Path m_errorFile;
    private final Path m_tailerLogBackupDir;
    private final List<LCFileRegexComponent> m_tailerBackupLogNameComps;
    private final LCCompressionType m_tailerBackupLogCompression;
    private final Pattern m_tailerBackupLogNameRegex;
    private final Set<LCDebugFlag> m_debugFlags;

    private LogCheckTail(final List<ILogEntryBuilder> mainLogEntryBuilders,
                         final Path logFile,
                         final Path deDupeDir,
                         final Long startPosition,
                         final Long delay,
                         final Boolean continueState,
                         final Boolean tailFromEnd,
                         final Boolean reOpenLogFile,
                         final Boolean saveState,
                         final Boolean startPosIgnoreError,
                         final Boolean validateTailerStatistics,
                         final Boolean collectTailerStatistics,
                         final Boolean tailerBackupReadLog,
                         final Boolean tailerBackupReadPriorLog,
                         final Boolean stopOnEOF,
                         final Boolean readOnlyFileMode,
                         final Boolean mainThread,
                         final Integer bufferSize,
                         final Integer readLogFileCount,
                         final Integer readMaxDeDupeEntries,
                         final Long stopAfter,
                         final LCHashType idBlockHash,
                         final Integer idBlockSize,
                         final String setName,
                         final Path stateFile,
                         final Path errorFile,
                         final Path tailerLogBackupDir,
                         final List<LCFileRegexComponent> tailerBackupLogNameComps,
                         final LCCompressionType tailerBackupLogCompression,
                         final Pattern tailerBackupLogNameRegex,
                         final Set<LCDebugFlag> debugFlags)
    {
        this.m_mainLogEntryBuilders = mainLogEntryBuilders;
        this.m_idBlockHash = idBlockHash;
        this.m_idBlockSize = idBlockSize;
        this.m_setName = setName;
        this.m_continueState = continueState;
        this.m_deDupeDir = deDupeDir;
        this.m_readLogFileCount = readLogFileCount;
        this.m_readMaxDeDupeEntries = readMaxDeDupeEntries;
        this.m_validateTailerStatistics = validateTailerStatistics;
        this.m_collectTailerStatistics = collectTailerStatistics;
        this.m_tailerLogBackupDir = tailerLogBackupDir;
        this.m_stopOnEOF = stopOnEOF;
        this.m_readOnlyFileMode = readOnlyFileMode;
        this.m_mainThread = mainThread;
        this.m_startPosition = startPosition;
        this.m_debugFlags = debugFlags;

        // Don't bother with logs we missed earlier

        if( tailFromEnd != null )
        {
            this.m_tailFromEnd = tailFromEnd;
        }
        else
        {
            this.m_tailFromEnd = true;
        }

        if( tailerBackupReadLog != null )
        {
            this.m_tailerBackupReadLog = tailerBackupReadLog;
        }
        else
        {
            this.m_tailerBackupReadLog = true;
        }

        if( tailerBackupReadPriorLog != null )
        {
            this.m_tailerBackupReadPriorLog = tailerBackupReadPriorLog;
        }
        else
        {
            this.m_tailerBackupReadPriorLog = false;
        }

        if( tailerBackupLogNameComps != null )
        {
            this.m_tailerBackupLogNameComps = tailerBackupLogNameComps;
        }
        else
        {
            this.m_tailerBackupLogNameComps = new ArrayList<>(10);
        }

        if( tailerBackupLogCompression != null )
        {
            this.m_tailerBackupLogCompression = tailerBackupLogCompression;
        }
        else
        {
            this.m_tailerBackupLogCompression = null;
        }

        if( tailerBackupLogNameRegex != null )
        {
            this.m_tailerBackupLogNameRegex = tailerBackupLogNameRegex;
        }
        else
        {
            this.m_tailerBackupLogNameRegex = null;
        }

        if( saveState != null )
        {
            this.m_saveState = saveState;
        }
        else
        {
            this.m_saveState = false;
        }

        if( startPosIgnoreError != null )
        {
            this.m_startPositionIgnoreError = startPosIgnoreError;
        }
        else
        {
            this.m_startPositionIgnoreError = false;
        }

        if( reOpenLogFile != null )
        {
            this.m_reOpenLogFile = reOpenLogFile;
        }
        else
        {
            this.m_reOpenLogFile = false;
        }

        if( bufferSize != null )
        {
            this.m_bufferSize = bufferSize;
        }
        else
        {
            this.m_bufferSize = LogCheckConstants.DEFAULT_LOG_READ_BUFFER_SIZE_BYTES;
        }

        if( delay != null )
        {
            this.m_delay = delay;
        }
        else
        {
            this.m_delay = LogCheckConstants.DEFAULT_POLL_INTERVAL;
        }

        if( stopAfter != null )
        {
            this.m_stopAfter = stopAfter;
        }
        else
        {
            this.m_stopAfter = 0L;
        }

        if( logFile != null )
        {
            this.m_logFile = logFile;
        }
        else
        {
            this.m_logFile = null;
        }

        if( stateFile != null )
        {
            this.m_stateFile = stateFile;
        }
        else
        {
            this.m_stateFile = null;
        }

        if( errorFile != null )
        {
            this.m_errorFile = errorFile;
        }
        else
        {
            this.m_errorFile = null;
        }
    }

    public static LogCheckTail from(final List<ILogEntryBuilder> mainLogEntryBuilders,
                                    final Path logFile,
                                    final Path deDupeDir,
                                    final Long startPosition,
                                    final Long delay,
                                    final Boolean continueState,
                                    final Boolean tailFromEnd,
                                    final Boolean reOpenOnChunk,
                                    final Boolean saveState,
                                    final Boolean startPosIgnoreError,
                                    final Boolean validateTailerStatistics,
                                    final Boolean collectTailerStatistics,
                                    final Boolean tailerBackupReadLog,
                                    final Boolean tailerBackupReadPriorLog,
                                    final Boolean stopOnEOF,
                                    final Boolean readOnlyFileMode,
                                    final Boolean mainThread,
                                    final Integer bufferSize,
                                    final Integer readLogFileCount,
                                    final Integer readMaxDeDupeEntries,
                                    final Long stopAfter,
                                    final LCHashType idBlockHash,
                                    final Integer idBlockSize,
                                    final String setName,
                                    final Path stateFile,
                                    final Path errorFile,
                                    final Path tailerLogBackupDir,
                                    final List<LCFileRegexComponent> tailerBackupLogNameComps,
                                    final LCCompressionType tailerBackupLogCompression,
                                    final Pattern tailerBackupLogNameRegex,
                                    final Set<LCDebugFlag> debugFlags)
    {
        LogCheckTail res = new LogCheckTail(mainLogEntryBuilders,
                logFile,
                deDupeDir,
                startPosition,
                delay,
                continueState,
                tailFromEnd,
                reOpenOnChunk,
                saveState,
                startPosIgnoreError,
                validateTailerStatistics,
                collectTailerStatistics,
                tailerBackupReadLog,
                tailerBackupReadPriorLog,
                stopOnEOF,
                readOnlyFileMode,
                mainThread,
                bufferSize,
                readLogFileCount,
                readMaxDeDupeEntries,
                stopAfter,
                idBlockHash,
                idBlockSize,
                setName,
                stateFile,
                errorFile,
                tailerLogBackupDir,
                tailerBackupLogNameComps,
                tailerBackupLogCompression,
                tailerBackupLogNameRegex,
                debugFlags);

        return res;
    }

    @Override
    public LogCheckResult call() throws LogCheckException, ExecutionException, InterruptedException, IOException
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);

        long currDelay = (m_delay == null) ? 0 : m_delay;
        boolean currTailFromEnd = BooleanUtils.isNotFalse(m_tailFromEnd);

        Long currPosition = m_startPosition;

        boolean currReOpen = BooleanUtils.isTrue(m_reOpenLogFile);
        boolean currStartPosIgrErr = BooleanUtils.isTrue(m_startPositionIgnoreError);
        boolean currStatsValidate = BooleanUtils.isTrue(m_validateTailerStatistics);
        boolean currStatsCollect = BooleanUtils.isNotFalse(m_collectTailerStatistics);
        boolean currTailerBackupReadLog = BooleanUtils.isTrue(m_tailerBackupReadLog);
        boolean currStopOnEOF = BooleanUtils.isTrue(m_stopOnEOF);
        boolean currSaveState = BooleanUtils.isNotFalse(m_saveState);

        final ScheduledExecutorService statsSchedulerExe;

        if( m_logFile == null )
        {
            throw new LogCheckException("Log File cannot be null.");
        }

        final AtomicReference<TailerStatistics> stats;

        if( currStatsCollect )
        {
            stats = new AtomicReference<>(TailerStatistics.from(m_stateFile,
                    m_errorFile,
                    m_setName));

            stats.get().setLastProcessedTimeStart(Instant.now());

            if( currSaveState )
            {
                BasicThreadFactory tailerSaveFactory = new BasicThreadFactory.Builder()
                        .namingPattern("tailerSaveThread-%d")
                        .build();

                statsSchedulerExe = Executors.newScheduledThreadPool(1, tailerSaveFactory);

                statsSchedulerExe.scheduleWithFixedDelay(() ->
                        {
                            try
                            {
                                stats.get().saveLastPending(false, true, false);
                            }
                            catch( LogCheckException ex )
                            {
                                LOGGER.debug("Error saving the logger state", ex);
                            }
                            catch( InterruptedException ex )
                            {
                                LOGGER.debug("", ex);
                            }
                        }, LogCheckConstants.DEFAULT_SAVE_STATE_INTERVAL_SECONDS,
                        LogCheckConstants.DEFAULT_SAVE_STATE_INTERVAL_SECONDS,
                        TimeUnit.SECONDS);
            }
            else
            {
                statsSchedulerExe = null;
            }
        }
        else
        {
            stats = null;
            statsSchedulerExe = null;
        }

        final AtomicReference<FileTailer> mainTailer = new AtomicReference<>();
        final AtomicBoolean exitNow = new AtomicBoolean(false);

        final ScheduledExecutorService stopSchedulerExe;
        if( (m_stopAfter != null)
                && (m_stopAfter > 0) )
        {
            BasicThreadFactory scheduleFactory = new BasicThreadFactory.Builder()
                    .namingPattern("tailerScheduleThread-%d")
                    .build();

            stopSchedulerExe = Executors.newScheduledThreadPool(1, scheduleFactory);

            stopSchedulerExe.schedule(() ->
            {
                FileTailer currFileTailer = mainTailer.get();
                if( currFileTailer == null )
                {
                    LOGGER.warn("Process Stop Scheduler called, but Tailer object is null.");
                }
                else
                {
                    currFileTailer.stop();
                    exitNow.set(true);

                    LOGGER.debug("Process Stop Scheduler called.");
                }

                stopSchedulerExe.shutdownNow();
            }, m_stopAfter, TimeUnit.SECONDS);

            stopSchedulerExe.shutdown();
        }
        else
        {
            stopSchedulerExe = null;
        }

        BasicThreadFactory tailerFactory = new BasicThreadFactory.Builder()
                .namingPattern("tailerThread-%d")
                .build();

        ExecutorService tailerExe = Executors.newSingleThreadExecutor(tailerFactory);

        FileTailerResult tailerRes = null;

        BigInteger passCount = BigInteger.ZERO;
        try
        {
            // Keeps track of the last backup file that has been processed
            // FIXME : Last backup file that has been processed should be stored/restored on/from disk.  Maybe with DeDupe files?
            //Path newestBackupFile = null;
            Deque<Pair<Path, LogFileBlock>> sessionBackupFiles = new ArrayDeque<>(10);


            Set<LCTailerResult> currTailerResults = null;

            ////////////////////////////////////
            // Main Tailer Loop
            ////////////////////////////////////

            // Tailer start
            // Main Tailer is restarted if 'reOpen' option is set.
            do
            {
                boolean skipMainLog = false;
                if( (passCount.compareTo(BigInteger.ZERO) <= 0)
                        && m_tailerBackupReadPriorLog )
                {
                    // Read the backups *before* starting with the main log
                    skipMainLog = true;
                }

                if( BooleanUtils.isTrue(m_continueState) ||
                        ((passCount.compareTo(BigInteger.ZERO) > 0) && currReOpen) )
                {
                    // Reopen implies continue.  Because we use the logs to serialize state.

                    // We don't read the state on the first pass of the loop, unless --continue is present
                    // We do on the other passes though.

                    if( (m_stateFile == null) || Files.notExists(m_stateFile) )
                    {
                        LOGGER.info(
                                String.format("Trying to 'continue' but the state file does not exist. '%s'",
                                        m_stateFile));
                    }
                    else if( stats.get() != null )
                    {
                        // Done every time before the Tailer thread starts.
                        LogCheckState currState = stats.get().restore(m_deDupeDir,
                                m_readLogFileCount,
                                m_readMaxDeDupeEntries);

                        // If we haven't been given a position, get one from disk
                        currPosition = LogFileState.positionFromLogFile(currState.getLogFile());
                        LOGGER.debug(String.format("Restored position %d from disk", currPosition));
                    }
                }

                if( skipMainLog == false )
                {
                    // Create this iteration's File Tailer Object
                    mainTailer.set(FileTailer.from(m_logFile,
                            currPosition,
                            Charset.defaultCharset(),
                            m_mainLogEntryBuilders,
                            currDelay * 1000, // Convert to milliseconds
                            currTailFromEnd,
                            currReOpen,
                            currStartPosIgrErr,
                            currStatsValidate,
                            currStatsCollect,
                            currStopOnEOF,
                            m_bufferSize,
                            currStatsCollect ? 2 : 0,
                            (stats == null) ? null : stats.get(),
                            m_idBlockHash,
                            m_idBlockSize,
                            m_setName,
                            m_debugFlags));

                    Future<FileTailerResult> tailerExeRes = tailerExe.submit(mainTailer.get());

                    // Wait until Tailer thread has completed.
                    tailerRes = tailerExeRes.get();

                    // FIXME : Set tail from end to be true or false based on Tailer result.
                    // This should help implement log-rotate support
                }

                if( tailerRes != null )
                {
                    currTailerResults = tailerRes.getResultSet();
                }
                else
                {
                    currTailerResults = new HashSet<>();
                    if( m_reOpenLogFile )
                    {
                        // Main loop needs this flag to continue
                        currTailerResults.add(LCTailerResult.REOPEN);
                    }

                    if( skipMainLog )
                    {
                        // Needed to process backup logs
                        currTailerResults.add(LCTailerResult.VALIDATION_FAIL);
                    }
                }

                if( currTailerResults.contains(LCTailerResult.VALIDATION_FAIL) )
                {
                    // Log rotation detected?
                    LOGGER.debug("Log validation failed. We may need to check the old log file here.");

                    if( m_tailerBackupReadLog )
                    {
                        Path firstPartialBackupFile = null;
                        LogFileState currFileState = null;

                        if( (tailerRes == null) || (tailerRes.getState() == null) )
                        {
                            LOGGER.debug("File Tailer Result : Log Check State is null.");
                        }
                        else
                        {
                            currFileState = tailerRes.getState().getLogFile();
                        }

                        ////////////////////////////////////////
                        // Backup File Processing Loop
                        ////////////////////////////////////////

                        while( true )
                        {
                            if( LOGGER.isDebugEnabled() )
                            {
                                String msg = "Begin main backup read loop. Backup Log Files :\n";

                                if( sessionBackupFiles != null )
                                {
                                    msg = String.format("%s    Session Backup File Cache Size : %d\n",
                                            msg, sessionBackupFiles.size());

                                    StringBuilder sb = new StringBuilder(100);
                                    for( Pair<Path, LogFileBlock> pathPair : sessionBackupFiles )
                                    {
                                        sb.append(String.format("    %s\n", pathPair.getLeft()));
                                    }
                                    msg = String.format("%s%s", msg, sb.toString());
                                }

                                LOGGER.debug(msg);
                            }

                            // Try to detect then read the backup logs

                            Path newestBackupFile = null;
                            if( (sessionBackupFiles != null)
                                    && (sessionBackupFiles.peekLast() != null) )
                            {
                                newestBackupFile = sessionBackupFiles.peekLast().getLeft();
                            }

                            LOGGER.debug(String.format("Newest Backup File is '%s'", newestBackupFile));

                            Path currLastBackupFileProcessed = null;
                            if( m_mainThread == false )
                            {
                                // NB : This should not be reached often since we're not currently
                                //      validating on "Sub-call" threads.

                                // If not the main thread, the main log file is probably a
                                // backup file itself being processed.  Start with that file.
                                currLastBackupFileProcessed = m_logFile;
                            }

                            Deque<Path> backupLogFilesQueue = new ArrayDeque<>(10);
                            Path backupLogFile = null;

                            ////////////////////////////////////////////////////
                            // Find the number of backup files we need to check
                            ////////////////////////////////////////////////////
                            do
                            {
                                backupLogFile = LogCheckFileRotate.prevName(
                                        m_logFile.toAbsolutePath().getParent(),
                                        currLastBackupFileProcessed,
                                        m_tailerBackupLogNameRegex,
                                        null,
                                        m_tailerBackupLogNameComps,
                                        true,
                                        true);

                                if( backupLogFile == null )
                                {
                                    LOGGER.warn("Could not find the previous backup file.");
                                }
                                else if( Files.notExists(backupLogFile) )
                                {
                                    LOGGER.warn(String.format("Backup Log File '%s' not found", backupLogFile));
                                }
                                else
                                {
                                    backupLogFilesQueue.addFirst(backupLogFile);
                                }

                                // Use to get the next backup file if any
                                if( backupLogFile != null )
                                {
                                    currLastBackupFileProcessed = backupLogFile;
                                }
                            }
                            while( backupLogFile != null );

                            // Add the new files to the session file cache
                            for( Path path : backupLogFilesQueue )
                            {
                                boolean found = false;
                                for( Pair<Path, LogFileBlock> filePair : sessionBackupFiles )
                                {
                                    if( filePair.getLeft().equals(path) )
                                    {
                                        found = true;
                                    }
                                }

                                if( found == false )
                                {
                                    sessionBackupFiles.addLast(Pair.of(path, null));
                                }
                            }

                            // looping in reverse since our backups count up from oldest to newest
                            // TODO : Make reversing a switch

                            if( (backupLogFilesQueue.size() < 1)
                                    || ((newestBackupFile != null)
                                            && newestBackupFile.equals(backupLogFilesQueue.peekLast())) )
                            {
                                // No [new] backup files found [since last run].
                                // Stop backup log processing loop

                                LOGGER.debug("Backup processing loop completed.  No new backup files found.");
                                break;
                            }

                            if( firstPartialBackupFile == null )
                            {
                                // The first backup file is most likely partially read.
                                // This only happens in a single backup file
                                firstPartialBackupFile = backupLogFilesQueue.peekFirst();
                            }

                            ////////////////////////////
                            // Now loop the found files
                            ////////////////////////////
                            boolean newestBackupFileSeen = false;
                            for( Path currBackupFile : backupLogFilesQueue )
                            {
                                LOGGER.debug(String.format("Processing Backup Log File '%s'", currBackupFile));

                                if( (newestBackupFile != null) && (newestBackupFileSeen == false) )
                                {
                                    // Should only happen on second search for backup files and after
                                    if( currBackupFile.equals(newestBackupFile) )
                                    {
                                        // Skip all files that have already been processed
                                        // FIXME : This will break with rotated logs, maybe?
                                        newestBackupFileSeen = true;
                                    }

                                    continue;
                                }

                                if( Files.notExists(currBackupFile) )
                                {
                                    LOGGER.warn(String.format("Backup Log File '%s' not found", currBackupFile));
                                }
                                else
                                {
                                    try
                                    {
                                        // TODO : Support decompressing the backup file first

                                        LOGGER.info(String.format("Processing found backup file '%s'", currBackupFile));

                                        // FIXME : Check that the FIRST_BLOCK in this file matches
                                        //         the current FIRST_BLOCK.  If it does, we check
                                        //         dedupe logs for last logs.  This is only needed
                                        //         for the first file.

                                        Long currStartPos = 0L;
                                        if( (currFileState != null)
                                                && currBackupFile.equals(firstPartialBackupFile) )
                                        {
                                            currStartPos = currFileState.getLastProcessedPosition();
                                        }

                                        // Parse that backup file
                                        LogCheckTail lct = LogCheckTail.from(m_mainLogEntryBuilders,
                                                currBackupFile,
                                                m_deDupeDir,
                                                currStartPos,
                                                0L,
                                                false, // continue
                                                false, // tail from end
                                                false, // reOpenOnChunk
                                                false, // saveState
                                                false, // collectStats
                                                false, // startPosIgnoreError
                                                false,  // validate states
                                                m_tailerBackupReadLog,
                                                false, // tailerBackupReadPriorLog
                                                true, // stopOnEOF
                                                true, // readOnlyFileMode
                                                false,  // mainThread
                                                null, // bufferSize
                                                m_readLogFileCount,
                                                m_readMaxDeDupeEntries,
                                                null, // stopAfter
                                                m_idBlockHash,
                                                m_idBlockSize,
                                                m_setName,
                                                m_stateFile,
                                                m_errorFile,
                                                m_tailerLogBackupDir,
                                                m_tailerBackupLogNameComps,
                                                m_tailerBackupLogCompression,
                                                m_tailerBackupLogNameRegex,
                                                m_debugFlags);

                                        BasicThreadFactory logCheckTailerFactory = new BasicThreadFactory.Builder()
                                                .namingPattern("tailerCompletionThread-%d")
                                                .build();

                                        ExecutorService logCheckTailerExe = Executors.newSingleThreadExecutor(logCheckTailerFactory);
                                        Future<LogCheckResult> fileTailFuture = logCheckTailerExe.submit(lct);

                                        LogCheckResult fileTailRes = fileTailFuture.get();

                                        LOGGER.debug(String.format("Backup Completion Thread returned result %s for backup file [ %s ]",
                                                currBackupFile, fileTailRes.getStatus()));
                                    }
                                    catch( ExecutionException ex )
                                    {
                                        String errMsg = "Error completing backup file";

                                        LOGGER.debug(errMsg, ex);

                                        throw ex;
                                    }
                                }
                            }

                            // Loop to re-check the FS for new backup files created in the short time
                            // we were processing
                        }

                        // FIXME: New backup files created between here and next FileTailer instance will be lost/skipped

                        // TODO : At this point VALIDATION_FAILED should now be fixed.

                    }

                    LOGGER.debug("Validation fail fix branch completed.");
                }

                if( LOGGER.isDebugEnabled() )
                {
                    if( m_mainThread )
                    {
                        LOGGER.debug(String.format("Main Thread : Tailer Loop : Pass Count %d completed.",
                                passCount));
                    }
                    else
                    {
                        LOGGER.debug(String.format("Sub-call : Tailer Loop : Pass Count %d completed.",
                                passCount));
                    }
                }

                passCount = passCount.add(BigInteger.ONE);
            }
            while( currTailerResults.contains(LCTailerResult.REOPEN) && (exitNow.get() == false) );
        }
        catch( InterruptedException ex )
        {
            LOGGER.error("Tailer Loop Thread was interrupted", ex);

            throw ex;
        }
        catch( ExecutionException ex )
        {
            LOGGER.error("Tailer Loop Thread execution exception", ex);

            throw ex;
        }
        catch( LogCheckException ex )
        {
            LOGGER.debug("Tailer Loop Application exception.", ex);

            throw ex;
        }
        finally
        {
            if( m_mainThread )
            {
                LOGGER.debug("Main Thread : Leaving Log Check Tail");
            }
            else
            {
                LOGGER.debug("Sub-call : Leaving Log Check Tail");
            }

            tailerExe.shutdownNow();

            if( statsSchedulerExe != null )
            {
                statsSchedulerExe.shutdownNow();
            }

            if( stopSchedulerExe != null )
            {
                stopSchedulerExe.shutdownNow();
            }
        }

        return res;
    }


}
