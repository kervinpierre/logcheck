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
package com.sludev.logs.logcheck.config.entities.impl;

import com.sludev.logs.logcheck.config.entities.LogCheckError;
import com.sludev.logs.logcheck.config.entities.LogCheckStateBase;
import com.sludev.logs.logcheck.config.entities.LogCheckStateStatusBase;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.enums.LCLogCheckStateType;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * The state of the Log Check job at a specific point in time.
 *
 * Created by kervin on 10/27/2015.
 */
public class LogCheckState extends LogCheckStateBase
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckState.class);

    private final LogFileState m_logFile;

    public LogFileState getLogFile()
    {
        return m_logFile;
    }

    private LogCheckState( final LogFileState logFile,
                           final UUID id,
                           final String setName,
                           final Instant saveDate,
                           final List<LogCheckError> errors,
                           final Deque<LogCheckStateStatusBase> completedLogFiles)
    {
        super(id,
                LCLogCheckStateType.FILE_STATE,
                setName,
                saveDate,
                errors,
                completedLogFiles);

        this.m_logFile = logFile;
    }

    public static LogCheckState from( final LogFileState logFile,
                                      final UUID id,
                                      final String setName,
                                      final Instant saveDate,
                                      final List<LogCheckError> errors,
                                      final Deque<LogCheckStateStatusBase> completedLogFiles )
    {
        LogCheckState res = new LogCheckState(logFile,
                                    id,
                                    setName,
                                    saveDate,
                                    errors,
                                    completedLogFiles);

        return res;
    }

    public static LogCheckState from( final LogFileState logFile,
                                      final String idStr,
                                      final String setName,
                                      final String saveDateStr,
                                      final List<LogCheckError> errors,
                                      final Deque<LogCheckStateStatusBase> completedLogFiles) throws LogCheckException
    {
        Instant saveDate = null;
        UUID id = null;
        Integer recordPosition = null;
        Integer recordCount = null;

        saveDate = getSaveDate(saveDateStr);
        id = getId(idStr);

        LogCheckState res = new LogCheckState(logFile,
                id,
                setName,
                saveDate,
                errors,
                completedLogFiles);

        return res;
    }

    public Deque<LogFileStatus> getCompletedFileStatuses()
    {
        Deque<LogCheckStateStatusBase> stats = super.getCompletedStatuses();
        Deque<LogFileStatus> res = new ArrayDeque<>();

        for( LogCheckStateStatusBase stat : stats )
        {
            if( stat instanceof LogFileStatus )
            {
                res.push((LogFileStatus)stat);
            }
        }

        return res;
    }
}
