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
    private static final Logger log 
                             = LogManager.getLogger(LogEntry.class);
    
    private LCLogLevel level;
    private String logger;
    private String message;
    private String exception;
    private LocalDateTime timeStamp;
    private final String type;
    private String host;

    public LCLogLevel getLevel()
    {
        return level;
    }

    public void setLevel(LCLogLevel l)
    {
        this.level = l;
    }

    public void setLevel(String s)
    {
        LCLogLevel l = LCLogLevel.valueOf(s);
        
        this.level = l;
    }
    
    public String getLogger()
    {
        return logger;
    }

    public void setLogger(String logger)
    {
        this.logger = logger;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getException()
    {
        return exception;
    }

    public void setException(String exception)
    {
        this.exception = exception;
    }

    public LocalDateTime getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime t)
    {
        this.timeStamp = t;
    }

    public void setTimeStamp(String s)
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss','SSS");
        LocalDateTime t = LocalDateTime.parse(s,dtf);
        
        this.timeStamp = t;
    }

    public String getType()
    {
        return type;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    private LogEntry(final String type)
    {
        if( type != null )
        {
            this.type = type;
        }
        else
        {
            this.type = LogCheckConstants.DEFAULT_LOG_TYPE;
        }
    }

    public static LogEntry from(final String type)
    {
        LogEntry logEntry = new LogEntry(type);

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
                StringUtils.defaultIfBlank(le.getLevel()==null?null:le.getLevel().toString(), ""),
                StringUtils.defaultIfBlank(le.getLogger(), ""),
                StringUtils.defaultIfBlank(le.getMessage(), ""),
                StringUtils.defaultIfBlank(le.getException(), ""),
                StringUtils.defaultIfBlank(le.getTimeStamp()==null?null:le.getTimeStamp().toString(), ""),
                StringUtils.defaultIfBlank(le.getType(), ""),
                StringUtils.defaultIfBlank(le.getHost(), "")
        );

        return res;
    }
}
