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
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simple implementation of the unix "tail -f" functionality.
 */
public final class Tailer implements Callable<LCTailerResult>
{
    private static final Logger log
                    = LogManager.getLogger(Tailer.class);

    public static final int DEFAULT_DELAY_MILLIS = 1000;

    public static final int DEFAULT_BUFSIZE = 4096;

    // The default charset used for reading files
    public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    /**
     * The file which will be tailed.
     */
    private final Path file;

    /**
     * The character set that will be used to read the file.
     */
    private final Charset cset;

    /**
     * The amount of time to wait for the file to be updated.
     */
    private final long delayMillis;

    private final Long startPosition;

    /**
     * Whether to tail from the end or start of file
     */
    private final boolean end;

    /**
     * The listener to notify of events when tailing.
     */
    private final List<ILogEntryBuilder> builders;

    /**
     * Whether to close and reopen the file whilst waiting for more input.
     */
    private final boolean reOpen;

    private final TailerStatistics statistics;

    private final Long stopAfter;

    // Mutable

    /**
     * The tailer will run as long as this value is true.
     */
    private volatile boolean run = true;

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file the file to follow.
     * @param cset the Charset to be used for reading the file
     * @param delayMillis the delay between checks of the file for new content
     * in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail
     * from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     * @param bufSize Buffer size
     */
    private Tailer(final Path file,
                   final Long startPosition,
                   final Long stopAfter,
                   final Charset cset,
                   final List<ILogEntryBuilder> builders,
                   final long delayMillis,
                   final boolean end,
                   final boolean reOpen,
                   final int bufSize,
                   final TailerStatistics stats)
    {
        this.file = file;
        this.delayMillis = delayMillis;
        this.end = end;

        // Save and prepare the listener
        this.builders = builders;
        this.reOpen = reOpen;
        this.cset = cset;
        this.statistics = stats;

        this.startPosition = startPosition;
        this.stopAfter = stopAfter;
    }

    public static Tailer from(final Path file,
                       final Long startPosition,
                       final Long stopAfter,
                       final Charset cset,
                       final List<ILogEntryBuilder> builders,
                       final long delayMillis,
                       final boolean end,
                       final boolean reOpen,
                       final int bufSize,
                       final TailerStatistics stats,
                       final String setName,
                       final Path stateFile)
    {
        Tailer res = new Tailer(file,
                startPosition,
                stopAfter,
                cset,
                builders,
                delayMillis,
                end,
                reOpen,
                bufSize,
                stats);

        return res;
    }

    /**
     * Return the file.
     *
     * @return the file
     */
    public Path getFile()
    {
        return file;
    }

    /**
     * Gets whether to keep on running.
     *
     * @return whether to keep on running.
     * @since 2.5
     */
    protected boolean getRun()
    {
        return run;
    }

    /**
     * Follows changes in the file, calling the ITailerListener's handle method
     * for each new line.
     */
    @Override
    public LCTailerResult call() throws Exception
    {
        log.debug(String.format("Starting Tailer on '%s'", file));

        LCTailerResult res = LCTailerResult.NONE;
        FileChannel reader = null;

        try
        {
            long position = 0; // position within the file

            // Open the file
            if( run )
            {
                reader = FileChannel.open(file, StandardOpenOption.READ);

                // The current position in the file
                if( startPosition != null && startPosition >= 0 )
                {
                    if( end )
                    {
                        log.debug(
                                String.format("Both '--tail-from-end' and a "
                                        + "starting byte position of %d where provided. Start Position has precedence",
                                        startPosition));
                    }

                    if( startPosition > reader.size() )
                    {
                        throw new LogCheckException(
                                String.format("File start position ( %d ) can not be further than the file's"
                                + "last position ( %d ).  Was the file truncated since last run?", startPosition, reader.size()));
                    }

                    // Start where instructed
                    position = startPosition;
                }
                else
                {
                    if( end )
                    {
                        // Tail from the end of the file
                        position = reader.size();
                    }
                }

                reader.position(position);
            }

            while( run )
            {
                // Check the file length to see if it was rotated
                /*final long length = Files.size(getFile());

                if( length < position )
                {
                    // File was rotated
                    listener.fileRotated();
                    // Reopen the reader after rotation

                    // Ensure that the old file is closed iff we re-open it successfully
                    final FileChannel save = reader;
                    reader = FileChannel.open(getFile(), StandardOpenOption.READ);

                    // At this point, we're sure that the old file is rotated
                    // Finish scanning the old file and then we'll start with the new one
                    try
                    {
                        readLines(save);
                    }
                    catch( IOException ioe )
                    {
                        listener.handle(ioe);
                    }
                    position = 0;
                    // close old file explicitly rather than relying on GC picking up previous RAF
                    IOUtils.closeQuietly(save);

                    continue;
                }
                else*/
                {
                    // The file has more content than it did last time
                    position = readLines(reader);
                }

                if( reOpen )
                {
                    // reOpen means read to the end of file then quit.
                    // The monitoring thread should relaunch after a period
                    // of time.
                    stop();

                    res = LCTailerResult.REOPEN;
                    // TODO : Ensure the LogCheckState is saved here
                }

                // Delay if requested
                Thread.sleep(delayMillis);
            }
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
            res = LCTailerResult.INTERRUPTED;
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }

        return res;
    }

    /**
     * Allows the tailer to complete its current loop and return.
     */
    public void stop()
    {
        this.run = false;
    }

    /**
     * Read new lines.
     *
     * @param reader The file to read
     * @return The new position after the lines have been read
     * @throws java.io.IOException if an I/O error occurs.
     */
    private long readLines(final FileChannel reader) throws IOException, LogCheckException, InterruptedException
    {
        ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(64);
        long pos = reader.position();
        long rePos = pos; // position to re-read
        boolean seenCR = false;
        ByteBuffer buffer = ByteBuffer.allocate(64);

        int bytesRead;
        while( run && ((bytesRead  = reader.read(buffer))!= -1))
        {
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
                    String ts = new String(lineBuf.toByteArray(), cset);
                    for( ILogEntryBuilder ib : builders)
                    {
                        ib.handleLogLine(ts);
                    }

                    lineBuf.reset();
                    rePos = pos + i + 1;

                    // Collect statistics
                    statistics.setLastProcessedPosition(pos);
                }
            }

            pos = reader.position();
            buffer.clear();
        }

        IOUtils.closeQuietly(lineBuf); // not strictly necessary
        reader.position(rePos); // Ensure we can re-read if necessary
        return rePos;
    }

}
