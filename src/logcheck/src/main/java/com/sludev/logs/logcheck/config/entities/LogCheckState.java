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

import java.time.Instant;
import java.time.format.DateTimeParseException;
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

    public static LogCheckState from( final LogFileState logFile,
                                      final String saveDateStr,
                                      final String idStr,
                                      final String setName,
                                      final List<LogCheckError> errors) throws LogCheckException
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
                log.debug(errMsg, ex);

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
                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        LogCheckState res = new LogCheckState(logFile,
                saveDate,
                id,
                setName,
                errors);

        return res;
    }
}
