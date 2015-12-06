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
package com.sludev.logs.logcheck.config.entities;

import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * The state of a single log file being processed.
 *
 * Created by kervin on 10/27/2015.
 */
public final class LogFileState
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogFileState.class);

    private final Path m_file;
    private final Instant m_lastProcessedTimeStart;
    private final Instant m_lastProcessedTimeEnd;
    private final Long m_lastProcessedPosition;
    private final Long m_lastProcessedLineNumber;
    private final Long m_lastProcessedCharNumber;
    private final LogFileBlock m_lastProcessedBlock;
    private final LogFileBlock m_firstBlock;

    public Path getFile()
    {
        return m_file;
    }

    public Instant getLastProcessedTimeStart()
    {
        return m_lastProcessedTimeStart;
    }

    public Instant getLastProcessedTimeEnd()
    {
        return m_lastProcessedTimeEnd;
    }

    public long getLastProcessedPosition()
    {
        return m_lastProcessedPosition;
    }

    public Long getLastProcessedLineNumber()
    {
        return m_lastProcessedLineNumber;
    }

    public Long getLastProcessedCharNumber()
    {
        return m_lastProcessedCharNumber;
    }

    public LogFileBlock getLastProcessedBlock()
    {
        return m_lastProcessedBlock;
    }

    public LogFileBlock getFirstBlock()
    {
        return m_firstBlock;
    }

    private LogFileState( final Path file,
                          final Instant lastProcessedTimeStart,
                          final Instant lastProcessedTimeEnd,
                          final Long lastProcessedPosition,
                          final Long lastProcessedLineNumber,
                          final Long lastProcessedCharNumber,
                          final LogFileBlock lastProcessedBlock,
                          final LogFileBlock firstBlock)
    {
        this.m_file = file;
        this.m_lastProcessedTimeStart = lastProcessedTimeStart;
        this.m_lastProcessedTimeEnd = lastProcessedTimeEnd;
        this.m_lastProcessedPosition = lastProcessedPosition;
        this.m_lastProcessedLineNumber = lastProcessedLineNumber;
        this.m_lastProcessedCharNumber = lastProcessedCharNumber;
        this.m_lastProcessedBlock = lastProcessedBlock;
        this.m_firstBlock = firstBlock;
    }

    public static LogFileState from( final Path file,
                         final Instant lastProcessedTimeStart,
                         final Instant lastProcessedTimeEnd,
                         final Long lastProcessedPosition,
                         final Long lastProcessedLineNumber,
                         final Long lastProcessedCharNumber,
                         final LogFileBlock lastProcessedBlock,
                         final LogFileBlock firstBlock)
    {
        LogFileState res = new LogFileState(file,
                lastProcessedTimeStart,
                lastProcessedTimeEnd,
                lastProcessedPosition,
                lastProcessedLineNumber,
                lastProcessedCharNumber,
                lastProcessedBlock,
                firstBlock);

        return res;
    }


    public static LogFileState from( final String fileStr,
                                     final String lastProcessedTimeStartStr,
                                     final String lastProcessedTimeEndStr,
                                     final String lastProcessedPositionStr,
                                     final String lastProcessedLineNumberStr,
                                     final String lastProcessedCharNumberStr,
                                     final LogFileBlock lastProcessedBlock,
                                     final LogFileBlock firstBlock) throws LogCheckException
    {
        Path file = null;
        Instant lastProcessedTimeStart = null;
        Instant lastProcessedTimeEnd = null;
        Long lastProcessedPosition = null;
        Long lastProcessedLineNumber = null;
        Long lastProcessedCharNumber = null;

        if( StringUtils.isNoneBlank(fileStr) )
        {
            file = Paths.get(fileStr);
        }

        if( StringUtils.isNoneBlank(lastProcessedPositionStr) )
        {
            try
            {
                lastProcessedPosition = Long.parseLong(lastProcessedPositionStr);
            }
            catch( IllegalArgumentException ex )
            {
                String errMsg = String.format("Invalid integer for Last Processed Position '%s'",
                        lastProcessedPositionStr);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(lastProcessedLineNumberStr) )
        {
            try
            {
                lastProcessedLineNumber = Long.parseLong(lastProcessedLineNumberStr);
            }
            catch( IllegalArgumentException ex )
            {
                String errMsg = String.format("Invalid integer for Last Processed Line Number '%s'",
                        lastProcessedLineNumberStr);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(lastProcessedCharNumberStr) )
        {
            try
            {
                lastProcessedCharNumber = Long.parseLong(lastProcessedCharNumberStr);
            }
            catch( IllegalArgumentException ex )
            {
                String errMsg = String.format("Invalid integer for Last Processed Char Number '%s'",
                        lastProcessedCharNumberStr);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(lastProcessedTimeStartStr) )
        {
            try
            {
                lastProcessedTimeStart = Instant.parse(lastProcessedTimeStartStr);
            }
            catch( DateTimeParseException ex )
            {
                String errMsg = String.format("Invalid integer for Last Processed Time Start '%s'",
                        lastProcessedTimeStartStr);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(lastProcessedTimeEndStr) )
        {
            try
            {
                lastProcessedTimeEnd = Instant.parse(lastProcessedTimeEndStr);
            }
            catch( DateTimeParseException ex )
            {
                String errMsg = String.format("Invalid integer for Last Processed Time End '%s'",
                        lastProcessedTimeEndStr);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        LogFileState res = new LogFileState(file,
                lastProcessedTimeStart,
                lastProcessedTimeEnd,
                lastProcessedPosition,
                lastProcessedLineNumber,
                lastProcessedCharNumber,
                lastProcessedBlock,
                firstBlock);

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
                    LOGGER.debug("Error calculating line position.", ex);
                }
            }
        }

        return res;
    }

    public static boolean isValidFileBlocks( final LogFileState state,
                                             final boolean ignoreMissingBlocks ) throws LogCheckException
    {
        if( state == null )
        {
            throw new LogCheckException("Log File State cannot be null.");
        }

        LogFileBlock firstBlock = state.getFirstBlock();
        LogFileBlock lastBlock = state.getLastProcessedBlock();

        boolean firstValid = false;
        boolean lastValid = false;

        if( firstBlock == null )
        {
            firstValid = ignoreMissingBlocks;
        }
        else
        {
            firstValid = LogFileBlock.isValidFileBlock(state.getFile(), firstBlock);
        }

        if( lastBlock == null )
        {
            lastValid = ignoreMissingBlocks;
        }
        else
        {
            lastValid = LogFileBlock.isValidFileBlock(state.getFile(), lastBlock);
        }

        return firstValid && lastValid;
    }
}
