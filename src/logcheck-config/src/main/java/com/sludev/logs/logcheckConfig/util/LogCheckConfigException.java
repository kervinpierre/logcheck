package com.sludev.logs.logcheckConfig.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2016-03-04.
 */
public final class LogCheckConfigException extends Exception
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckConfigException.class);

    public LogCheckConfigException(String msg)
    {
        super(msg);
    }

    public LogCheckConfigException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
