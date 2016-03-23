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

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * The state of the Log Check job at a specific point in time.
 *
 * Created by kervin on 10/27/2015.
 */
public class LogCheckState
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckState.class);

    private final LogFileState m_logFile;
    private final Instant m_saveDate;
    private final UUID id;
    private final String m_setName;
    private final List<LogCheckError> m_errors;
    private final Deque<LogFileStatus> m_completedLogFiles;

    // MUTABLE
    private volatile boolean m_pendingSave = false;

    public boolean isPendingSave()
    {
        return m_pendingSave;
    }

    public void setPendingSave(boolean save)
    {
        m_pendingSave = save;
    }

    public Deque<LogFileStatus> getCompletedLogFiles()
    {
        return m_completedLogFiles;
    }

    public LogFileState getLogFile()
    {
        return m_logFile;
    }

    public Instant getSaveDate()
    {
        return m_saveDate;
    }

    public UUID getId()
    {
        return id;
    }

    public String getSetName()
    {
        return m_setName;
    }

    public List<LogCheckError> getErrors()
    {
        return m_errors;
    }

    private LogCheckState( final LogFileState logFile,
                           final Instant saveDate,
                           final UUID id,
                           final String setName,
                           final List<LogCheckError> errors,
                           final Deque<LogFileStatus> completedLogFiles)
    {
        this.m_logFile = logFile;
        this.m_saveDate = saveDate;
        this.id = id;
        this.m_setName = setName;

        if( errors == null )
        {
            this.m_errors = new ArrayList<>(10);
        }
        else
        {
            this.m_errors = errors;
        }

        if( completedLogFiles == null )
        {
            this.m_completedLogFiles = new ArrayDeque<>(10);
        }
        else
        {
            this.m_completedLogFiles = completedLogFiles;
        }
    }

    public static LogCheckState from( final LogFileState logFile,
                                         final Instant saveDate,
                                         final UUID id,
                                         final String setName,
                                         final List<LogCheckError> errors,
                                         final Deque<LogFileStatus> completedLogFiles )
    {
        LogCheckState res = new LogCheckState(logFile,
                saveDate,
                id,
                setName,
                errors,
                completedLogFiles);

        return res;
    }

    public static LogCheckState from( final LogFileState logFile,
                                      final String saveDateStr,
                                      final String idStr,
                                      final String setName,
                                      final List<LogCheckError> errors,
                                      final Deque<LogFileStatus> completedLogFiles) throws LogCheckException
    {
        Instant saveDate = null;
        UUID id = null;

        if( StringUtils.isNoneBlank(saveDateStr) )
        {
            try
            {
                saveDate = Instant.parse(saveDateStr);
            }
            catch( DateTimeParseException ex )
            {
                String errMsg = String.format("Invalid Timestamp Save Date'%s'", saveDateStr);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(idStr) )
        {
            try
            {
                id = UUID.fromString(idStr);
            }
            catch( DateTimeParseException ex )
            {
                String errMsg = String.format("Invalid ID '%s'", saveDateStr);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        LogCheckState res = new LogCheckState(logFile,
                saveDate,
                id,
                setName,
                errors,
                completedLogFiles);

        return res;
    }

    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder(100);

        res.append("LogCheckState :\n");
        res.append(String.format("    Set Name  : %s\n", m_setName ));
        res.append(String.format("    Save Date : %s\n", m_saveDate ));
        res.append(String.format("    Log File State: %s\n", m_logFile));

        return res.toString();
    }
}
