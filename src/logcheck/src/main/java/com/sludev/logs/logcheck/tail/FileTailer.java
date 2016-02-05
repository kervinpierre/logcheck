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
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCFileBlockType;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple implementation of the unix "tail -f" functionality.
 *
 * TODO : This should be the ONLY class doing I/O on the file being tailed.
 */
public final class FileTailer implements Callable<FileTailerResult>
{
    private static final Logger LOGGER
                    = LogManager.getLogger(FileTailer.class);

    /**
     * The m_file which will be tailed.
     */
    private final Path m_file;

    /**
     * The character set that will be used to read the m_file.
     */
    private final Charset m_cset;

    /**
     * The amount of time to wait for the m_file to be updated.
     */
    private final long m_delayMillis;

    private final Long m_startPosition;

    private final int m_bufferSize;

    /**
     * Whether to tail from the end or start of m_file
     */
    private final boolean m_end;

    private final boolean m_statsValidate;
    private final boolean m_statsCollect;

    /**
     * Reset the statistics on disk before starting tailing
     */
    private final boolean m_statsReset;

    /**
     * The listener to notify of events when tailing.
     */
    private final List<ILogEntryBuilder> m_builders;

    /**
     * Whether to close and reopen the m_file whilst waiting for more input.
     */
    private final boolean m_reOpen;

    private final boolean m_startPosIgnoreErr;

    private final boolean m_stopOnEOF;

    private final int m_saveTimerSeconds;

    private final TailerStatistics m_statistics;

    private final LCHashType m_hashType;
    private final Integer m_idBlockSize;
    private final String m_setName;

    /**
     * Needed since Future will not differentiate between cancelled and
     * terminated.
     */
    private final CountDownLatch m_completionLatch;

    // Mutable

    private final Set<LCDebugFlag> m_debugFlags;

    /**
     * The tailer will run as long as this value is true.
     */
    private volatile boolean m_run = true;

    private volatile boolean m_doCollectStats = false;

    private String lineRemainder;

    private LogCheckState m_lastLCState;

    private FileTailerResult m_finalResult;
    /**
     * Used to debug and track Logcheck Log App's log sequence.
     */
    public static int DEBUG_LCAPP_LOG_SEQUENCE = 0;

    /**
     * Creates a Tailer for the given m_file, with a specified buffer size.
     *
     * @param file the m_file to follow.
     * @param cset the Charset to be used for reading the m_file
     * @param delayMillis the delay between checks of the m_file for new content
     * in milliseconds.
     * @param end Set to true to tail from the end of the m_file, false to tail
     * from the beginning of the m_file.
     * @param reOpen if true, close and reopen the m_file between reading chunks
     * @param bufSize Buffer size
     */
    private FileTailer(final Path file,
                       final Long startPosition,
                       final Charset cset,
                       final List<ILogEntryBuilder> builders,
                       final long delayMillis,
                       final boolean end,
                       final boolean reOpen,
                       final boolean startPosIgnoreErr,
                       final boolean statsValidate,
                       final boolean statsCollect,
                       final boolean statsReset,
                       final boolean stopOnEOF,
                       final int bufSize,
                       final int saveTimerSeconds,
                       final TailerStatistics stats,
                       final LCHashType hashType,
                       final Integer idBlockSize,
                       final String setName,
                       final Set<LCDebugFlag> debugFlags,
                       final CountDownLatch completionLatch)
    {
        this.m_file = file;
        this.m_delayMillis = delayMillis;
        this.m_end = end;
        this.m_startPosIgnoreErr = startPosIgnoreErr;
        this.m_bufferSize = bufSize;

        // Save and prepare the listener
        this.m_builders = builders;
        this.m_reOpen = reOpen;
        this.m_statsValidate = statsValidate;
        this.m_statsCollect = statsCollect;
        this.m_statsReset = statsReset;
        this.m_cset = cset;
        this.m_statistics = stats;

        this.m_hashType = hashType;
        this.m_idBlockSize = idBlockSize;
        this.m_setName = setName;

        this.m_stopOnEOF = stopOnEOF;

        this.m_startPosition = startPosition;

        this.lineRemainder = null;
        this.m_lastLCState = null;

        this.m_debugFlags = debugFlags;

        this.m_saveTimerSeconds = saveTimerSeconds;
        this.m_completionLatch = completionLatch;

        this.m_finalResult = null;
    }

