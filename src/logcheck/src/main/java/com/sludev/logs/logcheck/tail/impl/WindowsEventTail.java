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

import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.entities.impl.LogCheckState;
import com.sludev.logs.logcheck.config.entities.impl.WindowsEventLogCheckState;
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.tail.ILogCheckTail;
import com.sludev.logs.logcheck.tail.TailerResult;
import com.sludev.logs.logcheck.tail.TailerStatistics;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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
import java.util.stream.Collectors;

/**
 * Management class for tailing log files.
 *
 * @author kervin
 */
public final class WindowsEventTail implements ILogCheckTail
{
    private static final Logger LOGGER
                                    = LogManager.getLogger(WindowsEventTail.class);

    private final List<ILogEntryBuilder> m_mainLogEntryBuilders;

    private final String m_windowsConnectionStr;
    private final Path m_deDupeDir;
    private final Path m_stateFile;
    private final WindowsEventLogCheckState m_startingState;
    private final Path m_errorFile;
    private final Path m_preferredDir;
    private final Long m_delay;
    private final Boolean m_tailFromEnd;
    private final Boolean m_reOpenLogFile;
    private final Boolean m_continueState;
    private final Boolean m_saveState;
    private final Boolean m_startPositionIgnoreError;
    private final Boolean m_validateTailerStatistics;
    private final Boolean m_collectTailerStatistics;
    private final Boolean m_stopOnEOF;
    private final Boolean m_mainThread;
    private final Boolean m_statsReset;
    private final Integer m_bufferSize;
    private final Integer m_readLogFileCount;
    private final Integer m_readMaxDeDupeEntries;
    private final Long m_stopAfter;
    private final LCHashType m_idBlockHash;
    private final Integer m_idBlockSize;
    private final String m_setName;
    private final Set<LCDebugFlag> m_debugFlags;

