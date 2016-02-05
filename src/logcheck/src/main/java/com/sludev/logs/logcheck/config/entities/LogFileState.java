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

import com.sludev.logs.logcheck.enums.LCFileBlockType;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        if( (stateStartPos != null) && (stateStartPos >= 0) )
        {
            res = stateStartPos;
        }
        else
        {
            if( (stateStartLine != null) && (stateStartLine >= 0)
                    && (stateStartChar != null) && (stateStartChar >= 0) )
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
                                if( (lineCount == stateStartLine)
                                        && (charCount < stateStartChar) )
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

                            if( (lineCount == stateStartLine) && (charCount == stateStartChar) )
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

    public static Set<LCTailerResult> validateFileBlocks(final LogFileState state,
                                             final Path alternateFilePath,
                                             final boolean ignoreMissingBlocks,
                                             final LCFileBlockType... types) throws LogCheckException
    {
        Set<LCTailerResult> res = EnumSet.noneOf(LCTailerResult.class);

        Path currPath = alternateFilePath;

        if( state == null )
        {
            throw new LogCheckException("Log File State cannot be null.");
        }

        LogFileBlock firstBlock = state.getFirstBlock();
        LogFileBlock lastBlock = state.getLastProcessedBlock();

        if( (currPath == null) && (state.getFile() != null) )
        {
            currPath = state.getFile();
        }

        if( (currPath == null) || Files.notExists(currPath) )
        {
            String errMsg = String.format("validateFileBlocks() : Invalid path '%s'", currPath);

            LOGGER.debug(errMsg);
            throw new LogCheckException(errMsg);
        }

        List<LCFileBlockType> typesList = new ArrayList<>(5);
        if( (types != null) && (types.length > 0) )
        {
            typesList = Arrays.asList(types);
        }

        boolean ignoredFirst = false;
        boolean ignoredLast = false;

        if( typesList.isEmpty()
                || typesList.contains(LCFileBlockType.ALL)
                || typesList.contains(LCFileBlockType.FIRSTBLOCK))
        {
            if( (firstBlock == null)
                    || (ignoreMissingBlocks && LogFileBlock.isEmptyFileBlock(firstBlock)) )
            {
                LOGGER.debug("Ignoring missing first block.");
                if( ignoreMissingBlocks == false )
                {
                    res.add( LCTailerResult.VALIDATION_FAIL );
                }

                ignoredFirst = true;
            }
            else
            {
                Set<LCTailerResult> firstRes
                        =  LogFileBlock.validateFileBlock(currPath, firstBlock);

                if( firstRes != null )
                {
                    res.addAll(firstRes);
                }
            }
        }
        else
        {
            // Ignore since we weren't ask to check this block
            if( res.isEmpty() )
            {
                res.add(LCTailerResult.SUCCESS);
            }

            ignoredFirst = false;
        }

        if( typesList.isEmpty()
                || typesList.contains(LCFileBlockType.ALL)
                || typesList.contains(LCFileBlockType.LASTBLOCK))
        {
            if( (lastBlock == null)
                    || (ignoreMissingBlocks && LogFileBlock.isEmptyFileBlock(lastBlock)) )
            {
                LOGGER.debug("Ignoring missing last block.");
                if( ignoreMissingBlocks == false )
                {
                    res.add( LCTailerResult.VALIDATION_FAIL );
                }

                ignoredLast = true;
            }
            else
            {
                Set<LCTailerResult> lastRes
                        =  LogFileBlock.validateFileBlock(currPath, lastBlock);

                if( lastRes != null )
                {
                    res.addAll(lastRes);
                }
            }
        }
        else
        {
            // Ignore since we weren't ask to check this block
            if( res.isEmpty() )
            {
                res.add(LCTailerResult.SUCCESS);
            }

            ignoredLast = true;
        }

        if( LOGGER.isDebugEnabled()
                && ignoredFirst && ignoredLast )
        {
            LOGGER.debug("validateFileBlocks() : Both the first "
                    + "and last blocks were ignored.");

            // Risky.  But if both blocks are ignored, we have a success
            res.add( LCTailerResult.SUCCESS );
        }

        return res;
    }

    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder(100);

        res.append("LogFileState : \n");
        res.append(String.format("    File                    : %s\n", m_file));
        res.append(String.format("    Last Processed Position : %d\n", m_lastProcessedPosition));
        res.append(String.format("    Last Processed Line No. : %d\n", m_lastProcessedLineNumber));
        res.append(String.format("    Last Processed Char No. : %d\n", m_lastProcessedCharNumber));
        res.append(String.format("    Last Processed Start Time : %s\n", m_lastProcessedTimeStart));
        res.append(String.format("    Last Processed End Time : %s\n", m_lastProcessedTimeEnd));
        res.append(String.format("    First Block : %s\n", m_firstBlock));
        res.append(String.format("    Last Block : %s\n", m_lastProcessedBlock));

        return res.toString();
    }
}
