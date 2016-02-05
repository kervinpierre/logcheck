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
package com.sludev.logs.logcheck.config.entities;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.Serializable;

/**
 * Read-only Value-Object for the LogEntry class.
 *
 * @author kervin
 */
public final class LogEntryVO implements Serializable
{
    private final String m_level;
    private final String m_logger;
    private final String m_message;
    private final String m_exception;
    private final String m_timeStamp;
    private final String m_type;
    private final String m_host;

    public String getLevel()
    {
        return m_level;
    }

    public String getLogger()
    {
        return m_logger;
    }

    public String getMessage()
    {
        return m_message;
    }

    public String getException()
    {
        return m_exception;
    }

    public String getTimeStamp()
    {
        return m_timeStamp;
    }

    public String getType()
    {
        return m_type;
    }

    public String getHost()
    {
        return m_host;
    }

    private LogEntryVO(final String level,
                       final String logger,
                       final String message,
                       final String exception,
                       final String timeStamp,
                       final String type,
                       final String host)
    {
        this.m_level = level;
        this.m_logger = logger;
        this.m_message = message;
        this.m_exception = exception;
        this.m_timeStamp = timeStamp;
        this.m_type = type;
        this.m_host = host;
    }

    public static LogEntryVO from(final String level,
                       final String logger,
                       final String message,
                       final String exception,
                       final String timeStamp,
                       final String type,
                       final String host)
    {
        LogEntryVO res = new LogEntryVO(level,
                logger,
                message,
                exception,
                timeStamp,
                type,
                host);

        return res;
    }

    public static String toJSON(LogEntryVO entryVO)
    {
        String res;
        StringBuilder js = new StringBuilder(100);

        js.append("{");
        js.append( String.format("\"@timestamp\":\"%s\",", entryVO.m_timeStamp) );
        js.append( String.format("\"m_level\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_level)) );
        js.append( String.format("\"m_type\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_type)) );
        js.append( String.format("\"m_logger\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_logger)) );
        js.append( String.format("\"m_host\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_host)) );
        js.append( String.format("\"m_message\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_message)) );
        js.append( String.format("\"m_exception\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_exception)) );
        js.append("}");

        res = js.toString();

        return res;
    }
}
