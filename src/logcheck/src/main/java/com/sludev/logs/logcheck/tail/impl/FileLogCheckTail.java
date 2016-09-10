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
package com.sludev.logs.logcheck.tail.impl;

import com.sludev.logs.logcheck.config.entities.LogCheckStateBase;
import com.sludev.logs.logcheck.config.entities.LogCheckStateStatusBase;
import com.sludev.logs.logcheck.config.entities.impl.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.entities.impl.LogFileStatus;
import com.sludev.logs.logcheck.config.parsers.LogCheckStateParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.enums.LCCompressionType;
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCFileBlockType;
import com.sludev.logs.logcheck.enums.LCFileFormat;
import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.fs.BackupFileWatch;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.tail.TailerResult;
import com.sludev.logs.logcheck.tail.ILogCheckTail;
import com.sludev.logs.logcheck.tail.TailerStatistics;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckFileRotate;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Management class for tailing log files.
 *
 * @author kervin
 */
public final class FileLogCheckTail implements ILogCheckTail
{
    private static final Logger LOGGER
            = LogManager.getLogger(FileLogCheckTail.class);

    private final List<ILogEntryBuilder> m_mainLogEntryBuilders;

    private final Path m_logFile;
    private final Path m_deDupeDir;
    private final Path m_stateFile;
    private final Path m_stateProcessedLogsFilePath;
    private final Path m_errorFile;
    private final Path m_tailerLogBackupDir;
    private final Path m_preferredDir;
    private final Long m_startPosition;
    private final Long m_delay;
    private final Boolean m_tailFromEnd;
    private final Boolean m_reOpenLogFile;
    private final Boolean m_continueState;
    private final Boolean m_saveState;
    private final Boolean m_startPositionIgnoreError;
    private final Boolean m_validateTailerStatistics;
    private final Boolean m_collectTailerStatistics;
    private final Boolean m_watchBackupDirectory;
    private final Boolean m_tailerBackupReadLog;
    private final Boolean m_tailerBackupReadLogReverse;
    private final Boolean m_tailerBackupReadPriorLog;
    private final Boolean m_stopOnEOF;
    private final Boolean m_readOnlyFileMode;
    private final Boolean m_mainThread;
    private final Boolean m_statsReset;
    private final Integer m_bufferSize;
    private final Integer m_readLogFileCount;
    private final Integer m_readMaxDeDupeEntries;
    private final Long m_stopAfter;
    private final LCHashType m_idBlockHash;
    private final Integer m_idBlockSize;
    private final String m_setName;
    private final List<LCFileRegexComponent> m_tailerBackupLogNameComps;
    private final LCCompressionType m_tailerBackupLogCompression;
    private final Pattern m_tailerBackupLogNameRegex;
    private final Set<LCDebugFlag> m_debugFlags;

