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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single log entry in a file, for deduplication purposes.
 *
 * Created by kervin on 2015-11-13.
 */
public final class LogEntryDeDupe
{
    private static final Logger log = LogManager.getLogger(LogEntryDeDupe.class);

    private final UUID id;
    private final String logHashCode;
    private final String errorCode;
    private final String errorCodeType;
    private final String errorText;
    private final Instant timeStamp;

    public UUID getId()
    {
        return id;
    }

    public String getLogHashCode()
    {
        return logHashCode;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public String getErrorCodeType()
    {
        return errorCodeType;
    }

    public String getErrorText()
    {
        return errorText;
    }

    public Instant getTimeStamp()
    {
        return timeStamp;
    }

    private LogEntryDeDupe(final UUID id,
                           final String logHashCode,
                           final String errorCode,
                           final String errorCodeType,
                           final String errorText,
                           final Instant timeStamp)
    {
        this.id = id;
        this.logHashCode = logHashCode;
        this.errorCode = errorCode;
        this.errorCodeType = errorCodeType;
        this.errorText = errorText;
        this.timeStamp = timeStamp;
    }

    public static LogEntryDeDupe from(final UUID id,
                                      final String logHashCode,
                                      final String errorCode,
                                      final String errorCodeType,
                                      final String errorText,
                                      final Instant timeStamp)
    {
        LogEntryDeDupe ledd = new LogEntryDeDupe(id,
                logHashCode,
                errorCode,
                errorCodeType,
                errorText,
                timeStamp);

        return ledd;
    }

    public static LogEntryDeDupe from(final String idStr,
                                      final String logHashCode,
                                      final String errorCode,
                                      final String errorCodeType,
                                      final String errorText,
                                      final String timeStampStr)
    {
        UUID id = null;
        Instant timeStamp = null;

        timeStamp = Instant.parse(timeStampStr);
        id        = UUID.fromString(idStr);

        LogEntryDeDupe ledd = new LogEntryDeDupe(id,
                logHashCode,
                errorCode,
                errorCodeType,
                errorText,
                timeStamp);

        return ledd;
    }
}