    public static FileTailer from(final Path file,
                                  final Long startPosition,
                                  final Charset cset,
                                  final List<ILogEntryBuilder> builders,
                                  final long delayMillis,
                                  final boolean end,
                                  final boolean reOpen,
                                  final boolean startPosIgnoreErr,
                                  final boolean statsValidate,
                                  final boolean statsCollect,
                                  final boolean statsReset,
                                  final boolean stopOnEOF,
                                  final int bufSize,
                                  final int saveTimerSeconds,
                                  final TailerStatistics stats,
                                  final LCHashType hashType,
                                  final Integer idBlockSize,
                                  final String setName,
                                  final Set<LCDebugFlag> debugFlags,
                                  final CountDownLatch completionLatch)
    {
        FileTailer res = new FileTailer(file,
                startPosition,
                cset,
                builders,
                delayMillis,
                end,
                reOpen,
                startPosIgnoreErr,
                statsValidate,
                statsCollect,
                statsReset,
                stopOnEOF,
                bufSize,
                saveTimerSeconds,
                stats,
                hashType,
                idBlockSize,
                setName,
                debugFlags,
                completionLatch);

        return res;
    }

    /**
     * Return the file.
     *
     * @return the file
     */
    public Path getFile()
    {
        return m_file;
    }

    /**
     * Allows us to pass the result object out as soon as it's ready. No need
     * to wait for call() to complete and return that value.  Which can happen
     * if the completion latch is signalled before return.
     *
     * @return
     */
    public synchronized FileTailerResult getFinalResult()
    {
        return m_finalResult;
    }

