/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.config.entities;

import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a log or list of all the Log Entry objects already sent
 * to the Log Entry Sink.
 *
 * Created by kervin on 2015-11-13.
 */
public final class LogCheckDeDupeLog
{
    private static final Logger log = LogManager.getLogger(LogCheckDeDupeLog.class);

    private final UUID id;
    private final Instant startTime;
    private final Instant endTime;
    private final List<LogEntryDeDupe> logEntryDeDupes;

    public UUID getId()
    {
        return id;
    }

    public Instant getStartTime()
    {
        return startTime;
    }

    public Instant getEndTime()
    {
        return endTime;
    }

    public List<LogEntryDeDupe> getLogEntryDeDupes()
    {
        return logEntryDeDupes;
    }

    private LogCheckDeDupeLog(final UUID id,
                              final Instant startTime,
                              final Instant endTime,
                              final List<LogEntryDeDupe> logEntryDeDupes)
    {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;

        if( logEntryDeDupes == null )
        {
            this.logEntryDeDupes = new ArrayList<>();
        }
        else
        {
            this.logEntryDeDupes = logEntryDeDupes;
        }
    }

    public static LogCheckDeDupeLog from(final UUID id,
                                         final Instant startTime,
                                         final Instant endTime,
                                         final List<LogEntryDeDupe> logEntryDeDupes)
    {
        LogCheckDeDupeLog res = new LogCheckDeDupeLog(id,
                startTime,
                endTime,
                logEntryDeDupes);

        return res;
    }

    public static LogCheckDeDupeLog from(final UUID id,
                                         final Instant startTime,
                                         final Instant endTime)
    {
        LogCheckDeDupeLog res = LogCheckDeDupeLog.from(id,
                startTime,
                endTime,
                null);

        return res;
    }

    public static LogCheckDeDupeLog from(final String idStr,
                                         final String startTimeStr,
                                         final String endTimeStr,
                                         final List<LogEntryDeDupe> logEntryDeDupes)
    {
        UUID id = null;
        Instant startTime = null;
        Instant endTime = null;

        id = UUID.fromString(idStr);
        startTime = Instant.parse(startTimeStr);
        endTime = Instant.parse(endTimeStr);

        LogCheckDeDupeLog res = LogCheckDeDupeLog.from(id,
                startTime,
                endTime,
                logEntryDeDupes);

        return res;
    }

    public static Path nextFileName(Path deDupeDirPath) throws LogCheckException
    {
        Path newPath = null;

        for( int i = 0; i < LogCheckConstants.MAX_DEDUPE_LOG_FILES; i++)
        {
//            if( i >= maxBackups-1 )
//            {
//                ;
//            }
//
            String newName = String.format("%s.%06d.log.xml",
                    LogCheckConstants.DEFAULT_DEDUPE_LOG_FILE_NAME, i);

            newPath = deDupeDirPath.resolve(newName);
            if( Files.notExists(newPath))
            {
                break;
            }
        }

        if(newPath == null || Files.exists(newPath))
        {
            String errMsg = String.format("Invalid path '%s'\nNull or greater than %d?", newPath,
                    LogCheckConstants.MAX_DEDUPE_LOG_FILES);

            log.debug(errMsg);
            throw new LogCheckException(errMsg);
        }

        return newPath;
    }
}
