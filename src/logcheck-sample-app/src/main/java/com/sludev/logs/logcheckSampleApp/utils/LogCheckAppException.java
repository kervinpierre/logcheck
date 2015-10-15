package com.sludev.logs.logcheckSampleApp.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2015-10-14.
 */
public final class LogCheckAppException extends Exception
{
    private static final Logger log = LogManager.getLogger(LogCheckAppException.class);

    public LogCheckAppException(String msg)
    {
        super(msg);
    }

    public LogCheckAppException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