    /**
     * Follows changes in the file, calling the ITailerListener's handle method
     * for each new line.
     */
    @Override
    public FileTailerResult call() throws LogCheckException, IOException
    {
        if( LOGGER.isInfoEnabled() )
        {
            LOGGER.info(String.format("Starting tailer : TailerStart .\n%s", toString()));
        }

        FileTailerResult res = FileTailerResult.from(null, null);
        FileChannel reader = null;
        long position = 0; // position within the m_file

        if( m_reOpen )
        {
            // Default result for re-open, is REOPEN
            res.getResultSet().add(LCTailerResult.REOPEN);
        }

        if( m_saveTimerSeconds > 0 )
        {
            // Signals to the reader when to collect statistics
            BasicThreadFactory tailerSaveFactory = new BasicThreadFactory.Builder()
                    .namingPattern("tailerCollectThread-%d")
                    .build();

            final ScheduledExecutorService statsSchedulerExe
                    = Executors.newScheduledThreadPool(1, tailerSaveFactory);

            statsSchedulerExe.scheduleWithFixedDelay(() ->
                    {
                        m_doCollectStats = true;
                    }, m_saveTimerSeconds,
                    m_saveTimerSeconds,
                    TimeUnit.SECONDS );
        }

        try
        {
            if( m_file == null )
            {
                throw new LogCheckException("Log File cannot be null");
            }

            if( (m_run && Files.notExists(m_file)) )
            {
                LOGGER.info(String.format("File does not exist log file '%s'", m_file));
                m_run = false;

                if( m_end
                        && ((m_startPosition == null) || (m_startPosition < 0)) )
                {
                    // File does not exist AND we have an invalid start position AND "Tail-From-End"

                    // Rest the position to 0 on disk
                    if( m_statsCollect )
                    {
                        LOGGER.debug("call() : Saving state. File does not exist and invalid position.");
                        m_lastLCState = getStateReset(
                                m_file,
                                m_idBlockSize,
                                m_hashType,
                                m_setName,
                                Instant.now());

                        m_statistics.save(m_lastLCState, true, false);
                        m_statistics.clearPendingSaveState();
                    }
                }

                // Delay here in case re-open is attempted
                if( m_delayMillis > 0 )
                {
                    LOGGER.debug(String.format("Sleeping for %dms", m_delayMillis));
                    Thread.sleep(m_delayMillis);
                    LOGGER.debug("Delay end.");
                }
            }

            Long currStartPos = m_startPosition;
            if( m_run && m_statsReset )
            {
                // Reset the statistics on disk before beginning tailing.
                // E.g. if the are invalid
                LOGGER.debug("call() : Resetting the statistics  before start.");
                m_lastLCState = getStateReset(
                        m_file,
                        m_idBlockSize,
                        m_hashType,
                        m_setName,
                        Instant.now());

                m_statistics.save(m_lastLCState, true, false);
                m_statistics.clearPendingSaveState();

                currStartPos = 0L;
            }

            if( m_run && Files.exists(m_file) )
            {
                try
                {
                    reader = FileChannel.open(m_file, StandardOpenOption.READ);
                }
                catch( IOException ex )
                {
                    String errMSg = String.format("Error opening log file '%s'", m_file);

                    LOGGER.info(errMSg, ex);
                    m_run = false;

                    // Delay here in case re-open is attempted
                    if( m_delayMillis > 0 )
                    {
                        LOGGER.debug(String.format("Sleeping for %dms", m_delayMillis));
                        Thread.sleep(m_delayMillis);
                        LOGGER.debug("Delay end.");
                    }
                }
            }

            if( m_run && (reader == null) )
            {
                LOGGER.debug("Log file reader cannot be null");
                throw new LogCheckException("Log file reader cannot be null");
            }

            // Set the current position in the log m_file
            if( m_run && (reader != null) )
            {
                if( (currStartPos != null) && (currStartPos >= 0) )
                {
                    if( m_end )
                    {
                        LOGGER.debug(
                                String.format("Both 'tail-from-end' and a "
                                        + "starting byte position of %d where provided. Start Position has precedence",
                                        currStartPos));
                    }

                    if( (currStartPos > reader.size())
                            && (m_startPosIgnoreErr == false) )
                    {
                        String errMsg = String.format("File start position ( %d ) can not be further than the file's"
                                        + " last position ( %d ).  Was the file truncated since last run?",
                                currStartPos, reader.size());

                        LOGGER.debug(errMsg);

                        if( m_statsValidate )
                        {
                            // FIXME : Shouldn't we return the state?  So that backups can be processed?

                            stop();
                            res = FileTailerResult.from(res.getResultSet(), m_lastLCState);
                            res.getResultSet().add(LCTailerResult.VALIDATION_FAIL);
                        }
                        else
                        {
                            // FIXME : Start from the beginning?
                            ;
                        }
                    }

                    // Start where instructed
                    if( m_run )
                    {
                        position = (currStartPos > reader.size()) ? reader.size() : currStartPos;
                    }
                }
                else
                {
                    if( m_end )
                    {
                        // Tail from the end of the m_file
                        position = reader.size();

                        LOGGER.debug(String.format("call() : m_end is true.  Position set to %d", position));
                    }
                }

                reader.position(position);
            }

            // Now loop the log m_file
            while( m_run && (reader != null) )
            {
                if( m_statsValidate && (m_statsReset == false) )
                {
                    LogCheckState lastState = m_statistics.getRestoredStates().peekFirst();
                    if( m_lastLCState != null )
                    {
                        // Use the last state if we have it.
                        lastState = m_lastLCState;
                    }

                    if( lastState == null )
                    {
                        LOGGER.debug("call() : Validating state but no 'LAST_STATE' value.");
                    }
                    else
                    {
                        ///////////////////////////////////////////////
                        //
                        // Final validate before reading the log file
                        //
                        ///////////////////////////////////////////////
                        Set<LCTailerResult> valRes = validateStatistics(lastState);
                        if( valRes.contains( LCTailerResult.SUCCESS ) == false )
                        {
                            // Statistics validation from disk failed.
                            res = FileTailerResult.from(res.getResultSet(), lastState);
                            res.getResultSet().addAll(valRes);
                            stop();
                            break;
                        }
                    }
                }

                // FIXME : If the log file is rotated between validation above
                //         and here it is possible to lose the tail logs in that
                //         file.

                // FIXME : ID the FIRST_BLOCK on the file here, before reading.
                //         If this ID fails we don't bother with the log builders

                // Final check for a log rotate before reading the log file
                // This is really the last time we can try for now.
                if( Thread.interrupted() )
                {
                    m_run = false;

                    LOGGER.debug("call() : Interrupt detected.  Exiting.");

                    res.getResultSet().add(LCTailerResult.INTERRUPTED);
                }

                // ID the file then Read from the file on disk
                readLines(reader);

                // TODO : Confirm FIRST_BLOCK here, and ID LAST_BLOCK from read data
                //        Detects 'Log swap while tailing'

                // FIXME : Statistics thread should NOT ID file blocks.
                //         This causes lots of races

                // Save the last state if it hasn't been for specific
                // result codes.  Don't save the state if there was a
                // validation error.
                if( res.getResultSet().contains(LCTailerResult.VALIDATION_FAIL)
                        || res.getResultSet().contains(LCTailerResult.INTERRUPTED))
                {
                    LOGGER.debug("Skipping state save because result includes VALIDATION_FAIL.");
                }
                else
                {
                    if( m_statsCollect )
                    {
                        // FIXME : Last processed time incorrect or missing

                        LOGGER.debug( "call() : Saving state.");
                        m_lastLCState = getState(reader,
                                m_file,
                                m_idBlockSize,
                                m_hashType,
                                m_setName,
                                Instant.now(),
                                false);

                        m_statistics.save(m_lastLCState, true, false);
                        m_statistics.clearPendingSaveState();
                    }
                }

                if( m_reOpen || m_stopOnEOF )
                {
                    // reOpen means read to the end of m_file then quit.
                    // The monitoring thread should relaunch after a period
                    // of time.
                    stop();

                    // Close the reader before the delay so other processes
                    // are less likely to deal with m_file locking issues on
                    // Windows.
                    IOUtils.closeQuietly(reader);
                    reader = null;
                }

                // Delay exit if requested.
                // This gives us a fixed interval *between* calls.
                // BUG : Delay has to be inside this loop for non-reopen tailing
                if( m_delayMillis > 0 )
                {
                    LOGGER.debug(String.format("Sleeping/delay for %dms\n",
                            m_delayMillis));

                    // Delay without interrupts
                    final FileTailer objInstance = this;
                    final AtomicBoolean delayCompleted = new AtomicBoolean(false);
                    Thread delayThread = new Thread( () ->
                    {
                        try
                        {
                            Thread.sleep(m_delayMillis);
                        }
                        catch( InterruptedException ex )
                        {
                            LOGGER.debug("Uninterruptable sleep interrupted!"
                                    + "  This shouldn't have except during shutdown.");
                        }
                        finally
                        {
                            synchronized( objInstance )
                            {
                                objInstance.notifyAll();
                            }

                            delayCompleted.set(true);
                        }
                    });
                    delayThread.setDaemon(true);
                    delayThread.start();

                    while( delayCompleted.get() == false )
                    {
                        try
                        {
                            synchronized( this )
                            {
                                wait();
                            }
                        }
                        catch( InterruptedException ex )
                        {
                            // Ignore interrupts
                            res.getResultSet().add(LCTailerResult.INTERRUPTED);
                            LOGGER.debug("Delay ignored interrupt.");
                        }
                    }

                    LOGGER.debug("End delay/sleep");
                }
            }
        }
        catch( final InterruptedException ex )
        {
            res.getResultSet().add(LCTailerResult.INTERRUPTED);
        }
        finally
        {
            if( res.getState() == null )
            {
                LOGGER.debug("call() : result without a Log State object.  Adding last state...");
                res = FileTailerResult.from(res.getResultSet(), m_lastLCState);
            }

            m_finalResult = res;

            if( m_completionLatch != null )
            {
                m_completionLatch.countDown();
            }

            if( reader != null )
            {
                IOUtils.closeQuietly(reader);
            }
        }

        if( LOGGER.isDebugEnabled() )
        {
            StringBuilder resStr = new StringBuilder(100);
            for( LCTailerResult tr : res.getResultSet() )
            {
                resStr.append(String.format("%s, ", tr));
            }

            LOGGER.debug(String.format("Tailer thread exit ( %s ) for %s", resStr, m_file));
        }

        return res;
    }

    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder(100);

