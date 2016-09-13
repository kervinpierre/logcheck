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

    private final String m_jsonRaw;

    private final String m_appSource;
    private final String m_appStatusCode;
    private final String m_appChannel;
    private final String m_appType;
    private final String m_appEventId;
    private final String m_appRecordNumber;
    private final String m_appComputerName;
    private final String m_appTimeGenerated;
    private final String m_appDataStr;

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

    public String getJsonRaw()
    {
        return m_jsonRaw;
    }

    public String getAppSource()
    {
        return m_appSource;
    }

    public String getAppStatusCode()
    {
        return m_appStatusCode;
    }

    public String getAppChannel()
    {
        return m_appChannel;
    }

    public String getAppType()
    {
        return m_appType;
    }

    public String getAppRecordNumber()
    {
        return m_appRecordNumber;
    }

    public String getAppEventId()
    {
        return m_appEventId;
    }

    public String getAppComputerName()
    {
        return m_appComputerName;
    }

    public String getAppTimeGenerated()
    {
        return m_appTimeGenerated;
    }

    public String getAppDataStr()
    {
        return m_appDataStr;
    }

    private LogEntryVO( final String level,
                        final String logger,
                        final String message,
                        final String exception,
                        final String timeStamp,
                        final String type,
                        final String host,
                        final String jsonRaw,
                        final String appSource,
                        final String appStatusCode,
                        final String appChannel,
                        final String appType,
                        final String appRecordNumber,
                        final String appEventId,
                        final String appComputerName,
                        final String appTimeGenerated,
                        final String appDataStr)
    {
        this.m_level = level;
        this.m_logger = logger;
        this.m_message = message;
        this.m_exception = exception;
        this.m_timeStamp = timeStamp;
        this.m_type = type;
        this.m_host = host;
        this.m_jsonRaw = jsonRaw;
        this.m_appSource = appSource;
        this.m_appStatusCode = appStatusCode;
        this.m_appChannel = appChannel;
        this.m_appType = appType;
        this.m_appRecordNumber = appRecordNumber;
        this.m_appEventId = appEventId;
        this.m_appComputerName = appComputerName;
        this.m_appTimeGenerated = appTimeGenerated;
        this.m_appDataStr = appDataStr;
    }

    public static LogEntryVO from(final String level,
                       final String logger,
                       final String message,
                       final String exception,
                       final String timeStamp,
                       final String type,
                       final String host,
                        final String jsonRaw,
                        final String appSource,
                        final String appStatusCode,
                        final String appChannel,
                        final String appType,
                        final String appRecordNumber,
                        final String appEventId,
                        final String appComputerName,
                        final String appTimeGenerated,
                        final String appDataStr)
    {
        LogEntryVO res = new LogEntryVO(level,
                logger,
                message,
                exception,
                timeStamp,
                type,
                host,
                jsonRaw,
                appSource,
                appStatusCode,
                appChannel,
                appType,
                appRecordNumber,
                appEventId,
                appComputerName,
                appTimeGenerated,
                appDataStr);

        return res;
    }

    public static String toJSON(LogEntryVO entryVO)
    {
        String res;
        StringBuilder js = new StringBuilder(100);

        js.append("{");
        js.append( String.format("\"@timestamp\":\"%s\",", entryVO.m_timeStamp) );
        js.append( String.format("\"level\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_level)) );
        js.append( String.format("\"type\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_type)) );
        js.append( String.format("\"logger\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_logger)) );
        js.append( String.format("\"host\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_host)) );
        js.append( String.format("\"message\":\"%s\",",
                StringEscapeUtils.escapeJson(entryVO.m_message)) );
        js.append( String.format("\"exception\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_exception)) );

        js.append( String.format("\"appSource\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_appSource)) );
        js.append( String.format("\"appStatusCode\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_appStatusCode)) );
        js.append( String.format("\"appChannel\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_appChannel)) );
        js.append( String.format("\"appType\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_appType)) );
        js.append( String.format("\"appRecordNumber\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_appRecordNumber)) );
        js.append( String.format("\"appEventId\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_appEventId)) );
        js.append( String.format("\"appComputerName\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_appComputerName)) );
        js.append( String.format("\"appTimeGenerated\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_appTimeGenerated)) );
        js.append( String.format("\"appDataStr\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_appDataStr)) );

        js.append( String.format("\"jsonRaw\":\"%s\"",
                StringEscapeUtils.escapeJson(entryVO.m_jsonRaw)) );

        js.append("}");

        res = js.toString();

        return res;
    }
}
