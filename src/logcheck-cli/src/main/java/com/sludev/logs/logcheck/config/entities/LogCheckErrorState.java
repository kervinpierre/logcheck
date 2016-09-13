package com.sludev.logs.logcheck.config.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Represents an Error Document on disk.
 *
 * Created by kervin on 2015-10-30.
 */
public final class LogCheckErrorState
{
    private static final Logger log = LogManager.getLogger(LogCheckErrorState.class);

    private final List<LogCheckError> errorList;

    private LogCheckErrorState( final List<LogCheckError> errorList )
    {
        this.errorList = errorList;
    }

    public static LogCheckErrorState from( final List<LogCheckError> errorList )
    {
        LogCheckErrorState res = new LogCheckErrorState(errorList);

        return res;
    }
}
