/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sludev.logs.logcheck.tail;

import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.enums.LCFileBlockType;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    // Mutable

    /**
     * The tailer will run as long as this value is true.
     */
    private volatile boolean m_run = true;

    private volatile boolean m_doCollectStats = false;

    private String lineRemainder;

    private LogCheckState m_lastLCState;

    // DEBUG PURPOSES ONLY
    private static int debugSeq = 0;

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
                       final boolean stopOnEOF,
                       final int bufSize,
                       final int saveTimerSeconds,
                       final TailerStatistics stats,
                       final LCHashType hashType,
                       final Integer idBlockSize,
                       final String setName)
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
        this.m_cset = cset;
        this.m_statistics = stats;

        this.m_hashType = hashType;
        this.m_idBlockSize = idBlockSize;
        this.m_setName = setName;

        this.m_stopOnEOF = stopOnEOF;

        this.m_startPosition = startPosition;

        this.lineRemainder = null;
        this.m_lastLCState = null;

        this.m_saveTimerSeconds = saveTimerSeconds;
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
                                  final boolean stopOnEOF,
                                  final int bufSize,
                                  final int saveTimerSeconds,
                                  final TailerStatistics stats,
                                  final LCHashType hashType,
                                  final Integer idBlockSize,
                                  final String setName)
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
                stopOnEOF,
                bufSize,
                saveTimerSeconds,
                stats,
                hashType,
                idBlockSize,
                setName);

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
     * Follows changes in the file, calling the ITailerListener's handle method
     * for each new line.
     */
    @Override
    public FileTailerResult call() throws LogCheckException, IOException
    {
        if( LOGGER.isInfoEnabled() )
        {
            LOGGER.info(String.format("Starting tailer.\n%s", toString()));
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
            // Open the log m_file for reading
            if( m_run )
            {
                if( m_file == null )
                {
                    throw new LogCheckException("Log File cannot be null");
                }

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
                if( (m_startPosition != null) && (m_startPosition >= 0) )
                {
                    if( m_end )
                    {
                        LOGGER.debug(
                                String.format("Both '--tail-from-end' and a "
                                        + "starting byte position of %d where provided. Start Position has precedence",
                                        m_startPosition));
                    }

                    if( (m_startPosition > reader.size())
                            && (m_startPosIgnoreErr == false) )
                    {
                        String errMsg = String.format("File start position ( %d ) can not be further than the file's"
                                        + " last position ( %d ).  Was the file truncated since last run?",
                                m_startPosition, reader.size());

                        LOGGER.debug(errMsg);

                        if( m_statsValidate )
                        {
                            stop();
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
                        position = (m_startPosition > reader.size()) ? reader.size() : m_startPosition;
                    }
                }
                else
                {
                    if( m_end )
                    {
                        // Tail from the end of the m_file
                        position = reader.size();
                    }
                }

                reader.position(position);
            }

            // Now loop the log m_file
            while( m_run && (reader != null) )
            {
                if( m_statsValidate )
                {
                    LogCheckState lastState = m_statistics.getRestoredStates().peekFirst();
                    if( m_lastLCState != null )
                    {
                        // Use the last state if we have it.
                        lastState = m_lastLCState;
                    }

                    if( lastState != null )
                    {
                        LCTailerResult valRes = validateStatistics(lastState);
                        if( valRes != LCTailerResult.SUCCESS )
                        {
                            // Statistics validation from disk failed.
                            res = FileTailerResult.from(res.getResultSet(), lastState);
                            res.getResultSet().add(valRes);
                            stop();
                            break;
                        }
                    }
                }

                // FIXME : ID the FIRST_BLOCK on the file here, before reading.
                //         If this ID fails we don't bother with the log builders

                // ID the file then Read from the file on disk
                readLines(reader);

                // TODO : Confirm FIRST_BLOCK here, and ID LAST_BLOCK from read data
                //        Detects 'Log swap while tailing'

                // FIXME : Statistics thread should NOT ID file blocks.
                //         This causes lots of races

                // Save the last state if it hasn't been for specific
                // result codes.  Don't save the state if there was a
                // validation error.
                if( res.getResultSet().contains(LCTailerResult.VALIDATION_FAIL) )
                {
                    LOGGER.debug("Skipping state save because result includes VALIDATION_FAIL.");
                }
                else
                {
                    if( m_statsCollect )
                    {
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
                    LOGGER.debug(String.format("Sleeping/delay for %dms\n", m_delayMillis));
                    Thread.sleep(m_delayMillis);
                    LOGGER.debug("End delay/sleep");
                }
            }
        }
        catch( final InterruptedException ex )
        {
            Thread.currentThread().interrupt();
            res.getResultSet().add(LCTailerResult.INTERRUPTED);
        }
        finally
        {
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

                    if( LOGGER.isDebugEnabled() )
                    {
                        ////
                        // This matches the output logged by the LogCheckApp for testing
                        ////
                        Matcher m = Pattern.compile(".*?:\\s+\\[(\\d+)\\].*").matcher(ts);
                        if( m.matches() )
                        {
                            int seq = Integer.parseInt(m.group(1));
                            if( seq == debugSeq + 1 )
                            {
                                debugSeq++;
                            }
                            else if( debugSeq == 0 )
                            {
                                debugSeq = seq;
                            }
                            else
                            {
                                LOGGER.error(String.format("Expected %d in...'%s'", debugSeq + 1, ts));
                            }
                        }
                    }

                    LOGGER.debug(String.format("readLine() doHandle : '%s'", ts));

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

    public static LogCheckState getState( final FileChannel reader,
                                          final Path file,
                                          final int idBlockSize,
                                          final LCHashType hashType,
                                          final String setName,
                                          final Instant lastProcessedTimeStart,
                                   final Boolean ignoreMissingLogFile) throws LogCheckException, IOException
    {
        LogCheckState res = null;

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

    public static LCTailerResult validateStatistics(LogCheckState state) throws LogCheckException
    {
        LCTailerResult res = LCTailerResult.SUCCESS;

        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(String.format("Validating Statistics :\n%s", state));
        }

        // Validate start and stop blocks are correct?
       // LogCheckState state = getState(true);
        LogFileState currFState = state.getLogFile();
        if( currFState != null )
        {
            boolean valRes = false;

            try
            {
                valRes = LogFileState.isValidFileBlocks(currFState, null, true);
            }
            catch( LogCheckException ex )
            {
                LOGGER.debug("Error validating file block", ex);
            }

            if( valRes == false )
            {
                res = LCTailerResult.VALIDATION_FAIL;

                LOGGER.debug("Log Check File Block Validation failed.");
            }
        }

        LOGGER.debug(String.format("Log Check File Block Validation result is %s", res));

        return res;
    }

}
