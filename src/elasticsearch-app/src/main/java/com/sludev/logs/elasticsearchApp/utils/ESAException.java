package com.sludev.logs.elasticsearchApp.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2016-03-29.
 */
public final class ESAException extends Exception
{
    private static final Logger LOGGER = LogManager.getLogger(ESAException.class);
    
    public ESAException(String msg)
    {
        super(msg);
    }

    public ESAException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
