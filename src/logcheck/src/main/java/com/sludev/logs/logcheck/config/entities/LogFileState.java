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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.time.Instant;

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

    public Long getLastProcessedPosition()
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
}
