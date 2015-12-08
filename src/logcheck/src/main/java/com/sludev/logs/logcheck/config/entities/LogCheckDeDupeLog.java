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
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public static Path nextFileName(final Path dirPath,
                                    final Integer maxLogFiles,
                                    final boolean rotateOldFiles) throws LogCheckException
    {
        Path newPath = null;

        if(rotateOldFiles)
        {
            List<Path> files = null;

            try
            {
                files = new ArrayList<>();
                files.addAll(Arrays.asList(Files.list(dirPath).toArray(Path[]::new)));
                Collections.sort(files);
            }
            catch( IOException ex )
            {
                String errMsg = String.format("Error listing '%s'", dirPath);
                log.debug(errMsg, ex);
            }

            int extraLogFiles = files.size()-maxLogFiles;

            // Delete files over max
            for( int i = 0; i<=extraLogFiles; i++)
            {
                Path currPath = files.get(i);
                try
                {
                    Files.delete(currPath);

                    log.debug(String.format("deleted '%s'", currPath));
                }
                catch( IOException ex )
                {
                    String errMsg = String.format("Error deleting '%s'", currPath);
                    log.debug(errMsg, ex);
                }
            }

            // Rename the remaining files
            if( extraLogFiles >= 0 )
            {
                for( int i = extraLogFiles+1, j = 0; i < files.size(); i++, j++ )
                {
                    String newName = String.format("%s.%06d.log.xml",
                            LogCheckConstants.DEFAULT_DEDUPE_LOG_FILE_NAME, j);

                    newPath = dirPath.resolve(newName);
                    Path currPath = files.get(i);

                    try
                    {
                        Files.move(currPath, newPath, StandardCopyOption.REPLACE_EXISTING);

                        log.debug(String.format("moved '%s' to '%s'.", currPath, newPath));
                    }
                    catch( IOException ex )
                    {
                        String errMsg = String.format("Error moving '%s' to '%s'",
                                                        currPath, newPath);
                        log.debug(errMsg, ex);
                    }
                }
            }
        }

        for( int i = 0; i < LogCheckConstants.MAX_DEDUPE_LOG_FILES; i++)
        {
            String newName = String.format("%s.%06d.log.xml",
                    LogCheckConstants.DEFAULT_DEDUPE_LOG_FILE_NAME, i);

            newPath = dirPath.resolve(newName);
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
