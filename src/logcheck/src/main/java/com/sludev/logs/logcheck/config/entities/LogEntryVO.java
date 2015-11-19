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
    private final String level;
    private final String logger;
    private final String message;
    private final String exception;
    private final String timeStamp;
    private final String type;
    private final String host;

    public String getLevel()
    {
        return level;
    }

    public String getLogger()
    {
        return logger;
    }

    public String getMessage()
    {
        return message;
    }

    public String getException()
    {
        return exception;
    }

    public String getTimeStamp()
    {
        return timeStamp;
    }

    public String getType()
    {
        return type;
    }

    public String getHost()
    {
        return host;
    }

    private LogEntryVO(final String level,
                       final String logger,
                       final String message,
                       final String exception,
                       final String timeStamp,
                       final String type,
                       final String host)
    {
        this.level = level;
        this.logger = logger;
        this.message = message;
        this.exception = exception;
        this.timeStamp = timeStamp;
        this.type = type;
        this.host = host;
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

    public static String toJSON(LogEntryVO vo)
    {
        String res;
        StringBuilder js = new StringBuilder();

        js.append("{");
        js.append( String.format("\"@timestamp\":\"%s\",", vo.timeStamp) );
        js.append( String.format("\"level\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.level)) );
        js.append( String.format("\"type\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.type)) );
        js.append( String.format("\"logger\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.logger)) );
        js.append( String.format("\"host\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.host)) );
        js.append( String.format("\"message\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.message)) );
        js.append( String.format("\"exception\":\"%s\"",
                StringEscapeUtils.escapeJson(vo.exception)) );
        js.append("}");

        res = js.toString();

        return res;
    }
}
