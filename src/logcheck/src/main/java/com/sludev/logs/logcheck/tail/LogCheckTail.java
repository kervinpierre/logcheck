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
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.enums.LCCompressionType;
import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckFileRotate;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
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
    private final Long m_delay;
    private final Boolean m_tailFromEnd;
    private final Boolean m_reOpenLogFile;
    private final Boolean m_continueState;
    private final Boolean m_saveState;
    private final Boolean m_startPositionIgnoreError;
    private final Boolean m_validateTailerStatistics;
    private final Boolean m_tailerBackupReadLog;
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

    private LogCheckTail(final List<ILogEntryBuilder> mainLogEntryBuilders,
                         final Path logFile,
                         final Path deDupeDir,
                         final Long delay,
                         final Boolean continueState,
                         final Boolean tailFromEnd,
                         final Boolean reOpenLogFile,
                         final Boolean saveState,
                         final Boolean startPosIgnoreError,
                         final Boolean validateTailerStatistics,
                         final Boolean tailerBackupReadLog,
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
                         final Pattern tailerBackupLogNameRegex)
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
        this.m_tailerLogBackupDir = tailerLogBackupDir;

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
                                    final Long delay,
                                    final Boolean continueState,
                                    final Boolean tailFromEnd,
                                    final Boolean reOpenOnChunk,
                                    final Boolean saveState,
                                    final Boolean startPosIgnoreError,
                                    final Boolean validateTailerStatistics,
                                    final Boolean tailerBackupReadLog,
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
                                    final Pattern tailerBackupLogNameRegex)
    {
        LogCheckTail res = new LogCheckTail(mainLogEntryBuilders,
                logFile,
                deDupeDir,
                delay,
                continueState,
                tailFromEnd,
                reOpenOnChunk,
                saveState,
                startPosIgnoreError,
                validateTailerStatistics,
                tailerBackupReadLog,
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
                tailerBackupLogNameRegex);

        return res;
    }

    @Override
    public LogCheckResult call() throws LogCheckException
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);

        long currDelay = m_delay ==null?0: m_delay;
        boolean currTailFromEnd = BooleanUtils.isNotFalse(m_tailFromEnd);

        Long currPosition = null;

        boolean currReOpen = BooleanUtils.isTrue(m_reOpenLogFile);
        boolean currStartPosIgrErr = BooleanUtils.isTrue(m_startPositionIgnoreError);
        boolean currValidateTStats = BooleanUtils.isTrue(m_validateTailerStatistics);
        boolean currTailerBackupReadLog = BooleanUtils.isTrue(m_tailerBackupReadLog);

        final ScheduledExecutorService statsSchedulerExe;

        if( m_logFile == null )
        {
            throw new LogCheckException("Log File cannot be null.");
        }

        TailerStatistics stats = TailerStatistics.from(m_logFile,
                m_stateFile,
                m_errorFile,
                m_idBlockHash,
                m_idBlockSize,
                m_setName);

        stats.setLastProcessedTimeStart( Instant.now() );

        if( m_saveState != null && m_saveState )
        {
            BasicThreadFactory tailerSaveFactory = new BasicThreadFactory.Builder()
                    .namingPattern("tailerSaveThread-%d")
                    .build();

            statsSchedulerExe = Executors.newScheduledThreadPool(1, tailerSaveFactory);

            statsSchedulerExe.scheduleWithFixedDelay(() ->
            {
                try
                {
                    stats.save(false, true);
                }
                catch( LogCheckException ex )
                {
                    LOGGER.debug("Error saving the logger state", ex);
                }
            }, LogCheckConstants.DEFAULT_SAVE_STATE_INTERVAL_SECONDS,
               LogCheckConstants.DEFAULT_SAVE_STATE_INTERVAL_SECONDS,
               TimeUnit.SECONDS );
        }
        else
        {
            statsSchedulerExe = null;
        }

        final AtomicReference<Tailer> mainTailer = new AtomicReference<>();
        final AtomicBoolean exitNow = new AtomicBoolean(false);

        final ScheduledExecutorService stopSchedulerExe;
        if( m_stopAfter != null
                && m_stopAfter > 0 )
        {
            stopSchedulerExe = Executors.newScheduledThreadPool(1);

            stopSchedulerExe.schedule(() ->
            {
                Tailer currTailer = mainTailer.get();
                if( currTailer != null )
                {
                    currTailer.stop();
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
                .namingPattern("tailerthread-%d")
                .build();

        ExecutorService tailerExe = Executors.newSingleThreadExecutor(tailerFactory);

        Set<LCTailerResult> tailerRes = new HashSet<>();

        boolean firstPass = true;
        try
        {
            // Tailer start
            // Main Tailer is restarted if 'reOpen' option is set.
            do
            {
                if( BooleanUtils.isTrue(m_continueState) ||
                        ( firstPass == false &&  currReOpen ) )
                {
                    // Reopen implies continue.  Because we use the logs to serialize state.

                    // We don't read the state on the first pass of the loop, unless --continue is present
                    // We do on the other passes though.

                    if( m_stateFile == null || Files.notExists(m_stateFile) )
                    {
                        LOGGER.debug(
                                String.format("Trying to 'continue' but the state file does not exist. '%s'",
                                        m_stateFile));
                    }
                    else
                    {
                        // Done every time before the Tailer thread starts.
                        LogCheckState currState = TailerStatistics.restore(m_stateFile,
                                m_deDupeDir,
                                m_setName,
                                m_readLogFileCount,
                                m_readMaxDeDupeEntries);

                        currPosition
                                = LogFileState.positionFromLogFile(currState.getLogFile());
                    }

                }

                mainTailer.set( Tailer.from(m_logFile,
                        currPosition,
                        Charset.defaultCharset(),
                        m_mainLogEntryBuilders,
                        currDelay*1000, // Convert to milliseconds
                        currTailFromEnd,
                        currReOpen,
                        currStartPosIgrErr,
                        currValidateTStats,
                        m_bufferSize,
                        stats) );

                Future<Set<LCTailerResult>> tailerExeRes = tailerExe.submit(mainTailer.get());

                // Wait until Tailer thread has completed.
                tailerRes = tailerExeRes.get();

                // FIXME : Set tail from end to be true or false based on Tailer result.
                // This should help implement log-rotate support

                if( tailerRes.contains(LCTailerResult.VALIDATION_FAIL) )
                {
                    // Log rotation detected?
                    LOGGER.debug("Log validation failed. We may need to check the old log file here.");

                    if( m_tailerBackupReadLog )
                    {
                        // Try to detect then read the backup logs

                        // Get last backup file by name
                        Path backupLogFile = LogCheckFileRotate.nextName(m_logFile,
                                m_tailerBackupLogNameRegex,
                                m_tailerBackupLogNameComps,
                                true);

                        // TODO : Parse that backup file

                        // TODO : Support decompressing the backup file first
                    }
                }

                try
                {
                    // At this point we should make sure that the statistics have been saved.
                    if(     // If validation failed somehow...
                            tailerRes.contains(LCTailerResult.VALIDATION_FAIL)
                            // Or "reopen log" is set with no continue and the position is invalid
                            || (stats.getLastProcessedPosition() < 1
                                && currReOpen
                                && BooleanUtils.isNotTrue(m_continueState)) )
                    {
                        // Reset the start position on disk
                        stats.save(true, true);
                    }
                    else
                    {
                        stats.save(false, true);
                    }
                }
                catch( LogCheckException ex )
                {
                    // Happens if the file was rotated for instance
                    LOGGER.debug("Error saving state after Tailer", ex);
                }

                firstPass = false;
            }
            while( tailerRes.contains(LCTailerResult.REOPEN) && exitNow.get() == false);
        }
        catch (InterruptedException ex)
        {
            LOGGER.error("Application 'run' thread was interrupted", ex);

            // We don't have to do much here because the interrupt got us out
            // of the while loop.

        }
        catch (ExecutionException ex)
        {
            LOGGER.error("Application 'run' execution error", ex);
        }
        finally
        {
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
