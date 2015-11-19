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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The state of the Log Check job at a specific point in time.
 *
 * Created by kervin on 10/27/2015.
 */
public class LogCheckState
{
    private static final Logger log
            = LogManager.getLogger(LogCheckState.class);

    private final LogFileState logFile;
    private final Instant saveDate;
    private final UUID id;
    private final String setName;
    private final List<LogCheckError> errors;

    public LogFileState getLogFile()
    {
        return logFile;
    }

    public Instant getSaveDate()
    {
        return saveDate;
    }

    public UUID getId()
    {
        return id;
    }

    public String getSetName()
    {
        return setName;
    }

    public List<LogCheckError> getErrors()
    {
        return errors;
    }

    private LogCheckState( final LogFileState logFile,
                           final Instant saveDate,
                           final UUID id,
                           final String setName,
                           final List<LogCheckError> errors)
    {
        this.logFile = logFile;
        this.saveDate = saveDate;
        this.id = id;
        this.setName = setName;

        if( errors == null )
        {
            this.errors = new ArrayList<>();
        }
        else
        {
            this.errors = errors;
        }
    }

    public static LogCheckState from( final LogFileState logFile,
                                 final Instant saveDate,
                                 final UUID id,
                                 final String setName,
                                 final List<LogCheckError> errors)
    {
        LogCheckState res = new LogCheckState(logFile,
                saveDate,
                id,
                setName,
                errors);

        return res;
    }
}