    private FileLogCheckTail( final List<ILogEntryBuilder> mainLogEntryBuilders,
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
                              final Boolean watchBackupDirectory,
                              final Boolean tailerBackupReadLog,
                              final Boolean tailerBackupReadLogReverse,
                              final Boolean tailerBackupReadPriorLog,
                              final Boolean stopOnEOF,
                              final Boolean readOnlyFileMode,
                              final Boolean mainThread,
                              final Boolean statsReset,
                              final Integer bufferSize,
                              final Integer readLogFileCount,
                              final Integer readMaxDeDupeEntries,
                              final Long stopAfter,
                              final LCHashType idBlockHash,
                              final Integer idBlockSize,
                              final String setName,
                              final Path stateFile,
                              final Path stateProcessedLogsFilePath,
                              final Path errorFile,
                              final Path tailerLogBackupDir,
                              final Path preferredDir,
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
        this.m_watchBackupDirectory = watchBackupDirectory;
        this.m_collectTailerStatistics = collectTailerStatistics;
        this.m_tailerLogBackupDir = tailerLogBackupDir;
        this.m_stopOnEOF = stopOnEOF;
        this.m_readOnlyFileMode = readOnlyFileMode;
        this.m_mainThread = mainThread;
        this.m_startPosition = startPosition;
        this.m_debugFlags = debugFlags;
        this.m_statsReset = statsReset;
        this.m_preferredDir = preferredDir;

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

        if( tailerBackupReadLogReverse != null )
        {
            this.m_tailerBackupReadLogReverse = tailerBackupReadLogReverse;
        }
        else
        {
            this.m_tailerBackupReadLogReverse = true;
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

        if( stateProcessedLogsFilePath != null )
        {
            this.m_stateProcessedLogsFilePath = stateProcessedLogsFilePath;
        }
        else
        {
            this.m_stateProcessedLogsFilePath = null;
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

    public static FileLogCheckTail from( final List<ILogEntryBuilder> mainLogEntryBuilders,
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
                                         final Boolean watchBackupDirectory,
                                         final Boolean tailerBackupReadLog,
                                         final Boolean tailerBackupReadLogReverse,
                                         final Boolean tailerBackupReadPriorLog,
                                         final Boolean stopOnEOF,
                                         final Boolean readOnlyFileMode,
                                         final Boolean mainThread,
                                         final Boolean statsReset,
                                         final Integer bufferSize,
                                         final Integer readLogFileCount,
                                         final Integer readMaxDeDupeEntries,
                                         final Long stopAfter,
                                         final LCHashType idBlockHash,
                                         final Integer idBlockSize,
                                         final String setName,
                                         final Path stateFile,
                                         final Path stateProcessedLogsFilePath,
                                         final Path errorFile,
                                         final Path tailerLogBackupDir,
                                         final Path preferredDir,
                                         final List<LCFileRegexComponent> tailerBackupLogNameComps,
                                         final LCCompressionType tailerBackupLogCompression,
                                         final Pattern tailerBackupLogNameRegex,
                                         final Set<LCDebugFlag> debugFlags)
    {
        FileLogCheckTail res = new FileLogCheckTail(mainLogEntryBuilders,
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
                watchBackupDirectory,
                tailerBackupReadLog,
                tailerBackupReadLogReverse,
                tailerBackupReadPriorLog,
                stopOnEOF,
                readOnlyFileMode,
                mainThread,
                statsReset,
                bufferSize,
                readLogFileCount,
                readMaxDeDupeEntries,
                stopAfter,
                idBlockHash,
                idBlockSize,
                setName,
                stateFile,
                stateProcessedLogsFilePath,
                errorFile,
                tailerLogBackupDir,
                preferredDir,
                tailerBackupLogNameComps,
                tailerBackupLogCompression,
                tailerBackupLogNameRegex,
                debugFlags);

        return res;
    }

    @Override
    public LogCheckResult call() throws LogCheckException, ExecutionException, InterruptedException, IOException
    {
        if( LOGGER.isDebugEnabled() )
        {
            StringBuilder msg = new StringBuilder(100);

            msg.append("\nFileLogCheckTail\n{\n");
            msg.append(String.format("  Log File        : %s\n", m_logFile));
            msg.append(String.format("  Start Pos       : %s\n", m_startPosition));
            msg.append(String.format("  Main Thread     : %b\n", m_mainThread));
            msg.append(String.format("  Read Prior Logs : %b\n", m_tailerBackupReadPriorLog));
            msg.append(String.format("  Watch Logs Dir  : %b\n", m_watchBackupDirectory));
            msg.append("}\n");

            LOGGER.debug(msg.toString());
        }

        final LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);

        long currDelay = (m_delay == null) ? 0 : m_delay;
        boolean currTailFromEnd = BooleanUtils.isNotFalse(m_tailFromEnd);

        Long currPosition = m_startPosition;

        boolean currReOpen = BooleanUtils.isTrue(m_reOpenLogFile);
        boolean currStartPosIgrErr = BooleanUtils.isTrue(m_startPositionIgnoreError);
        boolean currStatsValidate = BooleanUtils.isTrue(m_validateTailerStatistics);
        boolean currStatsCollect = BooleanUtils.isNotFalse(m_collectTailerStatistics);
        boolean currTailerBackupReadLog = BooleanUtils.isTrue(m_tailerBackupReadLog);
        boolean currTailerBackupReadLogReverse = BooleanUtils.isTrue(m_tailerBackupReadLogReverse);
        boolean currStopOnEOF = BooleanUtils.isTrue(m_stopOnEOF);
        boolean currSaveState = BooleanUtils.isNotFalse(m_saveState);
        boolean currStatsReset = BooleanUtils.isTrue(m_statsReset);

        // Watch for backup files *only* if we are checking backup logs at all
        boolean currWatchBackupDirectory
                = BooleanUtils.isTrue(m_watchBackupDirectory) && currTailerBackupReadLog;

        final ScheduledExecutorService statsSchedulerExe;
        final ExecutorService watchBackupDirExe;

        final BackupFileWatch currBackupFileWatch;
        final Future<Integer> watchBackupDirRes;

        if( m_logFile == null )
        {
            throw new LogCheckException("Log File cannot be null.");
        }

        final AtomicReference<TailerStatistics> stats;
        final AtomicReference<TailerStatistics> statsProcessedLogFiles;

        // Check to see if we're allowed to collect stats at all
        if( currStatsCollect )
        {
            stats = new AtomicReference<>(TailerStatistics.from(m_stateFile,
                    m_errorFile,
                    m_setName));

            statsProcessedLogFiles = new AtomicReference<>(
                    TailerStatistics.from(m_stateProcessedLogsFilePath,
                                            m_errorFile,
                                            m_setName));

            Instant currNow = Instant.now();
            stats.get().setLastProcessedTimeStart(currNow);
            statsProcessedLogFiles.get().setLastProcessedTimeStart(currNow);

            // Create the statistics thread
            if( currSaveState )
            {
                if( (m_stateFile == null) || (m_stateProcessedLogsFilePath == null) )
                {
                    LOGGER.warn("SAVE_STATE option set but at least one state file option is missing.");
                }

                BasicThreadFactory tailerSaveFactory = new BasicThreadFactory.Builder()
                        .namingPattern("fileLogCheckTailerSaveThread-%d")
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
            statsProcessedLogFiles = null;
            statsSchedulerExe = null;
        }

        // Create the backup watch thread
        if( currWatchBackupDirectory )
        {
            currBackupFileWatch = BackupFileWatch.from(
                    Collections.singletonList(
                            m_logFile.toAbsolutePath().getParent()),
                    m_tailerBackupLogNameRegex,
                    null, null, null);

            BasicThreadFactory watchBackupDirFactory = new BasicThreadFactory.Builder()
                    .namingPattern("watchBackupDirThread-%d")
                    .daemon(true)
                    .build();

            watchBackupDirExe = Executors.newSingleThreadExecutor(
                    watchBackupDirFactory);

            watchBackupDirRes
                    = watchBackupDirExe.submit(currBackupFileWatch);
        }
        else
        {
            currBackupFileWatch = null;
            watchBackupDirExe = null;
            watchBackupDirRes = null;
        }

        final AtomicReference<FileTailer> mainTailer = new AtomicReference<>();
        final AtomicBoolean exitNow = new AtomicBoolean(false);

        final ScheduledExecutorService stopSchedulerExe;

        // Setup "Stop After"/Termination thread
        if( (m_stopAfter != null)
                && (m_stopAfter > 0) )
        {
            BasicThreadFactory scheduleFactory = new BasicThreadFactory.Builder()
                    .namingPattern("tailerScheduleThread-%d")
                    .daemon(true)
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
                    res.getStatuses().add(LCResultStatus.TIMEDOUT);

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

        TailerResult tailerRes = null;

        BigInteger passCount = BigInteger.ZERO;
        try
        {
            // Keeps track of the last backup file that has been processed
            // FIXME : Last backup file that has been processed should be stored/restored on/from disk.  Maybe with DeDupe files?
            //Path newestBackupFile = null;
            Deque<LogCheckStateStatusBase> sessionBackupFiles = new ArrayDeque<>(10);

            Set<LCTailerResult> currTailerResults = null;

            boolean nextStatsReset = currStatsReset;

            long currCreateFileEventCount = 0;

            ////////////////////////////////////
            // Main Tailer Loop
            ////////////////////////////////////

            // Tailer start
            // Main Tailer is restarted if 'reOpen' option is set.
            do
            {
                LOGGER.debug(String.format(
                        "Main Logcheck Tail Loop Start. PassCount %d start.",
                        passCount));

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
                        LogCheckState currState = stats.get().restoreFileState(m_stateFile,
                                m_deDupeDir,
                                m_readLogFileCount,
                                m_readMaxDeDupeEntries);

                        // If we haven't been given a position, get one from disk
                        Long storedPosition = LogFileState.positionFromLogFile(currState.getLogFile());
                        if( (currPosition == null) || (currPosition < 1) )
                        {
                            currPosition = storedPosition;
                        }
                        else if( (storedPosition != null) && (storedPosition > 0) )
                        {
                            LOGGER.info(String.format("Current Position of %d used instead of restored"
                                    + " position of %d", currPosition, storedPosition));
                        }
                    }
                }

                // Skips the main Log Tailer completely.  No making up.  Stats on disk are reset.
                boolean skipMainLog = false;
                // Skips the main Log Tailer for a single loop cycle.  The main Log Tailer is run again
                // next loop cycle.
                boolean delayMainLog = false;

                if( (passCount.compareTo(BigInteger.ZERO) <= 0)
                        && m_tailerBackupReadPriorLog )
                {
                    // Read the backups *before* starting with the main log,
                    // TODO : But *only* if there are unprocessed backups.  This means we should figure this out first.
                    LOGGER.debug("Delaying Main Log Tailer because passCount <= 0 and 'Backup Read Prior Log' is true."
                                    + "  Attempt to process backup logs first.");
                    delayMainLog = true;
                }

                // Check if any new backup file have been created since we last
                // processed all backup files
                if( (currBackupFileWatch != null)
                        && (currBackupFileWatch.getDetectedCreatePathCount() != currCreateFileEventCount) )
                {
                    // New files were created, so skip to the
                    // VALIDATION_SKIPPED/FAILED processing
                    LOGGER.debug(String.format("Skipping Main Log Tailer because. currCreateFileEventCount==%d"
                            + "!= currBackupFileWatch.getDetectedCreatePathCount()== %d .",
                            currCreateFileEventCount, currBackupFileWatch.getDetectedCreatePathCount()));

                    skipMainLog = true;
                }

                if( skipMainLog || delayMainLog )
                {
                    LOGGER.debug("Skipping/Delaying the main FileTailer thread.");
                }
                else
                {
                    final CountDownLatch currCompletionLatch = new CountDownLatch(1);

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
                            nextStatsReset,
                            currStopOnEOF,
                            m_bufferSize,
                            currStatsCollect ? 2 : 0,
                            (stats == null) ? null : stats.get(),
                            m_idBlockHash,
                            m_idBlockSize,
                            m_setName,
                            m_debugFlags,
                            currCompletionLatch));

                    // Reset the current position to zero
                    // for the next iteration.  Otherwise BUG
                    currPosition = null;

                    // Use the reset value, then return it to the
                    // default of FALSE
                    nextStatsReset = false;

                    Future<TailerResult> tailerExeRes = tailerExe.submit(mainTailer.get());

                    if( currBackupFileWatch != null )
                    {
                        currBackupFileWatch.addWatchOnTask(tailerExeRes);
                    }

                    // Wait until Tailer thread has completed.
                    try
                    {
                        tailerRes = tailerExeRes.get();
                    }
                    catch( CancellationException ex )
                    {
                        if( currBackupFileWatch != null )
                        {
                            // Wait for the Backup File Watch object
                            // to signal us.
                            currCompletionLatch.await();
                        }
                        else
                        {
                            LOGGER.error("Cancelled, but there's no Backup File Watch object.");

                            throw ex;
                        }

                        // New backup files cancel the running tailer task
                        LOGGER.debug(String.format("Tailer cancelled for '%s'",
                                m_logFile));

                        // The result from the object directly
                        tailerRes = mainTailer.get().getFinalResult();
                        if( tailerRes == null )
                        {
                            LOGGER.debug("Tailer cancelled and its result is null.");
                        }
                    }

                    // FIXME : Set tail from end to be true or false based on Tailer result.
                    // This should help implement log-rotate support
                }

                if( tailerRes != null )
                {
                    LOGGER.debug(String.format("tailerRes returned : %s", tailerRes));
                    currTailerResults = tailerRes.getResultSet();
                }
                else
                {
                    currTailerResults = EnumSet.noneOf(LCTailerResult.class);
                    if( m_reOpenLogFile )
                    {
                        // Main loop needs this flag to continue
                        currTailerResults.add(LCTailerResult.REOPEN);
                    }

                    if( skipMainLog || delayMainLog )
                    {
                        // Needed to process backup logs
                        currTailerResults.add(LCTailerResult.VALIDATION_SKIPPED);
                    }
                }

                if( currTailerResults.contains(LCTailerResult.VALIDATION_FAIL)
                        // Validation skipped for some reason, e.g. if the main tailer was skipped
                        || currTailerResults.contains(LCTailerResult.VALIDATION_SKIPPED)
                        // FIXME : Below is a bit presumptuous. VALIDATION_ERROR isn't synonymous with VALIDATION_FAIL
                        //         but we treat it as such here.
                        || currTailerResults.contains(LCTailerResult.VALIDATION_ERROR)
                        // Possibly interrupted by a new backup detected
                        || currTailerResults.contains(LCTailerResult.INTERRUPTED))
                {
                    if( LOGGER.isDebugEnabled() )
                    {
                        String resSet = currTailerResults.stream()
                                .map(Enum::toString)
                                .collect(Collectors.joining(", "));

                        // Log rotation detected?
                        LOGGER.debug(String.format("Log validation failed. We may need to check the old log file here. [%s]",
                                resSet));
                    }

                    if( currTailerResults.contains(LCTailerResult.FILE_TRUNCATED) )
                    {
                        if( BooleanUtils.isNotTrue(m_startPositionIgnoreError) )
                        {
                            String msg = String.format("FILE_TRUNCATED but 'Ignore Start Position Error' is false. Quiting. '%s'",
                                    m_logFile);

                            // We really just end processing at this point to avoid an infinite loop
                            LOGGER.debug(msg);

                            throw new LogCheckException(msg);
                        }
                    }

                    if( currTailerResults.contains(LCTailerResult.STATISTICS_RESET) )
                    {
                        nextStatsReset = true;
                    }

                    // Read the log backup files on disk
                    if( m_tailerBackupReadLog )
                    {
                        LogFileState currFileState = null;

                        if( (tailerRes == null) || (tailerRes.getState() == null) )
                        {
                            LOGGER.debug("File Tailer Result : Log Check State is null. Restoring from disk.");

                            // Use the Log File State on disk
                            if( Files.exists(m_stateFile) )
                            {
                                LogCheckState lcs = LogCheckStateParser.readConfig(
                                        ParserUtil.readConfig(m_stateFile,
                                                LCFileFormat.LCSTATE));

                                currFileState = lcs.getLogFile();
                            }
                        }
                        else
                        {
                            try
                            {
                                currFileState = ((LogCheckState) tailerRes.getState()).getLogFile();
                            }
                            catch( Exception ex )
                            {
                                String msg = "Error casting LogCheckState";

                                LOGGER.debug(msg);
                                throw new LogCheckException(msg, ex);
                            }
                        }

                        ////////////////////////////////////////
                        // Backup File Processing Loop
                        ////////////////////////////////////////

                        while( true )
                        {
                            Path newestBackupFile = null;
                            Deque<Path> backupLogFilesQueue = new ArrayDeque<>(10);

                            Deque<LogCheckStateStatusBase> currSessionBackupFiles = null;

                            /////////////////////////////////////////////////////
                            // Backup File Listing Block
                            /////////////////////////////////////////////////////
                            {
                                // Get the list of processed backup files if they are available on disk
                                if( (statsProcessedLogFiles != null)
                                        && (statsProcessedLogFiles.get() != null)
                                        && (m_stateProcessedLogsFilePath != null)
                                        && Files.exists(m_stateProcessedLogsFilePath) )
                                {
                                    LogCheckState currState = statsProcessedLogFiles.get().restoreFileState(
                                            m_stateProcessedLogsFilePath,
                                            m_deDupeDir,
                                            m_readLogFileCount,
                                            m_readMaxDeDupeEntries);

                                    currSessionBackupFiles = currState.getCompletedStatuses();
                                }
                                else
                                {
                                    if( (currSessionBackupFiles != null)
                                            && (currSessionBackupFiles != sessionBackupFiles) )
                                    {
                                        LOGGER.warn("Changing pointer to the current Session Backup Files list.");
                                    }

                                    currSessionBackupFiles = sessionBackupFiles;
                                }

                                if( LOGGER.isDebugEnabled() )
                                {
                                    String msg = "Begin main BACKUP read loop. Backup Log Files :\n";

                                    if( currSessionBackupFiles != null )
                                    {
                                        msg = String.format("%s    Session Backup File Cache Size : %d\n",
                                                msg, currSessionBackupFiles.size());

                                        StringBuilder sb = new StringBuilder(100);
                                        for( LogCheckStateStatusBase lfs : currSessionBackupFiles )
                                        {
                                            LogFileStatus lf = (LogFileStatus)lfs;
                                            sb.append(String.format("    %s\n", lf.getPath()));
                                        }
                                        msg = String.format("%s%s", msg, sb.toString());
                                    }

                                 //   LOGGER.debug(msg);
                                }

                                // Try to detect then read the backup logs

                                if( currSessionBackupFiles == null )
                                {
                                    LOGGER.debug("Session Backup Files list is null.");
                                }
                                else if( currSessionBackupFiles.peekLast() != null )
                                {
                                    Optional<LogFileStatus> currStatus = currSessionBackupFiles.stream()
                                            .map( b -> (LogFileStatus)b )
                                            .sorted( (bk1, bk2) -> bk1.getProcessedStamp().compareTo(bk2.getProcessedStamp() ) )
                                            .reduce( (bk1, bk2) -> bk2 );
                                    if( currStatus.isPresent() )
                                    {
                                        newestBackupFile = currStatus.get().getPath();
                                    }
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

                                Path backupLogFile = null;
                                //Path firstPartialBackupFile = null;

                                // Save the backup file count as an indicator
                                if( currBackupFileWatch != null )
                                {
                                    // FIXME : Small race between last backup check in the above loop and this count
                                    currCreateFileEventCount
                                            = currBackupFileWatch.getDetectedCreatePathCount();
                                }

                                ////////////////////////////////////////////////////
                                // Find the number of backup files we need to check
                                ////////////////////////////////////////////////////

                                // If backup files were created will this loop runs,
                                // then try again until that stops.

                                Path currLastBackupFileProcessedTemp;
                                Deque<Path> backupLogFilesQueueTemp;

                                int prevNameCount = 0;
                                do
                                {
                                    // Loop until we have accounted for all the backup
                                    // logs.
                                    LOGGER.debug(String.format("Before prevName() loop %d",
                                            prevNameCount++));

                                    // Reset some temp variables
                                    currLastBackupFileProcessedTemp
                                            = currLastBackupFileProcessed;
                                    backupLogFilesQueueTemp
                                            = new ArrayDeque<>(backupLogFilesQueue);
                                    do
                                    {
                                        backupLogFile = LogCheckFileRotate.prevName(
                                                m_logFile.toAbsolutePath().getParent(),
                                                currLastBackupFileProcessedTemp,
                                                m_tailerBackupLogNameRegex,
                                                null,
                                                m_tailerBackupLogNameComps,
                                                true,
                                                currTailerBackupReadLogReverse);

                                        if( backupLogFile == null )
                                        {
                                            LOGGER.info("Could not find the previous backup file.");
                                        }
                                        else if( Files.notExists(backupLogFile) )
                                        {
                                            LOGGER.warn(String.format("Backup Log File '%s' not found", backupLogFile));
                                        }
                                        else
                                        {
                                            backupLogFilesQueueTemp.addLast(backupLogFile);
                                        }

                                        // Use to get the next backup file if any
                                        if( backupLogFile != null )
                                        {
                                            currLastBackupFileProcessedTemp = backupLogFile;
                                        }
                                    }
                                    while( backupLogFile != null );
                                }
                                while( (currBackupFileWatch != null)
                                        && (currCreateFileEventCount
                                        != currBackupFileWatch.getDetectedCreatePathCount()) );

                                currLastBackupFileProcessed = currLastBackupFileProcessedTemp;
                                backupLogFilesQueueTemp.forEach(backupLogFilesQueue::addFirst);

                                // Add the new files to the session file cache
                                if( currSessionBackupFiles == null )
                                {
                                    LOGGER.warn("Current Backup Files List is null.");
                                }
                                else
                                {
                                    for( Path path : backupLogFilesQueue )
                                    {
                                        boolean found = false;
                                        for( LogCheckStateStatusBase ls : currSessionBackupFiles )
                                        {
                                            LogFileStatus lfs = (LogFileStatus)ls;

                                            if( lfs.getPath().equals(path) )
                                            {
                                                found = true;
                                            }
                                        }

                                        if( found == false )
                                        {
                                            currSessionBackupFiles.addLast(
                                                    LogFileStatus.from(path, null, null, false));
                                        }
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
                            }

//                            if( firstPartialBackupFile == null )
//                            {
//                                // The first backup file is most likely partially read.
//                                // This only happens in a single backup file
//                                firstPartialBackupFile = backupLogFilesQueue.peekFirst();
//
//                                LOGGER.debug(String.format("First Partial Backup File Selected : %s",
//                                        firstPartialBackupFile));
//                            }

                            ////////////////////////////
                            // Now loop the found files
                            ////////////////////////////
                            {
                                boolean newestBackupFileSeen = false;

                                BasicThreadFactory logCheckTailerFactory
                                        = new BasicThreadFactory.Builder()
                                        .namingPattern("tailerCompletionThread-%d")
                                        .build();

                                ExecutorService logCheckTailerExe
                                        = Executors.newSingleThreadExecutor(logCheckTailerFactory);

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
                                            if( currFileState == null )
                                            {
                                                LOGGER.debug("No current Log File State.");
                                            }
                                            else
                                            {
                                                // Do we have the partial backup file in the file validate
                                                // result.
                                                Set<LCTailerResult> partBackupFile = null;

                                                try
                                                {
                                                    partBackupFile = LogFileState.validateFileBlocks(currFileState,
                                                            currBackupFile, true, LCFileBlockType.FIRSTBLOCK);
                                                }
                                                catch( LogCheckException ex )
                                                {
                                                    LOGGER.info(String.format("Validate File Block failed for '%s'",
                                                            currBackupFile), ex);
                                                }

                                                if( (partBackupFile != null)
                                                        && partBackupFile.contains(LCTailerResult.SUCCESS) )
                                                {
                                                    currStartPos = currFileState.getLastProcessedPosition();
                                                    LOGGER.debug(String.format(
                                                            "First Partial File Position %d for %s",
                                                            currStartPos, currBackupFile));
                                                }
                                                else
                                                {
                                                    if( LOGGER.isDebugEnabled() )
                                                    {
                                                        String msg = ArrayUtils.toString(partBackupFile);

                                                        LOGGER.debug(String.format("validateFileBlocks() returned '%s' on '%s'\n%s\n",
                                                                msg, currBackupFile, currFileState));
                                                    }
                                                }
                                            }

                                            // Parse that backup file
                                            FileLogCheckTail lct = FileLogCheckTail.from(m_mainLogEntryBuilders,
                                                    currBackupFile,
                                                    m_deDupeDir,
                                                    currStartPos,
                                                    0L,
                                                    false, // continue
                                                    false, // tail from end
                                                    false, // reOpenOnChunk
                                                    false, // saveState
                                                    false, // startPosIgnoreError
                                                    false,  // validate states
                                                    false, // collectStats
                                                    false, // watch backup dir
                                                    m_tailerBackupReadLog,
                                                    m_tailerBackupReadLogReverse,
                                                    false, // tailerBackupReadPriorLog
                                                    true, // stopOnEOF
                                                    true, // readOnlyFileMode
                                                    false,  // mainThread
                                                    false, // statsReset
                                                    null, // bufferSize
                                                    m_readLogFileCount,
                                                    m_readMaxDeDupeEntries,
                                                    null, // stopAfter
                                                    m_idBlockHash,
                                                    m_idBlockSize,
                                                    m_setName,
                                                    m_stateFile,
                                                    m_stateProcessedLogsFilePath,
                                                    m_errorFile,
                                                    m_tailerLogBackupDir,
                                                    m_preferredDir,
                                                    m_tailerBackupLogNameComps,
                                                    m_tailerBackupLogCompression,
                                                    m_tailerBackupLogNameRegex,
                                                    m_debugFlags);

                                            Future<LogCheckResult> fileTailFuture
                                                    = logCheckTailerExe.submit(lct);

                                            LogCheckResult fileTailRes = fileTailFuture.get();

                                            // Mark the current backup file as processed
                                            if( currSessionBackupFiles != null )
                                            {
                                                List<LogFileStatus> statusToAdd =
                                                        new ArrayList<>();

                                                for( Iterator<LogCheckStateStatusBase> lfs
                                                     = currSessionBackupFiles.iterator();
                                                     lfs.hasNext(); )
                                                {
                                                    LogFileStatus currLFS = (LogFileStatus)lfs.next();

                                                    // FIXME : We should iterate by HASH instead or as an option

                                                    if( currLFS.getPath().equals(currBackupFile) )
                                                    {
                                                        if( currLFS.isProcessed() )
                                                        {
                                                            LOGGER.debug(String.format("File is present and marked as processed '%s'",
                                                                    currBackupFile));
                                                        }

                                                        lfs.remove();
                                                        statusToAdd.add(LogFileStatus.from(
                                                                currBackupFile, null, Instant.now(), true
                                                        ));

                                                    }
                                                }

                                                currSessionBackupFiles.addAll(statusToAdd);
                                            }

                                            // TODO : Find currBackupFile in currSessionBackupFiles, turn on the processed flag and save
                                            if( (statsProcessedLogFiles != null)
                                                    && (statsProcessedLogFiles.get() != null) )
                                            {
                                                LogCheckState currStats = null;
                                                if( Files.exists(m_stateProcessedLogsFilePath) )
                                                {
                                                    // Merge the existing file list on disk with the current
                                                    currStats = TailerStatistics.restoreFileState(
                                                            m_stateProcessedLogsFilePath,
                                                            m_deDupeDir,
                                                            m_setName,
                                                            m_readLogFileCount,
                                                            m_readMaxDeDupeEntries);

                                                    Set<Path> currPaths
                                                            = currSessionBackupFiles.stream()
                                                                .map( i -> (LogFileStatus)i )
                                                                .map( i -> i.getPath() )
                                                                .collect(Collectors.toSet());

                                                    Set<Path> currPathsFromDisk
                                                            = currStats.getCompletedFileStatuses()
                                                                    .stream().map( i -> i.getPath() )
                                                            .collect(Collectors.toSet());

                                                    currPaths.addAll(currPathsFromDisk);

                                                    Deque<LogFileStatus> tempStatus = new ArrayDeque<>();

                                                    // Add the Log File Statuses
                                                    for( Path p : currPaths )
                                                    {
                                                        Optional<LogFileStatus> lfs =
                                                                currSessionBackupFiles.stream()
                                                                        .map( i -> (LogFileStatus)i )
                                                                        .filter(i -> i.getPath().equals(p) )
                                                                .findFirst();
                                                        if( lfs.isPresent() )
                                                        {
                                                            tempStatus.add(lfs.get());
                                                            continue;
                                                        }

                                                        lfs = currStats.getCompletedFileStatuses().stream()
                                                                        .filter(i -> i.getPath().equals(p) )
                                                                        .findFirst();
                                                        if( lfs.isPresent() )
                                                        {
                                                            tempStatus.add(lfs.get());
                                                        }
                                                    }

                                                    currStats.getCompletedStatuses().clear();
                                                    currStats.getCompletedStatuses().addAll(tempStatus);
                                                }
                                                else
                                                {
                                                    currStats = LogCheckState.from(null,
                                                            UUID.randomUUID(),
                                                            m_setName,
                                                            Instant.now(),
                                                            null,
                                                            currSessionBackupFiles);
                                                }

                                                TailerStatistics.save(currStats, m_stateProcessedLogsFilePath,
                                                        m_errorFile, false, true);
                                            }

                                            LOGGER.debug(String.format("Backup Completion Thread returned "
                                                    + "result %s for backup file [ %s ]",
                                                    currBackupFile, Arrays.toString(
                                                                    fileTailRes.getStatuses().toArray())));
                                        }
                                        catch( ExecutionException ex )
                                        {
                                            String errMsg = "Error completing backup file";

                                            LOGGER.debug(errMsg, ex);

                                            throw ex;
                                        }
                                    }
                                }
                            }

                            // Loop to re-check the FS for new backup files created in the short time
                            // we were processing
                        }

                        // FIXME: New backup files created between here and next FileTailer instance will be lost/skipped

                        // TODO : At this point VALIDATION_FAILED should now be fixed.

                        // Signal a statistics reset the last read position on disk
                        if( skipMainLog )
                        {
                            // FYI : We don't reset for delayMainLog
                            nextStatsReset = true;
                        }
                    }

                    LOGGER.debug("Validation fail fix branch completed.");
                }

                if( LOGGER.isDebugEnabled() )
                {
                    if( m_mainThread )
                    {
                        LOGGER.debug(String.format(
                                "Main Thread : Tailer Loop : PassCount %d completed.",
                                passCount));
                    }
                    else
                    {
                        LOGGER.debug(String.format(
                                "Sub-call : Tailer Loop : PassCount %d completed.",
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