        res.append("FileTailer\n{\n");
        res.append(String.format("    File            : '%s'\n", m_file));
        res.append(String.format("    Start Position  : '%s'\n", m_startPosition));
        res.append(String.format("    Run             : '%b'\n", m_run));
        res.append(String.format("    Re-open wait    : '%b'\n", m_reOpen));
        res.append(String.format("    Stats Collect   : '%b'\n", m_statsCollect));
        res.append(String.format("    Stats Validate  : '%b'\n", m_statsValidate));
        res.append(String.format("    Stats Reset     : '%b'\n", m_statsReset));
        res.append(String.format("    Stop on EOF     : '%b'\n", m_stopOnEOF));
        res.append(String.format("    Tail from End   : '%b'\n", m_end));
        res.append(String.format("    Buffer Size     : '%d'\n", m_bufferSize));
        res.append(String.format("    Delay           : '%d' ms\n", m_delayMillis));
        res.append(String.format("    Charset         : '%s'\n", m_cset));
        res.append(String.format("    Ignore Start Pos Error : '%b'\n", m_startPosIgnoreErr));
        res.append("}\n");

        return res.toString();
    }

    /**
     * Allows the tailer to complete its current loop and return.
     */
    public void stop()
    {
        this.m_run = false;
    }

    /**
     * Read new lines.
     *
     * @param reader The m_file to read
     * @throws java.io.IOException if an I/O error occurs.
     */
    private void readLines( final FileChannel reader )
            throws IOException, LogCheckException, InterruptedException
    {
        ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(m_bufferSize);
        ByteBuffer buffer = ByteBuffer.allocate(m_bufferSize);
        boolean seenCR = false;
        int bytesRead;

        LOGGER.debug(String.format("readLines() : reader position is at %d", reader.position()));

        // Debugging variables
        long readCount = 0;
        String previousLine = null;

        while( m_run && ((bytesRead  = reader.read(buffer))!= -1))
        {
            readCount++;

            buffer.flip();

            for(int i = 0; i<buffer.limit(); i++)
            {
                final byte currBuff = buffer.get();
                boolean doHandle = false;
                switch (currBuff)
                {
                    case '\n':
                        seenCR = false; // swallow CR before LF
                        doHandle = true;
                        break;

                    case '\r':
                        if (seenCR)
                        {
                            lineBuf.write('\r');
                        }
                        seenCR = true;
                        break;

                    default:
                        if (seenCR)
                        {
                            seenCR = false; // swallow final CR
                            doHandle = true;
                        }
                        lineBuf.write(currBuff);
                }

                if( doHandle )
                {
                    String ts = new String(lineBuf.toByteArray(), m_cset);

                    LOGGER.debug(String.format("readLines() : doHandle : '%s'", ts));

                    if( LOGGER.isDebugEnabled() )
                    {
                        if( (m_debugFlags != null) && m_debugFlags.contains(LCDebugFlag.LOG_SOURCE_LC_APP) )
                        {
                            // This is a debug mode.

                            // This matches the output logged by the LogCheckApp for testing

                            Matcher debugMatcher = Pattern.compile(".*?:\\s+\\[(\\d+)\\].*").matcher(ts);
                            if( debugMatcher.matches() )
                            {
                                int seq = Integer.parseInt(debugMatcher.group(1));
                                if( seq == (DEBUG_LCAPP_LOG_SEQUENCE + 1) )
                                {
                                    DEBUG_LCAPP_LOG_SEQUENCE++;
                                }
                                else if( DEBUG_LCAPP_LOG_SEQUENCE == 0 )
                                {
                                    DEBUG_LCAPP_LOG_SEQUENCE = seq;
                                }
                                else
                                {
                                    String msg = String.format("Expected %d in...'%s'", DEBUG_LCAPP_LOG_SEQUENCE + 1, ts);

                                    LOGGER.error(msg);
                                    throw new RuntimeException(msg);
                                }
                            }
                        }
                    }

                    for( ILogEntryBuilder ib : m_builders )
                    {
                        ib.handleLogLine(ts);
                    }

                    lineBuf.reset();

                    // Collect statistics
                    if( m_statsCollect && m_doCollectStats )
                    {
                        //m_statistics.setLastProcessedPosition(reader.position());

                        try
                        {
                            m_lastLCState = getState(reader,
                                    m_file,
                                    m_idBlockSize,
                                    m_hashType,
                                    m_setName,
                                    Instant.now(),
                                    false);

                            m_statistics.putPendingSaveState(m_lastLCState);
                        }
                        catch( InterruptedException ex )
                        {
                            // Let's not stop processing over a Save State interrupt
                            LOGGER.info("readLines() : putPendingSaveState() interrupted.");
                        }

                        m_doCollectStats = false;
                    }

                    previousLine = ts;
                }
            }

            buffer.clear();
        }

        if( m_run == false )
        {
            // We've been asked to stop running
            LOGGER.debug("Tailer process stopping by request.");
        }

        if( lineBuf.size() > 0 )
        {
            LOGGER.warn(String.format("leaving %d in the line buffer...\n'%s'",
                    lineBuf.size(), lineBuf));
        }

        IOUtils.closeQuietly(lineBuf); // not strictly necessary
    }


    public static LogFileBlock getFirstBlock(final Path file,
                                      final int idBlockSize,
                                      final LCHashType hashType) throws LogCheckException
    {
        LogFileBlock res = null;

        res = LogFileBlock.from("FIRST_BLOCK",
                file,
                0L,
                idBlockSize,
                hashType,
                LCFileBlockType.FIRSTBLOCK);

        return res;
    }

    public static LogFileBlock getLastBlock(  final Path file,
                                              final int idBlockSize,
                                              final LCHashType hashType,
                                              final FileChannel reader ) throws LogCheckException, IOException
    {
        long pos = reader.position() - idBlockSize;
        if( pos < 0 )
        {
            LOGGER.debug(String.format("Not enough data. Last Position = %d, ID Block Size = %d",
                    reader.position(), idBlockSize));

            return null;
        }

        LogFileBlock res = LogFileBlock.from("LAST_BLOCK",
                file,
                pos,
                idBlockSize,
                hashType,
                LCFileBlockType.LASTBLOCK);

        return res;
    }

    public static LogCheckState getStateReset( final Path file,
                                          final int idBlockSize,
                                          final LCHashType hashType,
                                          final String setName,
                                          final Instant lastProcessedTimeStart)
    {
        LogCheckState res = null;

        LogFileState currLogFile = null;

        currLogFile = LogFileState.from(file,
                lastProcessedTimeStart,
                Instant.now(),
                0L,
                null,
                null,
                null,
                null);

        res = LogCheckState.from(currLogFile,
                Instant.now(),
                UUID.randomUUID(),
                setName,
                null);

        return res;
    }

    public static LogCheckState getState( final FileChannel reader,
                                          final Path file,
                                          final int idBlockSize,
                                          final LCHashType hashType,
                                          final String setName,
                                          final Instant lastProcessedTimeStart,
                                   final Boolean ignoreMissingLogFile) throws LogCheckException, IOException
    {
        LogCheckState res = null;

        LOGGER.debug(String.format("getState() : Called on %s", file));

        LogFileState currLogFile = null;

        // Generate the Log File tailer statistics
        try
        {
            LogFileBlock firstBlock = null;
            LogFileBlock lastBlock = null;

            if( Files.notExists(file)
                    && BooleanUtils.isNotTrue(ignoreMissingLogFile))
            {
                throw new LogCheckException(String.format("Log File does not exist '%s",
                        file));
            }

            if( Files.exists(file) )
            {
                try
                {
                    firstBlock = getFirstBlock(file, idBlockSize, hashType);
                }
                catch( LogCheckException ex )
                {
                    LOGGER.debug("Error retrieving first block.", ex);
                }

                try
                {
                    lastBlock = getLastBlock(file, idBlockSize, hashType, reader);
                }
                catch( LogCheckException ex )
                {
                    LOGGER.debug("Error retrieving last block", ex);
                }
            }

            currLogFile = LogFileState.from(file,
                    lastProcessedTimeStart,
                    Instant.now(),
                    reader.position(),
                    null,
                    null,
                    lastBlock,
                    firstBlock);
        }
        catch( LogCheckException ex )
        {
            String errMsg = String.format("Error generating statistics for '%s'",
                    file);

            LOGGER.debug(errMsg, ex);
        }

        res = LogCheckState.from(currLogFile,
                Instant.now(),
                UUID.randomUUID(),
                setName,
                null);

        return res;
    }

    public static Set<LCTailerResult> validateStatistics(LogCheckState state) throws LogCheckException
    {
        Set<LCTailerResult> res = EnumSet.noneOf(LCTailerResult.class);

        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(String.format("Validating Statistics :\n%s", state));
        }

        // Validate start and stop blocks are correct?
       // LogCheckState state = getState(true);
        LogFileState currFState = state.getLogFile();
        if( currFState != null )
        {
            try
            {
                res.addAll( LogFileState.validateFileBlocks(currFState, null, true) );
            }
            catch( LogCheckException ex )
            {
                LOGGER.warn("Error validating file block", ex);

                res.add(LCTailerResult.VALIDATION_ERROR);
            }

            if( res.contains( LCTailerResult.SUCCESS ) == false)
            {
                LOGGER.debug("Log Check File Block VALIDATION_FAIL or VALIDATION_ERROR.");
            }
        }

        LOGGER.debug(String.format("Log Check File Block Validation result is %s", res));

        return res;
    }

}