    private WindowsEventTail( final List<ILogEntryBuilder> mainLogEntryBuilders,
                              final String windowsConnectionStr,
                              final Path deDupeDir,
                              final WindowsEventLogCheckState startingState,
                              final Long delay,
                              final Boolean continueState,
                              final Boolean tailFromEnd,
                              final Boolean reOpenLogFile,
                              final Boolean saveState,
                              final Boolean startPosIgnoreError,
                              final Boolean validateTailerStatistics,
                              final Boolean collectTailerStatistics,
                              final Boolean stopOnEOF,
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
                              final Path errorFile,
                              final Path preferredDir,
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
        this.m_stopOnEOF = stopOnEOF;
        this.m_mainThread = mainThread;
        this.m_debugFlags = debugFlags;
        this.m_statsReset = statsReset;
        this.m_preferredDir = preferredDir;

        this.m_startingState = startingState;

        // Don't bother with logs we missed earlier

        if( tailFromEnd != null )
        {
            this.m_tailFromEnd = tailFromEnd;
        }
        else
        {
            this.m_tailFromEnd = true;
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

        if( windowsConnectionStr != null )
        {
            this.m_windowsConnectionStr = windowsConnectionStr;
        }
        else
        {
            this.m_windowsConnectionStr = null;
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

    public static WindowsEventTail from( final List<ILogEntryBuilder> mainLogEntryBuilders,
                                         final String windowsConnectionStr,
                                         final Path deDupeDir,
                                         final WindowsEventLogCheckState startingState,
                                         final Long delay,
                                         final Boolean continueState,
                                         final Boolean tailFromEnd,
                                         final Boolean reOpenOnChunk,
                                         final Boolean saveState,
                                         final Boolean startPosIgnoreError,
                                         final Boolean validateTailerStatistics,
                                         final Boolean collectTailerStatistics,
                                         final Boolean stopOnEOF,
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
                                         final Path errorFile,
                                         final Path preferredDir,
                                         final Set<LCDebugFlag> debugFlags)
    {
        WindowsEventTail res = new WindowsEventTail(mainLogEntryBuilders,
                windowsConnectionStr,
                deDupeDir,
                startingState,
                delay,
                continueState,
                tailFromEnd,
                reOpenOnChunk,
                saveState,
                startPosIgnoreError,
                validateTailerStatistics,
                collectTailerStatistics,
                stopOnEOF,
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
                errorFile,
                preferredDir,
                debugFlags);

        return res;
    }

    @Override
    public LogCheckResult call() throws LogCheckException, ExecutionException, InterruptedException, IOException
    {
        if( LOGGER.isDebugEnabled() )
        {
            StringBuilder msg = new StringBuilder(100);

            msg.append("\nWindowsEventTailTail\n{\n");
            msg.append(String.format("  Windows Event   : %s\n", m_windowsConnectionStr));
            msg.append(String.format("  Main Thread     : %b\n", m_mainThread));
            msg.append("}\n");

            LOGGER.debug(msg.toString());
        }

        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);

        long currDelay = (m_delay == null) ? 0 : m_delay;
        boolean currTailFromEnd = BooleanUtils.isNotFalse(m_tailFromEnd);

        boolean currReOpen = BooleanUtils.isTrue(m_reOpenLogFile);
        boolean currStartPosIgrErr = BooleanUtils.isTrue(m_startPositionIgnoreError);
        boolean currStatsValidate = BooleanUtils.isTrue(m_validateTailerStatistics);
        boolean currStatsCollect = BooleanUtils.isNotFalse(m_collectTailerStatistics);
        boolean currStopOnEOF = BooleanUtils.isTrue(m_stopOnEOF);
        boolean currSaveState = BooleanUtils.isNotFalse(m_saveState);
        boolean currStatsReset = BooleanUtils.isTrue(m_statsReset);

        final ScheduledExecutorService statsSchedulerExe;

        if( StringUtils.isBlank(m_windowsConnectionStr) )
        {
            throw new LogCheckException("Windows Connection String.");
        }

        final AtomicReference<TailerStatistics> stats;

        // Check to see if we're allowed to collect stats at all
        if( currStatsCollect )
        {
            stats = new AtomicReference<>(TailerStatistics.from(m_stateFile,
                    m_errorFile,
                    m_setName));

            Instant currNow = Instant.now();
            stats.get().setLastProcessedTimeStart(currNow);

            // Create the statistics thread
            if( currSaveState )
            {
                if( m_stateFile == null )
                {
                    LOGGER.warn("SAVE_STATE option set but at least one state file option is missing.");
                }

                BasicThreadFactory tailerSaveFactory = new BasicThreadFactory.Builder()
                        .namingPattern("windowsEventTailerSaveThread-%d")
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

        final AtomicReference<WindowsEventTailer> mainTailer = new AtomicReference<>();
        final AtomicBoolean exitNow = new AtomicBoolean(false);

        final ScheduledExecutorService stopSchedulerExe;

        // Setup "Stop After"/Termination thread
        if( (m_stopAfter != null)
                && (m_stopAfter > 0) )
        {
            BasicThreadFactory scheduleFactory = new BasicThreadFactory.Builder()
                    .namingPattern("windowsEventTailerScheduleThread-%d")
                    .daemon(true)
                    .build();

            stopSchedulerExe = Executors.newScheduledThreadPool(1, scheduleFactory);

            stopSchedulerExe.schedule(() ->
            {
                WindowsEventTailer currWindowsEventTailer = mainTailer.get();
                if( currWindowsEventTailer == null )
                {
                    LOGGER.warn("Process Stop Scheduler called, but Tailer object is null.");
                }
                else
                {
                    currWindowsEventTailer.stop();
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
                .namingPattern("windowsEventTailerThread-%d")
                .build();

        ExecutorService tailerExe = Executors.newSingleThreadExecutor(tailerFactory);

        TailerResult tailerRes = null;

        BigInteger passCount = BigInteger.ZERO;
        try
        {
            // Keeps track of the last backup file that has been processed
            // FIXME : Last backup file that has been processed should be stored/restored on/from disk.  Maybe with DeDupe files?
            //Path newestBackupFile = null;

            Set<LCTailerResult> currTailerResults = null;

            boolean nextStatsReset = currStatsReset;

            WindowsEventLogCheckState currPosition = m_startingState;

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
                        WindowsEventLogCheckState currState = stats.get().restoreWindowsEventState( m_stateFile,
                                m_deDupeDir,
                                m_readLogFileCount,
                                m_readMaxDeDupeEntries );

                        // If we haven't been given a position, get one from disk
                        if( currPosition == null )
                        {
                            currPosition = currState;
                        }
                    }
                }

                final CountDownLatch currCompletionLatch = new CountDownLatch(1);

                // Create this iteration's File Tailer Object
                mainTailer.set(WindowsEventTailer.from(m_windowsConnectionStr,
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
                        currPosition,
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

                // Wait until Tailer thread has completed.
                try
                {
                    tailerRes = tailerExeRes.get();
                }
                catch( CancellationException ex )
                {
                    if( currCompletionLatch != null )
                    {
                        // Wait to be signalled by the Tailer thread before continuing
                        LOGGER.debug("Waiting for tailer to complete.");

                        currCompletionLatch.await();
                    }

                    // New backup files cancel the running tailer task
//                        LOGGER.debug(String.format("Tailer cancelled for '%s'",
//                                m_logFile));

                    // The result from the object directly
                    tailerRes = mainTailer.get().getFinalResult();
                    if( tailerRes == null )
                    {
                        LOGGER.debug("Tailer cancelled and its result is null.");
                    }
                }

                // FIXME : Set tail from end to be true or false based on Tailer result.
                // This should help implement log-rotate support

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
                                   ""); //m_logFile);

                            // We really just end processing at this point to avoid an infinite loop
                            LOGGER.debug(msg);

                            throw new LogCheckException(msg);
                        }
                    }

                    if( currTailerResults.contains(LCTailerResult.STATISTICS_RESET) )
                    {
                        nextStatsReset = true;
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
