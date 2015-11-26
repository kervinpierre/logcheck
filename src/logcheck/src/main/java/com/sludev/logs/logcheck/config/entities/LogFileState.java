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

import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * The state of a single log file being processed.
 *
 * Created by kervin on 10/27/2015.
 */
public final class LogFileState
{
    private static final Logger log
            = LogManager.getLogger(LogFileState.class);

    private final Path file;
    private final Instant lastProcessedTimeStart;
    private final Instant lastProcessedTimeEnd;
    private final Long lastProcessedPosition;
    private final Long lastProcessedLineNumber;
    private final Long lastProcessedCharNumber;
    private final LogFileBlock lastProcessedBlock;
    private final LogFileBlock firstBlock;

    public Path getFile()
    {
        return file;
    }

    public Instant getLastProcessedTimeStart()
    {
        return lastProcessedTimeStart;
    }

    public Instant getLastProcessedTimeEnd()
    {
        return lastProcessedTimeEnd;
    }

    public long getLastProcessedPosition()
    {
        return lastProcessedPosition;
    }

    public Long getLastProcessedLineNumber()
    {
        return lastProcessedLineNumber;
    }

    public Long getLastProcessedCharNumber()
    {
        return lastProcessedCharNumber;
    }

    public LogFileBlock getLastProcessedBlock()
    {
        return lastProcessedBlock;
    }

    public LogFileBlock getFirstBlock()
    {
        return firstBlock;
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
        this.file = file;
        this.lastProcessedTimeStart = lastProcessedTimeStart;
        this.lastProcessedTimeEnd = lastProcessedTimeEnd;
        this.lastProcessedPosition = lastProcessedPosition;
        this.lastProcessedLineNumber = lastProcessedLineNumber;
        this.lastProcessedCharNumber = lastProcessedCharNumber;
        this.lastProcessedBlock = lastProcessedBlock;
        this.firstBlock = firstBlock;
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
                log.debug(errMsg, ex);

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
                log.debug(errMsg, ex);

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
                log.debug(errMsg, ex);

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
                log.debug(errMsg, ex);

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
                log.debug(errMsg, ex);

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
}
