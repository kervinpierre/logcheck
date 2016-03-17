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
package com.sludev.logs.logcheck.log;

import com.sludev.logs.logcheck.enums.LCLogLevel;
import com.sludev.logs.logcheck.config.entities.LogEntryVO;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kervin
 */
public final class LogEntry
{
    private static final Logger LOGGER
                             = LogManager.getLogger(LogEntry.class);

    private final String m_type;
    private LCLogLevel m_level;
    private String m_logger;
    private String m_message;
    private String m_exception;
    private LocalDateTime m_timeStamp;
    private String m_host;

    public LCLogLevel getLevel()
    {
        return m_level;
    }

    public void setLevel(LCLogLevel l)
    {
        this.m_level = l;
    }

    public void setLevel(String s)
    {
        LCLogLevel l = LCLogLevel.valueOf(s);
        
        this.m_level = l;
    }
    
    public String getLogger()
    {
        return m_logger;
    }

    public void setLogger(String logger)
    {
        this.m_logger = logger;
    }

    public String getMessage()
    {
        return m_message;
    }

    public void setMessage(String message)
    {
        this.m_message = message;
    }

    public String getException()
    {
        return m_exception;
    }

    public void setException(String exception)
    {
        this.m_exception = exception;
    }

    public LocalDateTime getTimeStamp()
    {
        return m_timeStamp;
    }

    public void setTimeStamp(LocalDateTime t)
    {
        this.m_timeStamp = t;
    }

    public void setTimeStamp(String s)
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss','SSS");
        LocalDateTime t = LocalDateTime.parse(s,dtf);
        
        this.m_timeStamp = t;
    }

    public String getType()
    {
        return m_type;
    }

    public String getHost()
    {
        return m_host;
    }

    public void setHost(String host)
    {
        this.m_host = host;
    }

    private LogEntry(   final String type,
                        final LCLogLevel level,
                        final String logger,
                        final String message,
                        final String exception,
                        final LocalDateTime timeStamp,
                        final String host)
    {
        if( type != null )
        {
            this.m_type = type;
        }
        else
        {
            this.m_type = LogCheckConstants.DEFAULT_LOG_TYPE;
        }

        this.m_level = level;
        this.m_logger = logger;
        this.m_message = message;
        this.m_exception = exception;
        this.m_timeStamp = timeStamp;
        this.m_host = host;
    }

    public static LogEntry from(final String type,
                                final LCLogLevel level,
                                final String logger,
                                final String message,
                                final String exception,
                                final LocalDateTime timeStamp,
                                final String host)
    {
        LogEntry logEntry = new LogEntry(type, level, logger, message,
                                            exception, timeStamp, host);

        return logEntry;
    }

    public static LogEntry from(final String type)
    {
        LogEntry logEntry = LogEntry.from(type, null, null, null,
                                            null, null, null);

        return logEntry;
    }

    @Override
    public String toString()
    {
        String res = null;

        res = ToStringBuilder.reflectionToString(this);

        return res;
    }

    /**
     * Create a new LogEntry Value Object from an existing LogEntry object.
     *
     * @param le
     * @return The new value object
     */
    public static LogEntryVO toValueObject(LogEntry le)
    {
        LogEntryVO res = LogEntryVO.from(
                StringUtils.defaultIfBlank((le.getLevel() == null) ? null : le.getLevel().toString(), ""),
                StringUtils.defaultIfBlank(le.getLogger(), ""),
                StringUtils.defaultIfBlank(le.getMessage(), ""),
                StringUtils.defaultIfBlank(le.getException(), ""),
                StringUtils.defaultIfBlank((le.getTimeStamp() == null) ? null : le.getTimeStamp().toString(), ""),
                StringUtils.defaultIfBlank(le.getType(), ""),
                StringUtils.defaultIfBlank(le.getHost(), "")
        );

        return res;
    }
}
