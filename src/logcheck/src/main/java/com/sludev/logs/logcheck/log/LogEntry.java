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
package com.sludev.logs.logcheck.log;

import com.sludev.logs.logcheck.enums.LogCheckLogLevel;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kervin
 */
public class LogEntry
{
    private static final Logger log 
                             = LogManager.getLogger(LogEntry.class);
    
    private LogCheckLogLevel level;
    private String logger;
    private String message;
    private String exception;
    private LocalDateTime timeStamp;
    private String type;
    private String host;

    public LogCheckLogLevel getLevel()
    {
        return level;
    }

    public void setLevel(LogCheckLogLevel l)
    {
        this.level = l;
    }

    public void setLevel(String s)
    {
        LogCheckLogLevel l = LogCheckLogLevel.valueOf(s);
        
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

    public void setType(String type)
    {
        this.type = type;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public LogEntry()
    {
        type = LogCheckConstants.DEFAULT_ELASTICSEARCH_LOG_TYPE;
    }
    
    
}
