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
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Simple implementation of the unix "tail -f" functionality.
 */
public final class Tailer implements Callable<LCTailerResult>
{
    private static final Logger LOGGER
                    = LogManager.getLogger(Tailer.class);

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

    private final boolean m_validateTailerStatistics;

    /**
     * The listener to notify of events when tailing.
     */
    private final List<ILogEntryBuilder> m_builders;

    /**
     * Whether to close and reopen the m_file whilst waiting for more input.
     */
    private final boolean m_reOpen;

    private final boolean m_startPosIgnoreErr;

    private final TailerStatistics m_statistics;

    // Mutable

    /**
     * The tailer will run as long as this value is true.
     */
    private volatile boolean m_run = true;

    private String lineRemainder;

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
    private Tailer(final Path file,
                   final Long startPosition,
                   final Charset cset,
                   final List<ILogEntryBuilder> builders,
                   final long delayMillis,
                   final boolean end,
                   final boolean reOpen,
                   final boolean startPosIgnoreErr,
                   final boolean validateTailerStatistics,
                   final int bufSize,
                   final TailerStatistics stats)
    {
        this.m_file = file;
        this.m_delayMillis = delayMillis;
        this.m_end = end;
        this.m_startPosIgnoreErr = startPosIgnoreErr;
        this.m_bufferSize = bufSize;

        // Save and prepare the listener
        this.m_builders = builders;
        this.m_reOpen = reOpen;
        this.m_validateTailerStatistics = validateTailerStatistics;
        this.m_cset = cset;
        this.m_statistics = stats;

        this.m_startPosition = startPosition;

        this.lineRemainder = null;
    }

    public static Tailer from(final Path file,
                       final Long startPosition,
                       final Charset cset,
                       final List<ILogEntryBuilder> builders,
                       final long delayMillis,
                       final boolean end,
                       final boolean reOpen,
                       final boolean startPosIgnoreErr,
                       final boolean validateTailerStatistics,
                       final int bufSize,
                       final TailerStatistics stats)
    {
        Tailer res = new Tailer(file,
                startPosition,
                cset,
                builders,
                delayMillis,
                end,
                reOpen,
                startPosIgnoreErr,
                validateTailerStatistics,
                bufSize,
                stats);

        return res;
    }

    /**
     * Return the m_file.
     *
     * @return the m_file
     */
    public Path getFile()
    {
        return m_file;
    }

    protected boolean isRunning()
    {
        return m_run;
    }

    /**
     * Follows changes in the m_file, calling the ITailerListener's handle method
     * for each new line.
     */
    @Override
    public LCTailerResult call() throws LogCheckException, IOException
    {
        LOGGER.debug(String.format("Starting Tailer on '%s'", m_file));

        LCTailerResult res = LCTailerResult.NONE;
        FileChannel reader = null;
        long position = 0; // position within the m_file

        if( m_reOpen )
        {
            // Default result for re-open, is REOPEN
            res = LCTailerResult.REOPEN;
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
                        Thread.sleep(m_delayMillis);
                    }
                }
            }

            if( m_run && reader == null )
            {
                LOGGER.debug("Log file reader cannot be null");
                throw new LogCheckException("Log file reader cannot be null");
            }

            // Set the current position in the log m_file
            if( m_run && reader != null )
            {
                if( m_startPosition != null && m_startPosition >= 0 )
                {
                    if( m_end )
                    {
                        LOGGER.debug(
                                String.format("Both '--tail-from-end' and a "
                                        + "starting byte position of %d where provided. Start Position has precedence",
                                        m_startPosition));
                    }

                    if( m_startPosition > reader.size()
                            && m_startPosIgnoreErr == false )
                    {
                        throw new LogCheckException(
                                String.format("File start position ( %d ) can not be further than the file's"
                                + " last position ( %d ).  Was the file truncated since last run?",
                                        m_startPosition, reader.size()));
                    }

                    // Start where instructed
                    position = (m_startPosition >reader.size())?reader.size(): m_startPosition;
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
            while( m_run && reader != null )
            {
                if( m_validateTailerStatistics )
                {
                    // Validate start and stop blocks are correct?
                    LogCheckState currState = m_statistics.getState(true);
                    LogFileState currFState = currState.getLogFile();
                    if( currFState != null )
                    {
                        if( LogFileState.isValidFileBlocks(currFState, true) == false )
                        {
                            res = LCTailerResult.VALIDATION_FAIL;

                            LOGGER.debug("Log Check File Block Validation failed.");

                            throw new LogCheckException("Log Check File Block Validation failed.");
                        }
                    }
                }

                // Read from the file on disk
                readLines(reader);

                if( m_reOpen )
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
                    Thread.sleep(m_delayMillis);
                }
            }
        }
        catch( final InterruptedException ex )
        {
            Thread.currentThread().interrupt();
            res = LCTailerResult.INTERRUPTED;
        }
        finally
        {
            if( reader != null )
            {
                IOUtils.closeQuietly(reader);
            }
        }

        return res;
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
                final byte ch = buffer.get();
                boolean doHandle = false;
                switch (ch)
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
                        lineBuf.write(ch);
                }

                if( doHandle )
                {
                    String ts = new String(lineBuf.toByteArray(), m_cset);

//                    if( ts.matches(".*?2015-12.*") == false )
//                    {
//                        log.debug(String.format("%s", ts));
//                    }

                    for( ILogEntryBuilder ib : m_builders )
                    {
                        ib.handleLogLine(ts);
                    }

                    lineBuf.reset();

                    // Collect statistics
                    m_statistics.setLastProcessedPosition(reader.position());

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

}
