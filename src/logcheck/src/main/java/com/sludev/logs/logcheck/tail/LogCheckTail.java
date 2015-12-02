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

import com.sludev.logs.logcheck.config.entities.LogCheckDeDupeLog;
import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogEntryDeDupe;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.parsers.LogCheckStateParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.dedupe.ContinueUtil;
import com.sludev.logs.logcheck.enums.LCFileFormats;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.BooleanUtils;
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

    private final List<ILogEntryBuilder> mainLogEntryBuilders;
    
    private final Path logFile;
    private final Path deDupeDir;
    private final Long delay;
    private final Boolean tailFromEnd;
    private final Boolean reOpenLogFile;
    private final Boolean continueState;
    private final Boolean saveState;
    private final Boolean startPositionIgnoreError;
    private final Integer bufferSize;
    private final Integer readLogFileCount;
    private final Integer readMaxDeDupeEntries;
    private final Long stopAfter;
    private final LCHashType idBlockHash;
    private final Integer idBlockSize;
    private final String setName;
    private final Path stateFile;
    private final Path errorFile;

    private LogCheckTail(final List<ILogEntryBuilder> mainLogEntryBuilders,
                         final Path logFile,
                         final Path deDupeDir,
                         final Long delay,
                         final Boolean continueState,
                         final Boolean tailFromEnd,
                         final Boolean reOpenLogFile,
                         final Boolean saveState,
                         final Boolean startPositionIgnoreError,
                         final Integer bufferSize,
                         final Integer readLogFileCount,
                         final Integer readMaxDeDupeEntries,
                         final Long stopAfter,
                         final LCHashType idBlockHash,
                         final Integer idBlockSize,
                         final String setName,
                         final Path stateFile,
                         final Path errorFile)
    {
        this.mainLogEntryBuilders = mainLogEntryBuilders;
        this.idBlockHash = idBlockHash;
        this.idBlockSize = idBlockSize;
        this.setName = setName;
        this.continueState = continueState;
        this.deDupeDir = deDupeDir;
        this.readLogFileCount = readLogFileCount;
        this.readMaxDeDupeEntries = readMaxDeDupeEntries;

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

        if( startPositionIgnoreError != null )
        {
            this.startPositionIgnoreError = startPositionIgnoreError;
        }
        else
        {
            this.startPositionIgnoreError = false;
        }

        if( reOpenLogFile != null )
        {
            this.reOpenLogFile = reOpenLogFile;
        }
        else
        {
            this.reOpenLogFile = false;
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

    public static LogCheckTail from(final List<ILogEntryBuilder> mainLogEntryBuilders,
                                    final Path logFile,
                                    final Path deDupeDir,
                                    final Long delay,
                                    final Boolean continueState,
                                    final Boolean tailFromEnd,
                                    final Boolean reOpenOnChunk,
                                    final Boolean saveState,
                                    final Boolean startPositionIgnoreError,
                                    final Integer bufferSize,
                                    final Integer readLogFileCount,
                                    final Integer readMaxDeDupeEntries,
                                    final Long stopAfter,
                                    final LCHashType idBlockHash,
                                    final Integer idBlockSize,
                                    final String setName,
                                    final Path stateFile,
                                    final Path errorFile)
    {
        LogCheckTail res = new LogCheckTail(mainLogEntryBuilders,
                logFile,
                deDupeDir,
                delay,
                continueState,
                tailFromEnd,
                reOpenOnChunk,
                saveState,
                startPositionIgnoreError,
                bufferSize,
                readLogFileCount,
                readMaxDeDupeEntries,
                stopAfter,
                idBlockHash,
                idBlockSize,
                setName,
                stateFile,
                errorFile);

        return res;
    }

    @Override
    public LogCheckResult call() throws LogCheckException
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);

        long currDelay = delay==null?0:delay;
        boolean currTailFromEnd = BooleanUtils.isNotFalse(tailFromEnd);

        Long currPosition = null;

        boolean currReOpen = BooleanUtils.isTrue(reOpenLogFile);
        boolean currStartPosIgnoreError = BooleanUtils.isTrue(startPositionIgnoreError);

        final ScheduledExecutorService statsSchedulerExe;

        if( logFile == null )
        {
            throw new LogCheckException("Log File cannot be null.");
        }

        TailerStatistics stats = TailerStatistics.from(logFile,
                stateFile,
                errorFile,
                idBlockHash,
                idBlockSize,
                setName);

        stats.setLastProcessedTimeStart( Instant.now() );

        if( saveState != null && saveState )
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

        final AtomicReference<Tailer> mainTailer = new AtomicReference<>();
        final AtomicBoolean exitNow = new AtomicBoolean(false);

        final ScheduledExecutorService stopSchedulerExe;
        if( stopAfter != null
                && stopAfter > 0 )
        {
            stopSchedulerExe = Executors.newScheduledThreadPool(1);

            stopSchedulerExe.schedule(() ->
            {
                Tailer currTailer = mainTailer.get();
                if( currTailer != null )
                {
                    currTailer.stop();
                    exitNow.set(true);

                    log.debug("Process Stop Scheduler called.");
                }

                stopSchedulerExe.shutdownNow();
            }, stopAfter, TimeUnit.SECONDS);

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

        LCTailerResult tailerRes = LCTailerResult.NONE;

        boolean firstPass = true;
        try
        {
            // Tailer start
            // Main Tailer should be restarted if 'reOpen' option is set.
            do
            {
                if( BooleanUtils.isTrue(continueState) ||
                        ( firstPass == false &&  currReOpen ) )
                {
                    // Reopen implies continue.  Because we use the logs to serialize state.

                    // We don't read the state on the first pass of the loop, unless --continue is present
                    // We do on the other passes though.

                    if( stateFile == null || Files.notExists(stateFile) )
                    {
                        log.debug(
                                String.format("Trying to 'continue' but the state file does not exist. '%s'",
                                        stateFile));
                    }
                    else
                    {
                        // Done every time before the Tailer thread starts.
                        LogCheckState currState = retrieveState(stateFile,
                                deDupeDir,
                                setName,
                                readLogFileCount,
                                readMaxDeDupeEntries);

                        currPosition
                                = positionFromLogFile(currState.getLogFile());
                    }

                }

                mainTailer.set( Tailer.from(logFile,
                        currPosition,
                        Charset.defaultCharset(),
                        mainLogEntryBuilders,
                        currDelay*1000, // Convert to milliseconds
                        currTailFromEnd,
                        currReOpen,
                        currStartPosIgnoreError,
                        Tailer.DEFAULT_BUFSIZE,
                        stats,
                        setName,
                        stateFile) );

                Future<LCTailerResult> tailerExeRes = tailerExe.submit(mainTailer.get());

                // Wait until Tailer thread has completed.
                tailerRes = tailerExeRes.get();

                // FIXME : Set tail from end to be true or false based on Tailer result.
                // This should help implement log-rotate support

                // At this point we should make sure that the statistics have been saved.
                if( stats.getLastProcessedPosition() < 1
                        && currReOpen
                        && BooleanUtils.isNotTrue(continueState) )
                {
                    // Reset the start position on disk
                    stats.save(true);
                }
                else
                {
                    stats.save();
                }

                firstPass = false;
            }
            while( tailerRes == LCTailerResult.REOPEN && exitNow.get() == false);
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

    public static Long positionFromLogFile(LogFileState state) throws LogCheckException
    {
        Long res = null;

        Long stateStartPos = state.getLastProcessedPosition();
        Long stateStartLine = state.getLastProcessedLineNumber();
        Long stateStartChar = state.getLastProcessedCharNumber();

        if( stateStartPos != null && stateStartPos >= 0 )
        {
            res = stateStartPos;
        }
        else
        {
            if( stateStartLine != null && stateStartLine >= 0
                    && stateStartChar != null && stateStartChar >= 0 )
            {
                // Use the line and char numbers to calculate the starting position
                try
                {
                    ByteBuffer buffer = ByteBuffer.allocate(64);
                    long lineCount = 0;
                    long charCount = 0;

                    FileChannel reader = FileChannel.open(state.getFile(),
                            StandardOpenOption.READ);
                    while( reader.read(buffer)!= -1 )
                    {
                        buffer.flip();

                        for( int i = 0; i < buffer.limit(); i++ )
                        {
                            final byte ch = buffer.get();
                            if( ch == '\n' )
                            {
                                if( lineCount == stateStartLine
                                        && charCount < stateStartChar)
                                {
                                    throw new LogCheckException(
                                            String.format("Asked to start at Line %d and Char %d,"
                                                            + " but line only has %d characters",
                                                    stateStartLine, stateStartChar, charCount));
                                }

                                lineCount++;
                            }
                            else
                            {
                                if( lineCount == stateStartLine )
                                {
                                    charCount++;
                                }
                            }

                            if( lineCount == stateStartLine && charCount == stateStartChar )
                            {
                                res = reader.position();
                            }
                        }
                    }
                }
                catch( IOException ex )
                {
                    log.debug("Error calculating line position.", ex);
                }
            }
        }

        return res;
    }

    public static LogCheckState retrieveState(final Path stateFile,
                                              final Path deDupeDir,
                                              final String setName,
                                              final Integer logFileCount,
                                              final Integer maxLogEntries) throws LogCheckException
    {
        // Read the last run's deduplication logs
        List<LogCheckDeDupeLog> ddLogs
                = ContinueUtil.readLastDeDupeLogs(deDupeDir,
                setName,
                null,
                logFileCount);

        List<LogEntryDeDupe> ddObjs
                = ContinueUtil.lastLogEntryDeDupes(ddLogs, maxLogEntries);

        // Read state file for information about the last run
        LogCheckState lcConf = LogCheckStateParser.readConfig(
                ParserUtil.readConfig(stateFile,
                        LCFileFormats.LCSTATE));

        // TODO : Allow 'look back' support to allow using the deduplication logs for confirming the file pointer's accuracy

        return lcConf;
    }
}
