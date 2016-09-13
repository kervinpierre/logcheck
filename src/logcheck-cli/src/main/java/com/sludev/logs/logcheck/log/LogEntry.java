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

    private String m_jsonRaw;

    private String m_appSource;
    private String m_appStatusCode;
    private String m_appChannel;
    private String m_appType;
    private String m_appRecordNumber;
    private String m_appEventId;
    private String m_appComputerName;
    private String m_appTimeGenerated;
    private String m_appDataStr;

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
        LCLogLevel l = LCLogLevel.from(s);
        
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

    public static Logger getLOGGER()
    {
        return LOGGER;
    }

    public String getJsonRaw()
    {
        return m_jsonRaw;
    }

    public void setJsonRaw( String jsonRaw )
    {
        this.m_jsonRaw = jsonRaw;
    }

    public String getAppSource()
    {
        return m_appSource;
    }

    public void setAppSource( String appSource )
    {
        this.m_appSource = appSource;
    }

    public String getAppStatusCode()
    {
        return m_appStatusCode;
    }

    public void setAppStatusCode( String appStatusCode )
    {
        this.m_appStatusCode = appStatusCode;
    }

    public String getAppChannel()
    {
        return m_appChannel;
    }

    public void setAppChannel( String appChannel )
    {
        this.m_appChannel = appChannel;
    }

    public String getAppType()
    {
        return m_appType;
    }

    public void setAppType( String appType )
    {
        this.m_appType = appType;
    }

    public String getAppRecordNumber()
    {
        return m_appRecordNumber;
    }

    public void setAppRecordNumber( String appRecordNumber )
    {
        this.m_appRecordNumber = appRecordNumber;
    }

    public String getAppComputerName()
    {
        return m_appComputerName;
    }

    public String getAppEventId()
    {
        return m_appEventId;
    }

    public void setAppEventId(String appEventId )
    {
        m_appEventId = appEventId;
    }

    public void setAppComputerName( String appComputerName )
    {
        this.m_appComputerName = appComputerName;
    }

    public String getAppTimeGenerated()
    {
        return m_appTimeGenerated;
    }

    public void setAppTimeGenerated( String appTimeGenerated )
    {
        this.m_appTimeGenerated = appTimeGenerated;
    }

    public String getAppDataStr()
    {
        return m_appDataStr;
    }

    public void setAppDataStr( String appDataStr )
    {
        this.m_appDataStr = appDataStr;
    }

    private LogEntry( final String type,
                      final LCLogLevel level,
                      final String logger,
                      final String message,
                      final String exception,
                      final LocalDateTime timeStamp,
                      final String host,
                      final String jsonRaw )
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
        this.m_jsonRaw = jsonRaw;
    }

    public static LogEntry from(final String type,
                                final LCLogLevel level,
                                final String logger,
                                final String message,
                                final String exception,
                                final LocalDateTime timeStamp,
                                final String host,
                                final String jsonRaw
    )
    {
        LogEntry logEntry = new LogEntry(type, level, logger, message,
                                            exception, timeStamp, host, jsonRaw);

        return logEntry;
    }

    public static LogEntry from(final String type)
    {
        LogEntry logEntry = LogEntry.from(type, null, null, null,
                                            null, null, null, null);

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
                StringUtils.defaultIfBlank(le.getHost(), ""),
                StringUtils.defaultIfBlank(le.getJsonRaw(), ""),
                StringUtils.defaultIfBlank(le.getAppSource(), ""),
                StringUtils.defaultIfBlank(le.getAppStatusCode(), ""),
                StringUtils.defaultIfBlank(le.getAppChannel(), ""),
                StringUtils.defaultIfBlank(le.getAppType(), ""),
                StringUtils.defaultIfBlank(le.getAppRecordNumber(), ""),
                StringUtils.defaultIfBlank(le.getAppEventId(), ""),
                StringUtils.defaultIfBlank(le.getAppComputerName(), ""),
                StringUtils.defaultIfBlank(le.getAppTimeGenerated(), ""),
                StringUtils.defaultIfBlank(le.getAppDataStr(), "")
        );

        return res;
    }
}
